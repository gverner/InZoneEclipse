package com.codeworks.pai.processor;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.codeworks.pai.R;
import com.codeworks.pai.db.model.PaiStudy;

public class UpdateService extends Service implements OnSharedPreferenceChangeListener {
	private static final String	TAG							= UpdateService.class.getSimpleName();
	public static final String	BROADCAST_ACTION			= "com.codeworks.pai.updateservice.results";
	public static final String	EXTRA_QUOTES				= "com.codeworks.pai.updateservice.quotes";
	public static final String	EXTRA_RESULTS				= "com.codeworks.pai.updateservice.quotes";
	public static final String	SERVICE_ACTION				= "com.codeworks.pai.updateservice.action";
	public static final String	SERVICE_SYMBOL				= "com.codeworks.pai.updateservice.symbol";
	static int					RUN_START_HOUR				= 9;
	static int					RUN_END_HOUR				= 17;
	public static final int		DAILY_INTENT_ID				= 5453;
	public static final int		ONE_TIME_INTENT_ID			= 5463;
	public static final String	ACTION_SCHEDULE				= "action_schedule";
	public static final String	ACTION_ONE_TIME				= "action_one_time";
	public static final String	ACTION_MANUAL				= "action_manual";
	public static final String	KEY_PREF_UPDATE_FREQUENCY_TYPE	= "pref_updateFrequencyType";
	Updater						updater;
	Processor					processor					= null;
	Notifier					notifier					= null;

	private static final class Lock {
	}

