package com.codeworks.pai.processor;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import com.codeworks.pai.R;
import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.ServiceLogTable;
import com.codeworks.pai.db.model.ServiceType;

public class AlarmSetup extends Thread {
	static final String	TAG						= AlarmSetup.class.getSimpleName();
	static final int	REPEAT_INTENT_ID		= 5453;
	static final int	DAILY_START_INTENT_ID	= 5473;

	static int			RUN_START_HOUR		= 9;
	static int			RUN_START_MINUTE    = 30;
	static int			RUN_END_HOUR		= 16;
	static int          HISTORY_LOAD_HOUR   = 5;

	Context				context;
	Notifier			notifier;
	boolean				running = false;
	
	public AlarmSetup(Context context, Notifier notifier) {
		this.context = context;
		this.notifier = notifier;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	@Override
	public void run() {
		running = true;
		updateAlarm();
		running = false;
	}

	/**
	 * setup Alarm Manager to restart this service every hour between the hours
	 * of 9AM AND 5PM US/EASTERN.
	 */
	void updateAlarm() {
		long startMillis = System.currentTimeMillis();
		DateTime startTime = getCurrentNYTime();
		int hour = startTime.getHourOfDay();
		int minute = startTime.getMinuteOfHour();
		if (hour >= RUN_END_HOUR || startTime.getDayOfWeek() == DateTimeConstants.SATURDAY || startTime.getDayOfWeek() == DateTimeConstants.SUNDAY) {
			// run the next date a 5AM to load history, if today is the weekend roll to Monday
			if (startTime.getDayOfWeek() == DateTimeConstants.SATURDAY || startTime.getDayOfWeek() == DateTimeConstants.SUNDAY) {
				startTime = rollPastWeekend(startTime);
			} else {
				startTime = startTime.dayOfMonth().addToCopy(1);
			}
			startTime = startTime.hourOfDay().setCopy(HISTORY_LOAD_HOUR);
			startTime = startTime.minuteOfHour().setCopy(0);
			cancelAlarm(REPEAT_INTENT_ID);
			setStartAlarm(startTime);
		} else if (hour > RUN_START_HOUR || (hour == RUN_START_HOUR && minute >= RUN_START_MINUTE)) {
			// repeating all day
			if (!isAlarmAlreadyUp(REPEAT_INTENT_ID)) {
				setRepeatingAlarm(startTime);
			} else {
				Resources res = context.getApplicationContext().getResources();
				logServiceEvent(ServiceType.SETUP, res.getString(R.string.scheduleRepeatingAlreadySetup));
			}
		} else if ((hour >= HISTORY_LOAD_HOUR && hour < RUN_START_HOUR) || (hour == RUN_START_HOUR && minute < RUN_START_MINUTE)) {
			
			// start when market opens
			startTime = rollPastWeekend(startTime);
			startTime = startTime.hourOfDay().setCopy(RUN_START_HOUR);
			startTime = startTime.minuteOfHour().setCopy(RUN_START_MINUTE);
			setStartAlarm(startTime);
		} else {
			// load history 5am
			if (startTime.getDayOfWeek() == DateTimeConstants.SATURDAY || startTime.getDayOfWeek() == DateTimeConstants.SUNDAY) {
				startTime = rollPastWeekend(startTime);
			}
			startTime = startTime.hourOfDay().setCopy(HISTORY_LOAD_HOUR);
			startTime = startTime.minuteOfHour().setCopy(0);
			setStartAlarm(startTime);
		}
		Log.i(TAG, "Setup Alarm time ms=" + (System.currentTimeMillis() - startMillis));
	}

	DateTime rollPastWeekend(DateTime startTime) {
		while (startTime.getDayOfWeek() == DateTimeConstants.SATURDAY || startTime.getDayOfWeek() == DateTimeConstants.SUNDAY) {
			startTime = startTime.dayOfMonth().addToCopy(1);
		}
		return startTime;
	}

	PendingIntent setupIntent(int intentId, String subAction) {
		Intent intent = new Intent(UpdateService.class.getName());
		intent.putExtra(UpdateService.SERVICE_ACTION, subAction);
		PendingIntent pDailyIntent = PendingIntent.getService(context, intentId, intent, 0);
		return pDailyIntent;
	}
	
	void setRepeatingAlarm(DateTime startTime) {
		PendingIntent pDailyIntent = setupIntent(REPEAT_INTENT_ID, UpdateService.ACTION_REPEATING);
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		long interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES / 2;
		alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, startTime.getMillis(), interval, pDailyIntent);
		Log.i(TAG, "Setup Alarm Manager to start REPEATING service at " + formatStartTime(startTime));
		scheduleSetupNotice(startTime, 15);
	}

	void setStartAlarm(DateTime startTime) {
		PendingIntent pDailyIntent = setupIntent(DAILY_START_INTENT_ID, UpdateService.ACTION_SCHEDULE);
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarm.set(AlarmManager.RTC_WAKEUP, startTime.getMillis(), pDailyIntent);
		Log.i(TAG, "Setup Alarm Manager to start service at " + formatStartTime(startTime));
		logServiceEvent(startTime);
	}
	
	void cancelAlarm(int intentId) {
		PendingIntent pIntent = setupIntent(intentId, UpdateService.ACTION_REPEATING);
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pIntent);
		Log.d(TAG,"Cancel Alarm "+intentId);
	}
	
	boolean isAlarmAlreadyUp(int intentId) {
		Intent intent = new Intent(UpdateService.class.getName());
		boolean alarmUp = (PendingIntent.getService(context.getApplicationContext(), intentId, intent, PendingIntent.FLAG_NO_CREATE) != null);

		if (alarmUp) {
			Log.d(TAG, "Alarm is already active");
		}
		return alarmUp;
	}
	
	String formatStartTime(DateTime startTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm aa", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		return sdf.format(startTime.toDate());
	}
	 
	void scheduleSetupNotice(DateTime startTime, int repeating) {
		Resources res = context.getApplicationContext().getResources();
		String message = String.format(res.getString(R.string.scheduleRepeatingMessage, formatStartTime(startTime)), repeating);
		logServiceEvent(ServiceType.SETUP, message);
	}

	void logServiceEvent(DateTime startTime) {
		Resources res = context.getApplicationContext().getResources();
		String message = String.format(res.getString(R.string.scheduleStartMessage, formatStartTime(startTime)));
		logServiceEvent(ServiceType.SETUP, message);
	}
	
	void logServiceEvent(ServiceType serviceType, String message) {
		ContentValues values = new ContentValues();
		values.put(ServiceLogTable.COLUMN_MESSAGE, message);
		values.put(ServiceLogTable.COLUMN_SERVICE_TYPE, serviceType.getIndex());
		values.put(ServiceLogTable.COLUMN_TIMESTAMP, DateTime.now().toString(ServiceLogTable.timestampFormat));
		context.getContentResolver().insert(PaiContentProvider.SERVICE_LOG_URI, values);
	}

	public DateTime getCurrentNYTime() {
		return DateUtils.getCurrentNYTime();
	}	
}
