package com.codeworks.pai.processor;

import java.util.List;

import com.codeworks.pai.db.model.PaiStudy;
import com.codeworks.pai.db.model.Price;

public interface DataReader {

	public abstract boolean readCurrentPrice(PaiStudy security);

	public abstract List<Price> readHistory(String symbol);

	public abstract boolean readRTPrice(PaiStudy security);

}