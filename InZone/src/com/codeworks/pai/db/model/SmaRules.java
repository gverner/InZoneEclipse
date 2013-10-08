package com.codeworks.pai.db.model;

import android.R.color;
import android.content.res.Resources;
import android.graphics.Color;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.processor.Notice;
import com.codeworks.pai.study.Period;

public class SmaRules  extends RulesBase {
	public static double				ZONE_INNER			= 0.5d;
	public static double				ZONE_OUTER			= 2d;

	public SmaRules(PaiStudy study) {
		this.study = study;
	}

	public double calcUpperSellZoneTop(Period period) {
		return calcUpperSellZoneBottom(period) + pierceOffset();
	}

	public double calcUpperSellZoneBottom(Period period) {
		if (Period.Week.equals(period)) {
			return study.getSmaWeek() + (study.getSmaStddevWeek() * ZONE_OUTER);
		} else {
			return study.getSmaMonth() + (study.getSmaStddevMonth() * ZONE_OUTER);
		}
	}

	public double calcUpperBuyZoneTop(Period period) {
		if (Period.Week.equals(period)) {
			return study.getSmaWeek() + (study.getSmaStddevWeek() * ZONE_INNER);
		} else {
			return study.getSmaMonth() + (study.getSmaStddevMonth() * ZONE_INNER);
		}
	}

	public double calcUpperBuyZoneBottom(Period period) {
		if (Period.Week.equals(period)) {
			return study.getSmaWeek();
		} else {
			return study.getSmaMonth();
		}
	}

	public double calcLowerSellZoneTop(Period period) {
		if (Period.Week.equals(period)) {
			return study.getSmaWeek();
		} else {
			return study.getSmaMonth();
		}
	}

	public double calcLowerSellZoneBottom(Period period) {
		if (Period.Week.equals(period)) {
			return study.getSmaWeek() - (study.getSmaStddevWeek() * ZONE_INNER);
		} else {
			return study.getSmaMonth() - (study.getSmaStddevMonth() * ZONE_INNER);
		}
	}

	public double calcLowerBuyZoneTop(Period period) {
		if (Period.Week.equals(period)) {
			return study.getSmaWeek() - (study.getSmaStddevWeek() * ZONE_OUTER);
		} else {
			return study.getSmaMonth() - (study.getSmaStddevMonth() * ZONE_OUTER);
		}
	}

	public double calcLowerBuyZoneBottom(Period period) {
		return calcLowerBuyZoneTop(period) - pierceOffset();
	}

	public double calcBuyZoneBottom() {
		if (study.getSmaWeek() == Double.NaN || study.getSmaStddevMonth() == Double.NaN) {
			return 0;
		}
		return study.getSmaMonth();
	}

	public double calcBuyZoneTop() {
		if (study.getSmaWeek() == Double.NaN || study.getSmaStddevWeek() == Double.NaN) {
			return 0;
		}
		return study.getSmaWeek();
	}

	public double calcSellZoneBottom() {
		return calcUpperSellZoneBottom(Period.Month);
	}

	public double calcSellZoneTop() {
		if (study.getSmaMonth() == Double.NaN || study.getSmaStddevWeek() == Double.NaN) {
			return 0;
		}
		return calcUpperSellZoneTop(Period.Month);
	}

	double pierceOffset() {
		return (study.getPrice() / 100d) * 2d;
	}

	public boolean isPriceInBuyZone() {
		return (study.getPrice() >= calcBuyZoneBottom() && study.getPrice() <= calcBuyZoneTop());
	}

	public boolean isPriceInSellZone() {
		return (study.getPrice() >= calcSellZoneBottom());// && price <=
												// calcSellZoneTop());
	}

	public boolean isUpTrend(Period period) {
		if (Period.Month.equals(period)) {
			return study.getSmaMonth() <= study.getPrice();
		} else {
			return study.getSmaLastWeek() <= study.getPriceLastWeek();
		}
	}

	public boolean isPossibleTrendTerminationWeekly() {
		return isPossibleDowntrendTermination(Period.Week) || isPossibleUptrendTermination(Period.Week);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Symbol=");
		sb.append(study.getSymbol());
		sb.append(" ema=");
		sb.append(PaiStudy.format(study.getMaWeek()));
		sb.append(" buy zone bottom=");
		sb.append(PaiStudy.format(this.calcBuyZoneBottom()));
		sb.append(" top=");
		sb.append(PaiStudy.format(this.calcBuyZoneTop()));
		sb.append(" sell zone bottom=");
		sb.append(PaiStudy.format(this.calcSellZoneBottom()));
		sb.append(" top=");
		sb.append(PaiStudy.format(this.calcSellZoneTop()));
		sb.append(" WUT=" + isUpTrendWeekly());
		sb.append(" MUT=" + isUpTrendMonthly());
		sb.append(" PLW=" + PaiStudy.format(study.getPriceLastWeek()));
		sb.append(" maLM=" + PaiStudy.format(study.getMaLastMonth()));
		sb.append(" PLM=" + PaiStudy.format(study.getPriceLastMonth()));
		return sb.toString();
	}
	
	@Override
	public boolean hasTradedBelowMAToday() {
		return isUpTrendMonthly() && study.getLow() > 0 && study.getLow() <  study.getMovingAverage(Period.Month);
	}

