package com.codeworks.pai.processor;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;

import com.codeworks.pai.R;
import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.ServiceLogTable;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.db.model.ServiceType;

public class UpdateService extends Service implements OnSharedPreferenceChangeListener {
	private static final String	TAG								= UpdateService.class.getSimpleName();

	public static final String	BROADCAST_ACTION				= "com.codeworks.pai.updateservice.results";
	public static final String	EXTRA_QUOTES					= "com.codeworks.pai.updateservice.quotes";
	public static final String	EXTRA_RESULTS					= "com.codeworks.pai.updateservice.quotes";
	public static final String	SERVICE_ACTION					= "com.codeworks.pai.updateservice.action";
	public static final String	SERVICE_SYMBOL					= "com.codeworks.pai.updateservice.symbol";
	public static final String	BROADCAST_UPDATE_PROGRESS_BAR	= "com.codeworks.pai.updateservice.progressBar";
	public static final String	PROGRESS_BAR_STATUS				= "com.codeworks.pai.updateservice.progress.status";

	public static final String	ACTION_SCHEDULE					= "action_schedule";
	public static final String	ACTION_REPEATING				= "action_repeating";
	public static final String	ACTION_ONE_TIME					= "action_one_time";
	public static final String	ACTION_MANUAL					= "action_manual";
	public static final String	ACTION_MANUAL_MENU				= "action_manual_menu";
	public static final String	ACTION_PRICE_UPDATE				= "action_price_update";
	public static final String	ACTION_SET_PROGRESS_BAR			= "action_set_progress_bar";
	public static final String	ACTION_BOOT						= "action_boot";

	// Service Control bit map
	public static final int		SERVICE_PRICE_ONLY				= 1;
	public static final int		SERVICE_FULL					= 2;
	public static final int		SERVICE_REPEATING				= 4;
	public static final int		SERVICE_ONE_TIME				= 8;

	public static final String	KEY_PREF_UPDATE_FREQUENCY_TYPE	= "pref_updateFrequencyType";
	public static final long	MS_BETWEEN_RUNS					= 60000;

	Processor					processor						= null;
	Notifier					notifier						= null;
	ProgressBar					progressBar						= null;
	boolean						shutdownInProcess				= false;
	Looper						mServiceLooper;
	ServiceHandler				mServiceHandler;

	public static final class Lock {
		boolean	notified	= false;
	}

	private final Lock	lock		= new Lock();

	volatile int		frequency	= 3;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		processor = new ProcessorImpl(getContentResolver(), new DataReaderYahoo(),getApplicationContext());
		notifier = new NotifierImpl(getApplicationContext());
		SharedPreferences sharedPref = getSharedPreferences();
		sharedPref.registerOnSharedPreferenceChangeListener(this);
		frequency = getPrefUpdateFrequency();

		// Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block. We also make it
		// background priority so CPU-intensive work will not disrupt our UI.

		HandlerThread thread = new HandlerThread("ServiceStartArguments", android.os.Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		shutdownInProcess = false;
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.d(TAG, "on Create'd2");
	}

