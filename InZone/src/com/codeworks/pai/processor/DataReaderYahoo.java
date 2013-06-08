package com.codeworks.pai.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

import com.codeworks.pai.db.model.PaiStudy;
import com.codeworks.pai.db.model.Price;

public class DataReaderYahoo implements DataReader {
	private static final String TAG = DataReaderYahoo.class.getSimpleName();
	SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.US);
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

	/*
	 * s=symbol l1=last price d1=last trade date t1=last trade time c1=change
	 * o=open h=day high g=day low v=volumn k1=last trade real times with time
	 * b=bin b2=bin real time a=asks b1=ask real time n=name
	 */
	/* (non-Javadoc)
	 * @see com.codeworks.pai.processor.SecurityDataReader#readCurrentPrice(com.codeworks.pai.db.model.Security)
	 */
	@Override
	public boolean readCurrentPrice(PaiStudy security) {
		List<String[]> results;// "MM/dd/yyyy hh:mmaa"
		boolean found = false;
		double quote = 0;
		if (security != null && security.getSymbol() != null)
		try {
			String url = "http://download.finance.yahoo.com/d/quotes.csv?s=" + security.getSymbol() + "&f=sl1d1k1b2b1nt1&e=.csv";
			results = downloadUrl(url);
			for (String[] line : results) {
				if (line.length >= 7) {
					quote = parseDouble(line[1], "Price");
					security.setPrice(quote);
					//security.setRtBid(parseDouble(line[4], "Bid"));
					//security.setRtAsk(parseDouble(line[5], "Ask"));
					if ("N/A".equals(line[2]) && quote == 0.0 && security.getSymbol().equals(line[6])) {
						found = false;
						security.setName("Not Found");
					} else {
						security.setName(line[6]);
						found = true;
					}
					security.setPriceDate(parseDateTime(line[2] + " " + line[7], " Date Time"));
					Log.d(TAG, line[0] + " last=" + line[1] + " rtLast=" + line[3] + " rtBid=" + line[4] + " rtAsk=" + line[5]);
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "readCurrentPrice " + e.getMessage(), e);
		}
		return found;
	}

	double parseDouble(String value, String fieldName) {
		try {
			return Double.parseDouble(value);
		} catch (Exception e) {
			Log.d(TAG, "unable to parse " + value + " as a double for field " + fieldName);
			return 0;
		}
	}

	Date parseDateTime(String value, String fieldName) {
		try {
			return dateTimeFormat.parse(value);
		} catch (Exception e) {
			Log.d(TAG, "Unable to parse " + value + " as date time for field " + fieldName);
			return null;
		}
	}

	Date parseDate(String value, String fieldName) {
		try {
			return dateFormat.parse(value);
		} catch (Exception e) {
			Log.d(TAG, "Unable to parse " + value + " as date for field " + fieldName);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.codeworks.pai.processor.SecurityDataReader#readHistory(java.lang.String)
	 */
	@Override
	public List<Price> readHistory(String symbol) {
		List<Price> history = new ArrayList<Price>();
		List<String[]> results;
		try {
			String url = buildHistoryUrl(symbol);
			results = downloadUrl(url);
			int counter = 0;
			for (String[] line : results) {
				counter++;
				if (counter % 100 == 0) {
					Log.d(TAG, counter + " records read for " + symbol);
				}
				if (!"Date".equals(line[0])) { // skip header

					Price price = new Price();
					history.add(price);
					price.setDate(parseDate(line[0], "Date"));
					price.setOpen(parseDouble(line[1], "Open"));
					price.setHigh(parseDouble(line[2], "High"));
					price.setLow(parseDouble(line[3], "Low"));
					price.setClose(parseDouble(line[4], "Close"));
					price.setAdjustedClose(parseDouble(line[6], "AdjustedClose"));
					if (counter % 20 == 0) {
						Log.d(TAG, symbol + " " + line[0] + " " + line[1]);
					}
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "readHistory " + e.getMessage(), e);
		}
		return history;
	}

	String buildHistoryUrl(String symbol) {
		Calendar cal = GregorianCalendar.getInstance();
		String endDay = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
		String endMonth = Integer.toString(cal.get(Calendar.MONTH));
		String endYear = Integer.toString(cal.get(Calendar.YEAR));
		cal.add(Calendar.WEEK_OF_YEAR, -80);
		String startDay = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
		String startMonth = Integer.toString(cal.get(Calendar.MONTH));
		String startYear = Integer.toString(cal.get(Calendar.YEAR));
		// chart.finance.yahoo.com/table.csv?s=SPY&amp;a=00&amp;b=1&amp;c=2012&amp;d=03&amp;e=12&amp;f=2013&amp;g=d&amp;ignore=.csv"
		String url = "http://ichart.finance.yahoo.com/table.csv?s=" + symbol + "&a=" + startMonth + "&b=" + startDay + "&c=" + startYear
				+ "&d=" + endMonth + "&e=" + endDay + "&f=" + endYear + "&g=d&ignore=.csv";
		return url;
	}

	/** 
	 * Given a URL, establishes an HttpUrlConnection and retrieves
	 * the content as a InputStream, which is CSV parsed and returned
	 * as an ArrayList of String arrays.
	 */
	List<String[]> downloadUrl(String myurl) throws IOException {
		InputStream is = null;

		try {
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// Starts the query
			conn.connect();
			int response = conn.getResponseCode();
			Log.d(TAG, "The response is: " + response);
			is = conn.getInputStream();

			CSVReader reader = new CSVReader(new InputStreamReader(is, "UTF-8"));
			List<String[]> lines = reader.readAll();
			return lines;

			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

}
