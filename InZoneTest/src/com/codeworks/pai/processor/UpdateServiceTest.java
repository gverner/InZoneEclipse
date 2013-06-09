package com.codeworks.pai.processor;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import android.content.Intent;
import android.test.AndroidTestCase;

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
