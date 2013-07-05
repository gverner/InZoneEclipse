package com.codeworks.pai.contentprovider;

import java.io.IOException;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.util.Log;

import com.codeworks.pai.db.PriceHistoryTable;
import com.codeworks.pai.db.PaiStudyTable;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.mock.TestDataLoader;
import com.codeworks.pai.processor.ProcessorImpl;

public class PaiContentProviderTest extends ProviderTestCase2<PaiContentProvider> {

	public PaiContentProviderTest() {
		super(PaiContentProvider.class, PaiContentProvider.AUTHORITY);
	}

	private static final String TAG = PaiContentProviderTest.class.getSimpleName();

	static final int PRICE_HISTORY = 10;
	static final int PRICE_HISTORY_ID = 20;
	static final int SETTINGS = 30;
	static final int SETTINGS_ID = 40;
	static final int PAI_STUDY = 50;
	static final int PAI_STUDY_ID = 60;

	private static final String AUTHORITY = "com.codeworks.pai.contentprovider";

	private static final String SECURITY_PATH = "security";
	private static final String PRICE_HISTORY_PATH = "price_history";
	private static final String PAI_STUDY_PATH = "pai_study";

	public static final Uri SETTINGS_URI = Uri.parse("content://" + AUTHORITY + "/" + SECURITY_PATH);
	public static final Uri PRICE_HISTORY_URI = Uri.parse("content://" + AUTHORITY + "/" + PRICE_HISTORY_PATH);
	public static final Uri PAI_STUDY_URI = Uri.parse("content://" + AUTHORITY + "/" + PAI_STUDY_PATH);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/pai_table";

	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/pai_table_item";

