package com.codeworks.pai.db.model;

import junit.framework.TestCase;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.mock.MockDataReader;
import com.codeworks.pai.study.Period;


public class SmaRulesTest extends TestCase {
	Study spyStudy;
	Study gldStudy;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		spyStudy = new Study("SPY");
		// Numbers from 4/12/2013
		spyStudy.setPrice(MockDataReader.SPY_PRICE);
		spyStudy.setMaType(MaType.E);
		spyStudy.setPriceLastWeek(155.16d);
		spyStudy.setAverageTrueRange(1.46d);
		spyStudy.setPriceLastMonth(156.67d);
		spyStudy.setSmaLastWeek(153.04d);
		spyStudy.setSmaLastMonth(142.85d);
		spyStudy.setSmaWeek(153.91d);
		spyStudy.setSmaMonth(144.42d);
		spyStudy.setSmaStddevWeek(((158.99-153.91d)/2));
		spyStudy.setSmaStddevMonth(7.89d);
		
		spyStudy.setEmaLastWeek(153.04d);
		spyStudy.setEmaLastMonth(142.85d);
		spyStudy.setEmaWeek(153.91d);
		spyStudy.setEmaMonth(144.42d);
		spyStudy.setEmaStddevWeek(((158.99-153.91d)/2));
		spyStudy.setEmaStddevMonth(7.89d);
		
		
		gldStudy = new Study("GLD");
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
		Study study = spyStudy;
		assertEquals(spyStudy.getSmaMonth() , PaiUtils.round(rules.calcBuyZoneBottom()));
		assertEquals(spyStudy.getSmaWeek(), rules.calcBuyZoneTop());
		assertEquals(spyStudy.getSmaMonth() + (spyStudy.getSmaStddevMonth() * SmaRules.ZONE_OUTER) ,rules.calcSellZoneBottom());
		assertEquals(spyStudy.getSmaMonth() + (spyStudy.getSmaStddevMonth() * SmaRules.ZONE_OUTER) + rules.pierceOffset(),rules.calcSellZoneTop());
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
		Study study = gldStudy;
		assertEquals(163.08d, PaiUtils.round(rules.calcSellZoneTop()));
		assertEquals(gldStudy.getSmaMonth() + (gldStudy.getSmaStddevMonth() * SmaRules.ZONE_OUTER), rules.calcSellZoneBottom());
		assertEquals(gldStudy.getSmaWeek(), rules.calcBuyZoneTop());
		assertEquals(gldStudy.getSmaMonth(), rules.calcBuyZoneBottom());
		assertEquals(rules.calcSellZoneTop(), rules.calcUpperSellZoneTop(Period.Month));
		assertEquals(rules.calcSellZoneBottom(), rules.calcUpperSellZoneBottom(Period.Month));
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
		Study study = spyStudy;
		study.setLow(study.getEmaWeek() - 0.01d );
		EmaRules rules = new EmaRules(study);
		assertTrue(rules.isUpTrend(Period.Week));
		assertTrue(rules.hasTradedBelowMAToday());
		SmaRules smaRules = new SmaRules(study);
		assertTrue(smaRules.isUpTrend(Period.Week));
		assertFalse(smaRules.hasTradedBelowMAToday());
		study.setLow(study.getEmaWeek() + 0.01d );
		assertFalse(rules.hasTradedBelowMAToday());
		smaRules = new SmaRules(study);
		assertFalse(smaRules.hasTradedBelowMAToday());
	}
}
