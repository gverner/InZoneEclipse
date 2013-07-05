package com.codeworks.pai.processor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import android.test.AndroidTestCase;

import com.codeworks.pai.study.Period;

public class DateUtilsTest extends AndroidTestCase {

	public void testFridayAt4pm() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		Date date = sdf.parse("06/28/2013 16:00");
		assertTrue(DateUtils.isAfterMarketClose(date, Period.Week));
	}
	
	public void testFridayAt359pm() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		Date date = sdf.parse("06/28/2013 15:59");
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		cal.setTime(date);
		System.out.println("hour "+cal.get(Calendar.HOUR_OF_DAY));
		System.out.println("minute "+cal.get(Calendar.MINUTE));
		assertFalse(DateUtils.isAfterMarketClose(date, Period.Week));
	}
	public void testEndOfMonthAfter() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		Date date = sdf.parse("06/28/2013 16:01");
		assertTrue(DateUtils.isAfterMarketClose(date, Period.Month));
	}
	
	public void testEndOfMonthBefore() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		Date date = sdf.parse("06/28/2013 16:00");
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		cal.setTime(date);
		System.out.println("hour "+cal.get(Calendar.HOUR_OF_DAY));
		System.out.println("minute "+cal.get(Calendar.MINUTE));
		assertFalse(DateUtils.isAfterMarketClose(date, Period.Month));
	}	
}
