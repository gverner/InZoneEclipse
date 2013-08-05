package com.codeworks.pai;

import java.math.BigDecimal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;

public class PaiUtils {
	public static String PREF_PORTFOLIO_KEY = "pref_portfolio_key";
	public static String    PREF_FILE = "com.codeworks.inzone.preferences";
	//public static String    PREF_PORTFOLIO = "com.codeworks.pai.preference.portfolio.name";
	
	public static final String PREF_PORTFOLIO_MA_TYPE = "com.codeworks.inzone.portfolio_ma_type";
	public static final String MA_TYPE_EMA = "E";
	public static final String MA_TYPE_SMA = "S";
	
	public static double round(double value) {
		return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static double round(double value, int scale) {
		return new BigDecimal(value).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	public static String getDefaultPortfolioName(Resources resources, int portfolioId) {
		int tab_id = R.string.tab_protfolio_1;
		switch (portfolioId) {
		case 1:
			tab_id = R.string.tab_protfolio_1;
			break;
		case 2:
			tab_id = R.string.tab_protfolio_2;
			break;
		case 3:
			tab_id = R.string.tab_protfolio_3;
			break;
		default:
			tab_id = R.string.tab_protfolio_1;
			break;
		}
		String preferenceName = resources.getString(tab_id);
		return preferenceName;
	}
	
	public static String getPortfolioName(Resources resources, SharedPreferences sharedPreferences, int portfolioId) {
		String preferenceName;
		preferenceName = getDefaultPortfolioName(resources, portfolioId);
		String portfolioName = sharedPreferences.getString(PREF_PORTFOLIO_KEY+portfolioId, preferenceName);
		return portfolioName;
	}
	
	public static SharedPreferences getSharedPreferences(Activity activity) {
		SharedPreferences sharedPreferences = activity.getSharedPreferences(PaiUtils.PREF_FILE, Activity.MODE_PRIVATE);
		return sharedPreferences;
	}
	
	public static String getStrategy(Activity activity, int portfolioId) {
		SharedPreferences sharedPreferences = getSharedPreferences(activity);
		return getStrategy(sharedPreferences, portfolioId);
	}
	
	public static String getStrategy(SharedPreferences sharedPreferences, int portfolioId) {
		String strategy = sharedPreferences.getString(PaiUtils.PREF_PORTFOLIO_MA_TYPE + portfolioId, portfolioId == 1 ? "E" : "S");
		return strategy;
	}
	
	public static void savePortfolioName(Resources resources, SharedPreferences sharedPreferences, int portfolioId, String portfolioName) {
		Editor editor = sharedPreferences.edit();
		editor.putString(PREF_PORTFOLIO_KEY+portfolioId, portfolioName);
		editor.commit();
	}
}
