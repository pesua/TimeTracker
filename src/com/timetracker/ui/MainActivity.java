package com.timetracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.GenericRawResults;
import com.timetracker.R;
import com.timetracker.domain.Task;
import com.timetracker.domain.TaskContext;
import com.timetracker.domain.TaskSwitchEvent;
import com.timetracker.domain.persistance.DatabaseHelper;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class MainActivity extends OrmLiteBaseActivity<DatabaseHelper> {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initContextSpinner();
        loadTaskList();
        refreshTimer();
        initReportButton();
    }

    private void initReportButton() {
        Button button = (Button) findViewById(R.id.showReportButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initContextSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        try {
            List<TaskContext> contexts = getHelper().getContextDao().queryForAll();
            ArrayAdapter<TaskContext> dataAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, contexts);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(dataAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    loadTaskList();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadTaskList() {
        try {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            TaskContext context = (TaskContext) spinner.getSelectedItem();
            final List<Task> tasks = getHelper().getTaskDao().queryForEq("context_id", context.id);
            ListAdapter adapter = new BaseAdapter() {
                @Override
                public int getCount() {
                    return tasks.size();
                }

                @Override
                public Object getItem(int position) {
                    return tasks.get(position);
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View row = convertView;

                    if (row == null) {
                        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                        row = inflater.inflate(R.layout.task, parent, false);
                    }

                    final Task task = tasks.get(position);
                    row.setTag(task);
                    TextView taskName = (TextView) row.findViewById(R.id.taskName);
                    taskName.setText(task.name);

                    Button button = (Button) row.findViewById(R.id.startTask);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startTask(task);
                            refreshTimer();
                        }
                    });
                    return row;
                }
            };
            ListView listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(adapter);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void refreshTimer() {
        TaskSwitchEvent lastEvent = lastTaskSwitch();
        if (lastEvent != null) {
            EditText taskName = (EditText) findViewById(R.id.currentTaskName);
            taskName.setText(lastEvent.task.name);

            Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer);
            chronometer.start();
            chronometer.setBase(SystemClock.elapsedRealtime() - (new Date().getTime() - lastEvent.switchTime.getTime()));
        } else {
            Log.i(this.getClass().getName(), "Started the first task!");
        }
    }

    private void startTask(Task task) {
        try {
            if (lastTaskSwitch().task.id.equals(task.id)) {
                return;
            }
            TaskSwitchEvent event = new TaskSwitchEvent();
            event.task = task;
            event.switchTime = new Date();
            getHelper().getEventsDao().create(event);

            List<TaskSwitchEvent> taskSwitchEvents = getHelper().getEventsDao().queryForAll();
            Log.i(this.getClass().getName(), "Switches: " + taskSwitchEvents);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private TaskSwitchEvent lastTaskSwitch() {
        try {
            GenericRawResults<String[]> results = getHelper().getEventsDao()
                    .queryRaw("select id from task_switch_events where switchTime = (select max(switchTime) from task_switch_events)");
            int lastEventId = Integer.valueOf(results.getFirstResult()[0]);
            return getHelper().getEventsDao().queryForId(lastEventId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
