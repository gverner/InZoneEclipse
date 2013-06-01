package com.codeworks.pai.study;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import com.codeworks.pai.db.model.Price;

public class Grouper {

	/**
	 * periodList Loop through daily history and create list containing period
	 * ending (weekly or monthly) days.
	 * 
	 * @param inHistory list of daily prices excluding holidays
	 * @param period Period.Week or Period.Month
	 * @return
	 */
	public List<Price> periodList(List<Price> inHistory, Period period) {
		List<Price> history = new ArrayList<Price>(inHistory);
		Collections.sort(history);
		List<Price> periodList = new ArrayList<Price>();
		int lastDay = -1;
		Price lastPrice = null;
		for (Price price : history) {
			if (price.valid()) {
				Calendar cal = GregorianCalendar.getInstance(Locale.US);
				cal.setTime(price.getDate());
				if (Period.Week.equals(period)) {
					int day = cal.get(Calendar.DAY_OF_WEEK);
					if (day < lastDay) { // week change
						periodList.add(lastPrice);
					}
					lastPrice = price;
					lastDay = day;
				}
				if (Period.Month.equals(period)) {
					int day = cal.get(Calendar.DAY_OF_MONTH);
					if (day < lastDay) { // week change
						periodList.add(lastPrice);
					}
					lastPrice = price;
					lastDay = day;
				}
			}
		}
		return periodList;
	}

	/**
	 * Loop through daily price history and creates a sublist containing the
	 * (weekly or monthly) price records. This version returns the beginning of
	 * the period.
	 * 
	 * @param inHistory
	 * @param period
	 * @return
	 */
	public List<Price> periodListBeginning(List<Price> inHistory, Period period) {
		List<Price> history = new ArrayList<Price>(inHistory);
		Collections.sort(history);
		List<Price> periodList = new ArrayList<Price>();
		int lastDay = -1;
		int lastMonth = -1;
		for (Price price : history) {
			if (price.valid()) {
				Calendar cal = GregorianCalendar.getInstance(Locale.US);
				cal.setTime(price.getDate());
				if (Period.Week.equals(period)) {
					int day = cal.get(Calendar.DAY_OF_WEEK);
					if (day < lastDay) { // week change
						periodList.add(price);
					}
					lastDay = day;
				}
				if (Period.Month.equals(period)) {
					int day = cal.get(Calendar.DAY_OF_MONTH);
					int month = cal.get(Calendar.MONTH);
					if (month != lastMonth) { // week change
						periodList.add(price);
					}
					lastMonth = month;
					lastDay = day;
				}
			}
		}
		return periodList;
	}
}
