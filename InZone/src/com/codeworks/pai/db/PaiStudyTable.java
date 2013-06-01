package com.codeworks.pai.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PaiStudyTable {
	  // Database table
	  public static final String TABLE_STUDY = "study";
	  public static final String COLUMN_ID = "_id";
	  public static final String COLUMN_SYMBOL = "symbol";
	  public static final String COLUMN_PRICE = "price";
	  public static final String COLUMN_PRICE_LAST_WEEK = "price_last_week";
	  public static final String COLUMN_PRICE_LAST_MONTH = "price_last_month";
	  public static final String COLUMN_MA_TYPE = "matype";
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
	      + COLUMN_PRICE + " real not null, " 
	      + COLUMN_PRICE_LAST_WEEK + " real not null, " 
	      + COLUMN_PRICE_LAST_MONTH + " real not null, " 
	      + COLUMN_MA_TYPE + " text not null, " 
	      + COLUMN_MA_WEEK + " real not null, " 
	      + COLUMN_MA_MONTH + " real not null, "
	      + COLUMN_MA_LAST_WEEK + " real not null, " 
	      + COLUMN_MA_LAST_MONTH + " real not null, "
	      + COLUMN_STDDEV_WEEK + " real not null, " 
	      + COLUMN_STDDEV_MONTH + " real not null, "
	      + COLUMN_AVG_TRUE_RANGE + " real not null, "
	      + COLUMN_NOTICE + " integer null, "
	      + COLUMN_NOTICE_DATE + " text null"
	      + ");";

	  public static void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
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
