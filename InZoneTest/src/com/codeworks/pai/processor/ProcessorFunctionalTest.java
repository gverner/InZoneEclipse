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
		PaiStudy study = new PaiStudy(TestDataLoader.SPY);
		List<Price> history = TestDataLoader.generateHistory(40.00, 10.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(study, "S&P 500", 9.10, sdf.format(new Date()));
		processor.calculateStudy(study, history);
		logStudy(study);
		assertEquals("Price", 9.10d, study.getPrice());
		assertEquals("ATR", 0.0d, study.getAverageTrueRange());
		assertEquals("MA week", 12.76d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 20.19d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 13.15d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 21.36d, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("StdDev Week", 1.81d, PaiUtils.round(study.getStddevWeek()));
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
		PaiStudy study = new PaiStudy(TestDataLoader.SPY);
		List<Price> history = TestDataLoader.generateHistory(40.00, 10.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(study, "S&P 500", 8.00,sdf.format(new Date()));
		processor.calculateStudy(study, history);
		logStudy(study);
		assertEquals("Price", 8.00d, study.getPrice());
		assertEquals("ATR", 0.0d, study.getAverageTrueRange());
		assertEquals("MA week", 12.66d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 20.08d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 13.15d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 21.36, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("StdDev Week", 1.94d, PaiUtils.round(study.getStddevWeek()));
		assertEquals("StdDev Month", 7.61d, PaiUtils.round(study.getStddevMonth()));
		assertEquals("DT Monthly", true, study.isDownTrendMonthly());
		assertEquals("DT Weekly", true, study.isDownTrendWeekly());
		assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, study.isPossibleUptrendTermination());
		assertEquals("TT", false, study.isPossibleDowntrendTermination());
		assertEquals("Buy", false, study.isPriceInBuyZone());
		assertEquals("Sell", false, study.isPriceInSellZone());
	}	
	public void testStudyGenSell() throws ParseException {
		PaiStudy study = new PaiStudy(TestDataLoader.SPY);
		List<Price> history = TestDataLoader.generateHistory(10.00, 40.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(study, "S&P 500", 41.10,sdf.format(new Date()));
		processor.calculateStudy(study, history);
		logStudy(study);
		assertEquals("Price", 41.10d, study.getPrice());
		assertEquals("ATR", 0.0d, study.getAverageTrueRange());
		assertEquals("StdDev Week", 1.83d, PaiUtils.round(study.getStddevWeek()));
		assertEquals("StdDev Month", 7.53d, PaiUtils.round(study.getStddevMonth()));
		assertEquals("MA week", 37.25d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 29.83d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 36.85d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 28.64d, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("DT Monthly", false, study.isDownTrendMonthly());
		assertEquals("DT Weekly", false, study.isDownTrendWeekly());
		assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, study.isPossibleUptrendTermination());
		assertEquals("TT", false, study.isPossibleDowntrendTermination());
		assertEquals("Buy", false, study.isPriceInBuyZone());
		assertEquals("Sell", true, study.isPriceInSellZone());
	}
	
	public void testStudyGenBuy() throws ParseException {
		PaiStudy study = new PaiStudy(TestDataLoader.SPY);
		List<Price> history = TestDataLoader.generateHistory(10.00, 40.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(study, "S&P 500", 37.50,sdf.format(new Date()));
		processor.calculateStudy(study, history);
		logStudy(study);
		assertEquals("Price", 37.50d, study.getPrice());
		assertEquals("ATR", 0.0d, study.getAverageTrueRange());
		assertEquals("StdDev Week", 1.61d, PaiUtils.round(study.getStddevWeek()));
		assertEquals("StdDev Month", 7.27d, PaiUtils.round(study.getStddevMonth()));
		assertEquals("MA week", 36.91d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 29.49d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 36.85d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 28.64d, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("DT Monthly", false, study.isDownTrendMonthly());
		assertEquals("DT Weekly", false, study.isDownTrendWeekly());
		assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, study.isPossibleUptrendTermination());
		assertEquals("TT", false, study.isPossibleDowntrendTermination());
		assertEquals("Buy", true, study.isPriceInBuyZone());
		assertEquals("Sell", false, study.isPriceInSellZone());
	}
	
	public void testStudyGenUTrendTT() throws ParseException {
		PaiStudy study = new PaiStudy(TestDataLoader.SPY);
		List<Price> history = TestDataLoader.generateHistory(10.00, 40.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(study, "S&P 500", 36.50,sdf.format(new Date()));
		processor.calculateStudy(study, history);
		logStudy(study);
		assertEquals("Price", 36.50d, study.getPrice());
		assertEquals("ATR", 0.0d, study.getAverageTrueRange());
		assertEquals("MA week", 36.82d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 29.39d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 36.85d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 28.64d, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("StdDev Week", 1.61d, PaiUtils.round(study.getStddevWeek()));
		assertEquals("StdDev Month", 7.21d, PaiUtils.round(study.getStddevMonth()));
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
			System.out.println("logHistory"+sdf.format(price.getDate())+  "  " + price.getClose());
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
		PaiStudy study = new PaiStudy(TestDataLoader.SPY);
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		// history.add(buildPrice(MockPaiStudyDataReader.SPY_PRICE,
		// "04/13/2013"));
		MockDataReader.buildSecurity(study, "S&P 500", MockDataReader.SPY_PRICE,"04/12/2013");
		processor.calculateStudy(study, history);
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