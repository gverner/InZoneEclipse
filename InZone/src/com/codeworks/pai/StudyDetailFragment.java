package com.codeworks.pai;

import android.app.Fragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.PaiStudyTable;
import com.codeworks.pai.db.model.MaType;
import com.codeworks.pai.db.model.PaiStudy;
import com.codeworks.pai.study.Period;

public class StudyDetailFragment extends Fragment {
	private static final String TAG = StudyDetailFragment.class.getSimpleName();

	@Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.study_detail_fragment,
	        container, false);
	    return view;
	  }
	  
	  public void setId(long id) {
		  fillData(id);
	  }
	  
	  public void setText(String item) {
		  if (item != null) {
			  fillData(Long.parseLong(item));
		  }
		  /*
	    TextView view = (TextView) getView().findViewById(R.id.tempStudyDetailsText);
	    view.setText(item);
	    */
	  }
		private void fillData(Long id) {
			String[] projection = { PaiStudyTable.COLUMN_ID, PaiStudyTable.COLUMN_SYMBOL, PaiStudyTable.COLUMN_NAME, PaiStudyTable.COLUMN_MA_TYPE, PaiStudyTable.COLUMN_MA_WEEK, PaiStudyTable.COLUMN_MA_MONTH,
					PaiStudyTable.COLUMN_MA_LAST_WEEK, PaiStudyTable.COLUMN_MA_LAST_MONTH, PaiStudyTable.COLUMN_PRICE, PaiStudyTable.COLUMN_PRICE_LAST_WEEK,
					PaiStudyTable.COLUMN_PRICE_LAST_MONTH, PaiStudyTable.COLUMN_STDDEV_WEEK, PaiStudyTable.COLUMN_STDDEV_MONTH,
					PaiStudyTable.COLUMN_AVG_TRUE_RANGE, PaiStudyTable.COLUMN_PRICE_DATE };
			
			Uri uri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + id);
			Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
			if (cursor != null) try {
				cursor.moveToFirst();
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

			    ((TextView) getView().findViewById(R.id.sdfSymbol)).setText(symbol);
			    ((TextView) getView().findViewById(R.id.sdfName)).setText(security.getName());
				populateView(security);
		
			    //mSymbolText.setText(symbol);
			} catch (Exception e) {
				Log.e(TAG,"Exception reading Study from db ",e);
			} finally {
				// Always close the cursor
				cursor.close();
			}
		}
		
		void populateView(PaiStudy study) {
			setDouble(getView(), study.getPrice(), R.id.sdfPrice);
			setDouble(getView(), study.getAverageTrueRange() / 4, R.id.sdfAtr25);
			setDouble(getView(), study.getMaWeek() + (study.getAverageTrueRange() / 4), R.id.sdfPricePlusAtr25);
			setDouble(getView(), study.calcUpperSellZoneTop(Period.Week), R.id.sdfWeeklyUpperSellTop);
			setDouble(getView(), study.calcUpperSellZoneBottom(Period.Week), R.id.sdfWeeklyUpperSellBottom);
			setDouble(getView(), study.calcUpperBuyZoneTop(Period.Week), R.id.sdfWeeklyUpperBuyTop);
			setDouble(getView(), study.calcUpperBuyZoneBottom(Period.Week), R.id.sdfMaWeekly);
			setDouble(getView(), study.calcLowerSellZoneBottom(Period.Week), R.id.sdfWeeklyLowerSellBottom);
			setDouble(getView(), study.calcLowerBuyZoneTop(Period.Week), R.id.sdfWeeklyLowerBuyTop);
			setDouble(getView(), study.calcLowerBuyZoneBottom(Period.Week), R.id.sdfWeeklyLowerBuyBottom);

			setDouble(getView(), study.calcUpperSellZoneTop(Period.Month), R.id.sdfMonthlyUpperSellTop);
			setDouble(getView(), study.calcUpperSellZoneBottom(Period.Month), R.id.sdfMonthlyUpperSellBottom);
			setDouble(getView(), study.calcUpperBuyZoneTop(Period.Month), R.id.sdfMonthlyUpperBuyTop);
			setDouble(getView(), study.calcUpperBuyZoneBottom(Period.Month), R.id.sdfMaMonthly);
			setDouble(getView(), study.calcLowerSellZoneBottom(Period.Month), R.id.sdfMonthlyLowerSellBottom);
			setDouble(getView(), study.calcLowerBuyZoneTop(Period.Month), R.id.sdfMonthlyLowerBuyTop);
			setDouble(getView(), study.calcLowerBuyZoneBottom(Period.Month), R.id.sdfMonthlyLowerBuyBottom);
			
		}
		
		TextView setDouble(View view, double value, int viewId) {
			TextView textView = (TextView) view.findViewById(viewId);
			textView.setText(PaiStudy.format(value));
			return textView;
		}

}
