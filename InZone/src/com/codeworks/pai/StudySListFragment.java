package com.codeworks.pai;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.db.model.Rules;
import com.codeworks.pai.db.model.SmaRules;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.processor.DateUtils;
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
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		adapter.notifyDataSetChanged();
		// Register mMessageReceiver to receive messages.
		getActivity().registerReceiver(mMessageReceiver, new IntentFilter(UpdateService.BROADCAST_UPDATE_PROGRESS_BAR));
		
	}
	  @Override
	public void onPause() {
		// Unregister since the activity is not visible
		getActivity().unregisterReceiver(mMessageReceiver);
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
	}

	private void fillData() {
		getLoaderManager().initLoader(0, null, this);
		adapter = new PaiCursorAdapter(this.getActivity());
		setListAdapter(adapter);

	}

	// Creates a new loader after the initLoader () call
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String selection = StudyTable.COLUMN_PORTFOLIO_ID + " = ? ";
		String[] selectionArgs = { Long.toString(portfolioId) };
		Log.i(TAG, "Prepare Cursor Loader portfolio "+portfolioId);
		CursorLoader cursorLoader = new CursorLoader(getActivity(), PaiContentProvider.PAI_STUDY_URI, StudyTable.getFullProjection(), selection, selectionArgs, null);
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
			Log.d(TAG,"bindView");
			if (null != cursor) {
				Study study = StudyTable.loadStudy(cursor);
				
				Rules rules = new SmaRules(study);
				// Set Symbol
				TextView symbol = (TextView) view.findViewById(R.id.quoteList_symbol);
				symbol.setText(study.getSymbol());
				// Price
				TextView price = (TextView) view.findViewById(R.id.quoteList_Price);
				price.setText(Study.format(study.getPrice()));

				if (study.isValidWeek()) {

					setTrend(view, rules.isUpTrendMonthly(), R.id.quoteList_MonthyTrend);
					setTrend(view, rules.isUpTrendWeekly(), R.id.quoteList_WeeklyTrend);
					// Set EMA
					TextView ema = (TextView) view.findViewById(R.id.quoteList_ema);
					ema.setText(Study.format(study.getSmaMonth()));
					
					double net = 0;
					Calendar cal = GregorianCalendar.getInstance();
					if ((study.getPriceDate() != null && DateUtils.isSameDay(study.getPriceDate(), new Date()))
							|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
						net = study.getPrice() - study.getLastClose();
					}
					TextView textNet = (TextView) view.findViewById(R.id.quoteList_net);
					if (net < 0) {
						textNet.setText(rules.formatNet(net));
						textNet.setTextColor(getResources().getColor(R.color.net_negative));
					} else {
						textNet.setText(rules.formatNet(net));
						textNet.setTextColor(getResources().getColor(R.color.net_positive));
					}

					if (rules.hasTradedBelowMAToday()) {
						price.setTextColor(getResources().getColor(R.color.net_negative));
					}

					//TextView textBuyZoneBot = setDouble(view, rules.calcBuyZoneBottom(), R.id.quoteList_BuyZoneBottom);
					TextView textBuyZoneTop = setDouble(view, rules.calcBuyZoneTop(), R.id.quoteList_BuyZoneTop);

					//textBuyZoneBot.setBackgroundColor(rules.getBuyZoneBackgroundColor());
					textBuyZoneTop.setBackgroundColor(rules.getBuyZoneBackgroundColor());
					//textBuyZoneBot.setTextColor(rules.getBuyZoneTextColor());
					textBuyZoneTop.setTextColor(rules.getBuyZoneTextColor());

					TextView textSellZoneBot = setDouble(view, rules.calcSellZoneBottom(), R.id.quoteList_SellZoneBottom);
					//TextView textSellZoneTop = setDouble(view, rules.calcSellZoneTop(), R.id.quoteList_SellZoneTop);

					textSellZoneBot.setBackgroundColor(rules.getSellZoneBackgroundColor());
					textSellZoneBot.setTextColor(rules.getSellZoneTextColor());
					//textSellZoneTop.setBackgroundColor(rules.getSellZoneBackgroundColor());
					//textSellZoneTop.setTextColor(rules.getSellZoneTextColor());

					TextView lastUpdated = (TextView) getActivity().findViewById(R.id.studyList_lastUpdated);
					if (study.getPriceDate() != null && lastUpdated != null) {
						lastUpdated.setText(lastUpdatedFormat.format(study.getPriceDate()));
					}
				} else {
					setText(view, "", R.id.quoteList_net);
					setText(view, "", R.id.quoteList_ema);
					setText(view, "", R.id.quoteList_BuyZoneTop);
					setText(view, "", R.id.quoteList_SellZoneBottom);
				}
			}
		}

		TextView setText(View view, String value, int viewId) {
			TextView textView = (TextView) view.findViewById(viewId);
			textView.setText(value);
			return textView;
		}
		
		TextView setDouble(View view, double value, int viewId) {
			TextView textView = (TextView) view.findViewById(viewId);
			textView.setText(Study.format(value));
			return textView;
		}

		void setTrend(View inView, boolean isUptrend, int viewId) {
			ImageView imageView = (ImageView) inView.findViewById(viewId);
			if (isUptrend) {
				imageView.setImageResource(R.drawable.ic_market_up);
			} else {
				imageView.setImageResource(R.drawable.ic_market_down);
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// Log.d("TAG", "CursorAdapter newView");
			final View customListView = mInflator.inflate(R.layout.study_s_list_row, null);
			return customListView;
		}
	}


}
