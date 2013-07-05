package com.codeworks.pai.processor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import com.codeworks.pai.study.Period;

public class DateUtils {

	public static final int	MARKET_CLOSE_HOUR	= 16;
	public static final int MARKET_OPEN_HOUR = 9;
	public static final int MARKET_OPEN_MINUTE = 30;

	/**
	 * NOTE: doesn't know holidays
	 * @param date
	 * @param period
	 * @return
	 */
	public static boolean isAfterMarketClose(Date date, Period period) {
		if (Period.Week.equals(period)) {
			Calendar cal = GregorianCalendar.getInstance(Locale.US);
			cal.setTimeZone(TimeZone.getTimeZone("US/Eastern"));

			cal.setTime(date);
			//System.out.println("hour1 "+cal.get(Calendar.HOUR_OF_DAY));
			//System.out.println("minute1 "+cal.get(Calendar.MINUTE));
			
			if ((cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY && cal.get(Calendar.HOUR_OF_DAY) >= MARKET_CLOSE_HOUR) || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
					|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
					|| (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && cal.get(Calendar.HOUR_OF_DAY) == MARKET_OPEN_HOUR && cal.get(Calendar.HOUR_OF_DAY) < MARKET_OPEN_MINUTE)
					|| (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && cal.get(Calendar.HOUR_OF_DAY) < MARKET_OPEN_MINUTE)) {
				return true;
			} else {
				return false;
			}
		} else if (Period.Month.equals(period)) {
			Calendar cal = GregorianCalendar.getInstance(Locale.US);
			cal.setTimeZone(TimeZone.getTimeZone("US/Eastern"));

			cal.add(Calendar.MONTH, 1);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			do {
				cal.add(Calendar.DAY_OF_MONTH, -1);
			} while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
			cal.set(Calendar.HOUR_OF_DAY, MARKET_CLOSE_HOUR);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND,0);
			/*
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm z", Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
			System.out.println(sdf.format(cal.getTime()));
			System.out.println(sdf.format(date.getTime()));
			*/
			return date.after(cal.getTime());
		}
		return false;
	}
}
