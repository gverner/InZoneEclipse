package com.codeworks.pai.db;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PaiStudyTable {
	  public static SimpleDateFormat priceDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.US);
	  // Database table
	  public static final String TABLE_STUDY = "study";
	  public static final String COLUMN_ID = "_id";
	  public static final String COLUMN_SYMBOL = "symbol";
	  public static final String COLUMN_NAME = "name";
	  public static final String COLUMN_PRICE = "price";
	  public static final String COLUMN_PRICE_DATE = "price_date";
	  public static final String COLUMN_PRICE_LAST_WEEK = "price_last_week";
	  public static final String COLUMN_PRICE_LAST_MONTH = "price_last_month";
	  public static final String COLUMN_MA_TYPE = "ma_type";
	  public static final String COLUMN_MA_WEEK = "ma_week";
	  public static final String COLUMN_MA_MONTH = "ma_month";
	  public static final String COLUMN_MA_LAST_WEEK = "ma_last_week";
	  public static final String COLUMN_MA_LAST_MONTH = "ma_last_month";
	  public static final String COLUMN_STDDEV_WEEK = "stddev_week";
	  public static final String COLUMN_STDDEV_MONTH = "stddev_month";
	  public static final String COLUMN_AVG_TRUE_RANGE = "avg_true_range";
	  public static final String COLUMN_NOTICE = "notice";
	  public static final String COLUMN_NOTICE_DATE = "notice_date";

	  // Database creation SQL statement
	  private static final String DATABASE_CREATE = "create table " 
	      + TABLE_STUDY
	      + "(" 
	      + COLUMN_ID + " integer primary key autoincrement, " 
	      + COLUMN_SYMBOL + " text not null, " 
	      + COLUMN_NAME + " text null, "
	      + COLUMN_PRICE + " real null, " 
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
	      + COLUMN_AVG_TRUE_RANGE + " real null, "
	      + COLUMN_NOTICE + " integer null, "
	      + COLUMN_NOTICE_DATE + " text null"
	      + ");";

	  public static void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
	    priceDateFormat.setTimeZone(TimeZone.getDefault());
	  }

	  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
	      int newVersion) {
	    Log.w(PriceHistoryTable.class.getName(), "Upgrading database from version "
	        + oldVersion + " to " + newVersion
	        + ", which will destroy all old data");
	    database.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDY);
	    onCreate(database);
	  }
}
