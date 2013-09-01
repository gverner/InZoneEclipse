package com.codeworks.pai.processor;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.test.AndroidTestCase;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.db.model.EmaRules;
import com.codeworks.pai.db.model.PaiStudy;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.db.model.Rules;
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
	public double round(double value) {
		return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	public void testStudyGenDTSell() throws ParseException {
		PaiStudy study = new PaiStudy(TestDataLoader.SPY);
		Rules rules = new EmaRules(study);
		List<Price> history = TestDataLoader.generateHistory(40.00, 10.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(study, "S&P 500", 9.10, "08/01/2013");
		processor.calculateStudy(study, history);
		logStudy(study);
		assertEquals("Price", 9.10d, study.getPrice());
		assertEquals("ATR", 0.06d, round(study.getAverageTrueRange()));
		assertEquals("MA week", 12.49d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 19.21d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 12.85d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 19.21d, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("StdDev Week", 1.78d, PaiUtils.round(study.getStddevWeek()));
		assertEquals("StdDev Month", 7.34d, PaiUtils.round(study.getStddevMonth()));
		assertEquals("DT Monthly", true, rules.isDownTrendMonthly());
		assertEquals("DT Weekly", true, rules.isDownTrendWeekly());
		assertEquals("TT", false, rules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, rules.isPossibleUptrendTermination());
		assertEquals("TT", false, rules.isPossibleDowntrendTermination());
		assertEquals("Buy", false, rules.isPriceInBuyZone());
		assertEquals("Sell", false, rules.isPriceInSellZone());
	}
	
	public void testStudyGenDTBelowSell() throws ParseException {
		PaiStudy study = new PaiStudy(TestDataLoader.SPY);
		Rules rules = new EmaRules(study);
		List<Price> history = TestDataLoader.generateHistory(40.00, 10.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(study, "S&P 500", 8.00, MockDataReader.PRICE_CLOSE_DATE2);
		processor.calculateStudy(study, history);
		logStudy(study);
		assertEquals("Price", 8.00d, study.getPrice());
		assertEquals("ATR", 0.06d, round(study.getAverageTrueRange()));
		assertEquals("MA week", 13,15d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 21.36d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 13.15d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 21.36, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("StdDev Week", 1.73d, PaiUtils.round(study.getStddevWeek()));
		assertEquals("StdDev Month", 7.53d, PaiUtils.round(study.getStddevMonth()));
		assertEquals("DT Monthly", true, rules.isDownTrendMonthly());
		assertEquals("DT Weekly", true, rules.isDownTrendWeekly());
		assertEquals("TT", false, rules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, rules.isPossibleUptrendTermination());
		assertEquals("TT", false, rules.isPossibleDowntrendTermination());
		assertEquals("Buy", false, rules.isPriceInBuyZone());
		assertEquals("Sell", false, rules.isPriceInSellZone());
	}	
	public void testStudyGenSell() throws ParseException {
		PaiStudy study = new PaiStudy(TestDataLoader.SPY);
		Rules rules = new EmaRules(study);
		List<Price> history = TestDataLoader.generateHistory(10.00, 40.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(study, "S&P 500", 41.10, MockDataReader.PRICE_CLOSE_DATE);
		processor.calculateStudy(study, history);
		logStudy(study);
		assertEquals("Price", 41.10d, study.getPrice());
		assertEquals("ATR", 0.06d, round(study.getAverageTrueRange()));
		assertEquals("StdDev Week", 1.73d, PaiUtils.round(study.getStddevWeek()));
		assertEquals("StdDev Month", 7.53d, PaiUtils.round(study.getStddevMonth()));
		assertEquals("MA week", 36.85D, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 28.64d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 36.85d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 28.64d, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("DT Monthly", false, rules.isDownTrendMonthly());
		assertEquals("DT Weekly", false, rules.isDownTrendWeekly());
		assertEquals("TT", false, rules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, rules.isPossibleUptrendTermination());
		assertEquals("TT", false, rules.isPossibleDowntrendTermination());
		assertEquals("Buy", false, rules.isPriceInBuyZone());
		assertEquals("Sell", true, rules.isPriceInSellZone());
	}
	
	public void testStudyGenBuy() throws ParseException {
		PaiStudy study = new PaiStudy(TestDataLoader.SPY);
		Rules rules = new EmaRules(study);
		List<Price> history = TestDataLoader.generateHistory(10.00, 40.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(study, "S&P 500", 37.50, MockDataReader.PRICE_CLOSE_DATE);
		processor.calculateStudy(study, history);
		logStudy(study);
		assertEquals("Price", 37.50d, study.getPrice());
		assertEquals("ATR", 0.06d, round(study.getAverageTrueRange()));
		assertEquals("StdDev Week", 1.73d, PaiUtils.round(study.getStddevWeek()));
		assertEquals("StdDev Month", 7.53d, PaiUtils.round(study.getStddevMonth()));
		assertEquals("MA week", 36.85d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 28.64d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 36.85d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 28.64d, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("DT Monthly", false, rules.isDownTrendMonthly());
		assertEquals("DT Weekly", false, rules.isDownTrendWeekly());
		assertEquals("TT", false, rules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, rules.isPossibleUptrendTermination());
		assertEquals("TT", false, rules.isPossibleDowntrendTermination());
		assertEquals("Buy", true, rules.isPriceInBuyZone());
		assertEquals("Sell", false, rules.isPriceInSellZone());
	}
	
	public void testStudyGenUTrendTT() throws ParseException {
		PaiStudy study = new PaiStudy(TestDataLoader.SPY);
		List<Price> history = TestDataLoader.generateHistory(10.00, 40.00, 500);
		Rules rules = new EmaRules(study);
		logHistory(history);
		MockDataReader.buildSecurity(study, "S&P 500", 36.50, MockDataReader.PRICE_CLOSE_DATE);
		processor.calculateStudy(study, history);
		logStudy(study);
		assertEquals("Price", 36.50d, study.getPrice());
		assertEquals("ATR", 0.06d, round(study.getAverageTrueRange()));
		assertEquals("MA week", 36.85d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 28.64d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 36.85d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 28.64d, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("StdDev Week", 1.73d, PaiUtils.round(study.getStddevWeek()));
		assertEquals("StdDev Month", 7.53d, PaiUtils.round(study.getStddevMonth()));
		assertEquals("DT Monthly", false, rules.isDownTrendMonthly());
		assertEquals("DT Weekly", false, rules.isDownTrendWeekly());
		assertEquals("TT", true, rules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", true, rules.isPossibleUptrendTermination());
		assertEquals("TT", false, rules.isPossibleDowntrendTermination());
		assertEquals("Buy", false, rules.isPriceInBuyZone());
		assertEquals("Sell", false, rules.isPriceInSellZone());
	}
	
	void logHistory(List<Price> history) {
		for (Price price : history) {
			System.out.println("logHistory"+sdf.format(price.getDate())+  "  " + price.getClose());
		}
	}
	
	void logStudy (PaiStudy study) {
		Rules rules = new EmaRules(study);
		System.out.println(study.getSymbol());
		System.out.println("price         "+study.getPrice());
		System.out.println("BuyZone Top   "+rules.calcBuyZoneTop());
		System.out.println("BuyZone Botto "+rules.calcBuyZoneBottom());
		System.out.println("SelZone Top   "+rules.calcSellZoneTop());
		System.out.println("SelZone Botto "+rules.calcSellZoneBottom());
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
		Rules rules = new EmaRules(study);
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		// history.add(buildPrice(MockPaiStudyDataReader.SPY_PRICE,
		// "04/13/2013"));
		MockDataReader.buildSecurity(study, "S&P 500", MockDataReader.SPY_PRICE,MockDataReader.PRICE_CLOSE_DATE);
		processor.calculateStudy(study, history);
		assertEquals("Price", MockDataReader.SPY_PRICE, study.getPrice());
		assertEquals("ATR", 1.46d, round(study.getAverageTrueRange()));
		assertEquals("StdDev Week", 5.56d, PaiUtils.round(study.getStddevWeek()));
		assertEquals("StdDev Month", 10.94d, PaiUtils.round(study.getStddevMonth()));
		assertEquals("MA week", 151.08d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA month", 141.13d, PaiUtils.round(study.getMaMonth()));
		assertEquals("MA last week", 150.27d, PaiUtils.round(study.getMaLastWeek()));
		assertEquals("MA last month", 139.27d, PaiUtils.round(study.getMaLastMonth()));
		assertEquals("DT Monthly", false, rules.isDownTrendMonthly());
		assertEquals("DT Weekly", false, rules.isDownTrendWeekly());
		assertEquals("TT", false, rules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, rules.isPossibleUptrendTermination());
		assertEquals("TT", false, rules.isPossibleDowntrendTermination());
		assertEquals("Buy", false, rules.isPriceInBuyZone());
		assertEquals("Sell", false, rules.isPriceInSellZone());

	}
	
	public void testFridayCutoff() throws ParseException {
		PaiStudy study = new PaiStudy(TestDataLoader.SPY);
		Rules rules = new EmaRules(study);
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY_FRIDAY_CLOSE);
		// history.add(buildPrice(MockPaiStudyDataReader.SPY_PRICE,
		// "04/13/2013"));
		MockDataReader.buildSecurity(study, "S&P 500", 169.11D,"07/26/2013");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mmaa Z", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("Eastern/US"));
		Date testDate = sdf.parse("07/26/2013 04:00pm EDT");
		study.setPriceDate(testDate);
		processor.calculateStudy(study, history);
		assertEquals("MA week", 161.43d, PaiUtils.round(study.getMaWeek()));
		assertEquals("MA last week", 161.43d, PaiUtils.round(study.getMaLastWeek()));
	}
	
	Price buildPrice(double price, String priceDate) throws ParseException {
		Price lastPrice = new Price();
		lastPrice.setClose(price);
		lastPrice.setDate(sdf.parse(priceDate));
		return lastPrice;
	}

}
