package com.codeworks.pai.processor;

import java.math.BigDecimal;
import java.util.List;

import android.content.ContentValues;
import android.net.Uri;
import android.test.ProviderTestCase2;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.PaiStudyTable;
import com.codeworks.pai.db.model.PaiStudy;
import com.codeworks.pai.mock.MockDataReader;
import com.codeworks.pai.mock.TestDataLoader;

public class ProcessorTest extends ProviderTestCase2<PaiContentProvider> {

	public ProcessorTest() {
		super(PaiContentProvider.class, PaiContentProvider.AUTHORITY);
	}

	ProcessorImpl processor;
	List<PaiStudy> studies;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		processor = new ProcessorImpl(getMockContentResolver(), new MockDataReader());
		//createSecurities();
		//studies = processor.process();
	}

	public Uri insertSecurity(String symbol) {
		ContentValues values = new ContentValues();
		values.put(PaiStudyTable.COLUMN_SYMBOL, symbol);
		Uri uri = getMockContentResolver().insert(PaiContentProvider.PAI_STUDY_URI, values);
		return uri;
	}

	public void createSecurities() {
		insertSecurity(TestDataLoader.SPY);
		insertSecurity(TestDataLoader.QQQ);
		insertSecurity(TestDataLoader.GLD);
		insertSecurity(TestDataLoader.UNG);
	}

	public double round(double value) {
		return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public PaiStudy getStudy(String symbol) {
		for (PaiStudy study : studies) {
			if (symbol.equalsIgnoreCase(study.getSymbol())) {
				return study;
			}
		}
		return null;
	}
/*
	public void testHyg() throws InterruptedException {
		insertSecurity(TestDataLoader.HYG);
		studies = processor.process();
		PaiStudy study = getStudy(TestDataLoader.HYG);
		assertEquals("Price", MockDataReader.HYG_PRICE, study.getPrice());
		assertEquals("ATR", 0.0d, study.getAverageTrueRange());
		assertEquals("MA week", 94.10d, round(study.getMaWeek()));
		assertEquals("MA month", 92.23d, round(study.getMaMonth()));
		assertEquals("MA last week", 94.28d, round(study.getMaLastWeek()));
		assertEquals("MA last month", 92.21d, round(study.getMaLastMonth()));
		assertEquals("Price last week", 92.92d, round(study.getPriceLastWeek()));
		assertEquals("Price last month", 92.92d, round(study.getPriceLastMonth()));
		assertEquals("StdDev Week", .87d, round(study.getStddevWeek()));
		assertEquals("StdDev Month", 2.18d, round(study.getStddevMonth()));
		assertEquals("DT Monthly",false, study.isDownTrendMonthly());
		assertEquals("DT Weekly", true, study.isDownTrendWeekly());
		assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, study.isPossibleUptrendTermination());
		assertEquals("TT", false, study.isPossibleDowntrendTermination());
		assertEquals("Buy", false, study.isPriceInBuyZone());
		assertEquals("Sell", false, study.isPriceInSellZone());

	}
*/
	public void testUng() throws InterruptedException {
		insertSecurity(TestDataLoader.UNG);
		studies = processor.process(TestDataLoader.UNG);
		PaiStudy study = getStudy(TestDataLoader.UNG);
		assertEquals("Price", MockDataReader.UNG_PRICE, study.getPrice());
		assertEquals("ATR", 0.0d, study.getAverageTrueRange());
		assertEquals("StdDev Week", 1.46d, round(study.getStddevWeek()));
		assertEquals("StdDev Month", 5.62d, round(study.getStddevMonth()));
		assertEquals("MA week", 20.46d, round(study.getMaWeek()));
		assertEquals("MA month", 17.72d, round(study.getMaMonth()));
		assertEquals("MA last week", 20.18d, round(study.getMaLastWeek()));
		assertEquals("MA last month", 17.16d, round(study.getMaLastMonth()));
		assertEquals("Price last week", 22.46d, round(study.getPriceLastWeek()));
		assertEquals("Price last month", 21.88d, round(study.getPriceLastMonth()));
		assertEquals("DT Monthly",false, study.isDownTrendMonthly());
		assertEquals("DT Weekly", false, study.isDownTrendWeekly());
		assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, study.isPossibleUptrendTermination());
		assertEquals("TT", false, study.isPossibleDowntrendTermination());
		assertEquals("Buy", false, study.isPriceInBuyZone());
		assertEquals("Sell", false, study.isPriceInSellZone());

	}

	public void testGld() throws InterruptedException {
		insertSecurity(TestDataLoader.GLD);
		studies = processor.process(null);
		PaiStudy study = getStudy(TestDataLoader.GLD);
		assertEquals("Price", MockDataReader.GLD_PRICE, study.getPrice());
		assertEquals("ATR", 0.0d, study.getAverageTrueRange());
		assertEquals("StdDev Week", 5.42d, round(study.getStddevWeek()));
		assertEquals("StdDev Month", 7.15d, round(study.getStddevMonth()));
		assertEquals("MA week", 156.61d, round(study.getMaWeek()));
		assertEquals("MA month", 155.75d, round(study.getMaMonth()));
		assertEquals("MA last week", 157.94d, round(study.getMaLastWeek()));
		assertEquals("MA last month", 156.99d, round(study.getMaLastMonth()));
		assertEquals("DT Monthly",true, study.isDownTrendMonthly());
		assertEquals("DT Weekly", true, study.isDownTrendWeekly());
		assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, study.isPossibleUptrendTermination());
		assertEquals("TT", false, study.isPossibleDowntrendTermination());
		assertEquals("Buy", true, study.isPriceInBuyZone());
		assertEquals("Sell", false, study.isPriceInSellZone());

	}

	public void testSpy() throws InterruptedException {
		insertSecurity(TestDataLoader.SPY);
		studies = processor.process(TestDataLoader.SPY);
		PaiStudy study = getStudy(TestDataLoader.SPY);
		assertEquals("Price", MockDataReader.SPY_PRICE, study.getPrice());
		assertEquals("ATR", 0.0d, study.getAverageTrueRange());
		assertEquals("MA week", 151.08d, round(study.getMaWeek()));
		assertEquals("MA month", 141.13d, round(study.getMaMonth()));
		assertEquals("MA last week", 150.27d, round(study.getMaLastWeek()));
		assertEquals("MA last month", 139.27d, round(study.getMaLastMonth()));
		assertEquals("DT Monthly", false, study.isDownTrendMonthly());
		assertEquals("DT Weekly", false, study.isDownTrendWeekly());
		assertEquals("StdDev Week", 5.56d, round(study.getStddevWeek()));
		assertEquals("StdDev Month", 10.94d, round(study.getStddevMonth()));
		assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, study.isPossibleUptrendTermination());
		assertEquals("TT", false, study.isPossibleDowntrendTermination());
		assertEquals("Buy", false, study.isPriceInBuyZone());
		assertEquals("Sell", false, study.isPriceInSellZone());

	}

	public void testQQQ() throws InterruptedException {
		insertSecurity(TestDataLoader.QQQ);
		studies = processor.process(null);
		
		PaiStudy study = getStudy(TestDataLoader.QQQ);
		assertEquals("Price", MockDataReader.QQQ_PRICE, study.getPrice());
		assertEquals("ATR", 0.0, study.getAverageTrueRange());
		assertEquals("StdDev Week", 1.42d, round(study.getStddevWeek()));
		assertEquals("StdDev Month", 4.64d, round(study.getStddevMonth()));
		assertEquals("MA week", 67.37d, round(study.getMaWeek()));
		assertEquals("MA month", 63.79d, round(study.getMaMonth()));
		assertEquals("MA last week", 67.32d, round(study.getMaLastWeek()));
		assertEquals("MA last month", 63.36d, round(study.getMaLastMonth()));
		assertEquals("DT Monthly", false, study.isDownTrendMonthly());
		assertEquals("DT Weekly", false, study.isDownTrendWeekly());
		assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, study.isPossibleUptrendTermination());
		assertEquals("TT", false, study.isPossibleDowntrendTermination());
		assertEquals("Buy", true, study.isPriceInBuyZone());
		assertEquals("Sell", false, study.isPriceInSellZone());

	}

	/*
	public void testProcessor1() {
		insertSecurity(TestDataLoader.SPY);
		studies = processor.process();
		
		assertTrue(studies.size() == 2);
		for (PaiStudy study : studies) {
			if (TestDataLoader.SPY.equalsIgnoreCase(study.getSymbol())) {
				assertEquals("Price", MockSecurityDataReader.SPY_PRICE, study.getPrice());
				assertEquals("ATR", 0.0d, study.getAverageTrueRange());
				assertEquals("StdDev Week", 5.56d, round(study.getStddevWeek()));
				assertEquals("StdDev Month", 10.94d, round(study.getStddevMonth()));
				assertEquals("MA week", 151.08d, round(study.getMaWeek()));
				assertEquals("MA month", 141.13d, round(study.getMaMonth()));
				assertEquals("MA last week", 150.27d, round(study.getMaLastWeek()));
				assertEquals("MA last month", 139.27d, round(study.getMaLastMonth()));
				assertEquals("DT Monthly", false, study.isDownTrendMonthly());
				assertEquals("DT Weekly", false, study.isDownTrendWeekly());
				assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
				assertEquals("TT", false, study.isPriceInPossibleUptrendTermination());
				assertEquals("TT", false, study.isPriceInPossibleDowntrendTermination());
				assertEquals("Buy", false, study.isPriceInBuyZone());
				assertEquals("Sell", false, study.isPriceInSellZone());

			} else if (TestDataLoader.QQQ.equalsIgnoreCase(study.getSymbol())) {
				assertEquals("Price", MockSecurityDataReader.QQQ_PRICE, study.getPrice());
				assertEquals("ATR", 0.0, study.getAverageTrueRange());
				assertEquals("StdDev Week", 1.42d, round(study.getStddevWeek()));
				assertEquals("StdDev Month", 4.64d, round(study.getStddevMonth()));
				assertEquals("MA week", 67.37d, study.getMaWeek());
				assertEquals("MA month", 63.79d, study.getMaMonth());
				assertEquals("MA last week", 67.32d, round(study.getMaLastWeek()));
				assertEquals("MA last month", 63.36d, round(study.getMaLastMonth()));
				assertEquals("DT Monthly", false, study.isDownTrendMonthly());
				assertEquals("DT Weekly", false, study.isDownTrendWeekly());
				assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
				assertEquals("TT", false, study.isPriceInPossibleUptrendTermination());
				assertEquals("TT", false, study.isPriceInPossibleDowntrendTermination());
				assertEquals("Buy", true, study.isPriceInBuyZone());
				assertEquals("Sell", false, study.isPriceInSellZone());
			}
		}
	}
	*/
}