	@Override
	public int getBuyZoneTextColor() {
		if (isPriceInBuyZone()) {
			return Color.GREEN;
		} else if (isPossibleUptrendTermination(Period.Month)) {
			return Color.MAGENTA;
		} else {
			return Color.BLACK;
		}
	}
	
	@Override
	public int getBuyZoneBackgroundColor() {
		if (isPriceInBuyZone()) {
			return Color.DKGRAY;
		} else if (isPossibleUptrendTermination(Period.Month)) {
			return color.holo_orange_light;
		} else {
			return color.background_light;
		}
	}

	@Override
	public int getSellZoneTextColor() {
		if (isPriceInSellZone()) {
			return Color.GREEN;
		} else if (isPossibleDowntrendTermination(Period.Month)) {
			return Color.MAGENTA;
		} else {
			return Color.BLACK;
		}

	}

	@Override
	public int getSellZoneBackgroundColor() {
		if (isPriceInSellZone()) {
			return color.holo_green_dark;
		} else if (isPossibleDowntrendTermination(Period.Month)) {
			return color.holo_orange_light;
		} else {
			return color.background_light;
		}

	}
	
	@Override
	public StringBuilder getAdditionalAlerts(Resources res) {
		return super.getAdditionalAlerts(res);
	}

	@Override
	public void updateNotice() {
		if (isPossibleDowntrendTermination(Period.Month)) {
			study.setNotice(Notice.POSSIBLE_WEEKLY_DOWNTREND_TERMINATION);
		} else if (isPossibleUptrendTermination(Period.Month)) {
			study.setNotice(Notice.POSSIBLE_WEEKLY_UPTREND_TEMINATION);
		} else {
			study.setNotice(Notice.NONE);
		}
	}
	
	@Override
	public String inCash() {
		String rule = "";
		if (isUpTrendMonthly()) {
			double buyZoneTop = calcBuyZoneTop();
			double AOBBUY = PaiUtils.round(Math.floor(buyZoneTop), 0);
			if (isPossibleUptrendTermination(Period.Month)) {
				rule = "Wait for Monthly Close to determine Termination or Confirmation of Trend";
			} else {
				rule = "Sell puts in the Buy Zone AOB " + Double.toString(AOBBUY) + "p";
			}
		} else { // Monthly DownTrend
			if (isPossibleDowntrendTermination(Period.Month)) {
				rule = "Wait for Monthly Close above moving average";
			} else {
				rule = "Sell Puts at Proximal demand level (PDL)";
			}
		}
		return rule;
	}

	@Override
	public String inCashAndPut() {
		String rule = "";
		if (isUpTrendMonthly()) {
			if (isPossibleUptrendTermination(Period.Month)) {
				rule = "Wait for Monthly Close to determine Termination or Confirmation of Trend";
			} else {
				rule = "Going for the Ride";
			}
		} else { // Monthly DownTrend
			if (isPossibleDowntrendTermination(Period.Month)) {
				rule = "Wait for Monthly Close above moving average";
			} else {
				rule = "If Necessary Roll Puts to Proximal Demand Level (PDL)";
			}
		}
		return rule;
	}

	@Override
	public String inStock() {
		String rule = "";
		if (isUpTrendMonthly()) {
			if (isPossibleUptrendTermination(Period.Month)) {
				rule = "Be ready to Sell Stock if Monthly close confirms a Down Trend";
			} else if (isPriceInBuyZone()) {
				rule = "Sell Calls in the Sell Zone";
			} else if (isPriceInSellZone()) {
				rule = "C: Sell Stock\nA: Place Stop Loss order at bottom of lower Sell Zone " + PaiUtils.round(calcSellZoneBottom());
			} else {
				rule = "Sell Calls in the Sell Zone";
			}
		} else { // Monthly DownTrend
			if (isPossibleDowntrendTermination(Period.Month)) {
				rule = "Wait for Monthly Close to determine Termination or Confirmation of Trend";
			} else {
				rule = "Sell Stock and Sell Puts at Proximal demand level (PDL)";
			}
		}
		return rule;
	}

	@Override
	public String inStockAndCall() {
		String rule = "";
		if (isUpTrendMonthly()) {
			if (isPossibleUptrendTermination(Period.Month)) {
				rule = "Wait for Monthly Close to determine Termination or Confirmation of Trend";
			} else if (isPriceInSellZone()) {
				rule = "C: Buy Back Calls and Sell Stock\nA: Place Stop Loss order at bottom of lower Sell Zone at " + PaiUtils.round(calcSellZoneBottom())
						+ " to Buy Back Calls and Sell Stock";
			} else {
				rule = "Going for the Ride";
			}
		} else { // Monthly DownTrend
			if (isPossibleDowntrendTermination(Period.Month)) {
				rule = "Wait for Monthly Close to determine Termination or Confirmation of Trend";
			} else {
				rule = "Buy Back Calls, Sell Stock and Sell Puts at Proximal demand level (PDL)";
			}

		}
		return rule;
	}

	@Override
	public boolean isWeeklyUpperSellZoneExpandedByMonthly() {
		return false;
	}

	@Override
	public boolean isWeeklyLowerBuyZoneCompressedByMonthly() {
		return false;
	}

	@Override
	public MaType getMaType() {
		return MaType.S;
	}

}
