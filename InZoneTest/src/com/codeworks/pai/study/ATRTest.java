package com.codeworks.pai.study;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import android.test.AndroidTestCase;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.mock.TestDataLoader;

public class ATRTest extends AndroidTestCase {

	public void testAtr() throws IOException {
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		Collections.sort(history);
		double value = 0;
		value = ATR.compute(history, 20);
		System.out.println("Daily ATR = "+format(value)+" for date "+history.get(history.size()-1).getDate());
		// Sink or Swim has 1.4918, has to be daily rounding?
		assertEquals(1.46d, PaiUtils.round(value));
	}
	
	String format(double value) {
		return new BigDecimal(value).setScale(6,BigDecimal.ROUND_HALF_UP).toPlainString();
	}
}
