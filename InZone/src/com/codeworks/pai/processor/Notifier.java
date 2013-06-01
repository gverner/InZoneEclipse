package com.codeworks.pai.processor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.codeworks.pai.PaiStudyListActivity;
import com.codeworks.pai.R;
import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.PaiStudyTable;
import com.codeworks.pai.db.model.PaiStudy;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class Notifier {
	private static final String TAG = Notifier.class.getSimpleName();
	SimpleDateFormat noticeDateFormat = new SimpleDateFormat("yyyyMMdd kk:mm", Locale.US);
	Context context;

	public Notifier(Context context) {
		this.context = context;
	}

	void updateNotification(List<PaiStudy> studies) {
		Resources res = context.getResources();

		for (PaiStudy study : studies) {
			if (Notice.NO_PRICE.equals(study.getNotice())) {
				// set by processor
			} else if (Notice.INSUFFICIENT_HISTORY.equals(study.getNotice())) {
				// set by processor
			} else	if (study.isPossibleDowntrendTermination()) {
				study.setNotice(Notice.POSSIBLE_WEEKLY_DOWNTREND_TERMINATION);
			} else if (study.isPossibleUptrendTermination()) {
				study.setNotice(Notice.POSSIBLE_WEEKLY_UPTREND_TEMINATION);
			} else if (study.isPriceInBuyZone()) {
				study.setNotice(Notice.IN_BUY_ZONE);
			} else if (study.isPriceInSellZone()) {
				study.setNotice(Notice.IN_SELL_ZONE);
			} else {
				study.setNotice(Notice.NONE);
			}
			if (saveStudyNoticeIfChanged(study) && !Notice.NONE.equals(study.getNotice())) {
				notify(study.getSecurityId(), res.getString(study.getNotice().getSubject()),
						String.format(res.getString(study.getNotice().getMessage()), study.getSymbol()));
			}
		}
	}

	boolean saveStudyNoticeIfChanged(PaiStudy study) {
		boolean changed = false;
		String[] projection = new String[] { PaiStudyTable.COLUMN_ID, PaiStudyTable.COLUMN_NOTICE, PaiStudyTable.COLUMN_NOTICE_DATE };
		String selection = "symbol = ? ";
		String[] selectionArgs = new String[] { study.getSymbol() };
		Cursor studyCursor = getContentResolver().query(PaiContentProvider.PAI_STUDY_URI, projection, selection, selectionArgs, null);
		try {
			ContentValues values = new ContentValues();
			values.put(PaiStudyTable.COLUMN_NOTICE, study.getNotice().getIndex());
			values.put(PaiStudyTable.COLUMN_NOTICE_DATE, noticeDateFormat.format(study.getNoticeDate()==null? new Date(): study.getNoticeDate()));
			Log.d(TAG, "Updating Study " + study.toString());
			studyCursor.moveToFirst();
			Uri studyUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + studyCursor.getLong(0));
			Notice lastNotice = Notice.fromIndex(studyCursor.getInt(1));
			if (study.getNotice() != null && !study.getNotice().equals(lastNotice)) {
				getContentResolver().update(studyUri, values, null, null);
				changed = true;
			} else {
				changed = false;
			}

		} finally {
			studyCursor.close();
		}
		return changed;
	}

	void notify(long securityId, String title, String text) {
		Log.d(TAG, String.format("create notice title %1$s with text %2$s", title, text));
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_input_add)
				.setContentTitle(title).setContentText(text);
		mBuilder.setAutoCancel(true);
		mBuilder.setOnlyAlertOnce(true);

		// the started Activity.
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(context, PaiStudyListActivity.class);

		// The stack builder object will contain an artificial back stack for
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context); //
		// Adds the back stack for the Intent (but not the Intent itself)
		// stackBuilder.addParentStack(PaiStudyListActivity.class); // Adds the
		// Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);

		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		// PendingIntent resultPendingIntent = PendingIntent.getActivity(this,
		// 0, resultIntent, 0);
		mBuilder.setContentIntent(resultPendingIntent);

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(Long.valueOf(securityId).intValue(), mBuilder.build());

	}
	
	
	ContentResolver getContentResolver() {
		return context.getContentResolver();
		
	}
}

