package com.codeworks.pai.processor;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.Intent;
import android.test.ServiceTestCase;

public class UpdateServiceTest extends ServiceTestCase<UpdateService> {

	public UpdateServiceTest() {
		super(UpdateService.class);
	}

	public void testIsMarketOpen() {
		// UpdateService service = new UpdateService();
		Calendar cal = GregorianCalendar.getInstance();
		boolean marketOpen = cal.get(Calendar.HOUR_OF_DAY) > 9 && cal.get(Calendar.HOUR_OF_DAY) < 17;
		assertEquals(marketOpen, this.getService().isMarketOpen());
		System.out.println("Market is open " + getService().isMarketOpen());
	}
	
	public void test() {
		Intent dailyIntent;
		dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		startService(dailyIntent);
	}
}
