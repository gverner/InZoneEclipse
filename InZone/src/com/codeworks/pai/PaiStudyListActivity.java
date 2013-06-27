/*
 *
 * Copyright (C) 2011 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.codeworks.pai;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.R.color;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.PaiStudyTable;
import com.codeworks.pai.db.model.PaiStudy;
import com.codeworks.pai.processor.UpdateService;

/**
 * Starts up the task list that will interact with the AccessibilityService
 * sample.
 */
public class PaiStudyListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = PaiStudyListActivity.class.getSimpleName();

	private Intent dailyIntent;

	// private Cursor cursor;
	private PaiCursorAdapter adapter;
	SimpleDateFormat lastUpdatedFormat = new SimpleDateFormat("MM/dd/yyyy hh:mmaa");
	// List<PaiStudy> quotes = new ArrayList<PaiStudy>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		dailyIntent = new Intent(this, UpdateService.class);
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_MANUAL);
		startService(dailyIntent);
		setContentView(R.layout.studylist_main);
		fillData();
		lastUpdatedFormat.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemStartSerivce:
			startService(dailyIntent);
			break;
		case R.id.itemStopService:
			stopService(dailyIntent);
			break;
		case R.id.portfolio:
			Intent intent = new Intent();
			intent.setClassName(getPackageName(), SecurityListActivity.class.getName());
			startActivity(intent);
			break;
		case R.id.action_settings:
			Intent settingsIntent = new Intent();
			settingsIntent.setClassName(getPackageName(), SettingsActivity.class.getName());
			startActivity(settingsIntent);
			break;
		}
		
		return true;
	}

	private void fillData() {
		getLoaderManager().initLoader(0, null, this);
		adapter = new PaiCursorAdapter(this);
		setListAdapter(adapter);
	}

	// Creates a new loader after the initLoader () call
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[] { PaiStudyTable.COLUMN_ID, PaiStudyTable.COLUMN_SYMBOL, PaiStudyTable.COLUMN_PRICE,
				PaiStudyTable.COLUMN_PRICE_LAST_WEEK, PaiStudyTable.COLUMN_PRICE_LAST_MONTH, PaiStudyTable.COLUMN_MA_WEEK,
				PaiStudyTable.COLUMN_MA_MONTH, PaiStudyTable.COLUMN_MA_LAST_WEEK, PaiStudyTable.COLUMN_MA_LAST_MONTH,
				PaiStudyTable.COLUMN_STDDEV_WEEK, PaiStudyTable.COLUMN_STDDEV_MONTH, PaiStudyTable.COLUMN_AVG_TRUE_RANGE, PaiStudyTable.COLUMN_PRICE_DATE };
		CursorLoader cursorLoader = new CursorLoader(this, PaiContentProvider.PAI_STUDY_URI, projection, null, null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// data is not available anymore, delete reference
		adapter.swapCursor(null);
	}

	class PaiCursorAdapter extends CursorAdapter {
		private LayoutInflater mInflator;

		public PaiCursorAdapter(Context context) {
			super(context, null, 0);
			//Log.d("TAG", "CursorAdapter Constr..");
			mInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			//Log.d("TAG", "CursorAdapter BindView");
			if (null != cursor) {
				//Log.d("TAG", "CursorAdapter BindView:Cursor not null");

				PaiStudy study = new PaiStudy(cursor.getString(cursor.getColumnIndex(PaiStudyTable.COLUMN_SYMBOL)));
				study.setPrice(cursor.getDouble(cursor.getColumnIndex(PaiStudyTable.COLUMN_PRICE)));
				study.setPriceLastWeek(cursor.getDouble(cursor.getColumnIndex(PaiStudyTable.COLUMN_PRICE_LAST_WEEK)));
				study.setPriceLastMonth(cursor.getDouble(cursor.getColumnIndex(PaiStudyTable.COLUMN_PRICE_LAST_MONTH)));
				study.setMaWeek(cursor.getDouble(cursor.getColumnIndex(PaiStudyTable.COLUMN_MA_WEEK)));
				study.setMaMonth(cursor.getDouble(cursor.getColumnIndex(PaiStudyTable.COLUMN_MA_MONTH)));
				study.setMaLastWeek(cursor.getDouble(cursor.getColumnIndex(PaiStudyTable.COLUMN_MA_LAST_WEEK)));
				study.setMaLastMonth(cursor.getDouble(cursor.getColumnIndex(PaiStudyTable.COLUMN_MA_LAST_MONTH)));
				study.setStddevWeek(cursor.getDouble(cursor.getColumnIndex(PaiStudyTable.COLUMN_STDDEV_WEEK)));
				study.setStddevMonth(cursor.getDouble(cursor.getColumnIndex(PaiStudyTable.COLUMN_STDDEV_MONTH)));
				study.setAverageTrueRange(cursor.getDouble(cursor.getColumnIndex(PaiStudyTable.COLUMN_AVG_TRUE_RANGE)));
				try {
					study.setPriceDate(cursor.getString(cursor.getColumnIndex(PaiStudyTable.COLUMN_PRICE_DATE)));
				} catch (ParseException e) {
					Log.d(TAG,"Parse Exception Price Date",e);
				}
				// Set the Menu Image
				// ImageView
				// menuImage=(ImageView)arg0.findViewById(R.id.iv_ContactImg);
				// menuImage.setImageResource(R.drawable.ic_launcher);

				// Set Synbol
				TextView symbol = (TextView) view.findViewById(R.id.quoteList_symbol);
				symbol.setText(study.getSymbol());
				setTrend(view, study.isUpTrendMonthly(), R.id.quoteList_MonthyTrend);
				setTrend(view, study.isUpTrendWeekly(), R.id.quoteList_WeeklyTrend);
				// Set EMA
				TextView ema = (TextView) view.findViewById(R.id.quoteList_ema);
				ema.setText(PaiStudy.format(study.getMaWeek()));
				// Price
				TextView price = (TextView) view.findViewById(R.id.quoteList_Price);
				price.setText(PaiStudy.format(study.getPrice()));
				TextView textBuyZoneBot = setDouble(view, study.calcBuyZoneBottom(), R.id.quoteList_BuyZoneBottom);
				TextView textBuyZoneTop = setDouble(view, study.calcBuyZoneTop(), R.id.quoteList_BuyZoneTop);
				if (study.isPriceInBuyZone()) {
					textBuyZoneBot.setBackgroundColor(Color.DKGRAY);
					textBuyZoneTop.setBackgroundColor(Color.DKGRAY);
					textBuyZoneBot.setTextColor(Color.GREEN);
					textBuyZoneTop.setTextColor(Color.GREEN);
				} else if (study.isPossibleUptrendTermination()) {
					textBuyZoneBot.setBackgroundColor(color.holo_orange_light);
					textBuyZoneTop.setBackgroundColor(color.holo_orange_light);
					textBuyZoneBot.setTextColor(Color.MAGENTA);
					textBuyZoneTop.setTextColor(Color.MAGENTA);
				} else {
					textBuyZoneBot.setBackgroundColor(Color.WHITE);
					textBuyZoneTop.setBackgroundColor(Color.WHITE);
					textBuyZoneBot.setTextColor(Color.BLACK);
					textBuyZoneTop.setTextColor(Color.BLACK);
				}
				
				TextView textSellZoneBot = setDouble(view, study.calcSellZoneBottom(), R.id.quoteList_SellZoneBottom);
				TextView textSellZoneTop = setDouble(view, study.calcSellZoneTop(), R.id.quoteList_SellZoneTop);
				if (study.isPriceInSellZone()) {
					textSellZoneBot.setBackgroundColor(color.holo_green_dark);
					textSellZoneTop.setBackgroundColor(color.holo_green_dark);
					textSellZoneBot.setTextColor(Color.GREEN);
					textSellZoneTop.setTextColor(Color.GREEN);
				} else if (study.isPossibleDowntrendTermination()) {
					textSellZoneBot.setBackgroundColor(color.holo_orange_light);
					textSellZoneTop.setBackgroundColor(color.holo_orange_light);
					textSellZoneBot.setTextColor(Color.MAGENTA);
					textSellZoneTop.setTextColor(Color.MAGENTA);
				} else {
					textSellZoneBot.setBackgroundColor(Color.WHITE);
					textSellZoneTop.setBackgroundColor(Color.WHITE);
					textSellZoneBot.setTextColor(Color.BLACK);
					textSellZoneTop.setTextColor(Color.BLACK);
				}

				TextView lastUpdated = (TextView) findViewById(R.id.studyList_lastUpdated);
				if (study.getPriceDate() != null && lastUpdated != null) {
					lastUpdated.setText(lastUpdatedFormat.format(study.getPriceDate()));
				}
				
			}
		}
		
		TextView setDouble(View view, double value, int viewId) {
			TextView textView = (TextView) view.findViewById(viewId);
			textView.setText(PaiStudy.format(value));
			return textView;
		}
		TextView setTrend(View view, boolean isUptrend, int viewId) {
			TextView textView = (TextView) view.findViewById(viewId);
			if (isUptrend) {
				textView.setText("U");
			} else {
				textView.setText("D");
				
			}
			return textView;
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			//Log.d("TAG", "CursorAdapter newView");
			final View customListView = mInflator.inflate(R.layout.studylist_row2, null);
			return customListView;
		}
	}


	public void showToast(final String toast)
	{
	    runOnUiThread(new Runnable() {
	        public void run()
	        {
	            Toast.makeText(PaiStudyListActivity.this, toast, Toast.LENGTH_SHORT).show();
	        }
	    });
	}	
}
