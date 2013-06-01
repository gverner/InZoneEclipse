package com.codeworks.pai;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.SecurityTable;

public class SecurityListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = SecurityListActivity.class.getSimpleName();

	// private Cursor cursor;
	private SimpleCursorAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.security_list);
		fillData();
	}

	// Create the menu based on the XML definition
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.security_list, menu);
		return true;
	}

	// Reaction to the menu selection
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.security_list_insert:
			createSecurity();
			return true;
		case R.id.menu_item_done:
            setResult(RESULT_OK);
            finish();
		}
		return super.onOptionsItemSelected(item);
	}

	private void createSecurity() {
		Intent i = new Intent(this, SecurityDetailActivity.class);
		startActivity(i);
	}

	public void deleteClickHandler(View v) {
		// get the row the clicked button is in
		RelativeLayout vwParentRow = (RelativeLayout) v.getParent();
		TextView securityTextView = (TextView) vwParentRow.getChildAt(0);
		if (securityTextView != null) {
			String securityId = securityTextView.getText().toString();
			Log.d(TAG, "SecurityId1=" + securityId);
			deleteSecurity(securityId);
		}
	}

	private void deleteSecurity(String securityId) {
		if (securityId.length() == 0) {
			return;
		}
		Uri securityUri = Uri.parse(PaiContentProvider.SECURITY_URI + "/" + securityId);
		int countDeleted = getContentResolver().delete(securityUri, null, null);
		Log.d(TAG, "Uri=" + securityUri.toString() + " Delete Count=" + countDeleted);
	}

	// Opens the second activity if an entry is clicked
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, SecurityDetailActivity.class);
		Uri todoUri = Uri.parse(PaiContentProvider.SECURITY_URI + "/" + id);
		i.putExtra(PaiContentProvider.CONTENT_ITEM_TYPE, todoUri);

		startActivity(i);
	}

	private void fillData() {
		// Must include the _id column for the adapter to work
		// SecurityTable.COLUMN_ID,

		String[] from = new String[] { SecurityTable.COLUMN_ID, SecurityTable.COLUMN_SYMBOL, SecurityTable.COLUMN_NAME };
		// Fields on the UI to which we map
		int[] to = new int[] { R.id.securityId, R.id.securitySymbol, R.id.securityName };

		getLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(this, R.layout.security_row, null, from, to, 0);

		setListAdapter(adapter);
	}

	// Creates a new loader after the initLoader () call
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { SecurityTable.COLUMN_ID, SecurityTable.COLUMN_SYMBOL, SecurityTable.COLUMN_NAME };
		CursorLoader cursorLoader = new CursorLoader(this, PaiContentProvider.SECURITY_URI, projection, null, null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// data is not available anymore, delete reference
		adapter.swapCursor(null);
	}
}
