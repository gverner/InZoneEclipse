package com.codeworks.pai.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SecurityTable {
	  // Database table
	  public static final String TABLE_SECURITY = "security";
	  public static final String COLUMN_ID = "_id";
	  public static final String COLUMN_SYMBOL = "symbol";
	  public static final String COLUMN_NAME = "name";

	  // Database creation SQL statement
	  private static final String DATABASE_CREATE = "create table " 
	      + TABLE_SECURITY
	      + "(" 
	      + COLUMN_ID + " integer primary key autoincrement, "
	      + COLUMN_NAME + " text null, "
	      + COLUMN_SYMBOL + " text not null " 
	      + ");";

	  public static void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
	  }

	  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
	      int newVersion) {
	    Log.w(PriceHistoryTable.class.getName(), "Upgrading database from version "
	        + oldVersion + " to " + newVersion
	        + ", which will destroy all old data");
	    database.execSQL("DROP TABLE IF EXISTS " + TABLE_SECURITY);
	    onCreate(database);
	  }

}
