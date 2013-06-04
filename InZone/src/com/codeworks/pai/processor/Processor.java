package com.codeworks.pai.processor;

import java.util.List;

import com.codeworks.pai.db.model.PaiStudy;

public interface Processor {
	public abstract List<PaiStudy> process() throws InterruptedException;
}