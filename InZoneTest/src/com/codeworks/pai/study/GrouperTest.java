package com.codeworks.pai.study;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.test.AndroidTestCase;

import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.mock.TestDataLoader;

public class GrouperTest extends AndroidTestCase {
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	


	public void testWeekGroup() throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		String[] resultDates = new String[] {

		"2003-01-31", "2003-02-07", "2003-02-14", "2003-02-21", "2003-02-28", "2003-03-07", "2003-03-14", "2003-03-21", "2003-03-28", "2003-04-04",
				"2003-04-11", "2003-04-17", "2003-04-25", "2003-05-02", "2003-05-09", "2003-05-16", "2003-05-23", "2003-05-30", "2003-06-06", "2003-06-13",
				"2003-06-20", "2003-06-27", "2003-07-03", "2003-07-11", "2003-07-18", "2003-07-25", "2003-08-01", "2003-08-08", "2003-08-15", "2003-08-22",
				"2003-08-29", "2003-09-05", "2003-09-12", "2003-09-19", "2003-09-26", "2003-10-03", "2003-10-10", "2003-10-17", "2003-10-24", "2003-10-31",
				"2003-11-07", "2003-11-14", "2003-11-21", "2003-11-28", "2003-12-05", "2003-12-12", "2003-12-19", "2003-12-26", "2004-01-02", "2004-01-09",
				"2004-01-16", "2004-01-23", "2004-01-30", "2004-02-06", "2004-02-13", "2004-02-20", "2004-02-27", "2004-03-05", "2004-03-12", "2004-03-19",
				"2004-03-26", "2004-04-02", "2004-04-08", "2004-04-16", "2004-04-23", "2004-04-30", "2004-05-07", "2004-05-14", "2004-05-21", "2004-05-28",
				"2004-06-04", "2004-06-10", "2004-06-18", "2004-06-25", "2004-07-02", "2004-07-09", "2004-07-16", "2004-07-23", "2004-07-30", "2004-08-06",
				"2004-08-13", "2004-08-20", "2004-08-27", "2004-09-03", "2004-09-10", "2004-09-17", "2004-09-24", "2004-10-01", "2004-10-08", "2004-10-15",
				"2004-10-22", "2004-10-29", "2004-11-05", "2004-11-12", "2004-11-19", "2004-11-26", "2004-12-03", "2004-12-10", "2004-12-17", "2004-12-23",
				"2004-12-31", "2005-01-07", "2005-01-14", "2005-01-21", "2005-01-28", "2005-02-04", "2005-02-11", "2005-02-18", "2005-02-25", "2005-03-04",
				"2005-03-11", "2005-03-18", "2005-03-24", "2005-04-01", "2005-04-08", "2005-04-15", "2005-04-22", "2005-04-29", "2005-05-06", "2005-05-13",
				"2005-05-20", "2005-05-27", "2005-06-03", "2005-06-10", "2005-06-17", "2005-06-24", "2005-07-01", "2005-07-08", "2005-07-15", "2005-07-22",
				"2005-07-29", "2005-08-05", "2005-08-12", "2005-08-19", "2005-08-26", "2005-09-02", "2005-09-09", "2005-09-16", "2005-09-23", "2005-09-30",
				"2005-10-07", "2005-10-14", "2005-10-21", "2005-10-28", "2005-11-04", "2005-11-11", "2005-11-18", "2005-11-25", "2005-12-02", "2005-12-09",
				"2005-12-16", "2005-12-23", "2005-12-30", "2006-01-06", "2006-01-13", "2006-01-20", "2006-01-27", "2006-02-03", "2006-02-10", "2006-02-17",
				"2006-02-24", "2006-03-03", "2006-03-10", "2006-03-17", "2006-03-24", "2006-03-31", "2006-04-07", "2006-04-13", "2006-04-21", "2006-04-28",
				"2006-05-05", "2006-05-12", "2006-05-19", "2006-05-26", "2006-06-02", "2006-06-09", "2006-06-16", "2006-06-23", "2006-06-30", "2006-07-07",
				"2006-07-14", "2006-07-21", "2006-07-28", "2006-08-04", "2006-08-11", "2006-08-18", "2006-08-25", "2006-09-01", "2006-09-08", "2006-09-15",
				"2006-09-22", "2006-09-29", "2006-10-06", "2006-10-13", "2006-10-20", "2006-10-27", "2006-11-03", "2006-11-10", "2006-11-17", "2006-11-24",
				"2006-12-01", "2006-12-08", "2006-12-15", "2006-12-22", "2006-12-29", "2007-01-05", "2007-01-12", "2007-01-19", "2007-01-26", "2007-02-02",
				"2007-02-09", "2007-02-16", "2007-02-23", "2007-03-02", "2007-03-09", "2007-03-16", "2007-03-23", "2007-03-30", "2007-04-05", "2007-04-13",
				"2007-04-20", "2007-04-27", "2007-05-04", "2007-05-11", "2007-05-18", "2007-05-25", "2007-06-01", "2007-06-08", "2007-06-15", "2007-06-22",
				"2007-06-29", "2007-07-06", "2007-07-13", "2007-07-20", "2007-07-27", "2007-08-03", "2007-08-10", "2007-08-17", "2007-08-24", "2007-08-31",
				"2007-09-07", "2007-09-14", "2007-09-21", "2007-09-28", "2007-10-05", "2007-10-12", "2007-10-19", "2007-10-26", "2007-11-02", "2007-11-09",
				"2007-11-16", "2007-11-23", "2007-11-30", "2007-12-07", "2007-12-14", "2007-12-21", "2007-12-28", "2008-01-04", "2008-01-11", "2008-01-18",
				"2008-01-25", "2008-02-01", "2008-02-08", "2008-02-15", "2008-02-22", "2008-02-29", "2008-03-07", "2008-03-14", "2008-03-20", "2008-03-28",
				"2008-04-04", "2008-04-11", "2008-04-18", "2008-04-25", "2008-05-02", "2008-05-09", "2008-05-16", "2008-05-23", "2008-05-30", "2008-06-06",
				"2008-06-13", "2008-06-20", "2008-06-27", "2008-07-03", "2008-07-11", "2008-07-18", "2008-07-25", "2008-08-01", "2008-08-08", "2008-08-15",
				"2008-08-22", "2008-08-29", "2008-09-05", "2008-09-12", "2008-09-19", "2008-09-26", "2008-10-03", "2008-10-10", "2008-10-17", "2008-10-24",
				"2008-10-31", "2008-11-07", "2008-11-14", "2008-11-21", "2008-11-28", "2008-12-05", "2008-12-12", "2008-12-19", "2008-12-26", "2009-01-02",
				"2009-01-09", "2009-01-16", "2009-01-23", "2009-01-30", "2009-02-06", "2009-02-13", "2009-02-20", "2009-02-27", "2009-03-06", "2009-03-13",
				"2009-03-20", "2009-03-27", "2009-04-03", "2009-04-09", "2009-04-17", "2009-04-24", "2009-05-01", "2009-05-08", "2009-05-15", "2009-05-22",
				"2009-05-29", "2009-06-05", "2009-06-12", "2009-06-19", "2009-06-26", "2009-07-02", "2009-07-10", "2009-07-17", "2009-07-24", "2009-07-31",
				"2009-08-07", "2009-08-14", "2009-08-21", "2009-08-28", "2009-09-04", "2009-09-11", "2009-09-18", "2009-09-25", "2009-10-02", "2009-10-09",
				"2009-10-16", "2009-10-23", "2009-10-30", "2009-11-06", "2009-11-13", "2009-11-20", "2009-11-27", "2009-12-04", "2009-12-11", "2009-12-18",
				"2009-12-24", "2009-12-31", "2010-01-08", "2010-01-15", "2010-01-22", "2010-01-29", "2010-02-05", "2010-02-12", "2010-02-19", "2010-02-26",
				"2010-03-05", "2010-03-12", "2010-03-19", "2010-03-26", "2010-04-01", "2010-04-09", "2010-04-16", "2010-04-23", "2010-04-30", "2010-05-07",
				"2010-05-14", "2010-05-21", "2010-05-28", "2010-06-04", "2010-06-11", "2010-06-18", "2010-06-25", "2010-07-02", "2010-07-09", "2010-07-16",
				"2010-07-23", "2010-07-30", "2010-08-06", "2010-08-13", "2010-08-20", "2010-08-27", "2010-09-03", "2010-09-10", "2010-09-17", "2010-09-24",
				"2010-10-01", "2010-10-08", "2010-10-15", "2010-10-22", "2010-10-29", "2010-11-05", "2010-11-12", "2010-11-19", "2010-11-26", "2010-12-03",
				"2010-12-10", "2010-12-17", "2010-12-23", "2010-12-31", "2011-01-07", "2011-01-14", "2011-01-21", "2011-01-28", "2011-02-04", "2011-02-11",
				"2011-02-18", "2011-02-25", "2011-03-04", "2011-03-11", "2011-03-18", "2011-03-25", "2011-04-01", "2011-04-08", "2011-04-15", "2011-04-21",
				"2011-04-29", "2011-05-06", "2011-05-13", "2011-05-20", "2011-05-27", "2011-06-03", "2011-06-10", "2011-06-17", "2011-06-24", "2011-07-01",
				"2011-07-08", "2011-07-15", "2011-07-22", "2011-07-29", "2011-08-05", "2011-08-12", "2011-08-19", "2011-08-26", "2011-09-02", "2011-09-09",
				"2011-09-16", "2011-09-23", "2011-09-30", "2011-10-07", "2011-10-14", "2011-10-21", "2011-10-28", "2011-11-04", "2011-11-11", "2011-11-18",
				"2011-11-25", "2011-12-02", "2011-12-09", "2011-12-16", "2011-12-23", "2011-12-30", "2012-01-06", "2012-01-13", "2012-01-20", "2012-01-27",
				"2012-02-03", "2012-02-10", "2012-02-17", "2012-02-24", "2012-03-02", "2012-03-09", "2012-03-16", "2012-03-23", "2012-03-30", "2012-04-05",
				"2012-04-13", "2012-04-20", "2012-04-27", "2012-05-04", "2012-05-11", "2012-05-18", "2012-05-25", "2012-06-01", "2012-06-08", "2012-06-15",
				"2012-06-22", "2012-06-29", "2012-07-06", "2012-07-13", "2012-07-20", "2012-07-27", "2012-08-03", "2012-08-10", "2012-08-17", "2012-08-24",
				"2012-08-31", "2012-09-07", "2012-09-14", "2012-09-21", "2012-09-28", "2012-10-05", "2012-10-12", "2012-10-19", "2012-10-26", "2012-11-02",
				"2012-11-09", "2012-11-16", "2012-11-23", "2012-11-30", "2012-12-07", "2012-12-14", "2012-12-21", "2012-12-28", "2013-01-04", "2013-01-11",
				"2013-01-18", "2013-01-25", "2013-02-01", "2013-02-08", "2013-02-15", "2013-02-22", "2013-03-01", "2013-03-08", "2013-03-15", "2013-03-22",
				"2013-03-28", "2013-04-05", "2013-04-12" };

		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		System.out.println("Price History record Count=" + history.size());
		Grouper grouper = new Grouper();
		List<Price> group = grouper.periodList(history, Period.Week);
		System.out.println("Price Weekly Group record Count=" + group.size());
		int ndx = 0;
		for (Price price : group) {
			assertEquals(resultDates[ndx], sdf.format(price.getDate()));
			ndx++;
			//System.out.println(price.toString());
		}
	}

	public void testWeekGroupFirst() throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		String[] resultDates = new String[] { 
				"2003-02-03", "2003-02-10", "2003-02-18", "2003-02-24", "2003-03-03", "2003-03-10", "2003-03-17", "2003-03-24", "2003-03-31", "2003-04-07", 
				"2003-04-14", "2003-04-21", "2003-04-28", "2003-05-05", "2003-05-12", "2003-05-19", "2003-05-27", "2003-06-02", "2003-06-09", "2003-06-16", 
				"2003-06-23", "2003-06-30", "2003-07-07", "2003-07-14", "2003-07-21", "2003-07-28", "2003-08-04", "2003-08-11", "2003-08-18", "2003-08-25", 
				"2003-09-02", "2003-09-08", "2003-09-15", "2003-09-22", "2003-09-29", "2003-10-06", "2003-10-13", "2003-10-20", "2003-10-27", "2003-11-03", 
				"2003-11-10", "2003-11-17", "2003-11-24", "2003-12-01", "2003-12-08", "2003-12-15", "2003-12-22", "2003-12-29", "2004-01-05", "2004-01-12", 
				"2004-01-20", "2004-01-26", "2004-02-02", "2004-02-09", "2004-02-17", "2004-02-23", "2004-03-01", "2004-03-08", "2004-03-15", "2004-03-22", 
				"2004-03-29", "2004-04-05", "2004-04-12", "2004-04-19", "2004-04-26", "2004-05-03", "2004-05-10", "2004-05-17", "2004-05-24", "2004-06-01", 
				"2004-06-07", "2004-06-14", "2004-06-21", "2004-06-28", "2004-07-06", "2004-07-12", "2004-07-19", "2004-07-26", "2004-08-02", "2004-08-09", 
				"2004-08-16", "2004-08-23", "2004-08-30", "2004-09-07", "2004-09-13", "2004-09-20", "2004-09-27", "2004-10-04", "2004-10-11", "2004-10-18", 
				"2004-10-25", "2004-11-01", "2004-11-08", "2004-11-15", "2004-11-22", "2004-11-29", "2004-12-06", "2004-12-13", "2004-12-20", "2004-12-27", 
				"2005-01-03", "2005-01-10", "2005-01-18", "2005-01-24", "2005-01-31", "2005-02-07", "2005-02-14", "2005-02-22", "2005-02-28", "2005-03-07", 
				"2005-03-14", "2005-03-21", "2005-03-28", "2005-04-04", "2005-04-11", "2005-04-18", "2005-04-25", "2005-05-02", "2005-05-09", "2005-05-16", 
				"2005-05-23", "2005-05-31", "2005-06-06", "2005-06-13", "2005-06-20", "2005-06-27", "2005-07-05", "2005-07-11", "2005-07-18", "2005-07-25", 
				"2005-08-01", "2005-08-08", "2005-08-15", "2005-08-22", "2005-08-29", "2005-09-06", "2005-09-12", "2005-09-19", "2005-09-26", "2005-10-03", 
				"2005-10-10", "2005-10-17", "2005-10-24", "2005-10-31", "2005-11-07", "2005-11-14", "2005-11-21", "2005-11-28", "2005-12-05", "2005-12-12", 
				"2005-12-19", "2005-12-27", "2006-01-03", "2006-01-09", "2006-01-17", "2006-01-23", "2006-01-30", "2006-02-06", "2006-02-13", "2006-02-21", 
				"2006-02-27", "2006-03-06", "2006-03-13", "2006-03-20", "2006-03-27", "2006-04-03", "2006-04-10", "2006-04-17", "2006-04-24", "2006-05-01", 
				"2006-05-08", "2006-05-15", "2006-05-22", "2006-05-30", "2006-06-05", "2006-06-12", "2006-06-19", "2006-06-26", "2006-07-03", "2006-07-10", 
				"2006-07-17", "2006-07-24", "2006-07-31", "2006-08-07", "2006-08-14", "2006-08-21", "2006-08-28", "2006-09-05", "2006-09-11", "2006-09-18", 
				"2006-09-25", "2006-10-02", "2006-10-09", "2006-10-16", "2006-10-23", "2006-10-30", "2006-11-06", "2006-11-13", "2006-11-20", "2006-11-27", 
				"2006-12-04", "2006-12-11", "2006-12-18", "2006-12-26", "2007-01-03", "2007-01-08", "2007-01-16", "2007-01-22", "2007-01-29", "2007-02-05", 
				"2007-02-12", "2007-02-20", "2007-02-26", "2007-03-05", "2007-03-12", "2007-03-19", "2007-03-26", "2007-04-02", "2007-04-09", "2007-04-16", 
				"2007-04-23", "2007-04-30", "2007-05-07", "2007-05-14", "2007-05-21", "2007-05-29", "2007-06-04", "2007-06-11", "2007-06-18", "2007-06-25", 
				"2007-07-02", "2007-07-09", "2007-07-16", "2007-07-23", "2007-07-30", "2007-08-06", "2007-08-13", "2007-08-20", "2007-08-27", "2007-09-04", 
				"2007-09-10", "2007-09-17", "2007-09-24", "2007-10-01", "2007-10-08", "2007-10-15", "2007-10-22", "2007-10-29", "2007-11-05", "2007-11-12", 
				"2007-11-19", "2007-11-26", "2007-12-03", "2007-12-10", "2007-12-17", "2007-12-24", "2007-12-31", "2008-01-07", "2008-01-14", "2008-01-22", 
				"2008-01-28", "2008-02-04", "2008-02-11", "2008-02-19", "2008-02-25", "2008-03-03", "2008-03-10", "2008-03-17", "2008-03-24", "2008-03-31", 
				"2008-04-07", "2008-04-14", "2008-04-21", "2008-04-28", "2008-05-05", "2008-05-12", "2008-05-19", "2008-05-27", "2008-06-02", "2008-06-09", 
				"2008-06-16", "2008-06-23", "2008-06-30", "2008-07-07", "2008-07-14", "2008-07-21", "2008-07-28", "2008-08-04", "2008-08-11", "2008-08-18", 
				"2008-08-25", "2008-09-02", "2008-09-08", "2008-09-15", "2008-09-22", "2008-09-29", "2008-10-06", "2008-10-13", "2008-10-20", "2008-10-27", 
				"2008-11-03", "2008-11-10", "2008-11-17", "2008-11-24", "2008-12-01", "2008-12-08", "2008-12-15", "2008-12-22", "2008-12-29", "2009-01-05", 
				"2009-01-12", "2009-01-20", "2009-01-26", "2009-02-02", "2009-02-09", "2009-02-17", "2009-02-23", "2009-03-02", "2009-03-09", "2009-03-16", 
				"2009-03-23", "2009-03-30", "2009-04-06", "2009-04-13", "2009-04-20", "2009-04-27", "2009-05-04", "2009-05-11", "2009-05-18", "2009-05-26", 
				"2009-06-01", "2009-06-08", "2009-06-15", "2009-06-22", "2009-06-29", "2009-07-06", "2009-07-13", "2009-07-20", "2009-07-27", "2009-08-03", 
				"2009-08-10", "2009-08-17", "2009-08-24", "2009-08-31", "2009-09-08", "2009-09-14", "2009-09-21", "2009-09-28", "2009-10-05", "2009-10-12", 
				"2009-10-19", "2009-10-26", "2009-11-02", "2009-11-09", "2009-11-16", "2009-11-23", "2009-11-30", "2009-12-07", "2009-12-14", "2009-12-21", 
				"2009-12-28", "2010-01-04", "2010-01-11", "2010-01-19", "2010-01-25", "2010-02-01", "2010-02-08", "2010-02-16", "2010-02-22", "2010-03-01", 
				"2010-03-08", "2010-03-15", "2010-03-22", "2010-03-29", "2010-04-05", "2010-04-12", "2010-04-19", "2010-04-26", "2010-05-03", "2010-05-10",
				"2010-05-17", "2010-05-24", "2010-06-01", "2010-06-07", "2010-06-14", "2010-06-21", "2010-06-28", "2010-07-06", "2010-07-12", "2010-07-19", 
				"2010-07-26", "2010-08-02", "2010-08-09", "2010-08-16", "2010-08-23", "2010-08-30", "2010-09-07", "2010-09-13", "2010-09-20", "2010-09-27", 
				"2010-10-04", "2010-10-11", "2010-10-18", "2010-10-25", "2010-11-01", "2010-11-08", "2010-11-15", "2010-11-22", "2010-11-29", "2010-12-06", 
				"2010-12-13", "2010-12-20", "2010-12-27", "2011-01-03", "2011-01-10", "2011-01-18", "2011-01-24", "2011-01-31", "2011-02-07", "2011-02-14", 
				"2011-02-22", "2011-02-28", "2011-03-07", "2011-03-14", "2011-03-21", "2011-03-28", "2011-04-04", "2011-04-11", "2011-04-18", "2011-04-25",
				"2011-05-02", "2011-05-09", "2011-05-16", "2011-05-23", "2011-05-31", "2011-06-06", "2011-06-13", "2011-06-20", "2011-06-27", "2011-07-05",
				"2011-07-11", "2011-07-18", "2011-07-25", "2011-08-01", "2011-08-08", "2011-08-15", "2011-08-22", "2011-08-29", "2011-09-06", "2011-09-12",
				"2011-09-19", "2011-09-26", "2011-10-03", "2011-10-10", "2011-10-17", "2011-10-24", "2011-10-31", "2011-11-07", "2011-11-14", "2011-11-21",
				"2011-11-28", "2011-12-05", "2011-12-12", "2011-12-19", "2011-12-27", "2012-01-03", "2012-01-09", "2012-01-17", "2012-01-23", "2012-01-30",
				"2012-02-06", "2012-02-13", "2012-02-21", "2012-02-27", "2012-03-05", "2012-03-12", "2012-03-19", "2012-03-26", "2012-04-02", "2012-04-09",
				"2012-04-16", "2012-04-23", "2012-04-30", "2012-05-07", "2012-05-14", "2012-05-21", "2012-05-29", "2012-06-04", "2012-06-11", "2012-06-18",
				"2012-06-25", "2012-07-02", "2012-07-09", "2012-07-16", "2012-07-23", "2012-07-30", "2012-08-06", "2012-08-13", "2012-08-20", "2012-08-27",
				"2012-09-04", "2012-09-10", "2012-09-17", "2012-09-24", "2012-10-01", "2012-10-08", "2012-10-15", "2012-10-22", "2012-10-31", "2012-11-05",
				"2012-11-12", "2012-11-19", "2012-11-26", "2012-12-03", "2012-12-10", "2012-12-17", "2012-12-24", "2012-12-31", "2013-01-07", "2013-01-14",
				"2013-01-22", "2013-01-28", "2013-02-04", "2013-02-11", "2013-02-19", "2013-02-25", "2013-03-04", "2013-03-11", "2013-03-18", "2013-03-25", "2013-04-01", "2013-04-08" 
		};

		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		System.out.println("Price History record Count=" + history.size());
		Grouper grouper = new Grouper();
		List<Price> group = grouper.periodListBeginning(history, Period.Week);
		int ndx = 0;
		//String alldates = "";
		for (Price price : group) {
			assertEquals(resultDates[ndx], sdf.format(price.getDate()));
			//alldates += "\""+dateFormat.format(price.getDate())+"\", ";
			ndx++;
			//System.out.println(price.toString());
		}
	}

	public void testMonthGroup() throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		String[] resultDates = new String[] {

		"2003-01-31", "2003-02-28", "2003-03-31", "2003-04-30", "2003-05-30", "2003-06-30", "2003-07-31", "2003-08-29", "2003-09-30", "2003-10-31",
				"2003-11-28", "2003-12-31", "2004-01-30", "2004-02-27", "2004-03-31", "2004-04-30", "2004-05-28", "2004-06-30", "2004-07-30", "2004-08-31",
				"2004-09-30", "2004-10-29", "2004-11-30", "2004-12-31", "2005-01-31", "2005-02-28", "2005-03-31", "2005-04-29", "2005-05-31", "2005-06-30",
				"2005-07-29", "2005-08-31", "2005-09-30", "2005-10-31", "2005-11-30", "2005-12-30", "2006-01-31", "2006-02-28", "2006-03-31", "2006-04-28",
				"2006-05-31", "2006-06-30", "2006-07-31", "2006-08-31", "2006-09-29", "2006-10-31", "2006-11-30", "2006-12-29", "2007-01-31", "2007-02-28",
				"2007-03-30", "2007-04-30", "2007-05-31", "2007-06-29", "2007-07-31", "2007-08-31", "2007-09-28", "2007-10-31", "2007-11-30", "2007-12-31",
				"2008-01-31", "2008-02-29", "2008-03-31", "2008-04-30", "2008-05-30", "2008-06-30", "2008-07-31", "2008-08-29", "2008-09-30", "2008-10-31",
				"2008-11-28", "2008-12-31", "2009-01-30", "2009-02-27", "2009-03-31", "2009-04-30", "2009-05-29", "2009-06-30", "2009-07-31", "2009-08-31",
				"2009-09-30", "2009-10-30", "2009-11-30", "2009-12-31", "2010-01-29", "2010-02-26", "2010-03-31", "2010-04-30", "2010-05-28", "2010-06-30",
				"2010-07-30", "2010-08-31", "2010-09-30", "2010-10-29", "2010-11-30", "2010-12-31", "2011-01-31", "2011-02-28", "2011-03-31", "2011-04-29",
				"2011-05-31", "2011-06-30", "2011-07-29", "2011-08-31", "2011-09-30", "2011-10-31", "2011-11-30", "2011-12-30", "2012-01-31", "2012-02-29",
				"2012-03-30", "2012-04-30", "2012-05-31", "2012-06-29", "2012-07-31", "2012-08-31", "2012-09-28", "2012-10-31", "2012-11-30", "2012-12-31",
				"2013-01-31", "2013-02-28", "2013-03-28", "2013-04-12" };

		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);

		System.out.println("Price History record Count=" + history.size());
		Grouper grouper = new Grouper();
		List<Price> group = grouper.periodList(history, Period.Month);
		System.out.println("Price Monthly Group record Count=" + group.size());
		int ndx = 0;
		for (Price price : group) {
			assertEquals(resultDates[ndx], sdf.format(price.getDate()));
			ndx++;
			//System.out.println(price.toString());
		}
	}

	public void testMonthGroupFirst() throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		String[] resultDates = new String[] {
				"2003-01-29", "2003-02-03", "2003-03-03", "2003-04-01", "2003-05-01", "2003-06-02", "2003-07-01", "2003-08-01", "2003-09-02", "2003-10-01", 
				"2003-11-03", "2003-12-01", "2004-01-02", "2004-02-02", "2004-03-01", "2004-04-01", "2004-05-03", "2004-06-01", "2004-07-01", "2004-08-02", 
				"2004-09-01", "2004-10-01", "2004-11-01", "2004-12-01", "2005-01-03", "2005-02-01", "2005-03-01", "2005-04-01", "2005-05-02", "2005-06-01", 
				"2005-07-01", "2005-08-01", "2005-09-01", "2005-10-03", "2005-11-01", "2005-12-01", "2006-01-03", "2006-02-01", "2006-03-01", "2006-04-03", 
				"2006-05-01", "2006-06-01", "2006-07-03", "2006-08-01", "2006-09-01", "2006-10-02", "2006-11-01", "2006-12-01", "2007-01-03", "2007-02-01",
				"2007-03-01", "2007-04-02", "2007-05-01", "2007-06-01", "2007-07-02", "2007-08-01", "2007-09-04", "2007-10-01", "2007-11-01", "2007-12-03", 
				"2008-01-02", "2008-02-01", "2008-03-03", "2008-04-01", "2008-05-01", "2008-06-02", "2008-07-01", "2008-08-01", "2008-09-02", "2008-10-01", 
				"2008-11-03", "2008-12-01", "2009-01-02", "2009-02-02", "2009-03-02", "2009-04-01", "2009-05-01", "2009-06-01", "2009-07-01", "2009-08-03", 
				"2009-09-01", "2009-10-01", "2009-11-02", "2009-12-01", "2010-01-04", "2010-02-01", "2010-03-01", "2010-04-01", "2010-05-03", "2010-06-01",
				"2010-07-01", "2010-08-02", "2010-09-01", "2010-10-01", "2010-11-01", "2010-12-01", "2011-01-03", "2011-02-01", "2011-03-01", "2011-04-01",
				"2011-05-02", "2011-06-01", "2011-07-01", "2011-08-01", "2011-09-01", "2011-10-03", "2011-11-01", "2011-12-01", "2012-01-03", "2012-02-01", 
				"2012-03-01", "2012-04-02", "2012-05-01", "2012-06-01", "2012-07-02", "2012-08-01", "2012-09-04", "2012-10-01", "2012-11-01", "2012-12-03",
				"2013-01-02", "2013-02-01", "2013-03-01", "2013-04-01"

		};

		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		System.out.println("Price History record Count=" + history.size());
		Grouper grouper = new Grouper();
		List<Price> group = grouper.periodListBeginning(history, Period.Month);
		int ndx = 0;
		//String resultString = "";
		for (Price price : group) {
			assertEquals(resultDates[ndx], sdf.format(price.getDate()));
			ndx++;
			//resultString += "\""+sdf.format(price.getDate())+"\", ";
		}
	}
}
