package com.timetracker.service;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import com.timetracker.ui.PomodoroService;
import com.timetracker.ui.TaskService;

/**
 * @author Anton Chernetskij
 */
public class ProgressBarService extends IntentService {

    private static Notification.Builder builder;
    private static long start;
    private static long duration;
    private static final int UPDATES_PER_POMODORO = 100;

    /**
     *
     * @param builder
     * @param timeStart
     * @param duration time of pomodoro in minutes
     * @param context
     */
    public static void startNewProgress(Notification.Builder builder, long timeStart, long duration, Context context) {
        ProgressBarService.builder = builder;
        ProgressBarService.duration = duration * PomodoroService.ONE_MINUTE;
        start = timeStart;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, start,
                ProgressBarService.duration / UPDATES_PER_POMODORO, buildIntent(context));
    }

    private static PendingIntent buildIntent(Context context) {
        Intent intent = new Intent(context, ProgressBarService.class);
        return PendingIntent.getService(context, 0, intent, 0);
    }

    public ProgressBarService() {
        super(ProgressBarService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (builder == null) {
            cancellUpdate();
            return;
        }
        long progress = Math.min(duration, System.currentTimeMillis() - start);
        Notification notification = builder.setProgress((int)duration, (int)progress, false).build();
        if (progress == duration) {
            cancellUpdate();
        }

        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        notification.flags |= Notification.FLAG_NO_CLEAR;
        notificationManager.notify(TaskService.CURRENT_TASK_NOTIFICATION_ID, notification);
    }

    private void cancellUpdate() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(buildIntent(getApplicationContext()));
    }
}
