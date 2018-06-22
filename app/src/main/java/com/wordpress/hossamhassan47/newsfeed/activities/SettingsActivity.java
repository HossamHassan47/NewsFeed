package com.wordpress.hossamhassan47.newsfeed.activities;

import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.wordpress.hossamhassan47.newsfeed.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Load Settings Fragment
        getFragmentManager().beginTransaction().replace(R.id.flContent, new MainSettingsFragment()).commit();
    }

    public static class MainSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_default_section)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_order_by)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_order_date)));
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());

            String preferenceString = preferences.getString(preference.getKey(), "");

            onPreferenceChange(preference, preferenceString);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            // The code in this method takes care of updating the displayed preference summary after it has been changed
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else if (preference instanceof EditTextPreference) {
                preference.setSummary(stringValue);
            }

            return true;
        }
    }
}
