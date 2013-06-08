package com.codeworks.pai.processor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
import com.codeworks.pai.db.model.MaType;
import com.codeworks.pai.db.model.PaiStudy;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.study.EMA2;
import com.codeworks.pai.study.Grouper;
import com.codeworks.pai.study.Period;
import com.codeworks.pai.study.StdDev;

public class ProcessorImpl implements Processor {
	private static final String TAG = ProcessorImpl.class.getSimpleName();
	DataReader reader = new DataReaderYahoo();
	ContentResolver contentResolver;
	public static SimpleDateFormat dbStringDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);

	public ProcessorImpl(ContentResolver contentResolver2, DataReader reader) {
		this.contentResolver = contentResolver2;
		this.reader = reader;
	}

	public List<PaiStudy> process() throws InterruptedException {
		List<PaiStudy> studies = getSecurities();
		updateCurrentPrice(studies);
		for (PaiStudy security : studies) {
			if (security.getPrice() != 0) {
				List<Price> history = getPriceHistory(security.getSymbol());
				if (history.size() >= 20) {
					if (Thread.interrupted()) {
						throw new InterruptedException();
					}
					calculateStudy(security, history);
					saveStudy(security);
				} else {
					security.setNotice(Notice.INSUFFICIENT_HISTORY);
				}
			} else {
				security.setNotice(Notice.NO_PRICE);
			}
		}
		return studies;
	}

	
	void calculateStudy(PaiStudy security, List<Price> history) {
		Collections.sort(history);

		Grouper grouper = new Grouper();
		{
			List<Price> weekly = grouper.periodList(history, Period.Week);
			if (weekly.size() >= 20) {
				security.setMaLastWeek(EMA2.compute(weekly, 20));
				security.setPriceLastWeek(weekly.get(weekly.size() - 1).getClose());

				appendCurrentPrice(weekly, security);
				security.setMaWeek(EMA2.compute(weekly, 20));
				security.setStddevWeek(StdDev.calculate(weekly, 20));
			}
		}
		{
			List<Price> monthly = grouper.periodList(history, Period.Month);
			if (monthly.size() >= 20) {
				security.setMaLastMonth(EMA2.compute(monthly, 20));
				security.setPriceLastMonth(monthly.get(monthly.size() - 1).getClose());
				appendCurrentPrice(monthly, security);
				security.setMaMonth(EMA2.compute(monthly, 20));

				security.setStddevMonth(StdDev.calculate(monthly, 20));
			}
		}
	}

	private void appendCurrentPrice(List<Price> weekly, PaiStudy security) {
		if (weekly != null && weekly.size() > 0) {
			Price lastHistory = weekly.get(weekly.size() - 1);
			if (security.getPriceDate().compareTo(lastHistory.getDate()) == 0) {
				if (security.getPrice() != lastHistory.getClose()) {
					lastHistory.setClose(security.getPrice());
					Log.d(TAG, "History and Price Close Differ Should not Happend=" + lastHistory.getDate() + " History Close" + lastHistory.getClose()+ " Current Price" + security.getPrice());
				}
			} else if (security.getPriceDate().after(weekly.get(weekly.size() - 1).getDate())) {
				Price lastPrice = new Price();
				lastPrice.setClose(security.getPrice());
				lastPrice.setDate(security.getPriceDate());
				weekly.add(lastPrice);
				Log.d(TAG, "Last History Date=" + lastHistory.getDate() + " Add Current Price Date" + security.getPriceDate());
			}
		}
	}

	private void updateCurrentPrice(List<PaiStudy> securities) throws InterruptedException {
		for (PaiStudy quote : securities) {
			Log.d(TAG, quote.getSymbol());
			String oldName = quote.getName();
			reader.readCurrentPrice(quote);
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

	void updateSecurityName(PaiStudy security) {
		ContentValues values = new ContentValues();
		values.put(PaiStudyTable.COLUMN_NAME, security.getName());
		Uri securityUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + security.getSecurityId());
		getContentResolver().update(securityUri, values, null, null);
	}

	List<Price> getPriceHistory(String symbol) {
		String[] projection = { PriceHistoryTable.COLUMN_SYMBOL, PriceHistoryTable.COLUMN_CLOSE, PriceHistoryTable.COLUMN_DATE,
				PriceHistoryTable.COLUMN_HIGH, PriceHistoryTable.COLUMN_LOW, PriceHistoryTable.COLUMN_OPEN,
				PriceHistoryTable.COLUMN_ADJUSTED_CLOSE };
		String selection = PaiStudyTable.COLUMN_SYMBOL + " = ? ";
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
		values.put(PaiStudyTable.COLUMN_PRICE_DATE, PaiStudyTable.priceDateFormat.format(study.getPriceDate()));
		Log.d(TAG, "Updating Study " + study.toString());
		Uri studyUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + study.getSecurityId());
		getContentResolver().update(studyUri, values, null, null);
	}
	
	void removeObsoleteStudies(List<PaiStudy> securities) {
		String[] projection = new String[] { PaiStudyTable.COLUMN_ID, PaiStudyTable.COLUMN_SYMBOL };
		Cursor studyCursor = getContentResolver().query(PaiContentProvider.PAI_STUDY_URI, projection, null, null, null);
		try {
			if (studyCursor.moveToFirst()) {
				do {
					int studyId = studyCursor.getInt(studyCursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_ID));
					String symbol = studyCursor.getString(studyCursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SYMBOL));
					boolean found = false;
					for (PaiStudy security : securities) {
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

	List<PaiStudy> getSecurities() {
		List<PaiStudy> securities = new ArrayList<PaiStudy>();
		String[] projection = { PaiStudyTable.COLUMN_ID, PaiStudyTable.COLUMN_SYMBOL, PaiStudyTable.COLUMN_NAME, PaiStudyTable.COLUMN_MA_TYPE, PaiStudyTable.COLUMN_MA_WEEK, PaiStudyTable.COLUMN_MA_MONTH,
				PaiStudyTable.COLUMN_MA_LAST_WEEK, PaiStudyTable.COLUMN_MA_LAST_MONTH, PaiStudyTable.COLUMN_PRICE, PaiStudyTable.COLUMN_PRICE_LAST_WEEK,
				PaiStudyTable.COLUMN_PRICE_LAST_MONTH, PaiStudyTable.COLUMN_STDDEV_WEEK, PaiStudyTable.COLUMN_STDDEV_MONTH,
				PaiStudyTable.COLUMN_AVG_TRUE_RANGE, PaiStudyTable.COLUMN_PRICE_DATE };
		Cursor cursor = getContentResolver().query(PaiContentProvider.PAI_STUDY_URI, projection, null, null, null);
		try {
			if (cursor != null) {
				if (cursor.moveToFirst())
					do {
						String symbol = cursor.getString(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SYMBOL));
						PaiStudy security = new PaiStudy(symbol);
						security.setSecurityId(cursor.getLong(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_ID)));
						security.setName(cursor.getString(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_NAME)));

						security.setMaType(MaType.parse(cursor.getString(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_MA_TYPE))));
						security.setMaWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_MA_WEEK)));
						security.setMaMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_MA_MONTH)));
						security.setMaLastWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_MA_LAST_WEEK)));
						security.setMaLastMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_MA_LAST_MONTH)));
						security.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_PRICE)));
						security.setPriceLastWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_PRICE_LAST_WEEK)));
						security.setPriceLastMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_PRICE_LAST_MONTH)));
						security.setStddevWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_STDDEV_WEEK)));
						security.setStddevMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_STDDEV_MONTH)));
						security.setAverageTrueRange(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_AVG_TRUE_RANGE)));
						security.setPriceDate(cursor.getString(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_PRICE_DATE)));
						securities.add(security);

					} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception in Processor GetSecurities", e);
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