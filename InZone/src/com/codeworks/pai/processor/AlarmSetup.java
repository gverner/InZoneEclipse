package com.codeworks.pai.processor;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.codeworks.pai.R;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

public class AlarmSetup extends Thread {
	static final String	TAG					= AlarmSetup.class.getSimpleName();
	static final int	DAILY_INTENT_ID		= 5453;
	static final int	ONE_TIME_INTENT_ID	= 5463;

	static int			RUN_START_HOUR		= 9;
	static int			RUN_END_HOUR		= 17;

	Context				context;
	Intent				alarmScheduleIntent;
	Notifier			notifier;
	boolean				running = false;
	
	public AlarmSetup(Context context, Notifier notifier) {
		this.context = context;
		Intent						alarmScheduleIntent 			= null;
		alarmScheduleIntent = new Intent(context, UpdateService.class);

		this.alarmScheduleIntent = alarmScheduleIntent;
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
	/*
	 * void updateAlarm() {
	 * 
	 * Calendar startTime = getCurrentTime();
	 * 
	 * startTime.set(Calendar.MINUTE, 0); int hour =
	 * startTime.get(Calendar.HOUR_OF_DAY); if (hour > RUN_END_HOUR) { // run
	 * next trade date a 9AM startTime.add(Calendar.DAY_OF_MONTH, 1); while
	 * (startTime.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
	 * startTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
	 * startTime.add(Calendar.DAY_OF_MONTH, 1); }
	 * startTime.set(Calendar.HOUR_OF_DAY, RUN_START_HOUR); setAlarm(startTime);
	 * } else if (hour > RUN_START_HOUR) { // run each hour
	 * startTime.add(Calendar.HOUR_OF_DAY, 1); if (!isAlarmAlreadyUp()) {
	 * setAlarm(startTime); } } else { // start today or next trade date at 9PM
	 * while (startTime.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
	 * startTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
	 * startTime.add(Calendar.DAY_OF_MONTH, 1); }
	 * startTime.set(Calendar.HOUR_OF_DAY, RUN_START_HOUR); if
	 * (!isAlarmAlreadyUp()) { setAlarm(startTime); } } }
	 */
	void updateAlarm() {
		long startMillis = System.currentTimeMillis();
		DateTime startTime = getCurrentNYTime();

		startTime = startTime.minuteOfHour().setCopy(0);
		int hour = startTime.getHourOfDay();
		if (hour >= RUN_END_HOUR) {
			// run next trade date a 9AM
			startTime = startTime.dayOfMonth().addToCopy(1);
			while (startTime.getDayOfWeek() == DateTimeConstants.SATURDAY || startTime.getDayOfWeek() == DateTimeConstants.SUNDAY) {
				startTime = startTime.dayOfMonth().addToCopy(1);
			}
			startTime = startTime.hourOfDay().setCopy(RUN_START_HOUR);
			setAlarm(startTime);
		} else if (hour >= RUN_START_HOUR) {
			// run each hour
			startTime = getCurrentNYTime();
			if (!isAlarmAlreadyUp()) {
				setAlarm(startTime);
			}
		} else {
			// start today or next trade date at 9PM
			while (startTime.getDayOfWeek() == DateTimeConstants.SATURDAY || startTime.getDayOfWeek() == DateTimeConstants.SUNDAY) {
				startTime = startTime.dayOfMonth().addToCopy(1);
			}
			startTime = startTime.hourOfDay().setCopy(RUN_START_HOUR);
			// if (!isAlarmAlreadyUp()) {
			setAlarm(startTime);
			// }
		}
		Log.i(TAG, "Setup Alarm time ms=" + (System.currentTimeMillis() - startMillis));
	}

	void setAlarm(DateTime startTime) {
		Intent dailyIntent = alarmScheduleIntent;
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		PendingIntent pDailyIntent = PendingIntent.getService(context, DAILY_INTENT_ID, dailyIntent, 0);
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		long interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
		alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, startTime.getMillis(), interval, pDailyIntent);
		Log.i(TAG, "Setup Alarm Manager to start service at " + formatStartTime(startTime));
		scheduleSetupNotice(startTime);
	}

	boolean isAlarmAlreadyUp() {
		boolean alarmUp = (PendingIntent.getService(context.getApplicationContext(), DAILY_INTENT_ID, alarmScheduleIntent, PendingIntent.FLAG_NO_CREATE) != null);

		if (alarmUp) {
			Log.d(TAG, "Alarm is already active");
		}
		return alarmUp;
	}
	/*
	 * String formatStartTime(Calendar startTime) { SimpleDateFormat sdf = new
	 * SimpleDateFormat("MM/dd/yyyy hh:mm aa Z", Locale.US);
	 * sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern")); return
	 * sdf.format(startTime.getTime()); }
	 */
	String formatStartTime(DateTime startTime) {
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		return fmt.print(startTime);
	}
	void scheduleSetupNotice(DateTime startTime) {
		Resources res = context.getApplicationContext().getResources();
		notifier.sendNotice(50000L, res.getString(R.string.scheduleSetupSubject),
				String.format(res.getString(R.string.scheduleSetupMessage, formatStartTime(startTime))));
	}

	public DateTime getCurrentNYTime() {
		DateTime dt = new DateTime();
		// translate to New York local time
		DateTime nyDateTime = dt.withZone(DateTimeZone.forID("America/New_York"));
		return nyDateTime;

	}	
}
