package com.codeworks.pai.study;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import junit.framework.TestCase;

import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.mock.TestDataLoader;
import com.codeworks.pai.study.EMA2;
import com.codeworks.pai.study.EMA3;
import com.codeworks.pai.study.EMA4;
import com.codeworks.pai.study.Grouper;
import com.codeworks.pai.study.Period;

public class EMATest extends TestCase {
	
	
	
	public void testEma2Weekly() throws IOException {
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		Grouper grouper = new Grouper();
		List<Price> weekly = grouper.periodList(history, Period.Week);
		EMA2 ema = new EMA2(20);
		double smaValue = 0;
		for (Price price : weekly) {
			smaValue = ema.compute(price.getClose());
		}
		BigDecimal decimal = new BigDecimal(smaValue);
		decimal = decimal.setScale(2,BigDecimal.ROUND_HALF_UP);
		System.out.println("Last History Date"+weekly.get(weekly.size()-1).getDate());
		System.out.println("Weekly EMA ="+  decimal.toPlainString());
		// Week April 1 through 5 Think Swim EMA 150.27
		assertEquals(150.27d, decimal.doubleValue());
	}
	
	public void testEma2Monthy() throws IOException {
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		Grouper grouper = new Grouper();
		List<Price> monthly = grouper.periodList(history, Period.Month);
		EMA2 ema = new EMA2(20);
		double smaValue = 0;
		for (Price price : monthly) {
			smaValue = ema.compute(price.getClose());
		}
		BigDecimal decimal = new BigDecimal(smaValue);
		decimal = decimal.setScale(2,BigDecimal.ROUND_HALF_UP);
		System.out.println("Last History Date"+monthly.get(monthly.size()-1).getDate());
		System.out.println("Monthly EMA ="+  decimal.toPlainString());
		// Think Or Swim Monthly EMA for March 2013 139.27
		assertEquals(139.27d, decimal.doubleValue());
	}

	public void testEma2WeeklyQQQ() throws IOException {
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.QQQ);
		Grouper grouper = new Grouper();
		List<Price> weekly = grouper.periodList(history, Period.Week);
		EMA2 ema = new EMA2(20);
		double smaValue = 0;
		for (Price price : weekly) {
			smaValue = ema.compute(price.getClose());
		}
		BigDecimal decimal = new BigDecimal(smaValue);
		decimal = decimal.setScale(2,BigDecimal.ROUND_HALF_UP);
		System.out.println("Last History Date"+weekly.get(weekly.size()-1).getDate());
		System.out.println("Weekly EMA ="+  decimal.toPlainString());
		// Week April 1 through 5 Think Swim EMA 67.32
		assertEquals(67.32d, decimal.doubleValue());
	}
	
	public void testEma2MonthyQQQ() throws IOException {
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.QQQ);
		Grouper grouper = new Grouper();
		List<Price> monthly = grouper.periodList(history, Period.Month);
		EMA2 ema = new EMA2(20);
		double smaValue = 0;
		for (Price price : monthly) {
			smaValue = ema.compute(price.getClose());
		}
		BigDecimal decimal = new BigDecimal(smaValue);
		decimal = decimal.setScale(2,BigDecimal.ROUND_HALF_UP);
		System.out.println("Last History Date"+monthly.get(monthly.size()-1).getDate());
		System.out.println("Monthly EMA ="+  decimal.toPlainString());
		// Think Or Swim Monthly EMA for March 2013 63.36
		assertEquals(63.36d, decimal.doubleValue());
	}	
	public void testEma3Weekly() throws IOException {
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		Grouper grouper = new Grouper();
		List<Price> weekly = grouper.periodList(history, Period.Week);
		System.out.println("history size = "+history.size()+" group size = " + weekly.size());
		double[] prices = new double[weekly.size()];
		for (int ndx = 0; ndx < weekly.size(); ndx++) {
			prices[ndx] = weekly.get(ndx).getClose();
		}
		System.out.println("Weekly EMA3 = "+  EMA3.format(EMA3.ema(20,prices)));
		assertEquals(150.27d, new BigDecimal(EMA3.ema(20,prices)).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
	}
	
	public void testEma3Monthy() throws IOException {
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		Grouper grouper = new Grouper();
		List<Price> monthly = grouper.periodList(history, Period.Month);
		System.out.println("history size = "+history.size()+" group size = " + monthly.size());
		double[] prices = new double[monthly.size()];
		for (int ndx = 0; ndx < monthly.size(); ndx++) {
			prices[ndx] = monthly.get(ndx).getClose();
		}
		System.out.println("Monthly EMA3 ="+  EMA3.format(EMA3.ema(20,prices)));
		// Think Or Swim Monthly EMA for March 2013 139.27
		assertEquals(139.27d, new BigDecimal(EMA3.ema(20,prices)).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
	}

	public void testEma4Weekly() throws IOException {
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		Grouper grouper = new Grouper();
		List<Price> weekly = grouper.periodList(history, Period.Week);
		System.out.println("history size = "+history.size()+" group size = " + weekly.size());
		EMA4 ema = new EMA4(20);
		for (int ndx = 0; ndx < weekly.size(); ndx++) {
			ema.update(weekly.get(ndx).getClose());
		}
		BigDecimal decimal = new BigDecimal(ema.value());
		decimal = decimal.setScale(2,BigDecimal.ROUND_HALF_UP);
		System.out.println("Weekly EMA4 ="+  decimal.toPlainString());
		// Think Or Swim Monthly EMA for March 2013 139.27
		assertEquals(150.27d, decimal.doubleValue());
	}
	
	public void testEma4Monthy() throws IOException {
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		Grouper grouper = new Grouper();
		List<Price> monthly = grouper.periodList(history, Period.Month);
		System.out.println("history size = "+history.size()+" group size = " + monthly.size());
		EMA4 ema = new EMA4(20);
		for (int ndx = 0; ndx < monthly.size(); ndx++) {
			ema.update(monthly.get(ndx).getClose());
		}
		BigDecimal decimal = new BigDecimal(ema.value());
		decimal = decimal.setScale(2,BigDecimal.ROUND_HALF_UP);
		System.out.println("Monthly EMA4 ="+  decimal.toPlainString());
		// Think Or Swim Monthly EMA for March 2013 139.27
		assertEquals(139.27d, decimal.doubleValue());
	}
}
