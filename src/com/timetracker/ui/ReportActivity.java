package com.timetracker.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
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
public class ReportActivity extends OrmLiteBaseActivity<DatabaseHelper> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);

        int hourHeight = 60;    //todo add zoom
        initTimeRuler(hourHeight);
        initTasksStack(hourHeight, new Date());
    }

    private void initTimeRuler(int hourHeight) {
        LinearLayout ruler = (LinearLayout) findViewById(R.id.timeRuler);

        for (int i = 0; i < 24; i++) {
            TextView textView = new TextView(this);
            textView.setText(String.format("%2d:00", i));
            textView.setHeight(hourHeight);
            ruler.addView(textView);
        }
    }

    private void initTasksStack(int hourHeight, Date date) {
        try {
            List<TaskSwitchEvent> events = getTaskEvents(date);

            if (events.isEmpty()) {
                return;
            }

            Task currentTask = null;
            Integer firstEventId = events.get(0).id;
            if (firstEventId > 1) {
                TaskSwitchEvent previousEvent = getHelper().getEventsDao().queryForId(firstEventId - 1);
                currentTask = previousEvent.task;
            }
            int hours = 0;
            int minutes = 0;

            LinearLayout tasksLine = (LinearLayout) findViewById(R.id.tasksLine);
            for (TaskSwitchEvent event : events) {
                TextView taskBox = new TextView(this);
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
                tasksLine.addView(taskBox, getLayoutParams());

                hours = calendar.get(Calendar.HOUR_OF_DAY);
                minutes = calendar.get(Calendar.MINUTE);
                currentTask = event.task;
            }
            TextView taskBox = new TextView(this);
            if (currentTask != null) {
                taskBox.setText(currentTask.name);
                taskBox.setBackground(getFill(currentTask.color));
            } else {
                taskBox.setBackground(getFill(0));
            }
            Calendar calendar = Calendar.getInstance();
            taskBox.setHeight((int) (((calendar.get(Calendar.HOUR_OF_DAY) - hours) * 60.0 + (calendar.get(Calendar.MINUTE) - minutes)) * hourHeight / 60));
            taskBox.setGravity(Gravity.CENTER);

            tasksLine.addView(taskBox, getLayoutParams());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    private List<TaskSwitchEvent> getTaskEvents(Date date) throws SQLException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date from = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date to = calendar.getTime();

        return getHelper().getEventsDao().queryBuilder().where().ge("switchTime", from).and().le("switchTime", to).query();
    }
}
