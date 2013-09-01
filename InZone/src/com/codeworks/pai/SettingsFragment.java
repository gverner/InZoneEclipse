package com.codeworks.pai;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.PaiStudyTable;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	private static final String	TAG	= SettingsFragment.class.getSimpleName();
	public static final String	KEY_PREF_SYNC_CONN		= "pref_syncConnectionType";
	public static final String	PREF_PORTFOLIO_NAME1	= "pref_portfolio_name1";
	public static final String	PREF_PORTFOLIO_NAME2	= "pref_portfolio_name2";
	public static final String	PREF_PORTFOLIO_NAME3	= "pref_portfolio_name3";
	public static final String	PREF_PORTFOLIO_TYPE1	= "pref_portfolio_type1";
	public static final String	PREF_PORTFOLIO_TYPE2	= "pref_portfolio_type2";
	public static final String	PREF_PORTFOLIO_TYPE3	= "pref_portfolio_type3";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		updateSummary();
	}


	void updateSummary() {
		for (int x = 1; x < 4; x++) {
			updateSummaryName("pref_portfolio_name"+x);
			updateSummaryType("pref_portfolio_type"+x);
		}
	}

	void updateSummaryName(String key) {
		EditTextPreference pref = (EditTextPreference) findPreference(key);
		if (pref != null) {
			Log.d(TAG, "Setting Preference " + key + " = " + pref.getText());
			pref.setSummary(pref.getText());
		} else {
			Log.d(TAG, "PREF IS NULL");
		}
	}

	String updateSummaryType(String key) {
		String value = "E";
		ListPreference pref = (ListPreference) findPreference(key);
		if (pref != null) {
			Log.d(TAG, "Setting Preference " + key + " = " + pref.getValue());
			value = pref.getValue();
			if ("E".equals(value)) {
				pref.setSummary("EMA");
			} else if ("S".equals(value)) {
				pref.setSummary("SMA");
			}
		} else {
			Log.d(TAG, "PREF IS NULL");
		}
		return value;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(KEY_PREF_SYNC_CONN)) {
			// SharedPreferences sharedPref =
			// PreferenceManager.getDefaultSharedPreferences(this);
			// Set summary to be the user-description for the selected value
			// sharedPref. Summary(sharedPreferences.getString(key, ""))

		} else if (PREF_PORTFOLIO_NAME1.equals(key) || PREF_PORTFOLIO_NAME2.equals(key) || PREF_PORTFOLIO_NAME3.equals(key)) {
			updateSummaryName(key);
		} else if (PREF_PORTFOLIO_TYPE1.equals(key) || PREF_PORTFOLIO_TYPE2.equals(key) || PREF_PORTFOLIO_TYPE3.equals(key)) {
			String portfolioId = "1";
		    if (PREF_PORTFOLIO_TYPE1.equals(key)) {
		    	portfolioId = "1";
		    } else if (PREF_PORTFOLIO_TYPE2.equals(key)) {
		    	portfolioId = "2";
		    } else if (PREF_PORTFOLIO_TYPE3.equals(key)) {
		    	portfolioId = "3";
		    }
			String value = updateSummaryType(key);
			String selection = PaiStudyTable.COLUMN_PORTFOLIO_ID + " = ?";
			String[] selectionArgs = { portfolioId };
			ContentValues values = new ContentValues();
			values.put(PaiStudyTable.COLUMN_MA_TYPE, String.valueOf(value.charAt(0)));
			getActivity().getContentResolver().update(PaiContentProvider.PAI_STUDY_URI, values, selection, selectionArgs);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sharedPref.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sharedPref.unregisterOnSharedPreferenceChangeListener(this);
	}

}
