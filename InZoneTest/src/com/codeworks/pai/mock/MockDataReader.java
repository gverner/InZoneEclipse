package com.codeworks.pai.mock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.db.model.Security;
import com.codeworks.pai.processor.DataReader;

public class MockDataReader implements DataReader {
	static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

	public static final double SPY_PRICE = 158.80d;
	public static final double QQQ_PRICE = 67.86d;
	public static final double GLD_PRICE = 143.95;
	public static final double UNG_PRICE = 23.10d;
	public static final double HYG_PRICE = 92.36d;

	@Override
	public boolean readCurrentPrice(Security security) {
		if (TestDataLoader.SPY.equalsIgnoreCase(security.getSymbol())) {
			buildSecurity(security, "S&P 500", SPY_PRICE,"04/12/2013");
		} else if (TestDataLoader.QQQ.equalsIgnoreCase(security.getSymbol())) {
			buildSecurity(security, "QQQ POWER SHARES", QQQ_PRICE,"04/12/2013");
		} else if (TestDataLoader.GLD.equalsIgnoreCase(security.getSymbol())) {
			buildSecurity(security, "Gold ETF", GLD_PRICE,"04/12/2013");
		} else if (TestDataLoader.UNG.equalsIgnoreCase(security.getSymbol())) {
			buildSecurity(security, "NAT GAS ETF", UNG_PRICE,"04/12/2013");
		} else if (TestDataLoader.HYG.equalsIgnoreCase(security.getSymbol())) {
			buildSecurity(security, "High Yield Bond ETF", HYG_PRICE, "06/03/2013");
		} else {
			return false;
		}
		return true;
	}

	public static void buildSecurity(Security security, String name, double price, String date) {
		try {
			security.setCurrentPrice(price);
			security.setRtBid(security.getCurrentPrice());
			security.setRtAsk(security.getCurrentPrice());
			security.setName(name);
			security.setPriceDate(sdf.parse(date));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public List<Price> readHistory(String symbol) {
		List<Price> history = TestDataLoader.getTestHistory(symbol);
		return history;
	}

}
