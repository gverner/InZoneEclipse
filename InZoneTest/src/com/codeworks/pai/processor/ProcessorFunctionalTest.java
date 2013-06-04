package com.codeworks.pai.processor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.test.AndroidTestCase;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.db.model.PaiStudy;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.db.model.Security;
import com.codeworks.pai.mock.MockDataReader;
import com.codeworks.pai.mock.TestDataLoader;

public class ProcessorFunctionalTest extends AndroidTestCase {
	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
	ProcessorImpl processor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		processor = new ProcessorImpl(null, new MockDataReader());
	}

	public void testStudyGenDTSell() throws ParseException {
		Security security = new Security(TestDataLoader.SPY);
		List<Price> history = TestDataLoader.generateHistory(40.00, 10.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(security, "S&P 500", 8.80, sdf.format(new Date()));
		PaiStudy study = processor.calculateStudy(security, history);
		logStudy(study);
		assertEquals("Price", 8.80d, study.getPrice());
		assertEquals("ATR", 0.0d, study.getAverageTrueRange());
		assertEquals("MA week", 12.52d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 19.94d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 12.91d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 21.12d, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("StdDev Week", 1.82d, PaiUtils.round(study.getStddevWeek()));
		assertEquals("StdDev Month", 7.52d, PaiUtils.round(study.getStddevMonth()));
		assertEquals("DT Monthly", true, study.isDownTrendMonthly());
		assertEquals("DT Weekly", true, study.isDownTrendWeekly());
		assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, study.isPossibleUptrendTermination());
		assertEquals("TT", false, study.isPossibleDowntrendTermination());
		assertEquals("Buy", true, study.isPriceInBuyZone());
		assertEquals("Sell", false, study.isPriceInSellZone());
	}
	
	public void testStudyGenDTBelowSell() throws ParseException {
		Security security = new Security(TestDataLoader.SPY);
		List<Price> history = TestDataLoader.generateHistory(40.00, 10.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(security, "S&P 500", 8.00,sdf.format(new Date()));
		PaiStudy study = processor.calculateStudy(security, history);
		logStudy(study);
		assertEquals("Price", 8.00d, study.getPrice());
		assertEquals("ATR", 0.0d, study.getAverageTrueRange());
		assertEquals("MA week", 12.44d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 19.87d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 12.91d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 21.12d, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("StdDev Week", 1.91d, PaiUtils.round(study.getStddevWeek()));
		assertEquals("StdDev Month", 7.59d, PaiUtils.round(study.getStddevMonth()));
		assertEquals("DT Monthly", true, study.isDownTrendMonthly());
		assertEquals("DT Weekly", true, study.isDownTrendWeekly());
		assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, study.isPossibleUptrendTermination());
		assertEquals("TT", false, study.isPossibleDowntrendTermination());
		assertEquals("Buy", false, study.isPriceInBuyZone());
		assertEquals("Sell", false, study.isPriceInSellZone());
	}	
	public void testStudyGenSell() throws ParseException {
		Security security = new Security(TestDataLoader.SPY);
		List<Price> history = TestDataLoader.generateHistory(10.00, 40.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(security, "S&P 500", 41.10,sdf.format(new Date()));
		PaiStudy study = processor.calculateStudy(security, history);
		logStudy(study);
		assertEquals("Price", 41.10d, study.getPrice());
		assertEquals("ATR", 0.0d, study.getAverageTrueRange());
		assertEquals("StdDev Week", 1.81d, PaiUtils.round(study.getStddevWeek()));
		assertEquals("StdDev Month", 7.52d, PaiUtils.round(study.getStddevMonth()));
		assertEquals("MA week", 37.47d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 30.05d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 37.09d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 28.88d, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("DT Monthly", false, study.isDownTrendMonthly());
		assertEquals("DT Weekly", false, study.isDownTrendWeekly());
		assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, study.isPossibleUptrendTermination());
		assertEquals("TT", false, study.isPossibleDowntrendTermination());
		assertEquals("Buy", false, study.isPriceInBuyZone());
		assertEquals("Sell", true, study.isPriceInSellZone());
	}
	
	public void testStudyGenBuy() throws ParseException {
		Security security = new Security(TestDataLoader.SPY);
		List<Price> history = TestDataLoader.generateHistory(10.00, 40.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(security, "S&P 500", 37.50,sdf.format(new Date()));
		PaiStudy study = processor.calculateStudy(security, history);
		logStudy(study);
		assertEquals("Price", 37.50d, study.getPrice());
		assertEquals("ATR", 0.0d, study.getAverageTrueRange());
		assertEquals("StdDev Week", 1.60d, PaiUtils.round(study.getStddevWeek()));
		assertEquals("StdDev Month", 7.26d, PaiUtils.round(study.getStddevMonth()));
		assertEquals("MA week", 37.13d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 29.70d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 37.09d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 28.88d, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("DT Monthly", false, study.isDownTrendMonthly());
		assertEquals("DT Weekly", false, study.isDownTrendWeekly());
		assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, study.isPossibleUptrendTermination());
		assertEquals("TT", false, study.isPossibleDowntrendTermination());
		assertEquals("Buy", true, study.isPriceInBuyZone());
		assertEquals("Sell", false, study.isPriceInSellZone());
	}
	
	public void testStudyGenUTrendTT() throws ParseException {
		Security security = new Security(TestDataLoader.SPY);
		List<Price> history = TestDataLoader.generateHistory(10.00, 40.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(security, "S&P 500", 36.50,sdf.format(new Date()));
		PaiStudy study = processor.calculateStudy(security, history);
		logStudy(study);
		assertEquals("Price", 36.50d, study.getPrice());
		assertEquals("ATR", 0.0d, study.getAverageTrueRange());
		assertEquals("MA week", 37.03d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 29.61d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 37.09d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 28.88d, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("StdDev Week", 1.61d, PaiUtils.round(study.getStddevWeek()));
		assertEquals("StdDev Month", 7.20d, PaiUtils.round(study.getStddevMonth()));
		assertEquals("DT Monthly", false, study.isDownTrendMonthly());
		assertEquals("DT Weekly", false, study.isDownTrendWeekly());
		assertEquals("TT", true, study.isPossibleTrendTerminationWeekly());
		assertEquals("TT", true, study.isPossibleUptrendTermination());
		assertEquals("TT", false, study.isPossibleDowntrendTermination());
		assertEquals("Buy", false, study.isPriceInBuyZone());
		assertEquals("Sell", false, study.isPriceInSellZone());
	}
	
	void logHistory(List<Price> history) {
		for (Price price : history) {
			System.out.println(sdf.format(price.getDate())+  "  " + price.getClose());
		}
	}
	
	void logStudy (PaiStudy study) {
		System.out.println(study.getSymbol());
		System.out.println("price         "+study.getPrice());
		System.out.println("BuyZone Top   "+study.calcBuyZoneTop());
		System.out.println("BuyZone Botto "+study.calcBuyZoneBottom());
		System.out.println("SelZone Top   "+study.calcSellZoneTop());
		System.out.println("SelZone Botto "+study.calcSellZoneBottom());
		System.out.println("ma week       "+study.getMaWeek());
		System.out.println("ma month      "+study.getMaMonth());
		System.out.println("pr last week  "+study.getPriceLastWeek());
		System.out.println("ma last week  "+study.getMaLastWeek());
		System.out.println("pr last month "+study.getPriceLastMonth());
		System.out.println("ma last month "+study.getMaLastMonth());
		System.out.println("stddev  week  "+study.getStddevWeek());
		System.out.println("stddev  month "+study.getStddevMonth());
		
	}
	public void testStudy() throws ParseException {
		Security security = new Security(TestDataLoader.SPY);
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		// history.add(buildPrice(MockSecurityDataReader.SPY_PRICE,
		// "04/13/2013"));
		MockDataReader.buildSecurity(security, "S&P 500", MockDataReader.SPY_PRICE,"04/12/2013");
		PaiStudy study = processor.calculateStudy(security, history);
		assertEquals("Price", MockDataReader.SPY_PRICE, study.getPrice());
		assertEquals("ATR", 0.0d, study.getAverageTrueRange());
		assertEquals("StdDev Week", 5.56d, PaiUtils.round(study.getStddevWeek()));
		assertEquals("StdDev Month", 10.94d, PaiUtils.round(study.getStddevMonth()));
		assertEquals("MA week", 151.08d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 141.13d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 150.27d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 139.27d, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("DT Monthly", false, study.isDownTrendMonthly());
		assertEquals("DT Weekly", false, study.isDownTrendWeekly());
		assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, study.isPossibleUptrendTermination());
		assertEquals("TT", false, study.isPossibleDowntrendTermination());
		assertEquals("Buy", false, study.isPriceInBuyZone());
		assertEquals("Sell", false, study.isPriceInSellZone());

	}

	Price buildPrice(double price, String priceDate) throws ParseException {
		Price lastPrice = new Price();
		lastPrice.setClose(price);
		lastPrice.setDate(sdf.parse(priceDate));
		return lastPrice;
	}

}
