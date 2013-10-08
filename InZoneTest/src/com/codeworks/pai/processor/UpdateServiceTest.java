package com.codeworks.pai.processor;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;
import android.test.mock.MockResources;

import com.codeworks.pai.R;
import com.codeworks.pai.mock.MockSharedPreferences;

public class UpdateServiceTest extends AndroidTestCase {

	class TestUpdateService extends UpdateService {


		SharedPreferences sharedPref = new MockSharedPreferences();
		int progressBarStopCalls = 0;
		int progressBarStartCalls = 0;
		int clearServiceLogCalls = 0;
		int insertServiceLogCalls = 0;
		
		boolean marketOpen = true;
		boolean callOriginalIsMarketOpen = false;

		@Override
		public void onCreate() {
			updater = new Updater();
		    notifier = new MockNotifier();
			processor = new MockProcessor();
			priceUpdater = new PriceUpdater();
			alarmSetup = new MockAlarmSetup(getContext(),notifier);
		}


		@Override
		public synchronized int onStartCommand(Intent updateIntent, int flags, int startId) {
			int returnvalue =super.onStartCommand(updateIntent, flags, startId);
			try {  // allow time for AlarmSetup thread
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return returnvalue;
		}
		@Override
		void clearServiceLog() {
			clearServiceLogCalls++;
		}
		@Override
		boolean isMarketOpen() {
			if (callOriginalIsMarketOpen) {
				return super.isMarketOpen();
			} else {
				return marketOpen;
			}
		}

		@Override
		void insertServiceLog(ContentValues values) {
			insertServiceLogCalls++;
		}
		
		@Override
		void makeToast(String message, int length) {
			System.out.println("MakeToast Message -> " + message);
		}

		
		int getPrefUpdateFrequency() {
			int frequency = 3;
			return frequency;
		}

		@Override
		SharedPreferences getSharedPreferences() {
			return sharedPref;
		}
		@Override
		public Context getApplicationContext() {
			
			Context context = new MockContext() {
				@Override
				public
				Resources getResources() {
					Resources resource = new MockResources() {
						@Override
						public String getString(int key) {
							if (key == R.string.scheduledStartSubject) {
								return "Value for Mock Key scheduledStartSubject";
							}
							if (key == R.string.scheduleSetupSubject) {
								return "Value for Mock Key R.string.scheduleSetupSubject";
							}
							if (key == R.string.updateServiceSubject) {
								return "Value for Mock key R.string.updateServiceSubject"; 
							}
							if (key == R.string.serviceStoppedMessage) {
								return "Value for Mock key R.string.serviceStoppedMessage";
							}
							if (key == R.string.serviceRunningMessage) {
								return "Value for Mock key R.string.serviceRunningMessage";
							}
							if (key == R.string.servicePausedMessage) {
								return "Value for Mock key R.string.servicePausedMessage";
							}
							if (key == R.string.scheduledStartMessage) {
								return "Value for Mock key R.string.scheduledStartMessage";
							}
							if (key == R.string.startTypeSchedule) {
								return "Value for Mock key R.string.startTypeSchedule";
							}
							if (key == R.string.startTypeRepeating) {
								return "Value for Mock key R.string.startTypeRepeating";
							}
							if (key == R.string.startTypeManual) {
								return "Value for Mock key R.string.startTypeManual";
							}
							if (key == R.string.startTypeManualMenu) {
								return "Value for Mock key R.string.startTypeManualMenu";
							}
							throw new NotFoundException(String.valueOf(key));
						}
						@Override
						public String getString(int key, Object ... formatArgs) {
							if (key == R.string.scheduledStartMessage) {
								return "Value for Mock R.string.scheduledStartMessage";
							}
						
							if (key == R.string.scheduledStartSubject) {
								return "Value for Mock Key scheduledStartSubject";
							}
							if (key == R.string.scheduleSetupSubject) {
								return "Value for Mock Key R.string.scheduleSetupSubject";
							}
							throw new NotFoundException(String.valueOf(key));
						}

					};
					return resource;
				}
			};
			
			return context;
		}


		@Override
		void progressBarStart() {
			progressBarStartCalls++;
		}


		@Override
		void progressBarStop() {
			progressBarStopCalls++;
		}
		
	}

