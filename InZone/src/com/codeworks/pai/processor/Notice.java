package com.codeworks.pai.processor;

import com.codeworks.pai.R;

public enum Notice {
	// making it obvious that we are using index instead of using ordinal.
	NONE(0,0,0), 
	POSSIBLE_WEEKLY_UPTREND_TEMINATION(1,R.string.possible_weekly_uptrend_termination, R.string.possible_weekly_uptrend_termination_text), 
	POSSIBLE_WEEKLY_DOWNTREND_TERMINATION(2,R.string.possible_weekly_downtrend_termination, R.string.possible_weekly_downtrend_termination_text),
	IN_SELL_ZONE(3, R.string.in_sell_zone, R.string.in_sell_zone_text),
	IN_BUY_ZONE(4, R.string.in_buy_zone, R.string.in_buy_zone_text),
	NO_PRICE(5, R.string.notice_no_price, R.string.notice_no_price_text),
	INSUFFICIENT_HISTORY(6, R.string.notice_insufficent_history, R.string.notice_insufficent_history_text);
	
	int index;
	int subject;
	int message;
	Notice ( int index, int subject, int message) {
		this.index = index;
		this.subject = subject;
		this.message = message;
	}
	
	public int getIndex() {
		return index;
	}
	public int getSubject() {
		return subject;
	}
	public int getMessage() {
		return message;
	}
	public static Notice fromIndex(int index) {
		for (Notice notice : values()) {
			if (index == notice.getIndex()) {
				return notice;
			}
		}
		return null;
	}
}
