package com.timetracker.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import com.timetracker.R;
import com.timetracker.service.MailReportService;

/**
 * @author Anton Chernetskij
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_SEND_REPORT = "pref_send_report";
    public static final String KEY_REPORT_EMAILS = "pref_report_emails";
    public static final String KEY_REPORT_TIME = "pref_daily_report_time";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_REPORT_TIME) || key.equals(KEY_REPORT_EMAILS) || key.equals(KEY_SEND_REPORT)) {
            Log.i(SettingsActivity.class.getSimpleName(), "Settings has been changed.");
            MailReportService.scheduleReport(getApplicationContext());
        }
    }
}
