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
	public static boolean isAfterOrEqualMarketClose(Date date, Period period) {
		boolean result = false;
		if (Period.Week.equals(period)) {
			Calendar cal = GregorianCalendar.getInstance(Locale.US);
			cal.setTimeZone(TimeZone.getTimeZone("US/Eastern"));

			cal.setTime(date);
			/*
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm z", Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
			System.out.println(sdf.format(cal.getTime()));
			System.out.println(sdf.format(date));
			*/
			
			if ((cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY && cal.get(Calendar.HOUR_OF_DAY) >= MARKET_CLOSE_HOUR) || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
					|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
					|| (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && cal.get(Calendar.HOUR_OF_DAY) == MARKET_OPEN_HOUR && cal.get(Calendar.MINUTE) < MARKET_OPEN_MINUTE)
					|| (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && cal.get(Calendar.MINUTE) < MARKET_OPEN_MINUTE)) {
				result =  true;
			} else {
				result =  false;
			}
		} else if (Period.Month.equals(period)) {
			Calendar cal = GregorianCalendar.getInstance(Locale.US);
			cal.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
			cal.setTime(date);

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
			result = date.compareTo(cal.getTime()) >= 0;
		}
		return result;
	}
	
    //-----------------------------------------------------------------------
    /**
     * <p>Checks if two date objects are on the same day ignoring time.</p>
     *
     * <p>28 Mar 2002 13:45 and 28 Mar 2002 06:01 would return true.
     * 28 Mar 2002 13:45 and 12 Mar 2002 13:45 would return false.
     * </p>
     * 
     * @param date1  the first date, not altered, not null
     * @param date2  the second date, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either date is <code>null</code>
     * @since 2.1
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    /**
     * <p>Checks if two calendar objects are on the same day ignoring time.</p>
     *
     * <p>28 Mar 2002 13:45 and 28 Mar 2002 06:01 would return true.
     * 28 Mar 2002 13:45 and 12 Mar 2002 13:45 would return false.
     * </p>
     * 
     * @param cal1  the first calendar, not altered, not null
     * @param cal2  the second calendar, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either calendar is <code>null</code>
     * @since 2.1
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }	
    
    public static Date truncate(Date date) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        cal1.set(Calendar.HOUR, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        return cal1.getTime();
    }
}
