package com.timetracker.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.timetracker.R;
import com.timetracker.domain.Task;
import com.timetracker.domain.TaskContext;
import com.timetracker.domain.TaskSwitchEvent;
import com.timetracker.domain.persistance.DatabaseHelper;
import com.timetracker.service.BackupTask;
import com.timetracker.service.MailReportService;
import com.timetracker.service.RestoreBackupTask;
import com.timetracker.ui.PomodoroService;
import com.timetracker.ui.TaskList;
import com.timetracker.ui.TaskService;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {
    private TaskService taskService;
    private PomodoroService pomodoroService;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        pomodoroService = new PomodoroService(getApplicationContext());
        taskService = new TaskService(this, pomodoroService);

        loadContextSpinner();
        loadTaskList();
        refreshTimer();
        initContextCreationButton();
        initTaskCreationButton();
        initRemoveContextButton();
        initUndoButton();
    }

    private void initUndoButton() {
        View undoButton = findViewById(R.id.undoStartButton);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (taskService.undoStartTask()) {
                    refreshTimer();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.showTimelineReportButton:
                intent = new Intent(MainActivity.this, TimelinePagerActivity.class);
                break;
            case R.id.showComparisonReportButton:
                intent = new Intent(MainActivity.this, ComparisonReportActivity.class);
                break;
            case R.id.sendReportButton:
                intent = new Intent(MainActivity.this, MailReportService.class);
                MainActivity.this.startService(intent);
                return true;
            case R.id.showSettings:
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                break;
            case R.id.exportDbButton:
                new BackupTask(this).execute();
                break;
            case R.id.importDbButton:
                importBackup();
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        pomodoroService.cancelPomodoroNotification();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAll();
    }

    public void refreshAll() {
        loadContextSpinner();
        loadTaskList();
        refreshTimer();
    }

    protected void onDestroy() {
        pomodoroService.onDestroy();    //todo check do we need this at all
        super.onDestroy();
    }

    private void loadContextSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        try {
            TaskContext currentContext = getCurrentContext();
            List<TaskContext> contexts = getHelper().getContextDao().queryBuilder().orderBy("name", true)
                    .where().eq("isDeleted", Boolean.FALSE).query();
            OpenHelperManager.releaseHelper();
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
            if (currentContext == null) {
                TaskSwitchEvent switchEvent = taskService.getLastTaskSwitch();
                if (switchEvent != null) {
                    currentContext = switchEvent.task.context;
                }
            }
            spinner.setSelection(contexts.indexOf(currentContext));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadTaskList() {
        try {
            TaskContext context = getCurrentContext();
            Integer id = context != null ? context.id : null;
            List<Task> queryResult;
            if (context == null) {
                queryResult = new ArrayList<>();
            } else {
                queryResult = getHelper().getTaskDao().queryBuilder().orderBy("name", true)
                        .where().eq("context_id", id).and().eq("isDeleted", Boolean.FALSE).query();
                OpenHelperManager.releaseHelper();
            }
            final List<Task> tasks = queryResult;
            ListAdapter adapter = new TaskList(this).createListAdapter(tasks);
            ListView listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(adapter);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public TaskContext getCurrentContext() {
        Spinner contextSpinner = (Spinner) findViewById(R.id.spinner);
        return (TaskContext) contextSpinner.getSelectedItem();
    }

    public void refreshTimer() {
        TaskSwitchEvent lastEvent = taskService.getLastTaskSwitch();
        if (lastEvent != null) {
            EditText taskName = (EditText) findViewById(R.id.currentTaskName);
            taskName.setText(lastEvent.task.name);

            Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer);
            chronometer.start();
            chronometer.setBase(SystemClock.elapsedRealtime() - (new Date().getTime() - lastEvent.switchTime.getTime()));
        }
    }

    private void initContextCreationButton() {
        View button = findViewById(R.id.createContextButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ContextCreationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initTaskCreationButton() {
        View button = findViewById(R.id.createTaskButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskContext context = getCurrentContext();
                if (context != null) {
                    Intent intent = new Intent(MainActivity.this, TaskCreationActivity.class);
                    intent.putExtra(TaskCreationActivity.CONTEXT_ID, context.id);
                    intent.putExtra(TaskCreationActivity.TASK_ID, -1);
                    startActivity(intent);
                }
            }
        });
    }

    private void initRemoveContextButton() {
        View removeContextButton = findViewById(R.id.removeContextButton);
        removeContextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TaskContext context = getCurrentContext();
                if (context != null) {
                    showRemoveDialog(context);
                }
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
                        taskService.removeTaskContext(context);
                        loadContextSpinner();
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

    public TaskService getTaskService() {
        return taskService;
    }

    private void importBackup() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle(R.string.import_dialog);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.select_dialog_singlechoice);
        File sd = Environment.getExternalStorageDirectory();
        String[] list = new File(sd, BackupTask.APP_FOLDER).list();
        for (String filename : list) {
            if (filename.endsWith(".sqlite")) {
                arrayAdapter.add(filename.substring(0, filename.length() - 7));
            }
        }
        builderSingle.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String filename = arrayAdapter.getItem(which);
                        AlertDialog.Builder builderInner = new AlertDialog.Builder(
                                MainActivity.this);
                        builderInner.setMessage(filename);
                        builderInner.setTitle(getString(R.string.restore_confirmation));
                        builderInner.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new RestoreBackupTask(MainActivity.this).execute(filename);
                            }
                        });
                        builderInner.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builderInner.show();
                    }
                });
        builderSingle.show();
    }

    private DatabaseHelper getHelper() {
        return OpenHelperManager.getHelper(getApplicationContext(), DatabaseHelper.class);
    }
}