	private final Object	lock			= new Lock();
	private DateTime		nextStartTime	= null;
	volatile int			frequency		= 3;


	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		updater = new Updater();
		processor = new ProcessorImpl(getContentResolver(), new DataReaderYahoo());
		notifier = new NotifierImpl(getApplicationContext());
		SharedPreferences sharedPref = getSharedPreferences();
		sharedPref.registerOnSharedPreferenceChangeListener(this);
		frequency = getPrefUpdateFrequency();
		Log.d(TAG, "on Create'd2");
	}

	@Override
	public synchronized void onDestroy() {
		super.onDestroy();

		if (updater.isRunning()) {
			updater.interrupt();
		}
		updater = null;
		SharedPreferences sharedPref = getSharedPreferences();
		sharedPref.unregisterOnSharedPreferenceChangeListener(this);

		Log.d(TAG, "on Destroy'd");
		updateServiceNotice(R.string.serviceStoppedMessage);
		makeToast("Price Update Service Stopped", Toast.LENGTH_LONG);
	}

	SharedPreferences getSharedPreferences() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		return sharedPref;
	}

	@Override
	public synchronized int onStartCommand(Intent intent, int flags, int startId) {
		Intent updateIntent;
		updateIntent = intent;
		Bundle bundle = updateIntent.getExtras();
		String action = (String) bundle.get(SERVICE_ACTION);
		if (ACTION_SCHEDULE.equals(action) || ACTION_MANUAL.equals(action)) {
			if (ACTION_MANUAL.equals(action)) {
				Log.d(TAG, "Manual start");
			} else {
				Log.d(TAG, "Scheduled start");
				scheduledStartNotice();
			}
			if (!updater.isRunning()) {
				updater.start();
				makeToast("Price Update Service Started", Toast.LENGTH_LONG);
				Log.d(TAG, "on Starte'd");
			} else {
				updater.restart(); // interrupt sleep and restart now
				Log.d(TAG, "an Re-Starte'd");
			}

		} else if (ACTION_ONE_TIME.equals(action)) {
			Log.d(TAG, "One Time start");
			String symbol = bundle.getString(SERVICE_SYMBOL);
			new OneTimeUpdate(symbol).start();
		}
		Log.d(TAG, "on Starte'd");
		updateAlarm();
		return START_STICKY;
	}

	void scheduledStartNotice() {
		Resources res = getApplicationContext().getResources();
		notifier.sendNotice(50000L, res.getString(R.string.scheduledStartSubject),
				String.format(res.getString(R.string.scheduledStartMessage, formatStartTime(new DateTime()))));
	}

	void scheduleSetupNotice(DateTime startTime) {
		Resources res = getApplicationContext().getResources();
		notifier.sendNotice(50000L, res.getString(R.string.scheduleSetupSubject),
				String.format(res.getString(R.string.scheduleSetupMessage, formatStartTime(startTime))));
	}

	void updateServiceNotice(int messageKey) {
		Resources res = getApplicationContext().getResources();
		notifier.sendNotice(50002L, res.getString(R.string.updateServiceSubject),
				res.getString(messageKey));
	}

	/**
	 * wrapping toast to remove dependency during unit test.
	 * 
	 * @param message
	 * @param length
	 */
	void makeToast(String message, int length) {
		Toast.makeText(getApplicationContext(), message, length).show();
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
			startTime = startTime.hourOfDay().addToCopy(1);
			if (!isAlarmAlreadyUp()) {
				setAlarm(startTime);
			}
		} else {
			// start today or next trade date at 9PM
			while (startTime.getDayOfWeek() == DateTimeConstants.SATURDAY || startTime.getDayOfWeek() == DateTimeConstants.SUNDAY) {
				startTime = startTime.dayOfMonth().addToCopy(1);
			}
			startTime = startTime.hourOfDay().setCopy(RUN_START_HOUR);
			if (!isAlarmAlreadyUp()) {
				setAlarm(startTime);
			}
		}
	}

	void setAlarm(DateTime startTime) {
		Intent dailyIntent = new Intent(this, UpdateService.class);
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		PendingIntent pDailyIntent = PendingIntent.getService(this, DAILY_INTENT_ID, dailyIntent, 0);
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		long interval = AlarmManager.INTERVAL_HOUR;
		alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, startTime.getMillis(), interval, pDailyIntent);
		Log.d(TAG, "Alarm Manager is setup to start service at " + formatStartTime(startTime));
		nextStartTime = startTime;
		scheduleSetupNotice(startTime);
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

	boolean isAlarmAlreadyUp() {
		boolean alarmUp = (PendingIntent.getService(getApplicationContext(), DAILY_INTENT_ID, new Intent(this, UpdateService.class),
				PendingIntent.FLAG_NO_CREATE) != null);

		if (alarmUp) {
			Log.d(TAG, "Alarm is already active");
		}
		return alarmUp;
	}


	boolean isMarketOpen() {
		DateTime cal = getCurrentNYTime();
		int hour = cal.getHourOfDay();
		Log.d(TAG, "Is EST Hour of day (" + hour + ") between 8 and 17 "+DateUtils.formatDateTime(getApplicationContext(), cal.getMillis(), DateUtils.FORMAT_ABBREV_RELATIVE));
		boolean marketOpen = false; // set to true to ignore market
		if (hour > 8 && hour < 17 && cal.getDayOfWeek() != DateTimeConstants.SATURDAY && cal.getDayOfWeek() != DateTimeConstants.SUNDAY) {
			marketOpen = true;
		}
		return marketOpen;
	}

	int getPrefUpdateFrequency() {
		int frequency = 3;
		try {
			SharedPreferences sharedPref = getSharedPreferences();
			frequency = Integer.parseInt(sharedPref.getString(KEY_PREF_UPDATE_FREQUENCY_TYPE, "3"));
		} catch (Exception e) {
			frequency = 3;
			Log.e(TAG, "Exception reading update frequency preference", e);
		}
		return frequency;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (KEY_PREF_UPDATE_FREQUENCY_TYPE.equals(key)) {
			SharedPreferences sharedPref = getSharedPreferences();
			String updateFrequency = sharedPref.getString(KEY_PREF_UPDATE_FREQUENCY_TYPE, "3");
			frequency = Integer.parseInt(updateFrequency);
		}
	}

	/*
	 * boolean isMarketOpen() { Calendar cal = getCurrentTime(); int hour =
	 * cal.get(Calendar.HOUR_OF_DAY);
	 * Log.d(TAG,"Is Hour of day ("+hour+") between 8 and 17 "); boolean
	 * marketOpen = false; // set to true to ignore market if (hour > 8 && hour
	 * < 17 && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY &&
	 * cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) { marketOpen = true; }
	 * return marketOpen; } Calendar getCurrentTime() { Calendar cal =
	 * GregorianCalendar.getInstance(TimeZone.getTimeZone("US/Eastern"),
	 * Locale.US); DateTime dt = new DateTime()zzz; return cal; }
	 */

	DateTime getCurrentNYTime() {
		DateTime dt = new DateTime();
		// translate to New York local time
		DateTime nyDateTime = dt.withZone(DateTimeZone.forID("America/New_York"));
		return nyDateTime;

	}

	class Updater extends Thread {
		public Updater() {
			super("Updater");
		}

		static final long	DELAY	= 180000L;
		boolean				running	= false;

		public boolean isRunning() {
			return running;
		}

		public void restart() {
			Log.d(TAG, "restart updater");
			synchronized (lock) {
				lock.notify();
			}
		}

		@Override
		public void run() {
			running = true;

			while (running) {
				try {
					updateServiceNotice(R.string.serviceRunningMessage);
					Log.d(TAG, "Updater Running");
					List<PaiStudy> studies = processor.process(null);
					notifier.updateNotification(studies);
					if (isMarketOpen()) {
						updateServiceNotice(R.string.servicePausedMessage);
						synchronized (lock) {
							lock.wait(frequency * 60000);
						}
					} else {
						Log.d(TAG, "Market is Closed - Service will stop");
						running = false;
						stopSelf();
					}
				} catch (InterruptedException e) {
					Log.d(TAG, "Service has been interrupted");
					running = false;
				}
			}
		}

	}

	class OneTimeUpdate extends Thread {
		String	symbol;

		public OneTimeUpdate(String symbol) {
			super("OneTimeUpdate");
			this.symbol = symbol;
		}

		@Override
		public void run() {
			try {
				Log.d(TAG, "One Time Update Running for " + symbol);
				List<PaiStudy> studies = processor.process(symbol);
				notifier.updateNotification(studies);
				Log.d(TAG, "One Time Update Complete for " + symbol);
				stopSelf();
			} catch (InterruptedException e) {
				Log.d(TAG, "One Time Update has been interrupted");
			}
		}

	}

}
