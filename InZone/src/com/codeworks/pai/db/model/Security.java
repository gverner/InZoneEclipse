package com.codeworks.pai.db.model;

import java.io.Serializable;
import java.util.Date;

public class Security implements Comparable<Security>, Serializable {

	private static final long serialVersionUID = 5334558632507658974L;

	private long id;
	private String symbol;
	private String name;
	private double currentPrice;
	private Date priceDate;
	private double rtPrice;
	private double rtBid;
	private double rtAsk;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getCurrentPrice() {
		return currentPrice;
	}

	public String getSymbol() {
		return symbol;
	}

	public Security(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public int compareTo(Security another) {
		return symbol.compareTo(another.getSymbol());
	}

	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getRtPrice() {
		return rtPrice;
	}

	public void setRtPrice(double rtPrice) {
		this.rtPrice = rtPrice;
	}

	public double getRtBid() {
		return rtBid;
	}

	public void setRtBid(double rtBid) {
		this.rtBid = rtBid;
	}

	public double getRtAsk() {
		return rtAsk;
	}

	public void setRtAsk(double rtAsk) {
		this.rtAsk = rtAsk;
	}
	public Date getPriceDate() {
		return priceDate;
	}

	public void setPriceDate(Date priceDate) {
		this.priceDate = priceDate;
	}

	
}
