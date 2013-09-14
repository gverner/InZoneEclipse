package com.codeworks.pai.db.model;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import android.R.color;
import android.graphics.Color;
import android.util.Log;

import com.codeworks.pai.study.Period;


public abstract class RulesBase implements Rules {
	private String TAG = RulesBase.class.getName();
	protected PaiStudy	study;

	@Override
	public String formatNet(double net) {
		NumberFormat format = NumberFormat.getInstance(Locale.US);
		if (format instanceof DecimalFormat) {
			((DecimalFormat)format).applyPattern("+####.00;-####.00");
		}
		String result = format.format(net);
		return result;
	}
	
	@Override
	public boolean hasTradedBelowMAToday() {
		Log.i(TAG, study.getSymbol()+" TradBelowMA="+(isUpTrendWeekly() && study.getLow() <  study.getMovingAverage(Period.Week))+" low="+study.getLow()+" ma="+study.getMovingAverage(Period.Week) + " maTyp="+study.getMaType().name() +" Id="+ study.getSecurityId());
		return isUpTrendWeekly() && study.getLow() > 0 && study.getLow() <  study.getMovingAverage(Period.Week);
	}
	
	@Override
	public int getBuyZoneTextColor() {
		if (isPriceInBuyZone()) {
			return Color.GREEN;
		} else if (isPossibleUptrendTermination(Period.Week)) {
			return Color.MAGENTA;
		} else {
			return Color.BLACK;
		}
	}
	
	@Override
	public int getBuyZoneBackgroundColor() {
		if (isPriceInBuyZone()) {
			return Color.DKGRAY;
		} else if (isPossibleUptrendTermination(Period.Week)) {
			return color.holo_orange_light;
		} else {
			return color.background_light;
		}
	}

	@Override
	public int getSellZoneTextColor() {
		if (isPriceInSellZone()) {
			return Color.GREEN;
		} else if (isPossibleDowntrendTermination(Period.Week)) {
			return Color.MAGENTA;
		} else {
			return Color.BLACK;
		}

	}

	@Override
	public int getSellZoneBackgroundColor() {
		if (isPriceInSellZone()) {
			return color.holo_green_dark;
		} else if (isPossibleDowntrendTermination(Period.Week)) {
			return color.holo_orange_light;
		} else {
			return color.background_light;
		}

	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#isPossibleUptrendTermination()
	 */
	@Override
	public boolean isPossibleUptrendTermination(Period period) {
		if (Period.Week.equals(period)) {
			return (isUpTrendWeekly() && study.getPrice() < study.getMaWeek()); 
		} else {
			return (isUpTrendMonthly() && study.getPrice() < study.getMaMonth()); 
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#isPossibleDowntrendTermination()
	 */
	@Override
	public boolean isPossibleDowntrendTermination(Period period) {
		if (Period.Week.equals(period)) {
			return (isDownTrendWeekly() && study.getPrice() > study.getMaWeek());
		} else {
			return (isDownTrendMonthly() && study.getPrice() > study.getMaMonth());
		}
	}

}