	@Override
	public synchronized void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "Shutdonw queue started");
		synchronized (lock) {
			shutdownInProcess = true;
			// wait for work to complete before shutdown of queue
			if (mServiceHandler != null) {
				mServiceHandler.getLooper().quit();
				mServiceHandler = null;
			}
		}
		Log.d(TAG, "Shutdonw queue complete");

		if (mServiceLooper != null) {
			mServiceLooper = null;
		}
		if (notifier != null) {
			notifier = null;
		}

		if (processor != null) {
			processor.onClose();
			processor = null;
		}

		SharedPreferences sharedPref = getSharedPreferences();
		sharedPref.unregisterOnSharedPreferenceChangeListener(this);

		Log.d(TAG, "on Destroy'd");
	}

	@Override
	public synchronized int onStartCommand(Intent updateIntent, int flags, int startId) {
		if (updateIntent == null) {
			Log.e(TAG, "onStartCommand receive null intent");
			return START_STICKY;
		}
		Bundle bundle = updateIntent.getExtras();
		if (bundle == null) {
			Log.e(TAG, "onStartCommand receive null bundle");
			return START_STICKY;
		}
		return serviceHandlerOnStartCommand(bundle, startId);
	}

	int serviceHandlerOnStartCommand(Bundle bundle, int startId) {
		long startMillis = System.currentTimeMillis();
		String action = (String) bundle.get(SERVICE_ACTION);
		if (ACTION_SCHEDULE.equals(action) || ACTION_MANUAL_MENU.equals(action) || ACTION_REPEATING.equals(action) || ACTION_BOOT.equals(action)) {
			Log.d(TAG, "Scheduled start");
			createLogEventStart(action);
			
			// clear service log on schedule start
			if (ACTION_SCHEDULE.equals(action) || ACTION_MANUAL_MENU.equals(action) || ACTION_BOOT.equals(action)) {
				clearServiceLog();
			}
			powerLockAquire(30000);
			// For each start request, send a message to start a job and deliver the
			// start ID so we know which request we're stopping when we finish the job
			// A second repeating can be added to the queue to get a FULL refresh 
			Message msg = mServiceHandler.obtainMessage();
			msg.what = SERVICE_REPEATING;
			msg.arg1 = startId;
			msg.arg2 = SERVICE_REPEATING | SERVICE_FULL;
			mServiceHandler.sendMessage(msg);
			Log.d(TAG, "Repeating Enqueued");
			getAlarmSetup().start();
		} else if (ACTION_MANUAL.equals(action)) {
			Log.d(TAG, "Manual Price Update start");
			Message msg = mServiceHandler.obtainMessage();
			msg.what = SERVICE_ONE_TIME;
			msg.arg1 = startId;
			msg.arg2 = SERVICE_ONE_TIME | SERVICE_PRICE_ONLY;
			mServiceHandler.sendMessage(msg);
		} else if (ACTION_ONE_TIME.equals(action)) {
			Log.d(TAG, "One Time start");
			String symbol = bundle.getString(SERVICE_SYMBOL);
			new OneTimeUpdate(symbol).start();
		} else if (ACTION_PRICE_UPDATE.equals(action)) {
			Log.d(TAG, "Price Update start");
			Message msg = mServiceHandler.obtainMessage();
			msg.what = SERVICE_ONE_TIME;
			msg.arg1 = startId;
			msg.arg2 = SERVICE_ONE_TIME | SERVICE_PRICE_ONLY;
			mServiceHandler.sendMessage(msg);
		} else {
			Log.d(TAG, "on Starte'd by unknown");
		}
		Log.d(TAG, "On Start Command execution time ms=" + (System.currentTimeMillis() - startMillis));
		return START_STICKY;
	}
	
	void powerLockAquire(long timeout) {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "InZone");
		wl.acquire(timeout);
	}
	
	// Handler that receives messages from the thread
	@SuppressLint("HandlerLeak")
	final class ServiceHandler extends Handler {
		int	numMessages	= 0;

		public ServiceHandler(Looper looper) {
			// leaks should not be a problem with short message delays
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			// synchronized so onDestroy waits for empty queue
			synchronized (lock) {
				if (shutdownInProcess) {
					return;
				}
				boolean priceOnly = (msg.arg2 & SERVICE_PRICE_ONLY) == SERVICE_PRICE_ONLY;
				boolean repeating = (msg.what == SERVICE_REPEATING);

				long startTime;
				try {
					Log.d(TAG, "Update Running " + msg.arg1);
					startTime = System.currentTimeMillis();
					progressBarStart();
					List<Study> studies;
					if (priceOnly) {
						studies = processor.updatePrice(null);
						Log.i(TAG, "Processor UpdatePrice Runtime milliseconds=" + (System.currentTimeMillis() - startTime));
					} else {
						studies = processor.process(null);
						Log.i(TAG, "Processor process Runtime milliseconds=" + (System.currentTimeMillis() - startTime));
					}
					if (isMarketOpen()) { // Notify only during market hours
						notifier.updateNotification(studies);
					}
					boolean historyReloaded = scanHistoryReloaded(studies);
					int logMessage;
					if (repeating && isMarketOpen()) {
						logMessage = historyReloaded ? R.string.servicePausedHistory : R.string.servicePausedMessage;
					} else {
						if (repeating) {
							Log.d(TAG, "Market is Closed - Service will stop");
						} else {
							Log.d(TAG, "Service will stop");
						}
						logMessage = historyReloaded ? R.string.serviceStoppedHistory : R.string.serviceStoppedMessage;
						repeating = false;
					}
					createLogEvent(logMessage, numMessages++, priceOnly, System.currentTimeMillis() - startTime, msg.arg1);
					progressBarStop();
					priceOnly = !(numMessages % 10 == 0);

				} catch (InterruptedException e) {
					Log.d(TAG, "Service has been interrupted");
				}
				if (repeating) {
					// we only want one repeating message in the queue
					if (!mServiceHandler.hasMessages(SERVICE_REPEATING)) {
						int arg2 = SERVICE_REPEATING | (priceOnly == true ? SERVICE_PRICE_ONLY : SERVICE_FULL);
						Log.d(TAG, "Old arg2= " + msg.arg2 + " new arg2 = " + arg2);
						Message nextMsg = mServiceHandler.obtainMessage(msg.what, msg.arg1, arg2);
						if (mServiceHandler.sendMessageDelayed(nextMsg, 60000)) {
							Log.d(TAG, "Repeating Re-Enqueued");
						} else {
							Log.d(TAG, "Service is down");
						}
					} else {
						Log.d(TAG, "Repeating Allready No Re-Enqueue");
					}
				} else {
					// Stop the service using the startId, so that we don't stop
					// the service in the middle of handling another job
					// stopSelf(msg.arg1);
					// this didn't work, stopped all and crashed, requires that
					// startId is in proper order.
				}
				Log.d(TAG, "Update Complete " + msg.arg1);
			}
		}
	}
	
	/**
	 * Wrap AlarmSetup to allow replacement during Unit Test
	 * @return
	 */
	AlarmSetup getAlarmSetup() {
		return new AlarmSetup(getApplicationContext(), notifier);
	}

	void createLogEventStart(String action) {
		Resources res = getApplicationContext().getResources();
		String message;
		if (ACTION_SCHEDULE.equals(action)) {
			message = res.getString(R.string.startTypeSchedule);
		} else if (ACTION_REPEATING.equals(action)) {
			message = res.getString(R.string.startTypeRepeating);
		} else if (ACTION_MANUAL.equals(action)) {
			message = res.getString(R.string.startTypeManual);
		} else if (ACTION_MANUAL_MENU.equals(action)) {
			message = res.getString(R.string.startTypeManualMenu);
		} else if (ACTION_BOOT.equals(action)) {
			message = res.getString(R.string.startTypeBoot);
		} else {
			message = action;
			// return; // no logging
		}
		ContentValues values = new ContentValues();

		values.put(ServiceLogTable.COLUMN_MESSAGE, message);
		values.put(ServiceLogTable.COLUMN_SERVICE_TYPE, ServiceType.START.getIndex());
		values.put(ServiceLogTable.COLUMN_TIMESTAMP, DateTime.now().toString(ServiceLogTable.timestampFormat));

		insertServiceLog(values);
	}

	void createLogEvent(int messageKey, int numMessages, boolean priceOnly, long runtime, long threadId) {
		Resources res = getApplicationContext().getResources();
		ContentValues values = new ContentValues();
		values.put(ServiceLogTable.COLUMN_MESSAGE, res.getString(messageKey) + " " + threadId);
		values.put(ServiceLogTable.COLUMN_SERVICE_TYPE, priceOnly ? ServiceType.PRICE.getIndex() : ServiceType.FULL.getIndex());
		values.put(ServiceLogTable.COLUMN_TIMESTAMP, DateTime.now().toString(ServiceLogTable.timestampFormat));
		values.put(ServiceLogTable.COLUMN_ITERATION, numMessages);
		values.put(ServiceLogTable.COLUMN_RUNTIME, runtime);

		insertServiceLog(values);
	}

	void logServiceEvent(ServiceType serviceType, int stringId) {
		Resources res = getApplicationContext().getResources();
		ContentValues values = new ContentValues();
		values.put(ServiceLogTable.COLUMN_MESSAGE, res.getString(stringId));
		values.put(ServiceLogTable.COLUMN_SERVICE_TYPE, serviceType.getIndex());
		values.put(ServiceLogTable.COLUMN_TIMESTAMP, DateTime.now().toString(ServiceLogTable.timestampFormat));
		
		insertServiceLog(values);
	}

	/**
	 * Wrap insert to allow replacement during unit test.
	 * @param values
	 */
	void insertServiceLog(ContentValues values) {
		getContentResolver().insert(PaiContentProvider.SERVICE_LOG_URI, values);
	}

	void clearServiceLog() {
		String selection = ServiceLogTable.COLUMN_TIMESTAMP + " < ? ";
		String[] selectionArgs = { new DateTime().toString(ServiceLogTable.timestampFormat).substring(0, 10) };
		int rowsDeleted = getContentResolver().delete(PaiContentProvider.SERVICE_LOG_URI, selection, selectionArgs);
		Log.d(TAG, rowsDeleted + " Deleted Sevice Log Events Deleted " + selectionArgs[0]);
	}

	/*
	 * String formatStartTime
	 */
	String formatStartTime(DateTime startTime) {
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		return fmt.print(startTime);
	}

	boolean isMarketOpen() {
		DateTime cal = getCurrentNYTime();
		int hour = cal.getHourOfDay();
		Log.d(TAG, "Is EST Hour of day (" + hour + ") between " + AlarmSetup.RUN_START_HOUR + " and " + AlarmSetup.RUN_END_HOUR + " " + cal.toString());
		boolean marketOpen = false; // set to true to ignore market
		if (hour >= AlarmSetup.RUN_START_HOUR && hour < AlarmSetup.RUN_END_HOUR && cal.getDayOfWeek() != DateTimeConstants.SATURDAY
				&& cal.getDayOfWeek() != DateTimeConstants.SUNDAY) {
			marketOpen = true;
		}
		return marketOpen;
	}

	/**
	 * getCurrentNYTime hook for Testing.
	 * 
	 * @return
	 */
	DateTime getCurrentNYTime() {
		return DateUtils.getCurrentNYTime();
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

	SharedPreferences getSharedPreferences() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		return sharedPref;
	}


	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (KEY_PREF_UPDATE_FREQUENCY_TYPE.equals(key)) {
			SharedPreferences sharedPref = getSharedPreferences();
			String updateFrequency = sharedPref.getString(KEY_PREF_UPDATE_FREQUENCY_TYPE, "3");
			frequency = Integer.parseInt(updateFrequency);
		}
	}

	void progressBarStart() {
		Intent intent = new Intent(BROADCAST_UPDATE_PROGRESS_BAR);
		// Add data
		intent.putExtra(PROGRESS_BAR_STATUS, 0);
		sendBroadcast(intent);
		Log.d(TAG, "Broadcast Progress Bar 0");
	}

	void progressBarStop() {
		Intent intent = new Intent(BROADCAST_UPDATE_PROGRESS_BAR);
		// Add data
		intent.putExtra(PROGRESS_BAR_STATUS, 100);
		sendBroadcast(intent);
		Log.d(TAG, "Broadcast Progress Bar 100");
	}

	boolean scanHistoryReloaded(List<Study> studies) {
		boolean historyReloaded = false;
		for (Study study : studies) {
			if (study.wasHistoryReloaded()) {
				historyReloaded = true;
			}
		}
		return historyReloaded;
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
				List<Study> studies = processor.process(symbol);
				notifier.updateNotification(studies);
				Log.d(TAG, "One Time Update Complete for " + symbol);
			} catch (InterruptedException e) {
				Log.d(TAG, "One Time Update has been interrupted");
			}
		}

	}

}
