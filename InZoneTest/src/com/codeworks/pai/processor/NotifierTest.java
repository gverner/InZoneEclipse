package com.codeworks.pai.processor;

import java.util.ArrayList;
import java.util.List;

import com.codeworks.pai.db.model.PaiStudy;

import android.test.AndroidTestCase;

public class NotifierTest extends AndroidTestCase {

	/**
	 * Testing for missing resource exception caused by DEPLAY_PRICE not having a notice subject and message.
	 * 
	 */
	public void testNotice() {
		NotifierImpl notifier = new NotifierImpl(getContext());
		PaiStudy study = new PaiStudy("SPY");
		study.setPrice(1.0d);
		study.setMaWeek(0.50d);
		study.setMaMonth(.050d);
		study.setStddevWeek(0.55d);
		study.setStddevMonth(0.55d);
		study.setNotice(Notice.NONE);
		List<PaiStudy> studies = new ArrayList<PaiStudy>();
		studies.add(study);
		notifier.updateNotification(studies);
		study.setNotice(Notice.DELAYED_PRICE);
		notifier.updateNotification(studies);
		study.setNotice(Notice.NONE);
		study.setPrice(.55d);
		notifier.updateNotification(studies);
		study.setPrice(1.55d);
		notifier.updateNotification(studies);
		study.setPrice(.40d);
		notifier.updateNotification(studies);
		study.setPrice(.60d);
		notifier.updateNotification(studies);
	}
}
