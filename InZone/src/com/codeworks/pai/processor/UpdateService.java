package com.codeworks.pai.processor;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codeworks.pai.R;
import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.PaiStudyTable;
import com.codeworks.pai.db.ServiceLogTable;
import com.codeworks.pai.db.model.PaiStudy;
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
	public static final String	ACTION_REPEATING	            = "action_repeating";
	public static final String	ACTION_ONE_TIME					= "action_one_time";
	public static final String	ACTION_MANUAL					= "action_manual";
	public static final String	ACTION_MANUAL_MENU				= "action_manual_menu";
	public static final String	ACTION_PRICE_UPDATE				= "action_price_update";
	public static final String	ACTION_SET_PROGRESS_BAR			= "action_set_progress_bar";

	public static final String	KEY_PREF_UPDATE_FREQUENCY_TYPE	= "pref_updateFrequencyType";

	Updater						updater;
	Processor					processor						= null;
	Notifier					notifier						= null;
	ProgressBar					progressBar						= null;

	public static final class Lock {
		boolean notified = false;
	}

	private final Lock	lock			= new Lock();
	private final Lock  priceUpdateLock = new Lock();

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
	}

	SharedPreferences getSharedPreferences() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		return sharedPref;
	}

	@Override
	public synchronized int onStartCommand(Intent updateIntent, int flags, int startId) {
		long startMillis = System.currentTimeMillis();
		if (updateIntent == null) {
			Log.e(TAG, "onStartCommand receive null intent");
			return START_STICKY;
		}
		Bundle bundle = updateIntent.getExtras();
		if (bundle == null) {
			Log.e(TAG, "onStartCommand receive null bundle");
			return START_STICKY;
		}
		String action = (String) bundle.get(SERVICE_ACTION);
		if (ACTION_SCHEDULE.equals(action) || ACTION_MANUAL.equals(action) || ACTION_MANUAL_MENU.equals(action) || ACTION_REPEATING.equals(action)) {
			if (ACTION_MANUAL.equals(action)) {
				Log.i(TAG, "Manual start");
			} else {
				Log.i(TAG, "Scheduled start");
				createLogEventStart(action);
			}
			// clear service log on schedule start 
			if (ACTION_SCHEDULE.equals(action)) {
				clearServiceLog();
			}
			if (!updater.isRunning()) {
				updater.start();
				//makeToast("Price Update Service Started", Toast.LENGTH_LONG);
				Log.i(TAG, "on Starte'd");
			} else {
				updater.restart(ACTION_MANUAL.equals(action)); // Full update if from MENU or Schedule otherwise priceOnly
				Log.i(TAG, "an Re-Starte'd");
			}

			getAlarmSetup().start();

		} else if (ACTION_ONE_TIME.equals(action)) {
			Log.d(TAG, "One Time start");
			String symbol = bundle.getString(SERVICE_SYMBOL);
			new OneTimeUpdate(symbol).start();
		} else if (ACTION_PRICE_UPDATE.equals(action)) {
			Log.i(TAG, "Price Update start");
			if (!updater.isRunning()) {
				Log.i(TAG, "Start Price Update starting");
				updater.priceOnly = true;
				updater.start();
				Log.i(TAG, "Price Update on Starte'd");
			} else {
				Log.i(TAG, "Re-Start Price Update starting");
				updater.restart(true); // true for price only
				Log.i(TAG, "Price Update an Re-Starte'd");
			}
		} else {
			Log.i(TAG, "on Starte'd by unknown");
		}
		Log.i(TAG, "On Start Command execution time ms=" + (System.currentTimeMillis() - startMillis));
		return START_STICKY;
	}

	
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
		} else {
			message = action;
			//return; // no logging
		}
		ContentValues values = new ContentValues();

		values.put(ServiceLogTable.COLUMN_MESSAGE, message);
		values.put(ServiceLogTable.COLUMN_SERVICE_TYPE, ServiceType.START.getIndex());
		values.put(ServiceLogTable.COLUMN_TIMESTAMP, DateTime.now().toString(ServiceLogTable.timestampFormat));

		insertServiceLog(values);
	}

	void createLogEvent(int messageKey, int numMessages, boolean priceOnly, long runtime) {
		Resources res = getApplicationContext().getResources();
		ContentValues values = new ContentValues();
		values.put(ServiceLogTable.COLUMN_MESSAGE, res.getString(messageKey));
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

	void insertServiceLog(ContentValues values) {
		getContentResolver().insert(PaiContentProvider.SERVICE_LOG_URI, values);
	}

	void clearServiceLog() {
		String selection = ServiceLogTable.COLUMN_TIMESTAMP + " < ? ";
		String[] selectionArgs = { new DateTime().toString(ServiceLogTable.timestampFormat).substring(0,10) };
		int rowsDeleted = getContentResolver().delete(PaiContentProvider.SERVICE_LOG_URI, selection, selectionArgs);
		Log.d(TAG,rowsDeleted+" Deleted Sevice Log Events Deleted "+selectionArgs[0]);
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

	boolean isMarketOpen() {
		DateTime cal = getCurrentNYTime();
		int hour = cal.getHourOfDay();
		Log.d(TAG, "Is EST Hour of day (" + hour + ") between "+AlarmSetup.RUN_START_HOUR+" and "+AlarmSetup.RUN_END_HOUR+" "+cal.toString());
		boolean marketOpen = false; // set to true to ignore market
		if (hour >= AlarmSetup.RUN_START_HOUR && hour < AlarmSetup.RUN_END_HOUR && cal.getDayOfWeek() != DateTimeConstants.SATURDAY && cal.getDayOfWeek() != DateTimeConstants.SUNDAY) {
			marketOpen = true;
		}
		return marketOpen;
	}
	
	/**
	 * getCurrentNYTime hook for Testing.
	 * @return
	 */
	DateTime getCurrentNYTime() {
		return DateUtils.getCurrentNYTime();
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




	void progressBarStart() {
		Intent intent = new Intent(BROADCAST_UPDATE_PROGRESS_BAR);
		// Add data
		intent.putExtra(PROGRESS_BAR_STATUS, 0);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	void progressBarStop() {
		Intent intent = new Intent(BROADCAST_UPDATE_PROGRESS_BAR);
		// Add data
		intent.putExtra(PROGRESS_BAR_STATUS, 100);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	class Updater extends Thread {
		public Updater() {
			super("Updater");
		}

		static final long	DELAY	= 180000L;
		boolean				running	= false;
		boolean				priceOnly = false;
		int					numMessages = 0;

		public boolean isRunning() {
			return running;
		}

		public int getNumMessages() {
			return numMessages;
		}
		public void restart(boolean priceOnly) {
			Log.d(TAG, "restart updater");
			synchronized (lock) {
				this.priceOnly = priceOnly;
				lock.notified = true;
				lock.notify();
			}
		}

		@Override
		public void run() {
			running = true;
			long startTime;
			while (running) {
				try {
					startTime = System.currentTimeMillis();
					progressBarStart();
					Log.d(TAG, "Updater Running");
					List<PaiStudy> studies;
					if (priceOnly) {
						studies = processor.updatePrice(null);
						Log.i(TAG,"Price Only Update Runtime milliseconds="+(System.currentTimeMillis() - startTime));
					} else {
						studies = processor.process(null);
						Log.i(TAG,"Complete Update Runtime milliseconds="+(System.currentTimeMillis() - startTime));
					}
				
					notifier.updateNotification(studies);
					boolean historyReloaded = scanHistoryReloaded(studies);
					if (isMarketOpen()) {
						createLogEvent(historyReloaded ?  R.string.servicePausedHistory: R.string.servicePausedMessage, numMessages++, priceOnly, System.currentTimeMillis() - startTime);
						progressBarStop();
						synchronized (lock) {
							// if notified true we missed a notify, so restart loop.
							if (!lock.notified) {
								// while running on timer we only update price.
								priceOnly = true;
								lock.wait(60000);
							}
							lock.notified = false;
						}
					} else {
						Log.d(TAG, "Market is Closed - Service will stop");
						running = false;
						createLogEvent(historyReloaded ? R.string.serviceStoppedHistory : R.string.serviceStoppedMessage, numMessages++, priceOnly, System.currentTimeMillis() - startTime);
						progressBarStop();
						stopSelf();
					}
				} catch (InterruptedException e) {
					Log.d(TAG, "Service has been interrupted");
					running = false;
				}
				//progressBarStop();
			}
		}

		boolean scanHistoryReloaded(List<PaiStudy> studies) {
			boolean historyReloaded = false;
			for (PaiStudy study : studies) {
				if (study.wasHistoryReloaded()) {
					historyReloaded = true;
				}
			}
			return historyReloaded;
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
			} catch (InterruptedException e) {
				Log.d(TAG, "One Time Update has been interrupted");
			}
		}

	}
	
}
