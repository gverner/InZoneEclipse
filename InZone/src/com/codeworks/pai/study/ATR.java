package com.codeworks.pai.study;

import java.util.Collections;
import java.util.List;

import com.codeworks.pai.db.model.Price;

public class ATR {
	private boolean			isFirst	= true;
	private double			atr;
	private int				periods;
	private int 			cnt;

	/**
	 * Construct an <tt>ATR</tt> instance.
	 * we are using SMA
	 * <blockquote><code><pre>  
	 * alpha = 2 / (numberPeriods + 1)  
	 * </pre></code></blockquote>
	 * 
	 * @param numberPeriods
	 */
	public ATR(int numberPeriods) {
		//alpha = 2d / (numberPeriods + 1d); // EMA
		periods = numberPeriods;
		cnt = 0;
	}

	public double getAtr() {
		return atr;
	}

	public double compute(double high, double low, double prevClose) {
		double tr = Math.max(Math.max(Math.abs(high - low), Math.abs(high - prevClose)), Math.abs(low - prevClose));
		//System.out.println("hi="+format(high)+" lo="+format(low)+" pCls="+format(prevClose)+" tr="+format(tr)+" hl="+format(Math.abs(high - low))+" hp="+format(Math.abs(high - prevClose))+" lp="+format(Math.abs(low - prevClose)));
		if (isFirst) {
			atr = tr;
			//satr = tr;
			isFirst = false;
			cnt++;
		} else {
			/* NOT NECESSARY TO start average at period */
			
			cnt++;
			if (cnt <= periods) {
				atr = (atr + tr);
			} else {
				if (cnt == periods + 1) {
					atr = atr / (periods);
				}
				atr = ((atr * (periods - 1)) + tr) / periods;
			}

			//atr = ((atr * (periods - 1)) + tr) / periods;

			// -- Multiply the previous 14-day ATR by 13.
			// -- Add the most recent day's TR value.
			// -- Divide the total by 14
			//atr = (tr * alpha) + (atr * (1d - alpha));
		}
		return atr;
	}

	public static double compute(List<Price> priceList, int noPeriods) {
		ATR atr = new ATR(noPeriods);
		int end = priceList.size();
		List<Price> subList = priceList.subList(end - (noPeriods * 2), end);
		Price prev = new Price();
		prev = subList.get(0);
		for (Price price : subList) {
			atr.compute(price.getHigh(), price.getLow(), prev.getClose());
			prev = price;
		}
		return atr.getAtr();
	}
	/*
	static String format(double value) {
		return new BigDecimal(value).setScale(4, BigDecimal.ROUND_HALF_UP).toPlainString();
	}*/

}
