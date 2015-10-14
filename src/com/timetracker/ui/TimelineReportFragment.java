package com.timetracker.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.timetracker.R;
import com.timetracker.domain.Task;
import com.timetracker.domain.TaskSwitchEvent;
import com.timetracker.domain.persistance.DatabaseHelper;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Anton Chernetskij
 */
public class TimelineReportFragment extends Fragment {

    public static final String ARG_OBJECT = "object";
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.timeline_report, container, false);
        Bundle args = getArguments();
        Date date = new Date(args.getLong(ARG_OBJECT));

        int hourHeight = 60;    //todo add zoom
        initTimeRuler(hourHeight);
        initTasksStack(hourHeight, date);
        return view;
    }

    private void initTimeRuler(int hourHeight) {
        LinearLayout ruler = (LinearLayout) view.findViewById(R.id.timeRuler);

        for (int i = 0; i < 24; i++) {
            TextView textView = new TextView(view.getContext());
            textView.setText(String.format("%2d:00", i));
            textView.setHeight(hourHeight);
            ruler.addView(textView);
        }
    }

    private void initTasksStack(int hourHeight, Date date) {
        try {
            DatabaseHelper helper = getHelper();
            List<TaskSwitchEvent> events = helper.getTaskEvents(date, date);

            if (events.isEmpty()) {
                return;
            }

            Task currentTask = null;
            Integer firstEventId = events.get(0).id;
            if (firstEventId > 1) {
                int eventId = firstEventId;
                TaskSwitchEvent previousEvent;
                while ((previousEvent = helper.getEventsDao().queryForId(--eventId)) == null){
                }
                currentTask = previousEvent.task;
            }
            OpenHelperManager.releaseHelper();
            int hours = 0;
            int minutes = 0;

            LinearLayout tasksLine = (LinearLayout) view.findViewById(R.id.tasksLine);
            for (TaskSwitchEvent event : events) {
                TextView taskBox = new TextView(view.getContext());
                if (currentTask != null) {
                    taskBox.setText(currentTask.name);
                    taskBox.setBackground(getFill(currentTask.color));
                } else {
                    taskBox.setBackground(getFill(0));
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(event.switchTime);
                taskBox.setHeight((int) (((calendar.get(Calendar.HOUR_OF_DAY) - hours) * 60.0 + (calendar.get(Calendar.MINUTE) - minutes)) * hourHeight / 60));
                taskBox.setGravity(Gravity.CENTER);
                taskBox.setTextColor(Color.WHITE);
                tasksLine.addView(taskBox, getLayoutParams());

                hours = calendar.get(Calendar.HOUR_OF_DAY);
                minutes = calendar.get(Calendar.MINUTE);
                currentTask = event.task;
            }
            TextView taskBox = new TextView(view.getContext());
            if (currentTask != null) {
                taskBox.setText(currentTask.name);
                taskBox.setBackground(getFill(currentTask.color));
            } else {
                taskBox.setBackground(getFill(0));
            }
            Calendar calendar = Calendar.getInstance();
            if (!isToday(date)) {
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
            }
            taskBox.setHeight((int) (((calendar.get(Calendar.HOUR_OF_DAY) - hours) * 60.0 + (calendar.get(Calendar.MINUTE) - minutes)) * hourHeight / 60));
            taskBox.setGravity(Gravity.CENTER);
            taskBox.setTextColor(Color.WHITE);

            tasksLine.addView(taskBox, getLayoutParams());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isToday(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
    }

    private GradientDrawable getFill(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setStroke(1, Color.WHITE);
        drawable.setColor(color);
        return drawable;
    }

    private LinearLayout.LayoutParams getLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        return params;
    }

    private DatabaseHelper getHelper(){
        return OpenHelperManager.getHelper(view.getContext(), DatabaseHelper.class);
    }
}