	class MockAlarmSetup extends AlarmSetup {
		int		month			= 6; // June
		int		day				= 3;
		int		year			= 2013;
		int		hour			= 9;
		int		minute			= 1;

		public DateTime mockSystemTime = new DateTime();

		public List<DateTime> alarmTimes = new ArrayList<DateTime>();
		public boolean	alarmStarted	= false;

		public MockAlarmSetup(Context context, Notifier notifier) {
			super(context, notifier);
			mockSystemTime = new DateTime(year,month,day,hour, minute, DateTimeZone.forID("America/New_York"));
		}
		
		public void resetTime() {
			mockSystemTime = new DateTime(year,month,day,hour, minute, DateTimeZone.forID("America/New_York"));
		}
		
		@Override
		void setRepeatingAlarm(DateTime startTime) {
			alarmStarted = true;
			System.out.println("Call set Alarm with start of " + formatStartTime(startTime));
			alarmTimes.add(startTime);
		}
		
		void setStartAlarm(DateTime startTime) {
			alarmStarted = true;
			System.out.println("Call set Alarm with start of " + formatStartTime(startTime));
			alarmTimes.add(startTime);
		}		
		@Override
		boolean isAlarmAlreadyUp(int intent) {
			if (alarmStarted) {
				System.out.println("Alarm is already installed");
				return true;
			} else {
				System.out.println("Alarm is not installed");
				return false;
			}
		}

		@Override
		public DateTime getCurrentNYTime() {
			System.out.println("Run Time="+formatStartTime(mockSystemTime)+ " hour="+mockSystemTime.getHourOfDay());
			System.out.println("day of week="+mockSystemTime.getDayOfWeek()+ " Sat="+DateTimeConstants.SATURDAY+ " Sun="+DateTimeConstants.SUNDAY);
			System.out.println("Test Time="+formatStartTime(mockSystemTime));
			return mockSystemTime;
		}
	
	}
	
	public void testIsMarketOpen() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		System.out.println("isMarketOpen="+service.isMarketOpen());
		assertEquals("Market is open", true, service.isMarketOpen());
		service.onDestroy();
	}

