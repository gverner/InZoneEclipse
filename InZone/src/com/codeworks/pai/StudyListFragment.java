package com.codeworks.pai;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.R.color;
import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.PaiStudyTable;
import com.codeworks.pai.db.model.PaiStudy;

public class StudyListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String		TAG					= StudyActivity.class.getSimpleName();

	// private Cursor cursor;
	private PaiCursorAdapter		adapter;
	SimpleDateFormat				lastUpdatedFormat	= new SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.US);

	private OnItemSelectedListener	listener;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		lastUpdatedFormat.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		
		ListView list = getListView();
		View headerView = View.inflate(getActivity(), R.layout.studylist_header, null);
		list.addHeaderView(headerView);
		
		View footerView = View.inflate(getActivity(), R.layout.studylist_footer, null);
		list.addFooterView(footerView);
		
		//ListView list = getListView();
	    list.setOnItemLongClickListener(new OnItemLongClickListener() {

	      @Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (view.getId() == R.id.quoteList_symbol) {
					Toast.makeText(getActivity().getApplicationContext(), "Item in position " + position + " clicked " + ((TextView)view).getText(), Toast.LENGTH_LONG)
							.show();
					updateDetail(id);
					// Return true to consume the click event. In this case the
					// onListItemClick listener is not called anymore.
					return true;
				} else {
					return false;
				}
			}
		});
	    /*	
		getListView().setOnItemClickListener(
		new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				updateDetail();
			}
			
		});
		*/
		fillData();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.studylist_main, container, false);
		/*
		 * Button button = (Button) view.findViewById(R.id.button1);
		 * button.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { updateDetail(); } });
		 */
		
		
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		adapter.notifyDataSetChanged();
	}

	public interface OnItemSelectedListener {
		public void onStudySelected(Long studyId);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnItemSelectedListener) {
			listener = (OnItemSelectedListener) activity;
		} else {
			throw new ClassCastException(activity.toString() + " must implemenet MyListFragment.OnItemSelectedListener");
		}
	}

	// May also be triggered from the Activity
	public void updateDetail(long id) {
		listener.onStudySelected(id);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		updateDetail(id);
	}

	private void fillData() {
		getLoaderManager().initLoader(0, null, this);
		adapter = new PaiCursorAdapter(this.getActivity());
		setListAdapter(adapter);

	}

	// Creates a new loader after the initLoader () call
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[] { PaiStudyTable.COLUMN_ID, PaiStudyTable.COLUMN_SYMBOL, PaiStudyTable.COLUMN_PRICE,
				PaiStudyTable.COLUMN_PRICE_LAST_WEEK, PaiStudyTable.COLUMN_PRICE_LAST_MONTH, PaiStudyTable.COLUMN_MA_WEEK, PaiStudyTable.COLUMN_MA_MONTH,
				PaiStudyTable.COLUMN_MA_LAST_WEEK, PaiStudyTable.COLUMN_MA_LAST_MONTH, PaiStudyTable.COLUMN_STDDEV_WEEK, PaiStudyTable.COLUMN_STDDEV_MONTH,
				PaiStudyTable.COLUMN_AVG_TRUE_RANGE, PaiStudyTable.COLUMN_PRICE_DATE };
		CursorLoader cursorLoader = new CursorLoader(getActivity(), PaiContentProvider.PAI_STUDY_URI, projection, null, null, null);
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
		private LayoutInflater	mInflator;

		public PaiCursorAdapter(Context context) {
			super(context, null, 0);
			// Log.d("TAG", "CursorAdapter Constr..");
			mInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// Log.d("TAG", "CursorAdapter BindView");
			if (null != cursor) {
				// Log.d("TAG", "CursorAdapter BindView:Cursor not null");

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
					Log.d(TAG, "Parse Exception Price Date", e);
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

				TextView lastUpdated = (TextView) getActivity().findViewById(R.id.studyList_lastUpdated);
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
			// Log.d("TAG", "CursorAdapter newView");
			final View customListView = mInflator.inflate(R.layout.studylist_row2, null);
			return customListView;
		}
	}

}