	public static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, PRICE_HISTORY_PATH, PRICE_HISTORY);
		sURIMatcher.addURI(AUTHORITY, PRICE_HISTORY_PATH + "/#", PRICE_HISTORY_ID);
		sURIMatcher.addURI(AUTHORITY, SECURITY_PATH, SETTINGS);
		sURIMatcher.addURI(AUTHORITY, SECURITY_PATH + "/#", SETTINGS_ID);
		sURIMatcher.addURI(AUTHORITY, PAI_STUDY_PATH, PAI_STUDY);
		sURIMatcher.addURI(AUTHORITY, PAI_STUDY_PATH + "/#", PAI_STUDY_ID);
	}

	public void testUriMatcher() {
		assertEquals(SETTINGS, sURIMatcher.match(SETTINGS_URI));
		assertEquals(PRICE_HISTORY, sURIMatcher.match(PRICE_HISTORY_URI));
		assertEquals(PAI_STUDY, sURIMatcher.match(PAI_STUDY_URI));
	}

	public void loadHistory() throws IOException {
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		for (Price price : history) {
			ContentValues values = new ContentValues();
			values.put(PriceHistoryTable.COLUMN_ADJUSTED_CLOSE, price.getAdjustedClose());
			values.put(PriceHistoryTable.COLUMN_CLOSE, price.getClose());
			values.put(PriceHistoryTable.COLUMN_DATE, ProcessorImpl.dbStringDateFormat.format(price.getDate()));
			values.put(PriceHistoryTable.COLUMN_HIGH, price.getHigh());
			values.put(PriceHistoryTable.COLUMN_LOW, price.getLow());
			values.put(PriceHistoryTable.COLUMN_OPEN, price.getOpen());
			values.put(PriceHistoryTable.COLUMN_SYMBOL, "SPY");
			getMockContentResolver().insert(PaiContentProvider.PRICE_HISTORY_URI, values);
		}
	}

	public void testHistoryQuery() throws IOException {
		loadHistory();
		String[] projection = { PriceHistoryTable.COLUMN_SYMBOL, PriceHistoryTable.COLUMN_CLOSE, PriceHistoryTable.COLUMN_DATE,
				PriceHistoryTable.COLUMN_HIGH, PriceHistoryTable.COLUMN_LOW, PriceHistoryTable.COLUMN_OPEN,
				PriceHistoryTable.COLUMN_ADJUSTED_CLOSE };
		String selection = PriceHistoryTable.COLUMN_SYMBOL + " = ? ";
		String[] selectionArgs = { "SPY" };
		Cursor historyCursor = getMockContentResolver().query(PaiContentProvider.PRICE_HISTORY_URI, projection, selection, selectionArgs,
				PriceHistoryTable.COLUMN_DATE);
		try {
			boolean rowResult = historyCursor.moveToFirst();

			while (rowResult) {
				Log.d(TAG,
						historyCursor.getString(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_SYMBOL)) + " "
								+ historyCursor.getString(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_DATE)) + " "
								+ historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_OPEN)) + " "
								+ historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_CLOSE)));
				rowResult = historyCursor.moveToNext();
			}
			System.out.println("history rowCount=" + historyCursor.getCount());
		} finally {
			historyCursor.close();
		}
	}

	
	public Uri loadSettings() throws IOException {
			ContentValues values = new ContentValues();
			values.put(PaiStudyTable.COLUMN_SYMBOL, "SPY");
			values.put(PaiStudyTable.COLUMN_MA_TYPE, "E");
			values.put(PaiStudyTable.COLUMN_PRICE, 1.1d);
			values.put(PaiStudyTable.COLUMN_NAME, "S&P");
			values.put(PaiStudyTable.COLUMN_PORTFOLIO_ID, 1L);
			return getMockContentResolver().insert(PaiContentProvider.PAI_STUDY_URI, values);
	}

	public void testSettings() throws IOException {
		Uri settingsUri = loadSettings();
		String[] projection = { PaiStudyTable.COLUMN_SYMBOL, PaiStudyTable.COLUMN_MA_TYPE, PaiStudyTable.COLUMN_PRICE,
				PaiStudyTable.COLUMN_NAME };

		Cursor cursor = getMockContentResolver().query(settingsUri, projection, null, null ,null);
		try {
			assertEquals("one row", 1, cursor.getCount());
			boolean rowResult = cursor.moveToFirst();
			assertEquals("moveToFirst", true, rowResult);
			assertEquals("SPY", cursor.getString(cursor.getColumnIndex(PaiStudyTable.COLUMN_SYMBOL)));
			assertEquals("E", cursor.getString(cursor.getColumnIndex(PaiStudyTable.COLUMN_MA_TYPE)));
			assertEquals(1.1d, cursor.getDouble(cursor.getColumnIndex(PaiStudyTable.COLUMN_PRICE)));
			assertEquals("S&P", cursor.getString(cursor.getColumnIndex(PaiStudyTable.COLUMN_NAME)));
		} finally {
			cursor.close();
		}
		ContentValues values = new ContentValues();
		values.put(PaiStudyTable.COLUMN_SYMBOL, "QQQ");
		values.put(PaiStudyTable.COLUMN_MA_TYPE, "S");
		values.put(PaiStudyTable.COLUMN_PRICE, 2.2d);
		values.put(PaiStudyTable.COLUMN_NAME, "THE Qs");
		assertEquals("Rows Updated", 1, getMockContentResolver().update(settingsUri, values, null, null));
		
		cursor = getMockContentResolver().query(settingsUri, projection, null, null ,null);
		try {
			assertEquals("one row", 1, cursor.getCount());
			boolean rowResult = cursor.moveToFirst();
			assertEquals("moveToFirst", true, rowResult);
			assertEquals("QQQ", cursor.getString(cursor.getColumnIndex(PaiStudyTable.COLUMN_SYMBOL)));
			assertEquals("S", cursor.getString(cursor.getColumnIndex(PaiStudyTable.COLUMN_MA_TYPE)));
			assertEquals(2.2d, cursor.getDouble(cursor.getColumnIndex(PaiStudyTable.COLUMN_PRICE)));
			assertEquals("THE Qs", cursor.getString(cursor.getColumnIndex(PaiStudyTable.COLUMN_NAME)));
		} finally {
			cursor.close();
		}
		getMockContentResolver().delete(settingsUri, null, null);
		cursor = getMockContentResolver().query(settingsUri, projection, null, null ,null);
		assertEquals("zero row", 0, cursor.getCount());
	}
	
}
