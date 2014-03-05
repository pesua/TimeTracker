package com.timetracker.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;
import com.timetracker.R;

/**
 * @author Anton Chernetskij
 */
public class TimeDialogPreference extends DialogPreference {

    private TimePicker timePicker;

    public TimeDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.time_dialog);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        timePicker = (TimePicker) view.findViewById(R.id.reportTimePicker);
        timePicker.setIs24HourView(true);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int time = preferences.getInt(SettingsActivity.KEY_REPORT_TIME, 0);
        timePicker.setCurrentHour(time / 100);
        timePicker.setCurrentMinute(time % 100);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            Integer hour = timePicker.getCurrentHour();
            Integer minute = timePicker.getCurrentMinute();
            persistInt(hour * 100 + minute);
        }
    }
}
