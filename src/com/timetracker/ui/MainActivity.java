package com.timetracker.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
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
        initContextCreationButton();
        initTaskCreationButton();
        initRemoveContextButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initContextSpinner();
        loadTaskList();
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
            List<TaskContext> contexts = getHelper().getContextDao().queryBuilder().orderBy("name", true)
                    .where().eq("isDeleted", Boolean.FALSE).query();
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
            TaskContext context = getCurrentContext();
            final List<Task> tasks = getHelper().getTaskDao().queryBuilder().orderBy("name", true)
                    .where().eq("context_id", context.id).and().eq("isDeleted", Boolean.FALSE).query();
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
                        row = inflater.inflate(R.layout.task_list_item, parent, false);
                    }

                    final Task task = tasks.get(position);
                    row.setTag(task);
                    TextView taskName = (TextView) row.findViewById(R.id.taskName);
                    taskName.setText(task.name);

                    Button taskStartButton = (Button) row.findViewById(R.id.startTask);
                    taskStartButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startTask(task);
                            refreshTimer();
                        }
                    });

                    Button removeTaskButton = (Button) row.findViewById(R.id.removeTaskButton);
                    removeTaskButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showRemoveDialog(task);
                        }
                    });

                    row.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {

                            Intent intent = new Intent(MainActivity.this, TaskCreationActivity.class);
                            intent.putExtra(TaskCreationActivity.CONTEXT_ID, getCurrentContext().id);
                            intent.putExtra(TaskCreationActivity.TASK_ID, task.id);
                            startActivity(intent);
                            return false;
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

    private void showRemoveDialog(final Task task) {
        Resources res = getResources();
        String msg = String.format(res.getString(R.string.removeContextDialogText, task.name));
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage(msg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeTask(task);
                        loadTaskList();
                        dialog.dismiss();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    private TaskContext getCurrentContext() {
        Spinner contextSpinner = (Spinner) findViewById(R.id.spinner);
        return (TaskContext) contextSpinner.getSelectedItem();
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

    private void initContextCreationButton() {
        Button button = (Button) findViewById(R.id.createContextButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ContextCreationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initTaskCreationButton() {
        Button button = (Button) findViewById(R.id.createTaskButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TaskCreationActivity.class);
                intent.putExtra(TaskCreationActivity.CONTEXT_ID, getCurrentContext().id);
                intent.putExtra(TaskCreationActivity.TASK_ID, -1);
                startActivity(intent);
            }
        });
    }

    private void initRemoveContextButton() {
        Button removeContextButton = (Button) findViewById(R.id.removeContextButton);
        removeContextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TaskContext context = getCurrentContext();
                showRemoveDialog(context);
            }
        });
    }

    private void showRemoveDialog(final TaskContext context) {
        Resources res = getResources();
        String msg = String.format(res.getString(R.string.removeContextDialogText, context.name));
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage(msg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeTaskContext(context);
                        initContextSpinner();
                        dialog.dismiss();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    private void startTask(Task task) {
        try {
            TaskSwitchEvent lastSwitchEvent = lastTaskSwitch();
            if (lastSwitchEvent != null && lastSwitchEvent.task.id.equals(task.id)) {
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

            String[] firstResult = results.getFirstResult();
            if (firstResult == null) {
                return null;
            } else {
                int lastEventId = Integer.valueOf(firstResult[0]);
                return getHelper().getEventsDao().queryForId(lastEventId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void removeTaskContext(TaskContext context) {
        context.isDeleted = true;
        try {
            getHelper().getContextDao().update(context);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void removeTask(Task task) {
        task.isDeleted = true;
        try {
            getHelper().getTaskDao().update(task);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
