package com.timetracker.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.j256.ormlite.dao.GenericRawResults;
import com.timetracker.R;
import com.timetracker.domain.Task;
import com.timetracker.domain.TaskContext;
import com.timetracker.domain.TaskSwitchEvent;
import com.timetracker.domain.persistance.DatabaseHelper;
import com.timetracker.ui.activities.MainActivity;

import java.sql.SQLException;
import java.util.Date;

public class TaskService {
    public static final int CURRENT_TASK_NOTIFICATION_ID = 1;

    private final Context applicationContext;
    private final DatabaseHelper databaseHelper;
    private final PomodoroService pomodoroService;

    public TaskService(Context context, DatabaseHelper databaseHelper, PomodoroService pomodoroService) {
        this.applicationContext = context.getApplicationContext();
        this.databaseHelper = databaseHelper;
        this.pomodoroService = pomodoroService;
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

            pomodoroService.stopPomodoro();
            if (task.pomodoroDuration != 0) {
                pomodoroService.startPomodoro(task.pomodoroDuration);
            }
            showCurrentTaskNotification(task);
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

    public void showCurrentTaskNotification() {
        Task task = getLastTaskSwitch().task;
        showCurrentTaskNotification(task);
    }

    public void showCurrentTaskNotification(Task task) {
        Intent notificationIntent = new Intent(applicationContext, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(applicationContext, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(applicationContext)
                .setContentTitle("Working on " + task.name)
                .setContentText("Click to open tracker")
                .setSmallIcon(R.drawable.clock)
                .setContentIntent(intent)
                .build();

        NotificationManager notificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notification.flags |= Notification.FLAG_NO_CLEAR;

        notificationManager.cancel(CURRENT_TASK_NOTIFICATION_ID);
        notificationManager.notify(CURRENT_TASK_NOTIFICATION_ID, notification);
    }
}