package com.codeworks.pai.db.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import com.codeworks.pai.db.PaiStudyTable;
import com.codeworks.pai.processor.Notice;
import com.codeworks.pai.study.Period;

public class PaiStudy implements Serializable {

	private static final long	serialVersionUID	= -1275103175237753227L;

	static double				ZONE_INNER			= 0.5d;
	static double				ZONE_OUTER			= 2d;

	String						symbol;
	String						name;
	double						price;
	Date						priceDate;
	double						open;
	double						high;
	double						low;
	double						priceLastWeek;
	double						priceLastMonth;
	MaType						maType;
	double						maMonth;
	double						maWeek;
	double						maLastWeek;
	double						maLastMonth;
	double						stddevWeek;
	double						stddevMonth;
	double						smaMonth;
	double						smaLastMonth;
	double						s_stddevMonth;
	double						averageTrueRange;
	long						securityId;
	long						portfolioId;
	Notice						notice;
	Date						noticeDate;

	public double getStddevWeek() {
		return stddevWeek;
	}

	public void setStddevWeek(double stdDeviation) {
		this.stddevWeek = stdDeviation;
	}

	public double getStddevMonth() {
		return stddevMonth;
	}

	public void setStddevMonth(double stddev_month) {
		this.stddevMonth = stddev_month;
	}

