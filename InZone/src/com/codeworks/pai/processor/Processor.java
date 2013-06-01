package com.codeworks.pai.processor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.PaiStudyTable;
import com.codeworks.pai.db.PriceHistoryTable;
import com.codeworks.pai.db.SecurityTable;
import com.codeworks.pai.db.model.PaiStudy;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.db.model.Security;
import com.codeworks.pai.study.EMA2;
import com.codeworks.pai.study.Grouper;
import com.codeworks.pai.study.Period;
import com.codeworks.pai.study.StdDev;

public class Processor {
	private static final String TAG = Processor.class.getSimpleName();
	SecurityDataReader reader = new YahooReader();
	ContentResolver contentResolver;
	public static SimpleDateFormat dbStringDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);

	public Processor(ContentResolver contentResolver2, SecurityDataReader reader) {
		this.contentResolver = contentResolver2;
		this.reader = reader;
	}

	public List<PaiStudy> process() throws InterruptedException {
		List<PaiStudy> studies = new ArrayList<PaiStudy>();
		List<Security> securities = new ArrayList<Security>();
		securities = getSecurities();
		updateCurrentPrice(securities);
		for (Security security : securities) {
			if (security.getCurrentPrice() != 0) {
				List<Price> history = getPriceHistory(security.getSymbol());
				if (history.size() >= 20) {
					if (Thread.interrupted()) {
						throw new InterruptedException();
					}
					PaiStudy study = calculateStudy(security, history);
					saveStudy(study);
					studies.add(study);
				} else {
					studies.add(saveProcessNotification(security, Notice.INSUFFICIENT_HISTORY));
				}
			} else {
				studies.add(saveProcessNotification(security, Notice.NO_PRICE));
			}
		}
		removeObsoleteStudies(securities);
		return studies;
	}

	PaiStudy saveProcessNotification(Security security, Notice notice) {
		PaiStudy study = new PaiStudy(security.getSymbol());
		study.setNotice(notice);
		return study;
	}
	
	PaiStudy calculateStudy(Security security, List<Price> history) {
		Collections.sort(history);
		PaiStudy study = new PaiStudy(security.getSymbol());
		study.setPrice(security.getCurrentPrice());
		study.setAverageTrueRange(0d);
		study.setSecurityId(security.getId());

		Grouper grouper = new Grouper();
		{
			List<Price> weekly = grouper.periodList(history, Period.Week);
			if (weekly.size() >= 20) {
				study.setMaLastWeek(EMA2.compute(weekly, 20));
				study.setPriceLastWeek(weekly.get(weekly.size() - 1).getClose());

				appendCurrentPrice(weekly, security);
				study.setMaWeek(EMA2.compute(weekly, 20));
				study.setStddevWeek(StdDev.calculate(weekly, 20));
			}
		}
		{
			List<Price> monthly = grouper.periodList(history, Period.Month);
			if (monthly.size() >= 20) {
				study.setMaLastMonth(EMA2.compute(monthly, 20));
				study.setPriceLastMonth(monthly.get(monthly.size() - 1).getClose());
				appendCurrentPrice(monthly, security);
				study.setMaMonth(EMA2.compute(monthly, 20));

				study.setStddevMonth(StdDev.calculate(monthly, 20));
			}
		}
		return study;
	}

	private void appendCurrentPrice(List<Price> weekly, Security security) {
		Price lastPrice = new Price();
		lastPrice.setClose(security.getCurrentPrice());
		lastPrice.setDate(new Date());
		weekly.add(lastPrice);
	}

	private void updateCurrentPrice(List<Security> securities) throws InterruptedException {
		for (Security quote : securities) {
			Log.d(TAG, quote.getSymbol());
			String oldName = quote.getName();
			if (!reader.readCurrentPrice(quote) ) {
				if (quote.getName() == null || quote.getName().trim().length() == 0) { 
				quote.setName("Not Found");
				}
			}
			// updating the name here, may need to move update to when security
			// is added by user or kick off Processor at that time.
			if (quote.getName() != null && !quote.getName().equals(oldName)) {
				updateSecurityName(quote);
			}
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
		}
	}

	void updateSecurityName(Security security) {
		ContentValues values = new ContentValues();
		values.put(SecurityTable.COLUMN_NAME, security.getName());
		Uri securityUri = Uri.parse(PaiContentProvider.SECURITY_URI + "/" + security.getId());
		getContentResolver().update(securityUri, values, null, null);
	}

	List<Price> getPriceHistory(String symbol) {
		String[] projection = { PriceHistoryTable.COLUMN_SYMBOL, PriceHistoryTable.COLUMN_CLOSE, PriceHistoryTable.COLUMN_DATE,
				PriceHistoryTable.COLUMN_HIGH, PriceHistoryTable.COLUMN_LOW, PriceHistoryTable.COLUMN_OPEN,
				PriceHistoryTable.COLUMN_ADJUSTED_CLOSE };
		String selection = SecurityTable.COLUMN_SYMBOL + " = ? ";
		String[] selectionArgs = { symbol };
		Log.d(TAG, "Reading Price from database");
		Cursor historyCursor = getContentResolver().query(PaiContentProvider.PRICE_HISTORY_URI, projection, selection, selectionArgs,
				PriceHistoryTable.COLUMN_DATE);
		boolean reloadHistory = true;
		List<Price> history = new ArrayList<Price>();
		try {
			if (historyCursor != null) {
				if (historyCursor.moveToLast()) {
					String lastHistoryDate = historyCursor.getString(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_DATE));
					if (lastHistoryDate.equals(lastProbableTradeDate())) {
						double lastClose = historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_CLOSE));
						Log.d(TAG, "Price History is upto date using data from database lastDate=" + lastHistoryDate + " last Clost "
								+ lastClose);
						reloadHistory = false;
						if (historyCursor.moveToFirst()) {
							do {
								Price price = new Price();
								price.setAdjustedClose(historyCursor.getDouble(historyCursor
										.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_ADJUSTED_CLOSE)));
								price.setClose(historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_CLOSE)));
								price.setOpen(historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_OPEN)));
								price.setLow(historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_LOW)));
								price.setHigh(historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_HIGH)));
								try {
									price.setDate(dbStringDateFormat.parse(historyCursor.getString(historyCursor
											.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_DATE))));
								} catch (Exception e) {
									Log.d(TAG, "failed to parse price history date ");
								}
								history.add(price);
							} while (historyCursor.moveToNext());
						}
					}
				}
			}
		} finally {
			historyCursor.close();
		}
		if (reloadHistory) {
			Log.d(TAG, "Price History is out-of-date reloading from history provider");
			history = reader.readHistory(symbol);
			if (history != null && history.size() > 0) {
				Log.d(TAG, "Replacing Price History in database");
				int rowsDeleted = getContentResolver().delete(PaiContentProvider.PRICE_HISTORY_URI, selection, selectionArgs);
				Log.d(TAG, "Deleted " + rowsDeleted + " history rows");
				for (Price price : history) {
					ContentValues values = new ContentValues();
					values.put(PriceHistoryTable.COLUMN_ADJUSTED_CLOSE, price.getAdjustedClose());
					values.put(PriceHistoryTable.COLUMN_CLOSE, price.getClose());
					values.put(PriceHistoryTable.COLUMN_DATE, dbStringDateFormat.format(price.getDate()));
					values.put(PriceHistoryTable.COLUMN_HIGH, price.getHigh());
					values.put(PriceHistoryTable.COLUMN_LOW, price.getLow());
					values.put(PriceHistoryTable.COLUMN_OPEN, price.getOpen());
					values.put(PriceHistoryTable.COLUMN_SYMBOL, symbol);
					getContentResolver().insert(PaiContentProvider.PRICE_HISTORY_URI, values);
				}
			}
		}
		Log.d(TAG, "Returning " + history.size() + " Price History records for sysbol s" + symbol);
		return history;
	}

	void saveStudy(PaiStudy study) {
		String[] projection = new String[] { PaiStudyTable.COLUMN_ID };
		String selection = "symbol = ? ";
		String[] selectionArgs = new String[] { study.getSymbol() };
		Cursor studyCursor = getContentResolver().query(PaiContentProvider.PAI_STUDY_URI, projection, selection, selectionArgs, null);
		try {
			if (studyCursor.getCount() > 1) {
				Log.e(TAG, "Too many study rows for symbol " + study.getSymbol());
				studyCursor.moveToFirst();
				Uri studyUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + studyCursor.getLong(studyCursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_ID)));
				getContentResolver().delete(studyUri, null, null);
			}
			ContentValues values = new ContentValues();
			values.put(PaiStudyTable.COLUMN_MA_TYPE, "E");
			values.put(PaiStudyTable.COLUMN_MA_WEEK, study.getMaWeek());
			values.put(PaiStudyTable.COLUMN_MA_MONTH, study.getMaMonth());
			values.put(PaiStudyTable.COLUMN_MA_LAST_WEEK, study.getMaLastWeek());
			values.put(PaiStudyTable.COLUMN_MA_LAST_MONTH, study.getMaLastMonth());
			values.put(PaiStudyTable.COLUMN_PRICE, study.getPrice());
			values.put(PaiStudyTable.COLUMN_PRICE_LAST_WEEK, study.getPriceLastWeek());
			values.put(PaiStudyTable.COLUMN_PRICE_LAST_MONTH, study.getPriceLastMonth());
			values.put(PaiStudyTable.COLUMN_STDDEV_WEEK, study.getStddevWeek());
			values.put(PaiStudyTable.COLUMN_STDDEV_MONTH, study.getStddevMonth());
			values.put(PaiStudyTable.COLUMN_AVG_TRUE_RANGE, study.getAverageTrueRange());
			values.put(PaiStudyTable.COLUMN_SYMBOL, study.getSymbol());
			if (studyCursor.getCount() == 0) {
				Log.d(TAG, "Inserting Study " + study.toString());
				getContentResolver().insert(PaiContentProvider.PAI_STUDY_URI, values);
			} else {
				Log.d(TAG, "Updating Study " + study.toString());
				studyCursor.moveToFirst();
				Uri studyUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + studyCursor.getLong(0));
				getContentResolver().update(studyUri, values, null, null);
			}

		} finally {
			studyCursor.close();
		}
	}
	
	void removeObsoleteStudies(List<Security> securities) {
		String[] projection = new String[] { PaiStudyTable.COLUMN_ID, PaiStudyTable.COLUMN_SYMBOL };
		Cursor studyCursor = getContentResolver().query(PaiContentProvider.PAI_STUDY_URI, projection, null, null, null);
		try {
			if (studyCursor.moveToFirst()) {
				do {
					int studyId = studyCursor.getInt(studyCursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_ID));
					String symbol = studyCursor.getString(studyCursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SYMBOL));
					boolean found = false;
					for (Security security : securities) {
						if (security.getSymbol().equals(symbol)) {
							found = true;
						}
					}
					if (!found) {
						Uri studyUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + studyId);
						getContentResolver().delete(studyUri, null, null);
					}
				} while (studyCursor.moveToNext());
			}
		} finally {
			studyCursor.close();
		}
	}

	/**
	 * last Probable Trade date because we don't have a holiday table.
	 * 
	 * @return
	 */
	String lastProbableTradeDate() {
		Calendar cal = GregorianCalendar.getInstance();
		do {
			cal.add(Calendar.DAY_OF_MONTH, -1);
		} while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
		return dbStringDateFormat.format(cal.getTime());
	}

	List<Security> getSecurities() {
		List<Security> securities = new ArrayList<Security>();
		String[] projection = { SecurityTable.COLUMN_ID, SecurityTable.COLUMN_SYMBOL, SecurityTable.COLUMN_NAME };
		Cursor cursor = getContentResolver().query(PaiContentProvider.SECURITY_URI, projection, null, null, null);
		try {
			if (cursor != null) {
				if (cursor.moveToFirst())
					do {
						String symbol = cursor.getString(cursor.getColumnIndexOrThrow(SecurityTable.COLUMN_SYMBOL));
						Security security = new Security(symbol);
						security.setId(cursor.getLong(cursor.getColumnIndexOrThrow(SecurityTable.COLUMN_ID)));
						security.setName(cursor.getString(cursor.getColumnIndexOrThrow(SecurityTable.COLUMN_NAME)));						
						security.setName(cursor.getString(cursor.getColumnIndexOrThrow(SecurityTable.COLUMN_NAME)));
						securities.add(security);
						
					} while (cursor.moveToNext());
			}
		} finally {
			// Always close the cursor
			cursor.close();
		}
		return securities;
	}

	ContentResolver getContentResolver() {
		return contentResolver;
		
	}
}