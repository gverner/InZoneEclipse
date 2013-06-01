package com.codeworks.pai.processor;

import java.util.Date;
import java.util.List;

import android.test.AndroidTestCase;

import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.db.model.Security;

public class YahooReaderTest extends AndroidTestCase {
	YahooReader reader;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		reader = new YahooReader();
	}
	
	public void testReadCurrentPrice() {
		Security security = new Security("SPY");
		assertTrue(reader.readCurrentPrice(security));
		assertNotSame(0d,security.getCurrentPrice());
	}

	public void testReadBlankCurrentPrice() {
		Security security = new Security("");
		assertFalse(reader.readCurrentPrice(security));
		assertEquals(0d,security.getCurrentPrice());
	}


	public void testReadNullCurrentPrice() {
		Security security = new Security(null);
		assertFalse(reader.readCurrentPrice(security));
		assertEquals(0d,security.getCurrentPrice());
	}
	
	public void testParseDate() {
		String testDate = "2013-05-25";
		Date date = reader.parseDate(testDate, "TEST PARSE DATE");
		assertEquals(testDate, reader.dateFormat.format(date));
	}

	public void testParseBadDate() {
		String testDate = "20130525";
		Date date = reader.parseDate(testDate, "TEST PARSE BAD DATE");
		assertEquals(null, date);
	}

	public void testParseDateTime() {
		String testDate = "05/25/2013 09:30AM";
		Date date = reader.parseDateTime(testDate, "TEST PARSE DATE TIME");
		assertEquals(testDate, reader.dateTimeFormat.format(date));
	}

	public void testParseBadDateTime() {
		String testDate = "";//"05/25/2013 09:30AM";
		Date date = reader.parseDateTime(testDate, "TEST PARSE BAD DATE TIME");
		assertEquals(null, date);
	}

	public void testParseNullDateTime() {
		String testDate = null;
		Date date = reader.parseDateTime(testDate, "TEST PARSE BAD DATE TIME");
		assertEquals(null, date);
	}
	
	public void testParseDouble() {
		String testDouble = "1.232";
		double result = reader.parseDouble(testDouble, "TEST PARSE DOUBLE");
		assertEquals(Double.parseDouble(testDouble), result);
	}
	
	public void testParseBlankDouble() {
		String testDouble = "";
		double result = reader.parseDouble(testDouble, "TEST PARSE BLANK DOUBLE");
		assertEquals(0d,result);
	}

	public void testParseNullDouble() {
		String testDouble = null;
		double result = reader.parseDouble(testDouble, "TEST PARSE NULL DOUBLE");
		assertEquals(0d,result);
	}

	public void testBuildHistoryUrl() {
		String url = reader.buildHistoryUrl("SPY");
		System.out.println(url);
	}
	
	public void testReadHistory() {
		List<Price> history = reader.readHistory("SPY");
		System.out.println("history size="+history.size());
		assertTrue(history.size() > 200);
	}	
	
	public void testReadBlankHistory() {
		List<Price> history = reader.readHistory("");
		System.out.println("history size="+history.size());
		assertTrue(history.size() == 0);
	}	

	public void testReadNullHistory() {
		List<Price> history = reader.readHistory("");
		System.out.println("history size="+history.size());
		assertTrue(history.size() == 0);
	}	
	
}
