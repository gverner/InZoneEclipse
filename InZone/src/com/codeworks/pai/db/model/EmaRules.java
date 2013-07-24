package com.codeworks.pai.db.model;

import com.codeworks.pai.study.Period;

public class EmaRules extends RulesBase {

	protected static double	ZONE_INNER	= 0.5d;
	protected static double	ZONE_OUTER	= 2d;

	public EmaRules(PaiStudy study) {
		this.study = study;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.codeworks.pai.db.model.Rules#calcUpperSellZoneTop(com.codeworks.pai
	 * .study.Period)
	 */
	@Override
	public double calcUpperSellZoneTop(Period period) {
		return calcUpperSellZoneBottom(period) + pierceOffset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.codeworks.pai.db.model.Rules#calcUpperSellZoneBottom(com.codeworks
	 * .pai.study.Period)
	 */
	@Override
	public double calcUpperSellZoneBottom(Period period) {
		if (Period.Week.equals(period)) {
			return study.getMaWeek() + (study.getStddevWeek() * ZONE_OUTER);
		} else {
			return study.getMaMonth() + (study.getStddevMonth() * ZONE_OUTER);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.codeworks.pai.db.model.Rules#calcUpperBuyZoneTop(com.codeworks.pai
	 * .study.Period)
	 */
	@Override
	public double calcUpperBuyZoneTop(Period period) {
		if (Period.Week.equals(period)) {
			return study.getMaWeek() + (study.getStddevWeek() * ZONE_INNER);
		} else {
			return study.getMaMonth() + (study.getStddevMonth() * ZONE_INNER);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.codeworks.pai.db.model.Rules#calcUpperBuyZoneBottom(com.codeworks
	 * .pai.study.Period)
	 */
	@Override
	public double calcUpperBuyZoneBottom(Period period) {
		if (Period.Week.equals(period)) {
			return study.getMaWeek();
		} else {
			return study.getMaMonth();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.codeworks.pai.db.model.Rules#calcLowerSellZoneTop(com.codeworks.pai
	 * .study.Period)
	 */
	@Override
	public double calcLowerSellZoneTop(Period period) {
		if (Period.Week.equals(period)) {
			return study.getMaWeek();
		} else {
			return study.getMaMonth();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.codeworks.pai.db.model.Rules#calcLowerSellZoneBottom(com.codeworks
	 * .pai.study.Period)
	 */
	@Override
	public double calcLowerSellZoneBottom(Period period) {
		if (Period.Week.equals(period)) {
			return study.getMaWeek() - (study.getStddevWeek() * ZONE_INNER);
		} else {
			return study.getMaMonth() - (study.getStddevMonth() * ZONE_INNER);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.codeworks.pai.db.model.Rules#calcLowerBuyZoneTop(com.codeworks.pai
	 * .study.Period)
	 */
	@Override
	public double calcLowerBuyZoneTop(Period period) {
		if (Period.Week.equals(period)) {
			return study.getMaWeek() - (study.getStddevWeek() * ZONE_OUTER);
		} else {
			return study.getMaMonth() - (study.getStddevMonth() * ZONE_OUTER);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.codeworks.pai.db.model.Rules#calcLowerBuyZoneBottom(com.codeworks
	 * .pai.study.Period)
	 */
	@Override
	public double calcLowerBuyZoneBottom(Period period) {
		return calcLowerBuyZoneTop(period) - pierceOffset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#calcBuyZoneBottom()
	 */
	@Override
	public double calcBuyZoneBottom() {
		if (study.getMaWeek() == Double.NaN || study.getStddevWeek() == Double.NaN) {
			return 0;
		}
		if (isUpTrendWeekly()) {
			return study.getMaWeek();
		} else {
			return study.getMaWeek() - (study.getStddevWeek() * ZONE_OUTER) - pierceOffset();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#calcBuyZoneTop()
	 */
	@Override
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#calcSellZoneBottom()
	 */
	@Override
	public double calcSellZoneBottom() {
		if (study.getMaWeek() == Double.NaN || study.getStddevWeek() == Double.NaN) {
			return 0;
		}
		if (isUpTrendWeekly()) {
			return study.getMaWeek() + (study.getStddevWeek() * ZONE_OUTER);
		} else {
			return study.getMaWeek() - (study.getStddevWeek() * ZONE_INNER);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#calcSellZoneTop()
	 */
	@Override
	public double calcSellZoneTop() {
		if (study.getMaWeek() == Double.NaN || study.getStddevWeek() == Double.NaN) {
			return 0;
		}
		if (isUpTrendWeekly()) {
			return study.getMaWeek() + (study.getStddevWeek() * ZONE_OUTER) + pierceOffset();
		} else {
			return study.getMaWeek();
		}
	}

	double pierceOffset() {
		return (study.getPrice() / 100d) * 2d;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#isPriceInBuyZone()
	 */
	@Override
	public boolean isPriceInBuyZone() {
		return (study.getPrice() >= calcBuyZoneBottom() && study.getPrice() <= calcBuyZoneTop());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#isPriceInSellZone()
	 */
	@Override
	public boolean isPriceInSellZone() {
		return (study.getPrice() >= calcSellZoneBottom());// && price <=
		// calcSellZoneTop());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#isUpTrendWeekly()
	 */
	@Override
	public boolean isUpTrendWeekly() {
		return study.getMaLastWeek() <= study.getPriceLastWeek();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#isUpTrendMonthly()
	 */
	@Override
	public boolean isUpTrendMonthly() {
		return study.getMaLastMonth() <= study.getPriceLastMonth();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.codeworks.pai.db.model.Rules#isUpTrend(com.codeworks.pai.study.Period
	 * )
	 */
	@Override
	public boolean isUpTrend(Period period) {
		if (Period.Month.equals(period)) {
			return study.getMaLastMonth() <= study.getPriceLastMonth();
		} else {
			return study.getMaLastWeek() <= study.getPriceLastWeek();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#isDownTrendWeekly()
	 */
	@Override
	public boolean isDownTrendWeekly() {
		return study.getMaLastWeek() > study.getPriceLastWeek();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#isDownTrendMonthly()
	 */
	@Override
	public boolean isDownTrendMonthly() {
		return study.getMaLastMonth() > study.getPriceLastMonth();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#isPossibleTrendTerminationWeekly()
	 */
	@Override
	public boolean isPossibleTrendTerminationWeekly() {
		return isPossibleDowntrendTermination() || isPossibleUptrendTermination();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#isPossibleUptrendTermination()
	 */
	@Override
	public boolean isPossibleUptrendTermination() {
		return (isUpTrendWeekly() && study.getPrice() < study.getMaWeek());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#isPossibleDowntrendTermination()
	 */
	@Override
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
		String rule = "";
		if (isUpTrendWeekly()) {
			if (isPossibleUptrendTermination()) {
				rule = "Place Stop Buy Order at moving average + 1/4 Averate True Range(ATR)";
			} else if (isPriceInBuyZone()) {
				double buyZoneTop = calcBuyZoneTop();
				double AOBBUY = Math.floor(buyZoneTop);
				rule = "C: Sell Puts in Buy Zone AOB " + Double.toString(AOBBUY) + "\n A: Buy Stock";
			} else if (isPriceInSellZone()) {
				double buyZoneTop = calcBuyZoneTop();
				double AOBBUY = Math.floor(buyZoneTop);
				rule = "C: Sell Puts in Buy Zone AOB " + Double.toString(AOBBUY);
			} else {
				double buyZoneTop = calcBuyZoneTop();
				double AOBBUY = Math.floor(buyZoneTop);
				rule = "Sell puts in the Buy Zone AOB " + Double.toString(AOBBUY);
			}
		} else { // Weekly DownTrend
			if (isUpTrendMonthly()) {
				if (isPossibleDowntrendTermination()) {
					rule = "Wait for Weekly Close above moving average";
				} else if (isPriceInBuyZone()) {
					double buyZoneTop = calcBuyZoneTop();
					double AOBBUY = Math.floor(buyZoneTop);
					rule = "C: Sell Puts in Buy Zone AOB " + Double.toString(AOBBUY) + "\n A: Buy Stock";
				} else if (isPriceInSellZone()) {
					rule = "Wait for Weekly Close above moving average";
				} else {
					double buyZoneTop = calcBuyZoneTop();
					double AOBBUY = Math.floor(buyZoneTop);
					rule = "Sell puts in the Buy Zone AOB " + Double.toString(AOBBUY);
				}
			} else { // Monthly DownTrend
				if (isPossibleDowntrendTermination()) {
					rule = "Wait for Weekly Close above moving average";
				} else if (isPriceInSellZone()) {
					rule = "Wait for Weekly Close above moving average";
				} else if (isPriceInBuyZone()) {
					if (study.getPrice() <= study.getMaLastMonth()) {
						rule = "C: Sell Puts at Proximal demand level (PDL)";
					} else {
						double buyZoneTop = calcBuyZoneTop();
						double AOBBUY = Math.floor(buyZoneTop);
						rule = "Sell puts in the Buy Zone AOB " + Double.toString(AOBBUY);
					}
				}

			}
		}
		return rule;
	}

	@Override
	public String inCashAndPut() {
		String rule = "";
		if (isUpTrendWeekly()) {
			if (isPossibleUptrendTermination()) {
				rule = "Buy back Put nad Place Stop Buy Order at moving average + 1/4 Averate True Range(ATR)";
			} else if (isPriceInBuyZone()) {
				rule = "C: Going For the Ride \n A: Buy Back Put and Buy Stock";
			} else if (isPriceInSellZone()) {
				rule = "Going for the Ride ";
			} else {
				rule = "Going for the Ride ";
			}
		} else { // Weekly DownTrend
			if (isUpTrendMonthly()) {
				if (isPossibleDowntrendTermination()) {
					rule = "Wait for Weekly Close above moving average";
				} else if (isPriceInBuyZone()) {
					rule = "C: Going for the Ride \n A: Buy Stock";
				} else if (isPriceInSellZone()) {
					rule = "Wait for Weekly Close above moving average";
				} else {
					rule = "Going for the Ride ";
				}
			} else { // Monthly DownTrend
				if (isPossibleDowntrendTermination()) {
					rule = "Wait for Weekly Close above moving average";
				} else if (isPriceInSellZone()) {
					rule = "Wait for Weekly Close above moving average";
				} else if (isPriceInBuyZone()) {
					if (study.getPrice() <= study.getMaLastMonth()) {
						rule = "Buy back Puts and Role to Puts at Proximal demand level (PDL)";
					} else {
						rule = "Going for the Ride";
					}
				}

			}
		}
		return rule;
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
