package com.codeworks.pai.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.text.format.DateUtils;
import au.com.bytecode.opencsv.CSVReader;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.study.GrouperTest;

public class TestDataLoader {
	public static String SPY = "SPY";
	public static String QQQ = "QQQ";
	public static String GLD = "GLD";
	public static String UNG = "UNG";
	public static String HYG = "HYG";
	
	public static List<Price> getTestHistory(String symbol) {
		
		String[] securities = new String[] {SPY, QQQ, GLD, UNG, HYG};
		boolean symbolFound = false;
		for (String security : securities) {
			if (security.equals(symbol)) {
				symbolFound = true;
			}
		}
		if (!symbolFound) {
			throw new IllegalArgumentException("Unsupported symbol "+symbol);
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		List<Price> history = new ArrayList<Price>();
		String filename = "/com/codeworks/pai/mock/" + symbol + "_history.csv";
		InputStream url = GrouperTest.class.getResourceAsStream(filename.toLowerCase(Locale.US));
		if (url == null) {
			throw new IllegalArgumentException(filename+" history data not found");
		}
		Reader streamReader = new java.io.InputStreamReader(url);
		CSVReader reader = new CSVReader(streamReader);
		try {
		List<String[]> lines = reader.readAll();
		for (String[] line : lines)
			try {
				if (!"Date".equals(line[0])) { // skip header
					Price price = new Price();
					price.setDate(sdf.parse(line[0]));
					price.setOpen(Double.parseDouble(line[1]));
					price.setHigh(Double.parseDouble(line[2]));
					price.setLow(Double.parseDouble(line[3]));
					price.setClose(Double.parseDouble(line[4]));
					price.setAdjustedClose(Double.parseDouble(line[6]));
					if (price.valid()) {
						history.add(price);
					}
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

		return history;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static List<Price> generateHistory(double startPrice, double endPrice, int days) throws ParseException {
		double diff = endPrice - startPrice;
		SimpleDateFormat sdf  = new SimpleDateFormat("MM/dd/yyyy",Locale.US);
		Date startDate = sdf.parse("06/15/2013");
		double perday = PaiUtils.round(diff / days);
		List<Price> history = new ArrayList<Price>();
		Calendar cal = GregorianCalendar.getInstance(Locale.US);
		// role calendar so we always start on the same date and generate weekly values based on the same value.
		while (cal.getTime().after(startDate) || cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
			cal.add(Calendar.DAY_OF_MONTH, -1);
		}
		double close = endPrice;
		do {
			cal.add(Calendar.DAY_OF_MONTH, -1); // start before today
			while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
				cal.add(Calendar.DAY_OF_MONTH, -1);
			}
			history.add(buildPrice(cal.getTime(), close));
			close = PaiUtils.round(close - perday);
		} while (history.size() <= days);
		Collections.sort(history);
		return history;
	}
	
	static Price buildPrice(Date date, double close) {
		Price price = new Price();
		price.setDate(date);
		price.setOpen((close));
		price.setHigh((close));
		price.setLow((close));
		price.setClose((close));
		price.setAdjustedClose((close));
		return price;
	}
}
