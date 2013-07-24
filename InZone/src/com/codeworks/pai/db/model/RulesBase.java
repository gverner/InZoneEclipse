package com.codeworks.pai.db.model;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


public abstract class RulesBase implements Rules {

	protected PaiStudy	study;

	@Override
	public String formatNet(double net) {
		NumberFormat format = NumberFormat.getInstance(Locale.US);
		if (format instanceof DecimalFormat) {
			((DecimalFormat)format).applyPattern("+####.00;-####.00");
		}
		String result = format.format(net);
		return result;
	}
}
