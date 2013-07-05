/*
 *
 * Copyright (C) 2011 The Android Open Source Project
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codeworks.pai.processor.UpdateService;

/**
 * Starts up the task list that will interact with the AccessibilityService
 * sample.
 */
public class StudyActivity extends Activity implements StudyListFragment.OnItemSelectedListener {
	private static final String TAG = StudyActivity.class.getSimpleName();

	private Intent dailyIntent;

	// List<PaiStudy> quotes = new ArrayList<PaiStudy>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		dailyIntent = new Intent(this, UpdateService.class);
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_MANUAL);
		startService(dailyIntent);
		setContentView(R.layout.study_activity);		
	}
	
	  @Override
	public void onStudySelected(Long studyId) {
		StudyDetailFragment fragment = (StudyDetailFragment) getFragmentManager().findFragmentById(R.id.studyDetailFragment);
		if (fragment != null && fragment.isInLayout()) {
			fragment.setId(studyId);
		} else {
			Intent intent = new Intent(getApplicationContext(), StudyDetailActivity.class);
			intent.putExtra(StudyDetailActivity.STUDY_ID, studyId);
			startActivity(intent);

		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemStartSerivce:
			startService(dailyIntent);
			break;
		case R.id.itemStopService:
			stopService(dailyIntent);
			break;
		case R.id.portfolio:
			Intent intent = new Intent();
			intent.setClassName(getPackageName(), SecurityListActivity.class.getName());
			startActivity(intent);
			break;
		case R.id.action_settings:
			Intent settingsIntent = new Intent();
			settingsIntent.setClassName(getPackageName(), SettingsActivity.class.getName());
			startActivity(settingsIntent);
			break;
		}
		
		return true;
	}



	public void showToast(final String toast)
	{
	    runOnUiThread(new Runnable() {
	        public void run()
	        {
	            Toast.makeText(StudyActivity.this, toast, Toast.LENGTH_SHORT).show();
	        }
	    });
	}	
}
