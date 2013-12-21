package com.timetracker.ui;

import com.timetracker.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.timetracker.domain.Task;
import com.timetracker.ui.activities.MainActivity;
import com.timetracker.ui.activities.TaskCreationActivity;

import java.util.Calendar;
import java.util.List;

public class TaskList {
    private final MainActivity mainActivity;

    public TaskList(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public BaseAdapter createListAdapter(final List<Task> tasks) {
        return new BaseAdapter() {
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
                    LayoutInflater inflater = mainActivity.getLayoutInflater();
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
                        mainActivity.getTaskManager().startTask(task);
                        mainActivity.refreshTimer();
                    }
                });
                taskStartButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (task.equals(mainActivity.getTaskManager().getLastTaskSwitch().task)) {
                            return false;
                        }
                        final TimePicker timePicker = new TimePicker(mainActivity);
                        timePicker.setIs24HourView(true);
                        Calendar now = Calendar.getInstance();
                        timePicker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
                        timePicker.setCurrentMinute(now.get(Calendar.MINUTE));
                        Calendar lastSwitch = Calendar.getInstance();
                        lastSwitch.setTime(mainActivity.getTaskManager().getLastTaskSwitch().switchTime);
                        final int switchMinutes = lastSwitch.get(Calendar.HOUR_OF_DAY) * 60 + lastSwitch.get(Calendar.MINUTE);
                        final int nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

                        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                            @Override
                            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                                int minutes = hourOfDay * 60 + minute;
                                if (
                                        (switchMinutes < nowMinutes && (switchMinutes > minutes || minutes > nowMinutes))
                                                ||
                                                (switchMinutes > nowMinutes && (switchMinutes > minutes && minutes > nowMinutes))
                                        ) {
                                    switchToClosestMoment(view, switchMinutes, nowMinutes);
                                }
                            }

                            void switchToClosestMoment(TimePicker picker, int minutes1, int minutes2) {
                                int minutes = picker.getCurrentHour() * 60 + picker.getCurrentMinute();
                                if (Math.abs(minutes - minutes1) < Math.abs(minutes - minutes2)) {
                                    picker.setCurrentHour(minutes1 / 60);
                                    picker.setCurrentMinute(minutes1 % 60);
                                } else {
                                    picker.setCurrentHour(minutes2 / 60);
                                    picker.setCurrentMinute(minutes2 % 60);
                                }
                                String msg = mainActivity.getResources().getString(R.string.incorrectBackSwitchTime);
                                Toast toast = Toast.makeText(picker.getContext(), msg, Toast.LENGTH_SHORT);
                                toast.setDuration(Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });

                        AlertDialog dialog = new AlertDialog.Builder(mainActivity).setMessage("Create task switch in the past")
                                .setView(timePicker)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Calendar calendar = Calendar.getInstance();
                                        int minutes = timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute();
                                        if (minutes > nowMinutes) {
                                            calendar.add(Calendar.DATE, -1);
                                        }
                                        calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                                        calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                                        mainActivity.getTaskManager().startTask(task, calendar.getTime());
                                        mainActivity.refreshTimer();
                                        dialog.dismiss();
                                    }
                                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create();
                        dialog.show();
                        return true;
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

                        Intent intent = new Intent(mainActivity, TaskCreationActivity.class);
                        intent.putExtra(TaskCreationActivity.CONTEXT_ID, mainActivity.getCurrentContext().id);
                        intent.putExtra(TaskCreationActivity.TASK_ID, task.id);
                        mainActivity.startActivity(intent);
                        return false;
                    }
                });
                return row;
            }
        };
    }

    private void showRemoveDialog(final Task task) {
        Resources res = mainActivity.getResources();
        String msg = String.format(res.getString(com.timetracker.R.string.removeTaskDialogText, task.name));
        AlertDialog dialog = new AlertDialog.Builder(mainActivity).setMessage(msg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mainActivity.getTaskManager().removeTask(task);
                        mainActivity.loadTaskList();
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
}