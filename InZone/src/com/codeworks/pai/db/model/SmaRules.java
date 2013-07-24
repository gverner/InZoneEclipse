package com.codeworks.pai.db.model;

import com.codeworks.pai.study.Period;

public class SmaRules  extends RulesBase {
	protected static double				ZONE_INNER			= 0.5d;
	protected static double				ZONE_OUTER			= 2d;

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 5444507227900171845L;
	
	public SmaRules(PaiStudy study) {
		this.study = study;
	}

	public double calcUpperSellZoneTop(Period period) {
		return calcUpperSellZoneBottom(period) + pierceOffset();
	}

	public double calcUpperSellZoneBottom(Period period) {
		if (Period.Week.equals(period)) {
			return study.getMaWeek() + (study.getStddevWeek() * ZONE_OUTER);
		} else {
			return study.getSmaMonth() + (study.getSmaStddevMonth() * ZONE_OUTER);
		}
	}

	public double calcUpperBuyZoneTop(Period period) {
		if (Period.Week.equals(period)) {
			return study.getMaWeek() + (study.getStddevWeek() * ZONE_INNER);
		} else {
			return study.getSmaMonth() + (study.getSmaStddevMonth() * ZONE_INNER);
		}
	}

	public double calcUpperBuyZoneBottom(Period period) {
		if (Period.Week.equals(period)) {
			return study.getMaWeek();
		} else {
			return study.getSmaMonth();
		}
	}

	public double calcLowerSellZoneTop(Period period) {
		if (Period.Week.equals(period)) {
			return study.getMaWeek();
		} else {
			return study.getSmaMonth();
		}
	}

	public double calcLowerSellZoneBottom(Period period) {
		if (Period.Week.equals(period)) {
			return study.getMaWeek() - (study.getStddevWeek() * ZONE_INNER);
		} else {
			return study.getSmaMonth() - (study.getSmaStddevMonth() * ZONE_INNER);
		}
	}

	public double calcLowerBuyZoneTop(Period period) {
		if (Period.Week.equals(period)) {
			return study.getMaWeek() - (study.getStddevWeek() * ZONE_OUTER);
		} else {
			return study.getSmaMonth() - (study.getSmaStddevMonth() * ZONE_OUTER);
		}
	}

	public double calcLowerBuyZoneBottom(Period period) {
		return calcLowerBuyZoneTop(period) - pierceOffset();
	}

	public double calcBuyZoneBottom() {
		if (study.getSmaMonth() == Double.NaN || study.getSmaStddevMonth() == Double.NaN) {
			return 0;
		}
		if (isUpTrendWeekly()) {
			return study.getSmaMonth();
		} else {
			return study.getSmaMonth() - (study.getSmaStddevMonth() * ZONE_OUTER) - pierceOffset();
		}
	}

	public double calcBuyZoneTop() {
		if (study.getMaWeek() == Double.NaN || study.getStddevWeek() == Double.NaN) {
			return 0;
		}
		if (isUpTrendWeekly()) {
			return study.getMaWeek() + (study.getStddevWeek() * ZONE_INNER);
		} else {
			return study.getMaWeek() - (study.getStddevWeek() * ZONE_OUTER);
		}
	}

	public double calcSellZoneBottom() {
		if (study.getSmaMonth() == Double.NaN || study.getSmaStddevMonth() == Double.NaN) {
			return 0;
		}
		if (isUpTrendWeekly()) {
			return study.getSmaMonth() + (study.getSmaStddevMonth() * ZONE_OUTER);
		} else {
			return study.getSmaMonth() - (study.getSmaStddevMonth() * ZONE_INNER);
		}
	}

	public double calcSellZoneTop() {
		if (study.getSmaMonth() == Double.NaN || study.getSmaStddevMonth() == Double.NaN) {
			return 0;
		}
		if (isUpTrendWeekly()) {
			return study.getSmaMonth() + (study.getSmaStddevMonth() * ZONE_OUTER) + pierceOffset();
		} else {
			return study.getSmaMonth();
		}
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

	public boolean isUpTrendWeekly() {
		return study.getMaLastWeek() <= study.getPriceLastWeek();
	}

	public boolean isUpTrendMonthly() {
		return study.getMaLastMonth() <= study.getPriceLastMonth();
	}

	public boolean isUpTrend(Period period) {
		if (Period.Month.equals(period)) {
			return study.getSmaLastMonth() <= study.getPriceLastMonth();
		} else {
			return study.getMaLastWeek() <= study.getPriceLastWeek();
		}
	}

	public boolean isDownTrendWeekly() {
		return study.getMaLastWeek() > study.getPriceLastWeek();
	}

	public boolean isDownTrendMonthly() {
		return study.getSmaLastMonth() > study.getPriceLastMonth();
	}

	public boolean isPossibleTrendTerminationWeekly() {
		return isPossibleDowntrendTermination() || isPossibleUptrendTermination();
	}

	public boolean isPossibleUptrendTermination() {
		return (isUpTrendWeekly() && study.getPrice() < study.getMaWeek());
	}

	public boolean isPossibleDowntrendTermination() {
		return (isDownTrendWeekly() && study.getPrice() > study.getMaWeek());
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
	public String inCash() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String inCashAndPut() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String inStock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String inStockAndCall() {
		// TODO Auto-generated method stub
		return null;
	}
}
