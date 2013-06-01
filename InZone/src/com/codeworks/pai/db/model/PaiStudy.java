package com.codeworks.pai.db.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.codeworks.pai.processor.Notice;


public class PaiStudy implements Serializable {

	private static final long serialVersionUID = -1275103175237753227L;

	static double zoneInner = 0.5d;
	static double zoneOuter = 2d;

	String symbol;
	String name;
	double price;
	double priceLastWeek;
	double priceLastMonth;
	double maMonth;
	double maWeek;
	double maLastWeek;
	double maLastMonth;
	double stddevWeek;
	double stddevMonth;
	double averageTrueRange;
	long securityId;
	Notice notice;
	Date noticeDate;
	
	List<Price> priceHistory = new ArrayList<Price>();
	
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

	public List<Price> getPriceHistory() {
		return priceHistory;
	}

	public void setPriceHistory(List<Price> priceHistory) {
		this.priceHistory = priceHistory;
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

	public void setPrice(double price) {
		this.price = round(price);
	}

	public double getMaMonth() {
		return maMonth;
	}

	public void setMaMonth(double ma_month) {
		this.maMonth = round(ma_month);
	}

	public double getPriceLastWeek() {
		return priceLastWeek;
	}

	public void setPriceLastWeek(double priceLastWeek) {
		this.priceLastWeek = round(priceLastWeek);
	}

	public double getPriceLastMonth() {
		return priceLastMonth;
	}

	public void setPriceLastMonth(double priceLastMonth) {
		this.priceLastMonth = round(priceLastMonth);
	}

	public double getMaWeek() {
		return maWeek;
	}

	public void setMaWeek(double maWeek) {
		this.maWeek = round(maWeek);
	}

	public double getMaLastWeek() {
		return maLastWeek;
	}

	public void setMaLastWeek(double maLastWeek) {
		this.maLastWeek = round(maLastWeek);
	}

	public double getMaLastMonth() {
		return maLastMonth;
	}

	public void setMaLastMonth(double maLastMonth) {
		this.maLastMonth = round(maLastMonth);
	}

	public double getAverageTrueRange() {
		return averageTrueRange;
	}

	public void setAverageTrueRange(double averageTrueRange) {
		this.averageTrueRange = averageTrueRange;
	}

	public double calcBuyZoneBottom() {
		if (maWeek == Double.NaN || stddevWeek == Double.NaN) {
			return 0;
		}		
		if (isUpTrendWeekly()) {
			return maWeek;
		} else {
			return maWeek - (stddevWeek * zoneOuter) - pierceOffset(); 
		}
	}

	public double calcBuyZoneTop() {
		if (maWeek == Double.NaN || stddevWeek == Double.NaN) {
			return 0;
		}		
		if (isUpTrendWeekly()) {
			return maWeek + (stddevWeek * zoneInner);
		} else {
			return maWeek - (stddevWeek * zoneOuter);
		}
	}

	public double calcSellZoneBottom() {
		if (maWeek == Double.NaN || stddevWeek == Double.NaN) {
			return 0;
		}		
		if (isUpTrendWeekly()) {
			return maWeek + (stddevWeek * zoneOuter);
		} else {
			return maWeek - (stddevWeek * zoneInner);
		}
	}

	public double calcSellZoneTop() {
		if (maWeek == Double.NaN || stddevWeek == Double.NaN) {
			return 0;
		}		
		if (isUpTrendWeekly()) {
			return maWeek + (stddevWeek * zoneOuter)  + pierceOffset();
		} else {
			return maWeek;
		}
	}
	
	double pierceOffset() {
		return (price / 100d) * 2d;
	}
	

	public static String format(double value) {
		if (value != Double.NaN) {
			return new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).toPlainString();
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
		sb.append(" sell zone bottom=" );
		sb.append(format(this.calcSellZoneBottom()));
		sb.append(" top=");
		sb.append(format(this.calcSellZoneTop()));
		return sb.toString();
	}

	public boolean isPriceInBuyZone() {
		return (price >= calcBuyZoneBottom() && price <= calcBuyZoneTop());
	}
	
	public boolean isPriceInSellZone() {
		return (price >= calcSellZoneBottom());// && price <= calcSellZoneTop());
	}
	public boolean isUpTrendWeekly() {
		return maLastWeek <= priceLastWeek;
	}
	
	public boolean isUpTrendMonthly() {
		return maLastMonth <= priceLastMonth;
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
	
	public double round(double value) {
		return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
}
