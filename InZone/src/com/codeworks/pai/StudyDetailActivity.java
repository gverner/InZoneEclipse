package com.codeworks.pai;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class StudyDetailActivity extends Activity {
	  public static final String STUDY_ID = "url";
	  public static final String PORTFOLIO_ID = "portfolio_id";
	  
	  @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Need to check if Activity has been switched to landscape mode
		// If yes, finished and go back to the start Activity
		/*
		 * if (getResources().getConfiguration().orientation ==
		 * Configuration.ORIENTATION_LANDSCAPE) { finish(); return; }
		 */
		int portfolioId = 1;
		setContentView(R.layout.study_detail_frame);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Long id = extras.getLong(STUDY_ID);
			portfolioId = extras.getInt(PORTFOLIO_ID);

			Fragment newFragment;
			if (PaiUtils.MA_TYPE_EMA.equals(PaiUtils.getStrategy(this, portfolioId))) {
				newFragment = new StudyEDetailFragment();
				Bundle args = new Bundle();
				args.putLong(StudyEDetailFragment.ARG_STUDY_ID, id);
				newFragment.setArguments(args);
			} else {
				newFragment = new StudySDetailFragment();
				Bundle args = new Bundle();
				args.putLong(StudySDetailFragment.ARG_STUDY_ID,id);
				newFragment.setArguments(args);
			}

			// Add the fragment to the 'fragment_container' FrameLayout
			getFragmentManager().beginTransaction().add(R.id.study_detail_frame, newFragment).commit();
		}

	}
	  
		// Create the menu based on the XML definition
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.study_detail, menu);
			return true;
		}	  
		
		// Reaction to the menu selection
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_item_done: {
		            setResult(RESULT_OK);
		            finish();
		          }
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
		
}
