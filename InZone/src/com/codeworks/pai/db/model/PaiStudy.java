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

	public static String format(double value) {
		if (value != Double.NaN) {
			return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
		}
		return "";
	}
	String						symbol;
	String						name;
	MaType						maType;
	double						price;
	double						open;
	double						high;
	double						low;
	double						priceLastWeek;
	double						priceLastMonth;
	double						maMonth;
	double						maWeek;
	double						maLastWeek;
	double						maLastMonth;
	double						stddevWeek;
	double						stddevMonth;
	double						smaMonth;
	double						smaLastMonth;
	double						smaStddevMonth;
	double						smaWeek;
	double						smaLastWeek;
	double						smaStddevWeek;
	double						lastClose;
	double						averageTrueRange;
	long						securityId;
	int							portfolioId;
	int							contracts;
	Notice						notice;
	Date						noticeDate;

	Date						priceDate;

	public PaiStudy(String symbol) {
		this.symbol = symbol;
	}

	public double getAverageTrueRange() {
		return averageTrueRange;
	}

	public int getContracts() {
		return contracts;
	}

	public double getHigh() {
		return high;
	}

	public double getLastClose() {
		return lastClose;
	}

	public double getLow() {
		return low;
	}

	public double getMaLastMonth() {
		return maLastMonth;
	}

	public double getMaLastWeek() {
		return maLastWeek;
	}

	public double getMaMonth() {
		return maMonth;
	}

	public MaType getMaType() {
		return maType;
	}

	public double getMaWeek() {
		return maWeek;
	}

	
	public double getMovingAverage(Period period) {
		if (MaType.E.equals(maType)) {
			if (Period.Week.equals(period)) {
				return getMaWeek();
			} else {
				return getMaMonth();
			}
		} else {
			if (Period.Week.equals(period)) {
				return getSmaWeek();
			} else {
				return getSmaMonth();
			}
		}
	}
	
	public String getName() {
		return name;
	}

	public Notice getNotice() {
		return notice;
	}

	public Date getNoticeDate() {
		return noticeDate;
	}

	public double getOpen() {
		return open;
	}

	public int getPortfolioId() {
		return portfolioId;
	}

	public double getPrice() {
		return price;
	}

	public Date getPriceDate() {
		return priceDate;
	}

	public double getPriceLastMonth() {
		return priceLastMonth;
	}

	public double getPriceLastWeek() {
		return priceLastWeek;
	}

	public long getSecurityId() {
		return securityId;
	}

	public double getSmaLastMonth() {
		return smaLastMonth;
	}

	public double getSmaLastWeek() {
		return smaLastWeek;
	}

	public double getSmaMonth() {
		return smaMonth;
	}

	public double getSmaStddevMonth() {
		return smaStddevMonth;
	}

	public double getSmaStddevWeek() {
		return smaStddevWeek;
	}

	public double getSmaWeek() {
		return smaWeek;
	}

	public double getStddevMonth() {
		return stddevMonth;
	}

	public double getStddevWeek() {
		return stddevWeek;
	}

	public String getSymbol() {
		return symbol;
	}

	public double round(double value) {
		return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public void setAverageTrueRange(double averageTrueRange) {
		this.averageTrueRange = averageTrueRange;
	}

	public void setContracts(int contracts) {
		this.contracts = contracts;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public void setLastClose(double lastClose) {
		this.lastClose = lastClose;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public void setMaLastMonth(double maLastMonth) {
		this.maLastMonth = maLastMonth;
	}

	public void setMaLastWeek(double maLastWeek) {
		this.maLastWeek = maLastWeek;
	}

	public void setMaMonth(double ma_month) {
		this.maMonth = ma_month;
	}

	public void setMaType(MaType maType) {
		this.maType = maType;
	}

	public void setMaWeek(double maWeek) {
		this.maWeek = maWeek;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNotice(Notice notice) {
		this.notice = notice;
	}

	public void setNoticeDate(Date noticeDate) {
		this.noticeDate = noticeDate;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public void setPortfolioId(int portfolioId) {
		this.portfolioId = portfolioId;
	}

	public void setPrice(double price) {
		this.price = price;
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

	public void setPriceLastMonth(double priceLastMonth) {
		this.priceLastMonth = priceLastMonth;
	}

	public void setPriceLastWeek(double priceLastWeek) {
		this.priceLastWeek = priceLastWeek;
	}

	public void setSecurityId(long seurityId) {
		this.securityId = seurityId;
	}

	public void setSmaLastMonth(double smaLastMonth) {
		this.smaLastMonth = smaLastMonth;
	}

	public void setSmaLastWeek(double smaLastWeek) {
		this.smaLastWeek = smaLastWeek;
	}

	public void setSmaMonth(double smaMonth) {
		this.smaMonth = smaMonth;
	}

	public void setSmaStddevMonth(double s_stddevMonth) {
		this.smaStddevMonth = s_stddevMonth;
	}

	public void setSmaStddevWeek(double smaStddevWeek) {
		this.smaStddevWeek = smaStddevWeek;
	}

	public void setSmaWeek(double smaWeek) {
		this.smaWeek = smaWeek;
	}

	public void setStddevMonth(double stddev_month) {
		this.stddevMonth = stddev_month;
	}

	public void setStddevWeek(double stdDeviation) {
		this.stddevWeek = stdDeviation;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Symbol=");
		sb.append(symbol);
		sb.append(" ema=");
		sb.append(format(this.getMaWeek()));
		sb.append(" PLW=" + format(priceLastWeek));
		sb.append(" maLM=" + format(maLastMonth));
		sb.append(" PLM=" + format(priceLastMonth));
		return sb.toString();
	}
}
