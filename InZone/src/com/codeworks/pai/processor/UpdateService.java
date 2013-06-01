package com.codeworks.pai.processor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.codeworks.pai.db.model.PaiStudy;

public class UpdateService extends Service {
	private static final String TAG = UpdateService.class.getSimpleName();
	public static final String BROADCAST_ACTION = "com.codeworks.pai.updateservice.results";
	public static final String EXTRA_QUOTES = "com.codeworks.pai.updateservice.quotes";
	public static final String EXTRA_RESULTS = "com.codeworks.pai.updateservice.quotes";
	public static final String SERVICE_ACTION = "com.codeworks.pai.updateservice.action";
	static int RUN_START_HOUR = 9;
	static int RUN_END_HOUR = 18;
	public static final int DAILY_INTENT_ID = 5453;
	public static final int ONE_TIME_INTENT_ID = 5463;
	public static final String ACTION_SCHEDULE = "action_schedule";
	public static final String ACTION_ONE_TIME = "action_one_time";
	
	private Updater updater;
	private static final class Lock {
	}

	private final Object lock = new Lock();
	private Calendar nextStartTime = null;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
    	 updater = new Updater();
		Log.d(TAG, "on Create'd2");
	}

	@Override
	public synchronized int onStartCommand(Intent intent, int flags, int startId) {
		Intent updateIntent;
		updateIntent = intent;
		Bundle bundle = updateIntent.getExtras();
		String action = (String) bundle.get(SERVICE_ACTION);
		if (ACTION_SCHEDULE.equals(action)) {
			Log.d(TAG,"Scheduled start");
			if (!updater.isRunning()) {
				updater.start();
				Toast.makeText(getApplicationContext(), "Price Update Service Started", Toast.LENGTH_LONG).show();
				Log.d(TAG, "on Starte'd");
			} else {
				Log.d(TAG, "allready Starte'd");
			}

		} else if (ACTION_ONE_TIME.equals(action)) {
			Log.d(TAG,"One Time start");
			if (updater.isRunning()) {
				updater.restart(); // interrupt sleep and restart now
			} else { 
			   updater.start();
			}
		}
		Log.d(TAG, "on Starte'd");
	    updateAlarm();
		return START_STICKY;
	}
	
	/**
	 * setup Alarm Manager to restart this service every hour between the hours of 9AM AND 5PM US/EASTERN.
	 */
	void updateAlarm() {

		Calendar startTime = GregorianCalendar.getInstance(TimeZone.getTimeZone("US/Eastern"));
		startTime.set(Calendar.MINUTE, 0);
		int hour = startTime.get(Calendar.HOUR_OF_DAY);
		if (hour > RUN_END_HOUR) {
			// run next trade date a 9AM
			startTime.add(Calendar.DAY_OF_MONTH, 1);
			while (startTime.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || startTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				startTime.add(Calendar.DAY_OF_MONTH, 1);
			}
			startTime.set(Calendar.HOUR_OF_DAY, RUN_START_HOUR);
			setAlarm(startTime);
		} else if (hour > RUN_START_HOUR) {
			// run each hour
			startTime.add(Calendar.HOUR_OF_DAY, 1);
			if (!isAlarmAlreadyUp()) {
				setAlarm(startTime);
			}
		} else {
			// start today or next trade date at 9PM
			while (startTime.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || startTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				startTime.add(Calendar.DAY_OF_MONTH, 1);
			}
			startTime.set(Calendar.HOUR_OF_DAY, RUN_START_HOUR);
			if (!isAlarmAlreadyUp()) {
				setAlarm(startTime);
			}
		}
	}
	
	void setAlarm(Calendar startTime) {
		Intent dailyIntent = new Intent(this, UpdateService.class);
		PendingIntent pDailyIntent = PendingIntent.getService(this, DAILY_INTENT_ID, dailyIntent, 0);
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		long interval = AlarmManager.INTERVAL_HOUR;
		alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, startTime.getTimeInMillis(), interval, pDailyIntent);
		Log.d(TAG, "Alarm Manager is setup to start service at " + formatStartTime(startTime));
		nextStartTime = startTime;
	}

	String formatStartTime(Calendar startTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm aa Z", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		return sdf.format(startTime.getTime());
	}
	
	boolean isAlarmAlreadyUp() {
		boolean alarmUp = (PendingIntent.getService(getApplicationContext(), DAILY_INTENT_ID, new Intent(this, UpdateService.class),
				PendingIntent.FLAG_NO_CREATE) != null);

		if (alarmUp) {
			Log.d(TAG, "Alarm is already active");
		}
		return alarmUp;
	}

	@Override
	public synchronized void onDestroy() {
		super.onDestroy();
		
		if (updater.isRunning()) {
			updater.interrupt();
		}
			updater = null;

		Log.d(TAG, "on Destroy'd");
		String formattedNextStartTime = "";
		if (nextStartTime != null) {
		   formattedNextStartTime = formatStartTime(nextStartTime);
		}
		Toast.makeText(getApplicationContext(), "Price Update Service Stopped Next Restart "+formattedNextStartTime, Toast.LENGTH_LONG).show();
	}

	boolean isMarketOpen() {
		Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("US/Eastern"), Locale.US);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		boolean marketOpen = false; // set to true to ignore market
		if (hour > 8 && hour < 17 && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
				&& cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
			marketOpen = true;
		}
		return marketOpen;
	}
		
	class Updater extends Thread {
		Processor processor = new Processor(getContentResolver(), new YahooReader());
		Notifier notification = new Notifier(getApplicationContext());
		public Updater() {
			super("Updater");
		}
		static final long DELAY = 180000L;
		boolean running = false;
		
		public boolean isRunning() {
			return running;
		}
		
		public void restart() {
			synchronized(lock) {
				lock.notify();
			}
		}
		
		@Override
		public void run() {
			running = true;

			while (running) {
				try {
					Log.d(TAG, "Updater Running");
					List<PaiStudy> studies = processor.process();
					notification.updateNotification(studies);
					if (isMarketOpen()) {
						synchronized(lock) {
							lock.wait(DELAY);
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
    
}
