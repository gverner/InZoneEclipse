package com.codeworks.pai.db.model;

import junit.framework.TestCase;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.mock.MockDataReader;
import com.codeworks.pai.study.Period;


public class SmaRulesTest extends TestCase {
	PaiStudy spyStudy;
	PaiStudy gldStudy;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		spyStudy = new PaiStudy("SPY");
		// Numbers from 4/12/2013
		spyStudy.setPrice(MockDataReader.SPY_PRICE);
		spyStudy.setPriceLastWeek(155.16d);
		spyStudy.setAverageTrueRange(1.46d);
		spyStudy.setPriceLastMonth(156.67d);
		spyStudy.setSmaLastWeek(153.04d);
		spyStudy.setSmaLastMonth(142.85d);
		spyStudy.setSmaWeek(153.91d);
		spyStudy.setSmaMonth(144.42d);
		spyStudy.setSmaStddevWeek(((158.99-153.91d)/2));
		spyStudy.setSmaStddevMonth(7.89d);
		
		spyStudy.setMaLastWeek(153.04d);
		spyStudy.setMaLastMonth(142.85d);
		spyStudy.setMaWeek(153.91d);
		spyStudy.setMaMonth(144.42d);
		spyStudy.setStddevWeek(((158.99-153.91d)/2));
		spyStudy.setStddevMonth(7.89d);
		
		
		gldStudy = new PaiStudy("GLD");
		gldStudy.setPrice(MockDataReader.GLD_PRICE);
		gldStudy.setPriceLastWeek(152.81d);
		gldStudy.setPriceLastMonth(154.47d);
		gldStudy.setAverageTrueRange(1.94D);
		gldStudy.setSmaWeek(156.46d);
		gldStudy.setSmaLastWeek(157.14d);
		gldStudy.setSmaMonth(144.42d);
		gldStudy.setSmaLastMonth(160.40d);
		gldStudy.setSmaStddevWeek(((164.20-154.86d)/2));
		gldStudy.setSmaStddevMonth(7.89d);


	}

	public void testSpyRules() {
		SmaRules rules = new SmaRules(spyStudy);
		PaiStudy study = spyStudy;
		assertEquals(spyStudy.getSmaWeek() ,rules.calcBuyZoneBottom());
		assertEquals(spyStudy.getSmaWeek() + (spyStudy.getSmaStddevWeek() * SmaRules.ZONE_INNER), rules.calcBuyZoneTop());
		assertEquals(spyStudy.getSmaWeek() + (spyStudy.getSmaStddevWeek() * SmaRules.ZONE_OUTER) ,rules.calcSellZoneBottom());
		assertEquals(spyStudy.getSmaWeek() + (spyStudy.getSmaStddevWeek() * SmaRules.ZONE_OUTER) + rules.pierceOffset(),rules.calcSellZoneTop());
		assertTrue(rules.isUpTrendMonthly());
		assertTrue(rules.isUpTrendWeekly());
		assertEquals(153.91d, rules.calcLowerSellZoneTop(Period.Week));
		assertEquals(153.91d - (spyStudy.getSmaStddevWeek() * SmaRules.ZONE_INNER), rules.calcLowerSellZoneBottom(Period.Week));
		assertEquals(153.91d - (spyStudy.getSmaStddevWeek() * SmaRules.ZONE_OUTER) ,rules.calcLowerBuyZoneTop(Period.Week));
		assertEquals(153.91d - ((spyStudy.getSmaStddevWeek() * SmaRules.ZONE_OUTER) + rules.pierceOffset()),rules.calcLowerBuyZoneBottom(Period.Week));
		
		assertEquals(study.getSmaMonth(), rules.calcLowerSellZoneTop(Period.Month));
		assertEquals(study.getSmaMonth() - (study.getSmaStddevMonth() * SmaRules.ZONE_INNER), rules.calcLowerSellZoneBottom(Period.Month));
		assertEquals(study.getSmaMonth() - (study.getSmaStddevMonth() * SmaRules.ZONE_OUTER) ,rules.calcLowerBuyZoneTop(Period.Month));
		assertEquals(study.getSmaMonth() - ((study.getSmaStddevMonth() * SmaRules.ZONE_OUTER) + rules.pierceOffset()),rules.calcLowerBuyZoneBottom(Period.Month));

		assertEquals(study.getSmaMonth() ,rules.calcUpperBuyZoneBottom(Period.Month));
		assertEquals(study.getSmaMonth() + (study.getSmaStddevMonth() * SmaRules.ZONE_INNER), rules.calcUpperBuyZoneTop(Period.Month));
		assertEquals(study.getSmaMonth() + (study.getSmaStddevMonth() * SmaRules.ZONE_OUTER) ,rules.calcUpperSellZoneBottom(Period.Month));
		assertEquals(study.getSmaMonth() + (study.getSmaStddevMonth() * SmaRules.ZONE_OUTER) + rules.pierceOffset(),rules.calcUpperSellZoneTop(Period.Month));

	}

	public void testGldRules() {
		SmaRules rules = new SmaRules(gldStudy);
		PaiStudy study = gldStudy;
		assertEquals(156.46d,rules.calcSellZoneTop());
		assertEquals(156.46d - (gldStudy.getSmaStddevWeek() * SmaRules.ZONE_INNER), rules.calcSellZoneBottom());
		assertEquals(156.46d - (gldStudy.getSmaStddevWeek() * SmaRules.ZONE_OUTER) ,rules.calcBuyZoneTop());
		assertEquals(156.46d - ((gldStudy.getSmaStddevWeek() * SmaRules.ZONE_OUTER) + rules.pierceOffset()),rules.calcBuyZoneBottom());
		assertEquals(rules.calcSellZoneTop(), rules.calcLowerSellZoneTop(Period.Week));
		assertEquals(rules.calcSellZoneBottom(), rules.calcLowerSellZoneBottom(Period.Week));
		assertEquals(rules.calcBuyZoneTop(), rules.calcLowerBuyZoneTop(Period.Week));
		assertEquals(rules.calcBuyZoneBottom(), rules.calcLowerBuyZoneBottom(Period.Week));
		assertEquals(gldStudy.getSmaWeek() ,rules.calcUpperBuyZoneBottom(Period.Week));
		assertEquals(gldStudy.getSmaWeek() + (gldStudy.getSmaStddevWeek() * SmaRules.ZONE_INNER), rules.calcUpperBuyZoneTop(Period.Week));
		assertEquals(gldStudy.getSmaWeek() + (gldStudy.getSmaStddevWeek() * SmaRules.ZONE_OUTER) ,rules.calcUpperSellZoneBottom(Period.Week));
		assertEquals(gldStudy.getSmaWeek() + (gldStudy.getSmaStddevWeek() * SmaRules.ZONE_OUTER) + rules.pierceOffset(),rules.calcUpperSellZoneTop(Period.Week));
		assertTrue(rules.isDownTrendMonthly());
		assertTrue(rules.isDownTrendWeekly());
		assertEquals(study.getSmaMonth(), rules.calcLowerSellZoneTop(Period.Month));
		assertEquals(study.getSmaMonth() - (study.getSmaStddevMonth() * SmaRules.ZONE_INNER), rules.calcLowerSellZoneBottom(Period.Month));
		assertEquals(study.getSmaMonth() - (study.getSmaStddevMonth() * SmaRules.ZONE_OUTER) ,rules.calcLowerBuyZoneTop(Period.Month));
		assertEquals(PaiUtils.round(study.getSmaMonth() - ((study.getSmaStddevMonth() * SmaRules.ZONE_OUTER) + rules.pierceOffset())), PaiUtils.round(rules.calcLowerBuyZoneBottom(Period.Month)));

		assertEquals(study.getSmaMonth() ,rules.calcUpperBuyZoneBottom(Period.Month));
		assertEquals(study.getSmaMonth() + (study.getSmaStddevMonth() * SmaRules.ZONE_INNER), rules.calcUpperBuyZoneTop(Period.Month));
		assertEquals(study.getSmaMonth() + (study.getSmaStddevMonth() * SmaRules.ZONE_OUTER) ,rules.calcUpperSellZoneBottom(Period.Month));
		assertEquals(study.getSmaMonth() + (study.getSmaStddevMonth() * SmaRules.ZONE_OUTER) + rules.pierceOffset(),rules.calcUpperSellZoneTop(Period.Month));

	}

	public void testTradeBelowMovingAverageToday() {
		PaiStudy study = spyStudy;
		study.setLow(study.getMaWeek() - 0.01d );
		EmaRules rules = new EmaRules(study);
		assertTrue(rules.isUpTrend(Period.Week));
		assertTrue(rules.hasTradedBelowMAToday());
		SmaRules smaRules = new SmaRules(study);
		assertTrue(smaRules.isUpTrend(Period.Week));
		assertTrue(smaRules.hasTradedBelowMAToday());
		study.setLow(study.getMaWeek() + 0.01d );
		assertFalse(rules.hasTradedBelowMAToday());
		smaRules = new SmaRules(study);
		assertFalse(smaRules.hasTradedBelowMAToday());
	}
}
