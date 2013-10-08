package com.codeworks.pai.processor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.PaiStudyTable;
import com.codeworks.pai.db.PriceHistoryTable;
import com.codeworks.pai.db.ServiceLogTable;
import com.codeworks.pai.db.model.MaType;
import com.codeworks.pai.db.model.PaiStudy;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.db.model.ServiceType;
import com.codeworks.pai.study.ATR;
import com.codeworks.pai.study.EMA2;
import com.codeworks.pai.study.Grouper;
import com.codeworks.pai.study.Period;
import com.codeworks.pai.study.SMA;
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

	/**
	 * Process all securities.
	 * lookup current price and cal¯culates study
	 * 
	 * @param symbol when symbol is null all securities are processed.
	 * @return
	 * @throws InterruptedException
	 */
	public List<PaiStudy> process(String symbol) throws InterruptedException {
		List<PaiStudy> studies = getSecurities(symbol);
		updateCurrentPrice(studies);
		String lastOnlineHistoryDbDate = getLastestOnlineHistoryDbDate("SPY");
		List<Price> history = new ArrayList<Price>();
		String lastSymbol = "";
		for (PaiStudy security : studies) {
			if (security.getPrice() != 0) {
				if (!lastSymbol.equals(security.getSymbol())) { // cache history
					history = getPriceHistory(security,lastOnlineHistoryDbDate);
					Collections.sort(history);
					history = Collections.unmodifiableList(history);
				} else {
					Log.d(TAG, "Using History Cache for "+security.getSymbol());
				}
				lastSymbol = security.getSymbol();
				if (history.size() >= 20) {
					if (Thread.interrupted()) {
						throw new InterruptedException();
					}
					calculateStudy(security, history); // shallow copy of cashed history because it is modified.
					saveStudy(security);
				} else {
					security.setInsufficientHistory(true);
				}
			} else {
				security.setNoPrice(true);
			}
		}
		return studies;
	}
	
	/**
	 * update Price all securities.
	 * lookup current price only
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public List<PaiStudy> updatePrice(String symbol) throws InterruptedException {
		List<PaiStudy> studies = getSecurities(symbol);
		updateCurrentPrice(studies);
		for (PaiStudy security : studies) {
			if (security.getPrice() != 0) {
					saveStudyPrice(security);
			} else {
				security.setNoPrice(true);
			}
		}
		return studies;
	}
	
	void calculateStudy(PaiStudy security, List<Price> daily) {
		// do not modify daily because it is cached.s
		List<Price> history = new ArrayList<Price>(daily);
		Log.d(TAG,"Daily price history start "+security.getSymbol()+" Price Date="+security.getPriceDate()+" ListHistoryDate="+daily.get(daily.size() -1).getDate());

		Grouper grouper = new Grouper();
		{
			appendCurrentPrice(history, security, Period.Day);
			List<Price> weekly = grouper.periodList(history, Period.Week);
			if (weekly.size() >= 20) {
				security.setMaLastWeek(EMA2.compute(weekly, 20));
				security.setSmaLastWeek(SMA.compute(weekly, 20));
				security.setPriceLastWeek(weekly.get(weekly.size() - 1).getClose());

				appendCurrentPrice(weekly, security, Period.Week);
				
				security.setMaWeek(EMA2.compute(weekly, 20));
				security.setStddevWeek(StdDev.calculate(weekly, 20));

				security.setSmaWeek(SMA.compute(weekly, 20));
				security.setSmaStddevWeek(StdDev.calculate(weekly, 20));

				if (DateUtils.isDateBetweenPeriodCloseAndOpen(security.getPriceDate(), Period.Week)) {
					security.setMaLastWeek(security.getMaWeek());
					security.setSmaLastWeek(security.getSmaWeek());
					security.setPriceLastWeek(security.getPrice());
					Log.i(TAG,"IS AFTER OR EQUAL MARKET CLOSE FOR "+security.getSymbol());
				} else {
					Log.i(TAG,"IS NOT AFTER OR EQUAL MARKET CLOSE FOR "+security.getSymbol());
				}
				
			} else {
				Log.w(TAG,"Insufficent Weekly History only "+weekly.size()+" periods.");
				security.setInsufficientHistory(true);
			}
		}
		{
			List<Price> monthly = grouper.periodList(history, Period.Month);
			if (monthly.size() >= 20) {
				security.setMaLastMonth(EMA2.compute(monthly, 20));
				security.setSmaLastMonth(SMA.compute(monthly, 12));
				security.setPriceLastMonth(monthly.get(monthly.size() - 1).getClose());
				
				appendCurrentPrice(monthly, security, Period.Month);
				
				security.setMaMonth(EMA2.compute(monthly, 20));
				security.setStddevMonth(StdDev.calculate(monthly, 20));

				security.setSmaMonth(SMA.compute(monthly, 12));
				security.setSmaStddevMonth(StdDev.calculate(monthly, 12));

				if (DateUtils.isDateBetweenPeriodCloseAndOpen(security.getPriceDate(), Period.Month)) {
					security.setMaLastMonth(security.getMaMonth());
					security.setSmaLastMonth(security.getSmaMonth());
					security.setPriceLastMonth(security.getPrice());
				}
				
			} else {
				Log.w(TAG,"Insufficent Monthly History only "+monthly.size()+" periods.");
				security.setInsufficientHistory(true);
			}
		}
		{
			if (daily.size() > 20) {
				
				Price lastHistory = daily.get(daily.size()-1);
				if (DateTimeComparator.getDateOnlyInstance().compare(security.getPriceDate(), lastHistory.getDate()) > 0) {
					Log.d(TAG,"Daily price history doesn't contains last price market must be open "+security.getSymbol()+" Price Date="+security.getPriceDate()+" ListHistoryDate="+lastHistory.getDate());
					security.setLastClose(lastHistory.getClose());
				} else {
					Log.d(TAG,"Daily price history contains last price, it must be after markect close. "+security.getSymbol()+" Price Date="+security.getPriceDate()+" ListHistoryDate="+lastHistory.getDate());
					security.setLastClose(daily.get(daily.size()-2).getClose());
				}
				//appendCurrentPrice(daily,security);
				 
				security.setAverageTrueRange(ATR.compute(daily, 20));
			}
		}
	}

	private void appendCurrentPrice(List<Price> weekly, PaiStudy security, Period period) {
		if (weekly != null && weekly.size() > 0) {
			Price lastHistory = weekly.get(weekly.size() - 1);
			/*
			if (DateUtils.isSameDay(security.getPriceDate(), lastHistory.getDate())) {
				if (security.getPrice() != lastHistory.getClose()) {
					lastHistory.setClose(security.getPrice());
					lastHistory.setOpen(security.getOpen());
					lastHistory.setLow(security.getLow());
					lastHistory.setHigh(security.getHigh());
					Log.d(TAG, "History and Price Close Differ Should not Happen=" + lastHistory.getDate() + " History Close" + lastHistory.getClose()+ " Current Price" + security.getPrice());
				}
			} else*/
			Date truncateDate = DateUtils.truncate(security.getPriceDate()); 
			if (truncateDate.after(lastHistory.getDate())) {
				Price lastPrice = new Price();
				lastPrice.setClose(security.getPrice()); // current price is close in history
				lastPrice.setDate(DateUtils.truncate(security.getPriceDate()));
				lastPrice.setOpen(security.getOpen());
				lastPrice.setLow(security.getLow());
				lastPrice.setHigh(security.getHigh());
				weekly.add(lastPrice);
				Log.d(TAG, "Last History "+period.name()+" Date=" + lastHistory.getDate() + " Add Current Price Date " + truncateDate);
			} else {
				Log.d(TAG, "Last History "+period.name()+" Date=" + lastHistory.getDate() + " Don't Add Current Price Date " + truncateDate);
			}
		}
	}

	private void updateCurrentPrice(List<PaiStudy> securities) throws InterruptedException {
		Map<String,PaiStudy> cacheQuotes = new HashMap<String, PaiStudy>();
		for (PaiStudy quote : securities) {
			Log.d(TAG, quote.getSymbol());
			String oldName = quote.getName();
			PaiStudy cachedQuote = cacheQuotes.get(quote.getSymbol());
			if (cachedQuote == null) {
				if (!reader.readRTPrice(quote)) {
					reader.readCurrentPrice(quote);
					quote.setDelayedPrice(true);
					Log.w(TAG, "FAILED to get real time price using delayed Price");
				} else {
					if (quote.getPriceDate() == null) {
						PaiStudy quote2 = new PaiStudy(quote.getSymbol());
						reader.readCurrentPrice(quote);
						quote.setPriceDate(quote2.getPriceDate());
						Log.w(TAG, "Using price Date from delayed Price");
					} else {
						cacheQuotes.put(quote.getSymbol(), quote);
					}
				}
			} else { // from cache
				Log.d(TAG,"Using cached quote "+quote.getSymbol());
				quote.setPrice(cachedQuote.getPrice());
				quote.setPriceDate(cachedQuote.getPriceDate());
				quote.setOpen(cachedQuote.getOpen());
				quote.setLow(cachedQuote.getLow());
				quote.setHigh(cachedQuote.getHigh());
				quote.setName(cachedQuote.getName());
			}
			// updating the name here, may need to move update to when security
			// is added by user or kick off Processor at that time.
			if (quote.getName() != null && !quote.getName().equals(oldName)) {
				updateSecurityName(quote);
			}
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			Log.d(TAG,"Price="+quote.getPrice()+" Low="+quote.getLow()+" High="+quote.getHigh()+ " Open="+quote.getOpen()+" Date="+quote.getPriceDate());
		}
	}

	void updateSecurityName(PaiStudy security) {
		ContentValues values = new ContentValues();
		values.put(PaiStudyTable.COLUMN_NAME, security.getName());
		Uri securityUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + security.getSecurityId());
		getContentResolver().update(securityUri, values, null, null);
	}

	List<Price> getPriceHistory(PaiStudy study, String lastOnlineHistoryDbDate) {
		String TAG = "Get Price History";
		long readDbHistoryStartTime = System.currentTimeMillis();
		String nowDbDate = DateUtils.toDatabaseFormat(new Date());
		String[] projection = { PriceHistoryTable.COLUMN_SYMBOL, PriceHistoryTable.COLUMN_CLOSE, PriceHistoryTable.COLUMN_DATE,
				PriceHistoryTable.COLUMN_HIGH, PriceHistoryTable.COLUMN_LOW, PriceHistoryTable.COLUMN_OPEN,
				PriceHistoryTable.COLUMN_ADJUSTED_CLOSE };
		String selection = PaiStudyTable.COLUMN_SYMBOL + " = ? ";
		String[] selectionArgs = { study.getSymbol() };
		Log.d(TAG, "Get History from database "+ study.getSymbol());
		Cursor historyCursor = getContentResolver().query(PaiContentProvider.PRICE_HISTORY_URI, projection, selection, selectionArgs,
				PriceHistoryTable.COLUMN_DATE);
		boolean reloadHistory = true;
		List<Price> history = new ArrayList<Price>();
		try {
			if (historyCursor != null) {
				if (historyCursor.moveToLast()) {
					String lastHistoryDate = historyCursor.getString(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_DATE));
					// how does lastHistoryDate get to be after now? added check 9/21/2013
					if (lastHistoryDate.compareTo(lastOnlineHistoryDbDate) >= 0 && lastHistoryDate.compareTo(nowDbDate) <= 0) {
						double lastClose = historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_CLOSE));
						Log.d(TAG, study.getSymbol()+" is upto date using data from database lastDate=" + lastHistoryDate + " now"+nowDbDate+" last Close "
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
					} else {
						Log.d(TAG,"Last History Date "+lastHistoryDate+ " not equal Last on line History Date "+ lastOnlineHistoryDbDate);
					}
				}
			}
		} finally {
			historyCursor.close();
		}
		Log.d(TAG, "Time to read db history ms = " + (System.currentTimeMillis()-readDbHistoryStartTime) + " Obsolete "+reloadHistory);

		if (reloadHistory) {
			long readHistoryStartTime = System.currentTimeMillis();
			Log.d(TAG, "Price History is out-of-date reloading from history provider");
			history = reader.readHistory(study.getSymbol());
			study.setHistoryReloaded(true);
			Log.d(TAG, "Time to read on line history ms = " + (System.currentTimeMillis()-readHistoryStartTime));
			long dbUpdateStartTime = System.currentTimeMillis();
			if (history != null && history.size() > 0) {
				Log.d(TAG, "Replacing Price History in database");
				int rowsDeleted = getContentResolver().delete(PaiContentProvider.PRICE_HISTORY_URI, selection, selectionArgs);
				Log.d(TAG, "Deleted " + rowsDeleted + " history rows");
				for (Price price : history) try {
					ContentValues values = new ContentValues();
					values.put(PriceHistoryTable.COLUMN_ADJUSTED_CLOSE, price.getAdjustedClose());
					values.put(PriceHistoryTable.COLUMN_CLOSE, price.getClose());
					values.put(PriceHistoryTable.COLUMN_DATE, dbStringDateFormat.format(price.getDate()));
					values.put(PriceHistoryTable.COLUMN_HIGH, price.getHigh());
					values.put(PriceHistoryTable.COLUMN_LOW, price.getLow());
					values.put(PriceHistoryTable.COLUMN_OPEN, price.getOpen());
					values.put(PriceHistoryTable.COLUMN_SYMBOL, study.getSymbol());
					getContentResolver().insert(PaiContentProvider.PRICE_HISTORY_URI, values);
				} catch (Exception e) {
					Log.e(TAG,"Exception on Insert History ",e);
				}
				Log.d(TAG, "Time to delete/insert history ms = " + (System.currentTimeMillis()-dbUpdateStartTime));
			}
			
		}
		Log.d(TAG, "Returning " + history.size() + " Price History records for symbol " + study.getSymbol());
		return history;
	}

	void recordServiceLogEventLoadHistory() {
		ContentValues values = new ContentValues();
		values.put(ServiceLogTable.COLUMN_MESSAGE, "History Reload");
		values.put(ServiceLogTable.COLUMN_SERVICE_TYPE, ServiceType.PRICE.getIndex());
		values.put(ServiceLogTable.COLUMN_TIMESTAMP, DateTime.now().toString(ServiceLogTable.timestampFormat));
		getContentResolver().insert(PaiContentProvider.SERVICE_LOG_URI, values);
	}

	String getLastestOnlineHistoryDbDate(String symbol) {
		String lastOnlineHistoryDbDate = DateUtils.lastProbableTradeDate();
		Date latestHistoryDate = reader.latestHistoryDate(symbol);
		if (latestHistoryDate != null) {
			lastOnlineHistoryDbDate = DateUtils.toDatabaseFormat(latestHistoryDate);
		}
		return lastOnlineHistoryDbDate;
	}

	void saveStudyPrice(PaiStudy study) {
		ContentValues values = new ContentValues();
		values.put(PaiStudyTable.COLUMN_PRICE, study.getPrice());
		values.put(PaiStudyTable.COLUMN_PRICE_DATE, PaiStudyTable.priceDateFormat.format(study.getPriceDate()));
		if (study.getStatusMap() != 0) {
			values.put(PaiStudyTable.COLUMN_STATUSMAP, study.getStatusMap());
		}
		Log.d(TAG, "Updating Price Study " + study.toString());
		Uri studyUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + study.getSecurityId());
		getContentResolver().update(studyUri, values, null, null);
	}
	
	void saveStudy(PaiStudy study) {
		ContentValues values = new ContentValues();
		values.put(PaiStudyTable.COLUMN_MA_WEEK, study.getMaWeek());
		values.put(PaiStudyTable.COLUMN_MA_MONTH, study.getMaMonth());
		values.put(PaiStudyTable.COLUMN_MA_LAST_WEEK, study.getMaLastWeek());
		values.put(PaiStudyTable.COLUMN_MA_LAST_MONTH, study.getMaLastMonth());
		values.put(PaiStudyTable.COLUMN_PRICE, study.getPrice());
		values.put(PaiStudyTable.COLUMN_PRICE_LAST_WEEK, study.getPriceLastWeek());
		values.put(PaiStudyTable.COLUMN_PRICE_LAST_MONTH, study.getPriceLastMonth());
		values.put(PaiStudyTable.COLUMN_PRICE_DATE, PaiStudyTable.priceDateFormat.format(study.getPriceDate()));
		values.put(PaiStudyTable.COLUMN_STDDEV_WEEK, study.getStddevWeek());
		values.put(PaiStudyTable.COLUMN_STDDEV_MONTH, study.getStddevMonth());
		values.put(PaiStudyTable.COLUMN_AVG_TRUE_RANGE, study.getAverageTrueRange());
		values.put(PaiStudyTable.COLUMN_SYMBOL, study.getSymbol());
		values.put(PaiStudyTable.COLUMN_SMA_MONTH, study.getSmaMonth());
		values.put(PaiStudyTable.COLUMN_SMA_LAST_MONTH, study.getSmaLastMonth());
		values.put(PaiStudyTable.COLUMN_SMA_STDDEV_MONTH, study.getSmaStddevMonth());
		values.put(PaiStudyTable.COLUMN_SMA_WEEK, study.getSmaWeek());
		values.put(PaiStudyTable.COLUMN_SMA_LAST_WEEK,study.getSmaLastWeek());
		values.put(PaiStudyTable.COLUMN_SMA_STDDEV_WEEK,study.getSmaStddevWeek());
		values.put(PaiStudyTable.COLUMN_OPEN, study.getOpen());
		values.put(PaiStudyTable.COLUMN_HIGH, study.getHigh());
		values.put(PaiStudyTable.COLUMN_LOW, study.getLow());
		values.put(PaiStudyTable.COLUMN_LAST_CLOSE,study.getLastClose());
		values.put(PaiStudyTable.COLUMN_PORTFOLIO_ID, study.getPortfolioId());
		values.put(PaiStudyTable.COLUMN_STATUSMAP, study.getStatusMap());

		
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


	List<PaiStudy> getSecurities(String inSymbol) {
		List<PaiStudy> securities = new ArrayList<PaiStudy>();
		String[] projection = { PaiStudyTable.COLUMN_ID, PaiStudyTable.COLUMN_SYMBOL, PaiStudyTable.COLUMN_NAME, PaiStudyTable.COLUMN_MA_TYPE,
				PaiStudyTable.COLUMN_MA_WEEK, PaiStudyTable.COLUMN_MA_MONTH, PaiStudyTable.COLUMN_MA_LAST_WEEK, PaiStudyTable.COLUMN_MA_LAST_MONTH,
				PaiStudyTable.COLUMN_PRICE, PaiStudyTable.COLUMN_PRICE_LAST_WEEK, PaiStudyTable.COLUMN_PRICE_LAST_MONTH, PaiStudyTable.COLUMN_STDDEV_WEEK,
				PaiStudyTable.COLUMN_STDDEV_MONTH, PaiStudyTable.COLUMN_AVG_TRUE_RANGE, PaiStudyTable.COLUMN_PRICE_DATE, PaiStudyTable.COLUMN_PORTFOLIO_ID,
				PaiStudyTable.COLUMN_OPEN, PaiStudyTable.COLUMN_HIGH, PaiStudyTable.COLUMN_LOW, PaiStudyTable.COLUMN_SMA_MONTH,
				PaiStudyTable.COLUMN_SMA_LAST_MONTH, PaiStudyTable.COLUMN_SMA_STDDEV_MONTH, PaiStudyTable.COLUMN_SMA_WEEK, PaiStudyTable.COLUMN_SMA_LAST_WEEK,
				PaiStudyTable.COLUMN_SMA_STDDEV_WEEK, PaiStudyTable.COLUMN_LAST_CLOSE, PaiStudyTable.COLUMN_CONTRACTS, 
				PaiStudyTable.COLUMN_SMA_WEEK,PaiStudyTable.COLUMN_SMA_LAST_WEEK,PaiStudyTable.COLUMN_SMA_STDDEV_WEEK,PaiStudyTable.COLUMN_LAST_CLOSE
		};
		String selection = null;
		String[] selectionArgs = null;
		if (inSymbol != null && inSymbol.length() > 0) {
			selection = PaiStudyTable.COLUMN_SYMBOL + " = ? ";
			selectionArgs = new String [] { inSymbol };
			Log.d(TAG, "Selecting Single Security from database");
		}
		Cursor cursor = getContentResolver().query(PaiContentProvider.PAI_STUDY_URI, projection, selection, selectionArgs, PaiStudyTable.COLUMN_SYMBOL);
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
						security.setPortfolioId(cursor.getInt(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_PORTFOLIO_ID)));
						security.setSmaMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SMA_MONTH)));
						security.setSmaLastMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SMA_LAST_MONTH)));
						security.setSmaStddevMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SMA_STDDEV_MONTH)));
						security.setOpen(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_OPEN)));
						security.setHigh(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_HIGH)));
						security.setLow(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_LOW)));
						security.setSmaWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SMA_WEEK)));
						security.setSmaLastWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SMA_LAST_WEEK)));
						security.setSmaStddevWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SMA_STDDEV_WEEK)));
						security.setLastClose(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_LAST_CLOSE)));
				
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