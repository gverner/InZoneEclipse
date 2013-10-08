package com.codeworks.pai.processor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.PaiStudyTable;
import com.codeworks.pai.db.model.MaType;
import com.codeworks.pai.db.model.PaiStudy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

public class NotifierTest extends AndroidTestCase {

	/**
	 * Testing for missing resource exception caused by DEPLAY_PRICE not having a notice subject and message.
	 * Probably obsolete after adding statusMap flags
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
		study.setDelayedPrice(true);
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

	class MockNotifier extends NotifierImpl {
		int numberOfCalls = 0;
		int numberOfStudies = 0;
		int numberOfSendNoticeCalls = 0;

		public MockNotifier(Context context) {
			super(context);
			numberOfCalls++;
		}
		@Override
		public void sendNotice(long securityId, String title, String text) {
			numberOfSendNoticeCalls++;
		}
		@Override
		public void sendServiceNotice(int notifyId, String title, String text, int numMessages) {
			numberOfSendNoticeCalls++;
			
		}		
	}
	
	public void testNoticeSma() {
		MockNotifier notifier = new MockNotifier(getContext());
		PaiStudy study = fetchStudyAndUpdateNotice("SPY",Notice.NONE);
		study.setMaType(MaType.S);
		study.setPrice(1.0d);
		study.setMaWeek(0.50d);
		study.setMaMonth(.050d);
		study.setStddevWeek(0.55d);
		study.setStddevMonth(0.55d);
		study.setNotice(Notice.NONE);
		List<PaiStudy> studies = new ArrayList<PaiStudy>();
		studies.add(study);
		notifier.updateNotification(studies);
		assertEquals(0, notifier.numberOfSendNoticeCalls );
		study.setDelayedPrice(true);
		notifier.updateNotification(studies);
		assertEquals(0, notifier.numberOfSendNoticeCalls );
		study.setNotice(Notice.NONE);
		study.setPrice(.55d);
		notifier.updateNotification(studies);
		assertEquals(0, notifier.numberOfSendNoticeCalls );
		study.setPrice(1.55d);
		notifier.updateNotification(studies);
		assertEquals(0, notifier.numberOfSendNoticeCalls );
		study.setPrice(.40d);
		notifier.updateNotification(studies);
		assertEquals(0, notifier.numberOfSendNoticeCalls );
		study.setPrice(.60d);
		notifier.updateNotification(studies);
		assertEquals(0, notifier.numberOfSendNoticeCalls );
	}
	
	public void testNoticeEma() {
		MockNotifier notifier = new MockNotifier(getContext());
		PaiStudy study = fetchStudyAndUpdateNotice("SPY",Notice.NONE);
		study.setMaType(MaType.E);
		study.setPrice(1.0d);
		study.setMaWeek(0.50d);
		study.setMaMonth(.050d);
		study.setSmaWeek(0.50d);
		study.setSmaMonth(.050d);
		study.setStddevWeek(0.55d);
		study.setStddevMonth(0.55d);
		study.setNotice(Notice.NONE);
		List<PaiStudy> studies = new ArrayList<PaiStudy>();
		studies.add(study);
		notifier.updateNotification(studies);
		assertEquals(0, notifier.numberOfSendNoticeCalls );
		study.setDelayedPrice(true);
		notifier.updateNotification(studies);
		assertEquals(0, notifier.numberOfSendNoticeCalls );
		study.setNotice(Notice.NONE);
		study.setPrice(.55d);
		notifier.updateNotification(studies);
		assertEquals(1, notifier.numberOfSendNoticeCalls );
		study.setPrice(1.55d);
		notifier.updateNotification(studies);
		assertEquals(2, notifier.numberOfSendNoticeCalls );
		study.setPrice(.40d);
		notifier.updateNotification(studies);
		assertEquals(3, notifier.numberOfSendNoticeCalls );
		study.setPrice(.60d);
		notifier.updateNotification(studies);
		assertEquals(4, notifier.numberOfSendNoticeCalls );
	}

	PaiStudy fetchStudyAndUpdateNotice(String symbol, Notice notice) {
		PaiStudy study = new PaiStudy(symbol);

		String[] projection = new String[] { PaiStudyTable.COLUMN_ID, PaiStudyTable.COLUMN_NOTICE, PaiStudyTable.COLUMN_NOTICE_DATE };
		String selection = "symbol = ? ";
		String[] selectionArgs = new String[] { study.getSymbol() };
		Cursor studyCursor = getContext().getContentResolver().query(PaiContentProvider.PAI_STUDY_URI, projection, selection, selectionArgs,
				PaiStudyTable.COLUMN_ID);
		try {
			studyCursor.moveToFirst();
			study.setSecurityId(studyCursor.getLong(studyCursor.getColumnIndex(PaiStudyTable.COLUMN_ID)));
			study.setNotice(Notice.fromIndex(studyCursor.getInt(studyCursor.getColumnIndex(PaiStudyTable.COLUMN_NOTICE))));
			study.setNotice(notice);
			ContentValues values = new ContentValues();
			values.put(PaiStudyTable.COLUMN_NOTICE, notice.getIndex());
			Uri studyUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + study.getSecurityId());
			getContext().getContentResolver().update(studyUri, values, null, null);
			return study;
		} finally {
			studyCursor.close();
		}
	}
	
	public void testSaveNotice() {
		NotifierImpl notifier = new NotifierImpl(getContext());
		PaiStudy study = fetchStudyAndUpdateNotice("SPY", Notice.NONE);
		study.setNotice(Notice.NONE);
		study.setNoticeDate(new Date());
		
		assertFalse(notifier.saveStudyNoticeIfChanged(study));
		study.setNotice(Notice.IN_BUY_ZONE);
		assertTrue(notifier.saveStudyNoticeIfChanged(study));
		assertFalse(notifier.saveStudyNoticeIfChanged(study));
	}
}
