package com.codeworks.pai.processor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import junit.framework.TestCase;

public class DateTest extends TestCase {
	/*
	 *                     <option data-selectbox-link="/q/op?s=SPY&date=1421452800" value="1421452800"  >January 17, 2015</option>
                

                    <option data-selectbox-link="/q/op?s=SPY&date=1421971200" value="1421971200"  >January 23, 2015</option>
                

                    <option data-selectbox-link="/q/op?s=SPY&date=1424390400" value="1424390400"  >February 20, 2015</option>
                

                    <option data-selectbox-link="/q/op?s=SPY&date=1426809600" value="1426809600"  >March 20, 2015</option>
                

                    <option data-selectbox-link="/q/op?s=SPY&date=1427760000" value="1427760000"  >March 31, 2015</option>
                

                    <option data-selectbox-link="/q/op?s=SPY&date=1429228800" value="1429228800"  >April 17, 2015</option>
                
	 */
	public void testDate() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
		sdf.setTimeZone(TimeZone.getDefault());
		System.out.println(sdf.parse("February 20, 2015").toString());
		System.out.println(sdf.parse("February 20, 2015").getTime() / 1000);
		long ms = (1424390400) - (60*60*4);
		System.out.println(ms);
		System.out.println(new DateTime(ms, DateTimeZone.UTC).toString());
		System.out.println(new Date(ms).toString());
	}
}
