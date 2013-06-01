package com.codeworks.pai;

import java.math.BigDecimal;

public class PaiUtils {
	public static double round(double value) {
		return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

}
