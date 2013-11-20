package com.timetracker.ui;

import com.j256.ormlite.dao.GenericRawResults;
import com.timetracker.domain.Task;
import com.timetracker.domain.TaskContext;
import com.timetracker.domain.TaskSwitchEvent;
import com.timetracker.domain.persistance.DatabaseHelper;

import java.sql.SQLException;
import java.util.Date;

public class TaskManager {
    private final MainActivity mainActivity;     //todo remove after introducing pomodoro component
    private final DatabaseHelper databaseHelper;

    public TaskManager(MainActivity mainActivity, DatabaseHelper databaseHelper) {
        this.mainActivity = mainActivity;
        this.databaseHelper = databaseHelper;
    }                                            

    public void startTask(Task task) {
        startTask(task, new Date());
    }

    public void startTask(Task task, Date timeStart) {
        try {
            TaskSwitchEvent lastSwitchEvent = getLastTaskSwitch();
            if (lastSwitchEvent != null && lastSwitchEvent.task.id.equals(task.id)) {
                return;
            }
            TaskSwitchEvent event = new TaskSwitchEvent();
            event.task = task;
            event.switchTime = timeStart;
            databaseHelper.getEventsDao().create(event);

            mainActivity.stopPomodoro();
            if (task.pomodoroDuration != 0) {
                mainActivity.startPomodoro(task.pomodoroDuration);
            }
            mainActivity.showCurrentTaskNotification(mainActivity, task);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public TaskSwitchEvent getLastTaskSwitch() {
        try {
            GenericRawResults<String[]> results = databaseHelper.getEventsDao()
                    .queryRaw("select id from task_switch_events where switchTime = (select max(switchTime) from task_switch_events)");

            String[] firstResult = results.getFirstResult();
            if (firstResult == null) {
                return null;
            } else {
                int lastEventId = Integer.valueOf(firstResult[0]);
                return databaseHelper.getEventsDao().queryForId(lastEventId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeTaskContext(TaskContext context) {
        context.isDeleted = true;
        try {
            databaseHelper.getContextDao().update(context);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeTask(Task task) {
        task.isDeleted = true;
        try {
            databaseHelper.getTaskDao().update(task);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}