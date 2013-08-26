package com.timetracker.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.timetracker.R;
import com.timetracker.domain.Task;
import com.timetracker.domain.TaskContext;
import com.timetracker.domain.persistance.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Anton Chernetskij
 */
public class TaskCreationActivity extends OrmLiteBaseActivity<DatabaseHelper> {

    public static final String CONTEXT_ID = "com.timetracker.ui.CONTEXT_ID";
    public static final String TASK_ID = "com.timetracker.ui.TASK_ID";
    private Task task;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task);
        Intent intent = getIntent();
        int contextId = intent.getIntExtra(CONTEXT_ID, -1);
        int taskId = intent.getIntExtra(TASK_ID, -1);

        TaskContext context = getContext(contextId);
        task = loadTask(taskId);
        if (task == null){
            task = new Task();
        } else {
            fillForm(task);
        }
        initContextSpinner(context);

        initSaveButton();
    }

    private void fillForm(Task task) {
        TextView nameEdit = (TextView) findViewById(R.id.taskNameEdit);
        nameEdit.setText(task.name);
    }

    private void initSaveButton() {
        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    EditText taskNameEdit = (EditText) findViewById(R.id.taskNameEdit);
                    String taskName = taskNameEdit.getText().toString();

                    if (taskName.trim().isEmpty()) {
                        Resources res = getResources();
                        String msg = String.format(res.getString(R.string.emptyName));
                        Toast toast = Toast.makeText(TaskCreationActivity.this, msg, Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        task.name = taskName;

                        Spinner contextSpinner = (Spinner) findViewById(R.id.contextSpinner);
                        task.context = (TaskContext) contextSpinner.getSelectedItem();

                        if (task.id == null) {
                            getHelper().getTaskDao().create(task);
                        } else {
                            getHelper().getTaskDao().update(task);
                        }
                        finish();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private TaskContext getContext(int contextId) {
        try {
            return getHelper().getContextDao().queryForId(contextId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Task loadTask(int taskId) {
        if (taskId == -1) {
            return null;
        }
        try {
            return getHelper().getTaskDao().queryForId(taskId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initContextSpinner(TaskContext selectedContext) {
        Spinner spinner = (Spinner) findViewById(R.id.contextSpinner);
        try {
            List<TaskContext> contexts = getHelper().getContextDao().queryForAll();
            ArrayAdapter<TaskContext> dataAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, contexts);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(dataAdapter);
            spinner.setSelection(contexts.indexOf(selectedContext));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
