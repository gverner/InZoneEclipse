package com.codeworks.pai.study;

import java.util.LinkedList;

public class SMA {
	private LinkedList<Double> values = new LinkedList<Double>();
	private int length;
	private double sum = 0;

	public SMA(int length) {
		if (length <= 0) {
			throw new IllegalArgumentException(
					"length must be greater than zero");
		}
		this.length = length;
	}

	public double compute(double value) {
		if (values.size() == length) {
			sum -= ((Double) values.getFirst()).doubleValue();
			values.removeFirst();
		}
		sum += value;
		values.addLast(Double.valueOf(value));
		return sum / length;
	}
}