	public PaiStudy(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public Date getPriceDate() {
		return priceDate;
	}

	public void setPriceDate(Date priceDate) {
		this.priceDate = priceDate;
	}

	public void setPriceDate(String priceDateStr) throws ParseException {
		if (priceDateStr == null) {
			this.priceDate = null;
		} else {
			this.priceDate = PaiStudyTable.priceDateFormat.parse(priceDateStr);
		}
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public MaType getMaType() {
		return maType;
	}

	public void setMaType(MaType maType) {
		this.maType = maType;
	}

	public double getMaMonth() {
		return maMonth;
	}

	public void setMaMonth(double ma_month) {
		this.maMonth = ma_month;
	}

	public double getPriceLastWeek() {
		return priceLastWeek;
	}

	public void setPriceLastWeek(double priceLastWeek) {
		this.priceLastWeek = priceLastWeek;
	}

	public double getPriceLastMonth() {
		return priceLastMonth;
	}

	public void setPriceLastMonth(double priceLastMonth) {
		this.priceLastMonth = priceLastMonth;
	}

	public double getMaWeek() {
		return maWeek;
	}

	public void setMaWeek(double maWeek) {
		this.maWeek = maWeek;
	}

	public double getMaLastWeek() {
		return maLastWeek;
	}

	public void setMaLastWeek(double maLastWeek) {
		this.maLastWeek = maLastWeek;
	}

	public double getMaLastMonth() {
		return maLastMonth;
	}

	public void setMaLastMonth(double maLastMonth) {
		this.maLastMonth = maLastMonth;
	}

	public double getAverageTrueRange() {
		return averageTrueRange;
	}

	public void setAverageTrueRange(double averageTrueRange) {
		this.averageTrueRange = averageTrueRange;
	}
	
	public double calcUpperSellZoneTop(Period period) {
		return calcUpperSellZoneBottom(period) + pierceOffset();
	}

	public double calcUpperSellZoneBottom(Period period) {
		if (Period.Week.equals(period)) {
			return maWeek + (stddevWeek * ZONE_OUTER);
		} else {
			return maMonth + (stddevMonth * ZONE_OUTER);
		}
	}

	public double calcUpperBuyZoneTop(Period period) {
		if (Period.Week.equals(period)) {
			return maWeek + (stddevWeek * ZONE_INNER);
		} else {
			return maMonth + (stddevMonth * ZONE_INNER);
		}
	}

	public double calcUpperBuyZoneBottom(Period period) {
		if (Period.Week.equals(period)) {
			return maWeek;
		} else {
			return maMonth;
		}
	}

	public double calcLowerSellZoneTop(Period period) {
		if (Period.Week.equals(period)) {
			return maWeek;
		} else {
			return maMonth;
		}
	}

	public double calcLowerSellZoneBottom(Period period) {
		if (Period.Week.equals(period)) {
			return maWeek - (stddevWeek * ZONE_INNER);
		} else {
			return maMonth - (stddevMonth * ZONE_INNER);
		}
	}

	public double calcLowerBuyZoneTop(Period period) {
		if (Period.Week.equals(period)) {
			return maWeek - (stddevWeek * ZONE_OUTER);
		} else {
			return maMonth - (stddevMonth * ZONE_OUTER);
		}
	}

	public double calcLowerBuyZoneBottom(Period period) {
		return calcLowerBuyZoneTop(period) - pierceOffset();
	}

	public double calcBuyZoneBottom() {
		if (maWeek == Double.NaN || stddevWeek == Double.NaN) {
			return 0;
		}
		if (isUpTrendWeekly()) {
			return maWeek;
		} else {
			return maWeek - (stddevWeek * ZONE_OUTER) - pierceOffset();
		}
	}

	public double calcBuyZoneTop() {
		if (maWeek == Double.NaN || stddevWeek == Double.NaN) {
			return 0;
		}
		if (isUpTrendWeekly()) {
			return maWeek + (stddevWeek * ZONE_INNER);
		} else {
			return maWeek - (stddevWeek * ZONE_OUTER);
		}
	}

	public double calcSellZoneBottom() {
		if (maWeek == Double.NaN || stddevWeek == Double.NaN) {
			return 0;
		}
		if (isUpTrendWeekly()) {
			return maWeek + (stddevWeek * ZONE_OUTER);
		} else {
			return maWeek - (stddevWeek * ZONE_INNER);
		}
	}

	public double calcSellZoneTop() {
		if (maWeek == Double.NaN || stddevWeek == Double.NaN) {
			return 0;
		}
		if (isUpTrendWeekly()) {
			return maWeek + (stddevWeek * ZONE_OUTER) + pierceOffset();
		} else {
			return maWeek;
		}
	}

	double pierceOffset() {
		return (price / 100d) * 2d;
	}

	public static String format(double value) {
		if (value != Double.NaN) {
			return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
		}
		return "";
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Symbol=");
		sb.append(symbol);
		sb.append(" ema=");
		sb.append(format(this.getMaWeek()));
		sb.append(" buy zone bottom=");
		sb.append(format(this.calcBuyZoneBottom()));
		sb.append(" top=");
		sb.append(format(this.calcBuyZoneTop()));
		sb.append(" sell zone bottom=");
		sb.append(format(this.calcSellZoneBottom()));
		sb.append(" top=");
		sb.append(format(this.calcSellZoneTop()));
		sb.append(" WUT=" + isUpTrendWeekly());
		sb.append(" MUT=" + isUpTrendMonthly());
		sb.append(" PLW=" + format(priceLastWeek));
		sb.append(" maLM=" + format(maLastMonth));
		sb.append(" PLM=" + format(priceLastMonth));
		return sb.toString();
	}

	public boolean isPriceInBuyZone() {
		return (price >= calcBuyZoneBottom() && price <= calcBuyZoneTop());
	}

	public boolean isPriceInSellZone() {
		return (price >= calcSellZoneBottom());// && price <=
												// calcSellZoneTop());
	}

	public boolean isUpTrendWeekly() {
		return maLastWeek <= priceLastWeek;
	}

	public boolean isUpTrendMonthly() {
		return maLastMonth <= priceLastMonth;
	}

	public boolean isUpTrend(Period period) {
		if (Period.Month.equals(period)) {
			return maLastMonth <= priceLastMonth;
		} else {
			return maLastWeek <= priceLastWeek;
		}
	}

	public boolean isDownTrendWeekly() {
		return maLastWeek > priceLastWeek;
	}

	public boolean isDownTrendMonthly() {
		return maLastMonth > priceLastMonth;
	}

	public boolean isPossibleTrendTerminationWeekly() {
		return isPossibleDowntrendTermination() || isPossibleUptrendTermination();
	}

	public boolean isPossibleUptrendTermination() {
		return (isUpTrendWeekly() && price < maWeek);
	}

	public boolean isPossibleDowntrendTermination() {
		return (isDownTrendWeekly() && price > maWeek);
	}

	public long getSecurityId() {
		return securityId;
	}

	public void setSecurityId(long seurityId) {
		this.securityId = seurityId;
	}

	public Notice getNotice() {
		return notice;
	}

	public void setNotice(Notice notice) {
		this.notice = notice;
	}

	public Date getNoticeDate() {
		return noticeDate;
	}

	public void setNoticeDate(Date noticeDate) {
		this.noticeDate = noticeDate;
	}

	public double getOpen() {
		return open;
	}

	public double getHigh() {
		return high;
	}

	public double getLow() {
		return low;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getSmaMonth() {
		return smaMonth;
	}

	public double getSmaLastMonth() {
		return smaLastMonth;
	}

	public double getS_stddevMonth() {
		return s_stddevMonth;
	}

	public long getPortfolioId() {
		return portfolioId;
	}

	public void setSmaMonth(double smaMonth) {
		this.smaMonth = smaMonth;
	}

	public void setSmaLastMonth(double smaLastMonth) {
		this.smaLastMonth = smaLastMonth;
	}

	public void setS_stddevMonth(double s_stddevMonth) {
		this.s_stddevMonth = s_stddevMonth;
	}

	public void setPortfolioId(long portfolioId) {
		this.portfolioId = portfolioId;
	}

	public double round(double value) {
		return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
}
