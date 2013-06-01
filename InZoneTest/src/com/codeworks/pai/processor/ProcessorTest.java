package com.codeworks.pai.processor;

import java.math.BigDecimal;
import java.util.List;

import android.content.ContentValues;
import android.net.Uri;
import android.test.ProviderTestCase2;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.SecurityTable;
import com.codeworks.pai.db.model.PaiStudy;
import com.codeworks.pai.mock.MockSecurityDataReader;
import com.codeworks.pai.mock.TestDataLoader;

public class ProcessorTest extends ProviderTestCase2<PaiContentProvider> {

	public ProcessorTest() {
		super(PaiContentProvider.class, PaiContentProvider.AUTHORITY);
	}

	Processor processor;
	List<PaiStudy> studies;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		processor = new Processor(getMockContentResolver(), new MockSecurityDataReader());
		//createSecurities();
		//studies = processor.process();
	}

	public void insertSecurity(String symbol) {
		ContentValues values = new ContentValues();
		values.put(SecurityTable.COLUMN_SYMBOL, symbol);
		Uri uri = getMockContentResolver().insert(PaiContentProvider.SECURITY_URI, values);
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

	public void testUng() throws InterruptedException {
		insertSecurity(TestDataLoader.UNG);
		studies = processor.process();
		PaiStudy study = getStudy(TestDataLoader.UNG);
		assertEquals("Price", MockSecurityDataReader.UNG_PRICE, study.getPrice());
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
		studies = processor.process();
		PaiStudy study = getStudy(TestDataLoader.GLD);
		assertEquals("Price", MockSecurityDataReader.GLD_PRICE, study.getPrice());
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
		studies = processor.process();
		PaiStudy study = getStudy(TestDataLoader.SPY);
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
		assertEquals("TT", false, study.isPossibleUptrendTermination());
		assertEquals("TT", false, study.isPossibleDowntrendTermination());
		assertEquals("Buy", false, study.isPriceInBuyZone());
		assertEquals("Sell", false, study.isPriceInSellZone());

	}

	public void testQQQ() throws InterruptedException {
		insertSecurity(TestDataLoader.QQQ);
		studies = processor.process();
		
		PaiStudy study = getStudy(TestDataLoader.QQQ);
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
