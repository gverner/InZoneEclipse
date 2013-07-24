package com.codeworks.pai.processor;

import java.util.List;

import com.codeworks.pai.db.model.PaiStudy;

public interface Processor {
	public abstract List<PaiStudy> process(String symbol) throws InterruptedException;
	public abstract List<PaiStudy> updatePrice(String symbol) throws InterruptedException;
	
}