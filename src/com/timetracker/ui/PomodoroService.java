package com.timetracker.ui;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import com.timetracker.R;
import com.timetracker.ui.activities.MainActivity;

import java.util.Date;

/**
 * @author Anton Chernetskij
 */
public class PomodoroService {
    public static final long ONE_MINUTE = 60 * 1000;
    public static final int POMODORO_NOTIFICATION_ID = 0;

    private AlarmManager alarmManager;
    private PendingIntent pomodoroIntent;
    private BroadcastReceiver pomodoroBroadcastReceiver;
    private Context context;

    public PomodoroService(Context context) {
        this.context = context;
        pomodoroBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                showPomodoroNotification(c);
            }
        };
        String action = "com.timetracker.pomodoroEnd";
        context.registerReceiver(pomodoroBroadcastReceiver, new IntentFilter(action));
        pomodoroIntent = PendingIntent.getBroadcast(context, 0, new Intent(action), 0);
        alarmManager = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));
    }

    public void startPomodoro(int durationMinutes, Date timeStart) {
        alarmManager.cancel(pomodoroIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeStart.getTime() + durationMinutes * ONE_MINUTE, pomodoroIntent);
    }

    public void stopPomodoro() {
        if (pomodoroIntent != null) {
            alarmManager.cancel(pomodoroIntent);
        }
    }

    private void showPomodoroNotification(Context context) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification noti = new Notification.Builder(context)
                .setContentTitle("Pomodoro finished")
                .setContentText("Click to open tracker")
                .setSmallIcon(R.drawable.check)
                .setSound(soundUri)
                .setContentIntent(intent)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(POMODORO_NOTIFICATION_ID, noti);
    }

    public void cancelPomodoroNotification() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(PomodoroService.POMODORO_NOTIFICATION_ID);
    }

    public void onDestroy(){
        alarmManager.cancel(pomodoroIntent);
        context.unregisterReceiver(pomodoroBroadcastReceiver);
    }
}
