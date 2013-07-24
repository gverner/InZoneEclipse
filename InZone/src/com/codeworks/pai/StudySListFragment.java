package com.codeworks.pai;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.R.color;
import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.PaiStudyTable;
import com.codeworks.pai.db.model.PaiStudy;
import com.codeworks.pai.db.model.Rules;
import com.codeworks.pai.db.model.SmaRules;
import com.codeworks.pai.processor.UpdateService;

public class StudySListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String		TAG					= StudyActivity.class.getSimpleName();

	public static final String	ARG_PORTFOLIO_ID	= "com.codeworks.pai.portfolioId";

	// private Cursor cursor;
	private PaiCursorAdapter		adapter;
	SimpleDateFormat				lastUpdatedFormat	= new SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.US);

	private OnItemSelectedListener	listener;
	private long portfolioId = 1;
	View footerView;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (getArguments() != null)
		if (getArguments().getInt(ARG_PORTFOLIO_ID)  != 0) {
			portfolioId = getArguments().getInt(ARG_PORTFOLIO_ID);
		}
		Log.i(TAG, "Activity Created portfolioid="+portfolioId);
		lastUpdatedFormat.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		
		ListView list = getListView();
		
		View headerView = View.inflate(getActivity(), R.layout.study_s_list_header, null);
		list.addHeaderView(headerView);
		
		footerView = View.inflate(getActivity(), R.layout.studylist_footer, null);
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
		// Register mMessageReceiver to receive messages.
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver, new IntentFilter(UpdateService.BROADCAST_UPDATE_PROGRESS_BAR));
		
	}
	  @Override
	public void onPause() {
		// Unregister since the activity is not visible
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
		super.onPause();
	}

		// handler for received Intents for the "ProgressBar status" even
		BroadcastReceiver	mMessageReceiver	= new BroadcastReceiver() {
													@Override
													public void onReceive(Context context, Intent intent) {
														Integer status = intent.getIntExtra(UpdateService.PROGRESS_BAR_STATUS, 0);
														Log.d(TAG, "Received Broadcase with status: " + status);
														ProgressBar progressBar = (ProgressBar) footerView.findViewById(R.id.progressBar1);
														if (status == 0) {
															progressBar.setVisibility(ProgressBar.VISIBLE);
														} else {
															progressBar.setVisibility(ProgressBar.INVISIBLE);
														}
													}
												};

	public interface OnItemSelectedListener {
		public void onSStudySelected(Long studyId);
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
		listener.onSStudySelected(id);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		updateDetail(id);
	}

	public void setPortfolioId(long id) {
			portfolioId = id;
			//adapter.notifyDataSetChanged();
			//fillData();
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
				PaiStudyTable.COLUMN_AVG_TRUE_RANGE, PaiStudyTable.COLUMN_PRICE_DATE, PaiStudyTable.COLUMN_SMA_MONTH, PaiStudyTable.COLUMN_SMA_LAST_MONTH, PaiStudyTable.COLUMN_SMA_STDDEV_MONTH, PaiStudyTable.COLUMN_LAST_CLOSE };
		String selection = PaiStudyTable.COLUMN_PORTFOLIO_ID + " = ? ";
		String[] selectionArgs = { Long.toString(portfolioId) };
		Log.i(TAG, "Prepare Cursor Loader portfolio "+portfolioId);
		CursorLoader cursorLoader = new CursorLoader(getActivity(), PaiContentProvider.PAI_STUDY_URI, projection, selection, selectionArgs, null);
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
				study.setSmaMonth(cursor.getDouble(cursor.getColumnIndex(PaiStudyTable.COLUMN_SMA_MONTH)));
				study.setSmaLastMonth(cursor.getDouble(cursor.getColumnIndex(PaiStudyTable.COLUMN_SMA_LAST_MONTH)));
				study.setSmaStddevMonth(cursor.getDouble(cursor.getColumnIndex(PaiStudyTable.COLUMN_SMA_STDDEV_MONTH)));
				study.setLastClose(cursor.getDouble(cursor.getColumnIndex(PaiStudyTable.COLUMN_LAST_CLOSE)));
				
				try {
					study.setPriceDate(cursor.getString(cursor.getColumnIndex(PaiStudyTable.COLUMN_PRICE_DATE)));
				} catch (ParseException e) {
					Log.d(TAG, "Parse Exception Price Date", e);
				}
				// Set the Menu Image
				// ImageView
				// menuImage=(ImageView)arg0.findViewById(R.id.iv_ContactImg);
				// menuImage.setImageResource(R.drawable.ic_launcher);
				Rules rules = new SmaRules(study);
				// Set Synbol
				TextView symbol = (TextView) view.findViewById(R.id.quoteList_symbol);
				symbol.setText(study.getSymbol());
				setTrend(view, rules.isUpTrendMonthly(), R.id.quoteList_MonthyTrend);
				setTrend(view, rules.isUpTrendWeekly(), R.id.quoteList_WeeklyTrend);
				// Set EMA
				TextView ema = (TextView) view.findViewById(R.id.quoteList_ema);
				ema.setText(PaiStudy.format(study.getSmaMonth()));

				double net = study.getPrice() - study.getLastClose();
				TextView textNet = (TextView) view.findViewById(R.id.quoteList_net);
				if (net < 0 ) {
					textNet.setText(rules.formatNet(net));
					textNet.setTextColor(getResources().getColor(R.color.net_negative));
				} else {
					textNet.setText(rules.formatNet(net));
					textNet.setTextColor(getResources().getColor(R.color.net_positive));
				}
				
				// Price
				TextView price = (TextView) view.findViewById(R.id.quoteList_Price);
				price.setText(PaiStudy.format(study.getPrice()));
				TextView textBuyZoneBot = setDouble(view, rules.calcBuyZoneBottom(), R.id.quoteList_BuyZoneBottom);
				TextView textBuyZoneTop = setDouble(view, rules.calcBuyZoneTop(), R.id.quoteList_BuyZoneTop);
				if (rules.isPriceInBuyZone()) {
					textBuyZoneBot.setBackgroundColor(Color.DKGRAY);
					textBuyZoneTop.setBackgroundColor(Color.DKGRAY);
					textBuyZoneBot.setTextColor(Color.GREEN);
					textBuyZoneTop.setTextColor(Color.GREEN);
				} else if (rules.isPossibleUptrendTermination()) {
					textBuyZoneBot.setBackgroundColor(color.holo_orange_light);
					textBuyZoneTop.setBackgroundColor(color.holo_orange_light);
					textBuyZoneBot.setTextColor(Color.MAGENTA);
					textBuyZoneTop.setTextColor(Color.MAGENTA);
				} else {
					textBuyZoneBot.setBackgroundColor(color.background_light);
					textBuyZoneTop.setBackgroundColor(color.background_light);
					textBuyZoneBot.setTextColor(Color.BLACK);
					textBuyZoneTop.setTextColor(Color.BLACK);
				}

				TextView textSellZoneBot = setDouble(view, rules.calcSellZoneBottom(), R.id.quoteList_SellZoneBottom);
				TextView textSellZoneTop = setDouble(view, rules.calcSellZoneTop(), R.id.quoteList_SellZoneTop);
				if (rules.isPriceInSellZone()) {
					textSellZoneBot.setBackgroundColor(color.holo_green_dark);
					textSellZoneTop.setBackgroundColor(color.holo_green_dark);
					textSellZoneBot.setTextColor(Color.GREEN);
					textSellZoneTop.setTextColor(Color.GREEN);
				} else if (rules.isPossibleDowntrendTermination()) {
					textSellZoneBot.setBackgroundColor(color.holo_orange_light);
					textSellZoneTop.setBackgroundColor(color.holo_orange_light);
					textSellZoneBot.setTextColor(Color.MAGENTA);
					textSellZoneTop.setTextColor(Color.MAGENTA);
				} else {
					textSellZoneBot.setBackgroundColor(color.background_light);
					textSellZoneTop.setBackgroundColor(color.background_light);
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
			final View customListView = mInflator.inflate(R.layout.study_s_list_row, null);
			return customListView;
		}
	}


}
