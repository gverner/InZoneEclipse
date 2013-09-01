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
//		Log.i(TAG, study.getSymbol()+" hasTradedBelowMAToday="+(isUpTrendWeekly() && study.getLow() <  study.getMovingAverage(Period.Week))+" low="+study.getLow()+" ma="+study.getMovingAverage(Period.Week));
		return isUpTrendWeekly() && study.getLow() > 0 && study.getLow() <  study.getMovingAverage(Period.Week);
	}
	
	@Override
	public int getBuyZoneTextColor() {
		if (isPriceInBuyZone()) {
			return Color.GREEN;
		} else if (isPossibleUptrendTermination()) {
			return Color.MAGENTA;
		} else {
			return Color.BLACK;
		}
	}
	
	@Override
	public int getBuyZoneBackgroundColor() {
		if (isPriceInBuyZone()) {
			return Color.DKGRAY;
		} else if (isPossibleUptrendTermination()) {
			return color.holo_orange_light;
		} else {
			return color.background_light;
		}
	}

	@Override
	public int getSellZoneTextColor() {
		if (isPriceInSellZone()) {
			return Color.GREEN;
		} else if (isPossibleDowntrendTermination()) {
			return Color.MAGENTA;
		} else {
			return Color.BLACK;
		}

	}

	@Override
	public int getSellZoneBackgroundColor() {
		if (isPriceInSellZone()) {
			return color.holo_green_dark;
		} else if (isPossibleDowntrendTermination()) {
			return color.holo_orange_light;
		} else {
			return color.background_light;
		}

	}

	@Override
	public String getAlertText() {
		StringBuilder alert = new StringBuilder();
		if (hasTradedBelowMAToday()) {
			alert.append("has recently traded below moving average check stop loss selling");
		}
		if (isPriceInSellZone()) {
			if (alert.length() > 0) {
				alert.append("\n");
			}
			alert.append("Price is in the Sell Zone");
		}
		if (isPriceInBuyZone()) {
			if (alert.length() > 0) {
				alert.append("\n");
			}
			alert.append("Price is in the Buy Zone");
		}
		if (isPossibleTrendTerminationWeekly()) {
			if (alert.length() > 0) {
				alert.append("\n");
			}
			alert.append("Possible Weekly Trend Termination");
		}
		if (isWeeklyUpperSellZoneExpandedByMonthly()) {
			if (alert.length() > 0) {
				alert.append("\n");
			}
			alert.append("Sell Zone is expanded by lower Monthly Range");
		}
		return alert.toString();
	}
}
