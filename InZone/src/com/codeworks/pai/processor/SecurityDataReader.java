package com.codeworks.pai.processor;

import java.util.List;

import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.db.model.Security;

public interface SecurityDataReader {

	public abstract boolean readCurrentPrice(Security security);

	public abstract List<Price> readHistory(String symbol);

}