package com.codeworks.pai.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.codeworks.pai.db.model.MaType;
import com.codeworks.pai.db.model.PaiStudy;
import com.codeworks.pai.processor.Notice;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PaiStudyTable {
	static String TAG = PaiStudyTable.class.getSimpleName();
	public static SimpleDateFormat	priceDateFormat			= new SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.US);
	public static SimpleDateFormat	noticeDateFormat		= new SimpleDateFormat("yyyyMMdd kk:mm", Locale.US);

	// Database table
	public static final String		TABLE_STUDY				= "study";
	public static final String		COLUMN_ID				= "_id";
	public static final String		COLUMN_PORTFOLIO_ID		= "portfolio_id";
	public static final String		COLUMN_SYMBOL			= "symbol";
	public static final String		COLUMN_NAME				= "name";
	public static final String		COLUMN_PRICE			= "price";
	public static final String		COLUMN_OPEN				= "open";
	public static final String		COLUMN_HIGH				= "high";
	public static final String		COLUMN_LOW				= "low";
	public static final String		COLUMN_PRICE_DATE		= "price_date";
	public static final String		COLUMN_PRICE_LAST_WEEK	= "price_last_week";
	public static final String		COLUMN_PRICE_LAST_MONTH	= "price_last_month";
	public static final String		COLUMN_MA_TYPE			= "ma_type";
	public static final String		COLUMN_MA_WEEK			= "ma_week";
	public static final String		COLUMN_MA_MONTH			= "ma_month";
	public static final String		COLUMN_MA_LAST_WEEK		= "ma_last_week";
	public static final String		COLUMN_MA_LAST_MONTH	= "ma_last_month";
	public static final String		COLUMN_STDDEV_WEEK		= "stddev_week";
	public static final String		COLUMN_STDDEV_MONTH		= "stddev_month";
	public static final String		COLUMN_AVG_TRUE_RANGE	= "avg_true_range";
	public static final String		COLUMN_NOTICE			= "notice";
	public static final String		COLUMN_NOTICE_DATE		= "notice_date";
	public static final String		COLUMN_SMA_MONTH		= "sma_month";
	public static final String		COLUMN_SMA_LAST_MONTH	= "sma_last_month";
	public static final String		COLUMN_SMA_STDDEV_MONTH	= "sma_stddev_month";
	public static final String		COLUMN_SMA_WEEK			= "sma_week";
	public static final String		COLUMN_SMA_LAST_WEEK	= "sma_last_week";
	public static final String		COLUMN_SMA_STDDEV_WEEK	= "sma_stddev_week";
	public static final String		COLUMN_LAST_CLOSE		= "last_close";
	public static final String		COLUMN_CONTRACTS		= "contracts";
	public static final String 		COLUMN_STATUSMAP        = "status_map";

	  // Database creation SQL statement
	  private static final String DATABASE_CREATE = "create table " 
	      + TABLE_STUDY
	      + "(" 
	      + COLUMN_ID + " integer primary key autoincrement, " 
	      + COLUMN_PORTFOLIO_ID + " integer not null, "
	      + COLUMN_SYMBOL + " text not null, " 
	      + COLUMN_NAME + " text null, "
	      + COLUMN_PRICE + " real null, " 
	      + COLUMN_OPEN + " real null, "
	      + COLUMN_HIGH + " real null, "
	      + COLUMN_LOW + " real null, "
	      + COLUMN_PRICE_DATE + " string null, "
	      + COLUMN_PRICE_LAST_WEEK + " real null, " 
	      + COLUMN_PRICE_LAST_MONTH + " real null, " 
	      + COLUMN_MA_TYPE + " text null, " 
	      + COLUMN_MA_WEEK + " real null, " 
	      + COLUMN_MA_MONTH + " real null, "
	      + COLUMN_MA_LAST_WEEK + " real null, " 
	      + COLUMN_MA_LAST_MONTH + " real null, "
	      + COLUMN_STDDEV_WEEK + " real null, " 
	      + COLUMN_STDDEV_MONTH + " real null, "
	      + COLUMN_SMA_MONTH + " real null, " 
	      + COLUMN_SMA_LAST_MONTH + " real null, "
	      + COLUMN_SMA_STDDEV_MONTH + " real null, "
	      + COLUMN_SMA_WEEK + " real null, " 
	      + COLUMN_SMA_LAST_WEEK + " real null, "
	      + COLUMN_SMA_STDDEV_WEEK + " real null, "
	      + COLUMN_AVG_TRUE_RANGE + " real null, "
		  + COLUMN_LAST_CLOSE + " real null, "
		  + COLUMN_CONTRACTS + " integer null, "
	      + COLUMN_NOTICE + " integer null, "
	      + COLUMN_NOTICE_DATE + " text null, "
	      + COLUMN_STATUSMAP + " integer null"
	      + ");";

	public static String[] getFullProjection() {
		String[] projection = new String[] { COLUMN_ID, COLUMN_PORTFOLIO_ID, COLUMN_SYMBOL, COLUMN_NAME, COLUMN_PRICE, COLUMN_OPEN, COLUMN_HIGH, COLUMN_LOW,
				COLUMN_PRICE_DATE, COLUMN_PRICE_LAST_WEEK, COLUMN_PRICE_LAST_MONTH, COLUMN_MA_TYPE, COLUMN_MA_WEEK, COLUMN_MA_MONTH, COLUMN_MA_LAST_WEEK,
				COLUMN_MA_LAST_MONTH, COLUMN_STDDEV_WEEK, COLUMN_STDDEV_MONTH, COLUMN_SMA_MONTH, COLUMN_SMA_LAST_MONTH, COLUMN_SMA_STDDEV_MONTH,
				COLUMN_SMA_WEEK, COLUMN_SMA_LAST_WEEK, COLUMN_SMA_STDDEV_WEEK, COLUMN_AVG_TRUE_RANGE, COLUMN_LAST_CLOSE, COLUMN_CONTRACTS, COLUMN_NOTICE,
				COLUMN_NOTICE_DATE, COLUMN_STATUSMAP };
		return projection;
	}

	public static PaiStudy loadStudy(Cursor cursor)  {
		String symbol = cursor.getString(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SYMBOL));
		PaiStudy study = new PaiStudy(symbol);

		study.setSecurityId(cursor.getLong(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_ID)));
		study.setPortfolioId(cursor.getInt(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_PORTFOLIO_ID)));
		study.setName(cursor.getString(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_NAME)));
		study.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_PRICE)));
		study.setOpen(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_OPEN)));
		study.setHigh(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_HIGH)));
		study.setLow(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_LOW)));
		try {
			study.setPriceDate(cursor.getString(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_PRICE_DATE)));
		} catch (Exception e) {
			Log.d(TAG, "Parse Exception Price Date", e);
		}
		study.setPriceLastWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_PRICE_LAST_WEEK)));
		study.setPriceLastMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_PRICE_LAST_MONTH)));
		study.setMaType(MaType.parse(cursor.getString(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_MA_TYPE))));
		study.setMaWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_MA_WEEK)));
		study.setMaMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_MA_MONTH)));
		study.setMaLastWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_MA_LAST_WEEK)));
		study.setMaLastMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_MA_LAST_MONTH)));
		study.setStddevWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_STDDEV_WEEK)));
		study.setStddevMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_STDDEV_MONTH)));
		study.setSmaMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SMA_MONTH)));
		study.setSmaLastMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SMA_LAST_MONTH)));
		study.setSmaStddevMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SMA_STDDEV_MONTH)));
		study.setSmaWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SMA_WEEK)));
		study.setSmaLastWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SMA_LAST_WEEK)));
		study.setSmaStddevWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SMA_STDDEV_WEEK)));
		study.setAverageTrueRange(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_AVG_TRUE_RANGE)));
		study.setLastClose(cursor.getDouble(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_LAST_CLOSE)));
		study.setContracts(cursor.getInt(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_CONTRACTS)));
		study.setNotice(Notice.fromIndex(cursor.getInt(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_NOTICE))));
		try {
			String noticeDateStr = cursor.getString(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_NOTICE_DATE));
			if (noticeDateStr != null) {
				study.setNoticeDate(noticeDateFormat.parse(noticeDateStr));
			}
		} catch (Exception e) {
			Log.d(TAG, "Parse Exception Notice Date", e);
		}
		study.setStatusMap(cursor.getInt(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_STATUSMAP)));
		return study;
	}

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
		priceDateFormat.setTimeZone(TimeZone.getDefault());
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(PriceHistoryTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDY);
		onCreate(database);
	}
}
