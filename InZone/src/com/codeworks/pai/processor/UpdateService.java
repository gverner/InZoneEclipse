package com.codeworks.pai.processor;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codeworks.pai.R;
import com.codeworks.pai.db.model.PaiStudy;

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
	public static final String	ACTION_ONE_TIME					= "action_one_time";
	public static final String	ACTION_MANUAL					= "action_manual";
	public static final String	ACTION_MANUAL_MENU				= "action_manual_menu";
	public static final String	ACTION_PRICE_UPDATE				= "action_price_update";
	public static final String	ACTION_SET_PROGRESS_BAR			= "action_set_progress_bar";

	public static final String	KEY_PREF_UPDATE_FREQUENCY_TYPE	= "pref_updateFrequencyType";
	Updater						updater;
	PriceUpdater				priceUpdater;
	Processor					processor						= null;
	Notifier					notifier						= null;
	ProgressBar					progressBar						= null;
	AlarmSetup					alarmSetup						= null;

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
		priceUpdater = new PriceUpdater();
		alarmSetup = new AlarmSetup(getApplicationContext(), notifier);
		Log.d(TAG, "on Create'd2");
	}

	@Override
	public synchronized void onDestroy() {
		super.onDestroy();

		if (updater.isRunning()) {
			updater.interrupt();
		}
		updater = null;
		
		if (priceUpdater.isRunning()) {
			priceUpdater.interrupt();
		}
		priceUpdater = null;
		
		alarmSetup = null;
		
		SharedPreferences sharedPref = getSharedPreferences();
		sharedPref.unregisterOnSharedPreferenceChangeListener(this);

		Log.d(TAG, "on Destroy'd");
		updateServiceNotice(R.string.serviceStoppedMessage, updater != null ? updater.getNumMessages(): 0);
		makeToast("Price Update Service Stopped", Toast.LENGTH_LONG);
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
		if (ACTION_SCHEDULE.equals(action) || ACTION_MANUAL.equals(action) || ACTION_MANUAL_MENU.equals(action)) {
			if (ACTION_MANUAL.equals(action)) {
				Log.i(TAG, "Manual start");
			} else {
				Log.i(TAG, "Scheduled start");
				scheduledStartNotice();
			}
			if (!updater.isRunning()) {
				updater.start();
				makeToast("Price Update Service Started", Toast.LENGTH_LONG);
				Log.i(TAG, "on Starte'd");
			} else {
				
				updater.restart(!ACTION_MANUAL_MENU.equals(action)); // read history if from MENU
				Log.i(TAG, "an Re-Starte'd");
			}
			if (!alarmSetup.isAlive() && !alarmSetup.isRunning()) {
				try {
					alarmSetup.start();
				} catch (IllegalThreadStateException e) {
					Log.e(TAG, "Alarm Setup already Running");
				}
			}

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
				updater.restart(true); // TODO should be true for price only
										// ..interrupt sleep and restart now
				Log.i(TAG, "Price Update an Re-Starte'd");
			}
		} else {
			Log.i(TAG, "on Starte'd by unknown");
		}
		Log.i(TAG, "On Start Command execution time ms=" + (System.currentTimeMillis() - startMillis));
		return START_STICKY;
	}

	void scheduledStartNotice() {
		Resources res = getApplicationContext().getResources();
		notifier.sendNotice(50000L, res.getString(R.string.scheduledStartSubject),
				String.format(res.getString(R.string.scheduledStartMessage)));
	}

	void updateServiceNotice(int messageKey, int numMessages) {
		Resources res = getApplicationContext().getResources();
		notifier.sendServiceNotice(50002, res.getString(R.string.updateServiceSubject),
				res.getString(messageKey), numMessages);
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
		DateTime cal = alarmSetup.getCurrentNYTime();
		int hour = cal.getHourOfDay();
		Log.d(TAG, "Is EST Hour of day (" + hour + ") between "+AlarmSetup.RUN_START_HOUR+" and "+AlarmSetup.RUN_END_HOUR+" "+DateUtils.formatDateTime(getApplicationContext(), cal.getMillis(), DateUtils.FORMAT_ABBREV_RELATIVE));
		boolean marketOpen = false; // set to true to ignore market
		if (hour >= AlarmSetup.RUN_START_HOUR && hour < AlarmSetup.RUN_END_HOUR && cal.getDayOfWeek() != DateTimeConstants.SATURDAY && cal.getDayOfWeek() != DateTimeConstants.SUNDAY) {
			marketOpen = true;
		}
		return marketOpen;
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
					updateServiceNotice(R.string.serviceRunningMessage, numMessages++);
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
					if (isMarketOpen()) {
						updateServiceNotice(R.string.servicePausedMessage, numMessages++);
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
	
	class PriceUpdater extends Thread {
		public PriceUpdater() {
			super("PriceUpdater");
		}

		static final long	DELAY	= 30000L;
		boolean				running	= false;

		public boolean isRunning() {
			return running;
		}

		public void restart() {
			Log.d(TAG, "restart price updater");
			synchronized (priceUpdateLock) {
				priceUpdateLock.notify();
			}
		}

		@Override
		public void run() {
			running = true;

			while (running) {
				try {
					Log.d(TAG, "Price Updater Running");
					List<PaiStudy> studies = processor.updatePrice(null);
					if (isMarketOpen()) {
						synchronized (priceUpdateLock) {
							priceUpdateLock.wait(DELAY);
						}
					} else {
						Log.d(TAG, "Market is Closed - Price Updater Service will stop");
						running = false;
					}
				} catch (InterruptedException e) {
					Log.d(TAG, "Price Updater Service has been interrupted");
					running = false;
				}
			}
		}

	}
}
