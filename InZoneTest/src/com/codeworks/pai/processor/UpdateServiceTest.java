package com.codeworks.pai.processor;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

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
		boolean	alarmStarted	= false;
		int		month			= 6; // June
		int		day				= 3;
		int		year			= 2013;
		int		hour			= 9;
		int		minute			= 1;
		DateTime mockSystemTime = new DateTime();
		List<DateTime> alarmTimes = new ArrayList<DateTime>();
		SharedPreferences sharedPref = new MockSharedPreferences();
		
		@Override
		public void onCreate() {
			updater = new Updater();
		    notifier = new MockNotifier();
			processor = new MockProcessor();
			mockSystemTime = new DateTime(year,month,day,hour, minute, DateTimeZone.forID("America/New_York"));
		}

		@Override
		void setAlarm(DateTime startTime) {
			alarmStarted = true;
			System.out.println("Call set Alarm with start of " + formatStartTime(startTime));
			alarmTimes.add(startTime);
		}

		@Override
		void makeToast(String message, int length) {
			System.out.println("MakeToast Message -> " + message);
		}

		@Override
		boolean isAlarmAlreadyUp() {
			if (alarmStarted) {
				System.out.println("Alarm is already installed");
				return true;
			} else {
				System.out.println("Alarm is not installed");
				return false;
			}
		}

		@Override
		DateTime getCurrentNYTime() {
			System.out.println("Run Time="+formatStartTime(mockSystemTime)+ " hour="+mockSystemTime.getHourOfDay());
			System.out.println("day of week="+mockSystemTime.getDayOfWeek()+ " Sat="+DateTimeConstants.SATURDAY+ " Sun="+DateTimeConstants.SUNDAY);
			System.out.println("Test Time="+formatStartTime(mockSystemTime));
			return mockSystemTime;
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
		service.onCreate();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		Thread.sleep(1000);
		while (service.updater.isRunning()) {
			service.mockSystemTime = service.mockSystemTime.hourOfDay().addToCopy(1);
			service.updater.restart();
			Thread.sleep(1000);
		}
		assertEquals("Number of Process Calls",9, ((MockProcessor)service.processor).numberOfCalls);
		assertEquals("Number of Notifier Calls",9, ((MockNotifier)service.notifier).numberOfCalls);

	}

	public void testOneTimeIntent() throws InterruptedException {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		Intent oneTimeIntent = new Intent();
		oneTimeIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_ONE_TIME);
		service.onStartCommand(oneTimeIntent, 0, 0);
		Thread.sleep(1000);
		assertEquals("Number of Process Calls", 1, ((MockProcessor) service.processor).numberOfCalls);
		assertEquals("Number of Notifier Calls", 1, ((MockNotifier) service.notifier).numberOfCalls);

	}
	
	public void testSetAlarmBeforeMarketOpen() {
		TestUpdateService service = new TestUpdateService();
		service.hour = 5;
		service.onCreate();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, service.alarmTimes.size());
		assertEquals("Hour of Start",9, service.alarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", service.day, service.alarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", service.month, service.alarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", service.year, service.alarmTimes.get(0).getYear());
	}

	public void testSetAlarmBeforeMarketOpenWeekEnd() {
		TestUpdateService service = new TestUpdateService();
		service.hour = 5;
		service.day = 29;
		service.onCreate();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, service.alarmTimes.size());
		assertEquals("Hour of Start",9, service.alarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", 1, service.alarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", 7, service.alarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", service.year, service.alarmTimes.get(0).getYear());
	}

	public void testSetAlarmAfterMarketClose() {
		TestUpdateService service = new TestUpdateService();
		service.hour = 17;
		service.onCreate();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, service.alarmTimes.size());
		assertEquals("Hour of Start",9, service.alarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", service.day+1, service.alarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", service.month, service.alarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", service.year, service.alarmTimes.get(0).getYear());
	}

	public void testSetAlarmAfterMarketCloseWeekEnd() {
		TestUpdateService service = new TestUpdateService();
		service.hour = 17;
		service.day = 7;
		service.onCreate();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, service.alarmTimes.size());
		assertEquals("Hour of Start",9, service.alarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", 10, service.alarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", service.month, service.alarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", service.year, service.alarmTimes.get(0).getYear());
	}

	public void testSetAlarmAfterMarketCloseMonthEnd() {
		TestUpdateService service = new TestUpdateService();
		service.hour = 17;
		service.day = 28;
		service.onCreate();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, service.alarmTimes.size());
		assertEquals("Hour of Start",9, service.alarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", 1, service.alarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", 7, service.alarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", service.year, service.alarmTimes.get(0).getYear());
	}
	
	public void testSetAlarmBeforeMarketOpenAlreadyRunning() {
		TestUpdateService service = new TestUpdateService();
		service.alarmStarted = true;
		service.hour = 5;
		service.onCreate();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",0, service.alarmTimes.size());
	}

	public void testSetAlarmAfterMarketCloseAlreadyRunning() {
		TestUpdateService service = new TestUpdateService();
		service.alarmStarted = true;
		service.hour = 17;
		service.onCreate();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, service.alarmTimes.size());
		assertEquals("Hour of Start",9, service.alarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", service.day+1, service.alarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", service.month, service.alarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", service.year, service.alarmTimes.get(0).getYear());
	}

}
