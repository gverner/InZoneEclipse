package com.codeworks.pai.processor;

import java.util.ArrayList;
import java.util.List;

import com.codeworks.pai.db.model.PaiStudy;

public class MockProcessor implements Processor {
	int numberOfProcessCalls = 0;
	int numberOfUpdatePriceCalls = 0;
	
	@Override
	public List<PaiStudy> process(String symbol) throws InterruptedException {
		numberOfProcessCalls++;
		List<PaiStudy> studies = new ArrayList<PaiStudy>();
		PaiStudy study = new PaiStudy("SPY");
		studies.add(study);
		study.setPrice(150.00d);
		study.setMaLastMonth(150.00d);
		study.setMaLastWeek(150.00d);
		study.setAverageTrueRange(0);
		study.setNotice(Notice.NONE);
		study.setMaMonth(150.00d);
		study.setMaWeek(150.00d);
		study.setPriceLastMonth(150.00d);
		study.setPriceLastWeek(150.00d);
		study.setName("S&P");
		return studies;
	}

	@Override
	public List<PaiStudy> updatePrice(String symbol) throws InterruptedException {
		numberOfUpdatePriceCalls++;
		return new ArrayList<PaiStudy>();
	}

}
