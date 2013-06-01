/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.codeworks.pai;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.codeworks.pai.R;
import com.codeworks.pai.db.model.PaiStudy;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Acts as a go-between for all AccessibilityEvents sent from items in the
 * ListView, providing the option of sending more context to an
 * AccessibilityService by adding more AccessiblityRecords to an event.
 */
public class PaiStudyListViewNotUsed extends ListView {

	public PaiStudyListViewNotUsed(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	/**
	 * This method will fire whenever a child event wants to send an
	 * AccessibilityEvent. As a result, it's a great place to add more
	 * AccessibilityRecords, if you want. In this case, the code is grabbing the
	 * position of the item in the list, and assuming that to be the priority
	 * for the task.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public boolean onRequestSendAccessibilityEvent(View child, AccessibilityEvent event) {
		// Add a record for ourselves as well.
		AccessibilityEvent record = AccessibilityEvent.obtain();
		super.onInitializeAccessibilityEvent(record);

		int priority = (Integer) child.getTag();
		String priorityStr = "Priority: " + priority;
		record.setContentDescription(priorityStr);

		event.appendRecord(record);
		return true;
	}
}

/**
 * Adds Accessibility information to individual child views of rows in the list.
 */
final class TaskAdapter extends BaseAdapter {

	private Context mContext = null;
	List<PaiStudy> quotes = new ArrayList<PaiStudy>();

	public List<PaiStudy> getQuotes() {
		return quotes;
	}

	public void setQuotes(List<PaiStudy> quotes) {
		this.quotes = quotes;
	}

	public TaskAdapter(Context context, List<PaiStudy> quotes) {
		super();
		this.quotes = quotes;
		mContext = context;
	}

	@Override
	public int getCount() {
		return quotes.size();
	}

	/**
	 * Expands the views for individual list entries, and sets content
	 * descriptions for use by the TaskBackAccessibilityService.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.studylist_row, parent, false);
		}
		PaiStudy quote = quotes.get(position);

		// CheckBox checkbox = (CheckBox)
		// convertView.findViewById(R.id.tasklist_finished);
		// checkbox.setChecked(mCheckboxes[position]);

		((TextView) convertView.findViewById(R.id.quoteList_symbol)).setText(quote.getSymbol());

		((TextView) convertView.findViewById(R.id.quoteList_Price)).setText(format(quote.getPrice()));

		((TextView) convertView.findViewById(R.id.quoteList_ema)).setText(format(quote.getMaWeek()));

		((TextView) convertView.findViewById(R.id.quoteList_BuyZoneTop)).setText(format(quote.calcBuyZoneTop()));

		((TextView) convertView.findViewById(R.id.quoteList_SellZoneBottom)).setText(format(quote.calcSellZoneBottom()));

		((TextView) convertView.findViewById(R.id.quoteList_SellZoneTop)).setText(format(quote.calcSellZoneTop()));

		convertView.setTag(position);

		return convertView;
	}

	String format(BigDecimal decimal) {
		if (decimal != null) {
			return decimal.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
		} else {
			return "";
		}
	}
	String format(double decimal) {
		if (decimal != Double.NaN) {
			return new BigDecimal(decimal).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
		} else {
			return "";
		}
	}
	@Override
	public Object getItem(int position) {
		return quotes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