	public void testRestartAndShutdown() throws InterruptedException {
		TestUpdateService service = new TestUpdateService();
		service.callOriginalIsMarketOpen = true;
		service.onCreate();
		MockAlarmSetup alarm = ((MockAlarmSetup)service.alarmSetup);
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		Thread.sleep(1000);
		while (service.updater.isRunning()) {
			alarm.mockSystemTime = alarm.mockSystemTime.hourOfDay().addToCopy(1);
			service.updater.restart(false);
			Thread.sleep(1000);
		}
		assertEquals("Number of Process Calls",8, ((MockProcessor)service.processor).numberOfProcessCalls);
		assertEquals("Number of Notifier Calls",8, ((MockNotifier)service.notifier).numberOfCalls);
		assertEquals("Progress Bar Start Calls", 8, service.progressBarStartCalls);
		assertEquals("Progress Bar Stop Calls", 8, service.progressBarStopCalls);
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 9, service.insertServiceLogCalls);

	}

	public void testOneTimeIntent() throws InterruptedException {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		Intent oneTimeIntent = new Intent();
		oneTimeIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_ONE_TIME);
		service.onStartCommand(oneTimeIntent, 0, 0);
		Thread.sleep(1000);
		assertEquals("Number of Process Calls", 1, ((MockProcessor) service.processor).numberOfProcessCalls);
		assertEquals("Number of Notifier Calls", 1, ((MockNotifier) service.notifier).numberOfCalls);
		assertEquals("Number of ClearServiceLog Calls", 0, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 0, service.insertServiceLogCalls);

	}
	
	public void testSetAlarmBeforeMarketOpen() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = ((MockAlarmSetup)service.alarmSetup);
		alarm.hour = 5;
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, alarm.alarmTimes.size());
		assertEquals("Hour of Start",9, alarm.alarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", alarm.day, alarm.alarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", alarm.month, alarm.alarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", alarm.year, alarm.alarmTimes.get(0).getYear());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 2, service.insertServiceLogCalls);
	
	}

	public void testSetAlarmBeforeMarketOpenWeekEnd() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = ((MockAlarmSetup)service.alarmSetup);
		alarm.hour = 5;
		alarm.day = 29;
		alarm.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, alarm.alarmTimes.size());
		assertEquals("Hour of Start",9, alarm.alarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", 1, alarm.alarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", 7, alarm.alarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", alarm.year, alarm.alarmTimes.get(0).getYear());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 2, service.insertServiceLogCalls);
		
	}

	public void testSetAlarmAfterMarketClose() throws InterruptedException {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = ((MockAlarmSetup)service.alarmSetup);
		alarm.hour = 17;
		alarm.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		Thread.sleep(2000);
		assertEquals("Number of set alarm calls",1, alarm.alarmTimes.size());
		assertEquals("Hour of Start",9, alarm.alarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", alarm.day+1, alarm.alarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", alarm.month, alarm.alarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", alarm.year, alarm.alarmTimes.get(0).getYear());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 2, service.insertServiceLogCalls);
		
	}

	public void testSetAlarmAfterMarketCloseWeekEnd() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = ((MockAlarmSetup)service.alarmSetup);
		alarm.hour = 17;
		alarm.day = 7;
		alarm.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, alarm.alarmTimes.size());
		assertEquals("Hour of Start",9, alarm.alarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", 10, alarm.alarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", alarm.month, alarm.alarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", alarm.year, alarm.alarmTimes.get(0).getYear());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 2, service.insertServiceLogCalls);
		
	}

	public void testSetAlarmAfterMarketCloseMonthEnd() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = ((MockAlarmSetup)service.alarmSetup);
		alarm.hour = 17;
		alarm.day = 28;
		alarm.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, alarm.alarmTimes.size());
		assertEquals("Hour of Start",9, alarm.alarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", 1, alarm.alarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", 7, alarm.alarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", alarm.year, alarm.alarmTimes.get(0).getYear());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 2, service.insertServiceLogCalls);
		
	}
	
	public void testSetAlarmBeforeMarketOpenAlreadyRunning() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = ((MockAlarmSetup)service.alarmSetup);
		alarm.alarmStarted = true;
		alarm.hour = 5;
		alarm.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, alarm.alarmTimes.size());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 2, service.insertServiceLogCalls);
		
	}

	
	public void testSetAlarmAfterMarketOpenAlreadyRunning() throws InterruptedException {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = ((MockAlarmSetup)service.alarmSetup);
		alarm.alarmStarted = true;
		alarm.hour = 5;
		alarm.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, alarm.alarmTimes.size());
		assertEquals("Number of process Calls", 1, ((MockProcessor)service.processor).numberOfProcessCalls);
		assertEquals("Number of process Calls", 0, ((MockProcessor)service.processor).numberOfUpdatePriceCalls);
		//Thread.sleep(2000);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, alarm.alarmTimes.size());
		assertEquals("Number of process Calls", 2, ((MockProcessor)service.processor).numberOfProcessCalls);
		assertEquals("Number of update Price Calls", 0, ((MockProcessor)service.processor).numberOfUpdatePriceCalls);
		assertEquals("Number of ClearServiceLog Calls", 2, service.clearServiceLogCalls);

	}
		
	public void testSetAlarmAfterMarketCloseAlreadyRunning() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = ((MockAlarmSetup)service.alarmSetup);
		alarm.alarmStarted = true;
		alarm.hour = 17;
		alarm.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, alarm.alarmTimes.size());
		assertEquals("Hour of Start",9, alarm.alarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", alarm.day+1, alarm.alarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", alarm.month, alarm.alarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", alarm.year, alarm.alarmTimes.get(0).getYear());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		
	}

}
