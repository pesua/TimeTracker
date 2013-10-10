package com.timetracker.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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
    private GridView colorsGrid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task);
        Intent intent = getIntent();
        int contextId = intent.getIntExtra(CONTEXT_ID, -1);
        int taskId = intent.getIntExtra(TASK_ID, -1);

        TaskContext context = getContext(contextId);
        task = loadTask(taskId);
        if (task == null) {
            task = new Task();
        } else {
            setName(task.name);
        }
        initContextSpinner(context);

        initColorChoser(task.color);

        initSaveButton();

        initDurationPicker();
    }

    private void initDurationPicker() {
        TimePicker durationPicker = (TimePicker) findViewById(R.id.durationPicker);
        durationPicker.setIs24HourView(true);
        durationPicker.setCurrentMinute(task.pomodoroDuration % 60);
        durationPicker.setCurrentHour(task.pomodoroDuration / 60);
    }

    private void refreshGrid() {
        colorsGrid.invalidateViews();
    }

    public class ColorChooserAdapter extends BaseAdapter {

        public ColorChooserAdapter(int color) {
            selected = -1;
            int[] colors = getColors();
            for (int i = 0; i < colors.length; i++) {
                if (colors[i] == color) {
                    selected = i;
                    break;
                }
            }
            if (selected < 0 || selected > colors.length - 1) {
                selected = 0;
            }
        }

        public int getSelectedColor() {
            return getColors()[selected];
        }

        private int selected = 0;

        private int[] getColors() {
            return Task.COLORS;
        }

        @Override
        public int getCount() {
            return getColors().length;
        }

        @Override
        public Object getItem(int position) {
            return getColors()[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Button button = new Button(TaskCreationActivity.this);
            GradientDrawable drawable = getBackground(getColors()[position], position == selected);
            button.setBackground(drawable);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selected = position;
                    TaskCreationActivity.this.refreshGrid();
                }
            });

            return button;
        }

        private GradientDrawable getBackground(int color, boolean isSelected) {
            GradientDrawable drawable = new GradientDrawable();
            if (isSelected) {
                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setStroke(5, Color.WHITE);
            }
            drawable.setColor(color);
            drawable.setAlpha(Task.DEFAULT_COLOR_ALPHA);
            return drawable;
        }
    }

    private void setName(String name) {
        TextView nameEdit = (TextView) findViewById(R.id.taskNameEdit);
        nameEdit.setText(name);
    }

    private void initColorChoser(int color) {
        colorsGrid = (GridView) findViewById(R.id.colorsGrid);
        colorsGrid.setAdapter(new ColorChooserAdapter(color));
        colorsGrid.setColumnWidth(20);
        colorsGrid.setNumColumns(5);
    }

    private int getSelectedColor() {
        ColorChooserAdapter colorChooser = (ColorChooserAdapter) colorsGrid.getAdapter();
        return colorChooser.getSelectedColor();
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
                        task.color = getSelectedColor();

                        TimePicker durationPicker = (TimePicker) findViewById(R.id.durationPicker);
                        task.pomodoroDuration = durationPicker.getCurrentHour() * 60 + durationPicker.getCurrentMinute();

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
