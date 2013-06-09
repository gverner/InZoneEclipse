package com.codeworks.pai;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.PaiStudyTable;
import com.codeworks.pai.processor.UpdateService;

/*
 * SecurityDetailActivity allows to enter a new security item 
 * or to change an existing
 */
public class SecurityDetailActivity extends Activity {
  private EditText mSymbolText;

  private Uri securityUri;

  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.security_edit);

    mSymbolText = (EditText) findViewById(R.id.security_edit_symmbol);

    Bundle extras = getIntent().getExtras();

    // Check from the saved Instance
    securityUri = (bundle == null) ? null : (Uri) bundle
        .getParcelable(PaiContentProvider.CONTENT_ITEM_TYPE);

    // Or passed from the other activity
    if (extras != null) {
      securityUri = extras
          .getParcelable(PaiContentProvider.CONTENT_ITEM_TYPE);

      fillData(securityUri);
    }

  }

	private void fillData(Uri uri) {
		String[] projection = { PaiStudyTable.COLUMN_SYMBOL };
		Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
		if (cursor != null) try {
			cursor.moveToFirst();
			String symbol = cursor.getString(cursor.getColumnIndexOrThrow(PaiStudyTable.COLUMN_SYMBOL));
			mSymbolText.setText(symbol);
		} finally {
			// Always close the cursor
			cursor.close();
		}
	}

	// Create the menu based on the XML definition
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.security_detail, menu);
		return true;
	}

	// Reaction to the menu selection
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_done:
	        if (TextUtils.isEmpty(mSymbolText.getText().toString())) {
	            setResult(RESULT_CANCELED);
	            finish();
	          } else {
	            setResult(RESULT_OK);
	            finish();
	          }
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    saveState();
    outState.putParcelable(PaiContentProvider.CONTENT_ITEM_TYPE, securityUri);
  }

  @Override
  protected void onPause() {
    super.onPause();
    saveState();
  }

  private void saveState() {
    String symbol = mSymbolText.getText().toString();
    // must have a symbol
    if (symbol.length() == 0) {
      return;
    }

    ContentValues values = new ContentValues();
    values.put(PaiStudyTable.COLUMN_SYMBOL, symbol.trim());

    if (securityUri == null) {
      // New security
      ContentResolver contentResolver = getContentResolver();
      securityUri = contentResolver.insert(PaiContentProvider.PAI_STUDY_URI, values);
    } else {
      // Update security
      getContentResolver().update(securityUri, values, null, null);
    }
    startServiceOneTime(symbol);
  }

  void startServiceOneTime(String symbol) {
		Intent oneTimeIntent = new Intent(this, UpdateService.class);
		oneTimeIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_ONE_TIME);
		oneTimeIntent.putExtra(UpdateService.SERVICE_SYMBOL, symbol);
		startService(oneTimeIntent);
  }
  
} 