package com.codeworks.pai;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class StudyDetailActivity extends Activity {
	  public static final String STUDY_ID = "url";
	  
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // Need to check if Activity has been switched to landscape mode
	    // If yes, finished and go back to the start Activity
	    /*
	    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
	      finish();
	      return;
	    }
	    */
	    
	    setContentView(R.layout.study_detail_activity);
	    Bundle extras = getIntent().getExtras();
	    if (extras != null) {
	      Long id = extras.getLong(STUDY_ID);
			StudyDetailFragment fragment = (StudyDetailFragment) getFragmentManager().findFragmentById(R.id.studyDetailFragment);
			if (fragment != null && fragment.isInLayout()) {
				fragment.setId(id);
			}
	      //TextView view = (TextView) findViewById(R.id.tempStudyDetailsText);
	      //view.setText(s);
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
