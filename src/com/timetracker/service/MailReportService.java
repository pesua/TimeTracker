package com.timetracker.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import biz.source_code.miniTemplator.MiniTemplator;
import com.timetracker.R;
import com.timetracker.ui.ReportGenerator;
import com.timetracker.ui.activities.SettingsActivity;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Anton Chernetskij
 */
public class MailReportService extends IntentService {

    public MailReportService() {
        super(MailReportService.class.getSimpleName());
    }

    private Session createSessionObject() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        return Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("report.timetracker", "wasted-time");
            }
        });
    }

    private Message createMessage(String[] emails, String subject, String messageBody, Session session) throws MessagingException, UnsupportedEncodingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("report.timetracker@gmail.com", "TimeTracker"));
        for (String email : emails) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email, email));
        }
        message.setSubject(subject);
        message.setContent(messageBody, "text/html; charset=utf-8");
        return message;
    }

    public void sendReports(Context context, String[] emails) {
        try {
            String body = generateReport(context);
            String date = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
            Message message = createMessage(emails, date, body, createSessionObject());
            Transport.send(message);
            Log.i(MailReportService.class.getSimpleName(), "Report has been sent.");
        } catch (MessagingException | IOException e) {
            Log.e(MailReportService.class.getSimpleName(), "Error sending report ", e);
        }
    }

    private String generateReport(Context context) throws IOException {
        List<ReportGenerator.AggregatedTaskItem> reportItems = new ReportGenerator().generateReport(new Date(), new Date(), context);

        MiniTemplator templator = buildTemplator(context);
        if (!reportItems.isEmpty()) {
            long max = reportItems.get(0).duration;
            for (ReportGenerator.AggregatedTaskItem item : reportItems) {
                templator.setVariable("name", item.task.name + " " + buildTime(item.duration));
                templator.setVariable("width", Double.toString(100 * ((double) item.duration) / max));
                templator.setVariable("color", "#" + Integer.toHexString(item.task.color).substring(2, 8));
                templator.addBlock("bar");
            }
            StringWriter report = new StringWriter();
            templator.generateOutput(report);
            return report.toString();
        } else {
            return "Nothing to report.";
        }
    }

    private MiniTemplator buildTemplator(Context context) throws IOException {
        InputStream stream = context.getResources().openRawResource(R.raw.email_report_template);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        StringBuilder templateText = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            templateText.append(line).append("\n");
        }
        MiniTemplator.TemplateSpecification templateSpecification = new MiniTemplator.TemplateSpecification();
        templateSpecification.templateText = templateText.toString();

        return new MiniTemplator(templateSpecification);
    }

    private String buildTime(long time) {
        time /= 1000;
        long s = time % 60;
        time /= 60;
        long m = time % 60;
        time /= 60;
        long h = time;
        return String.format("%d:%02d", h, m);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sendReport();
    }

    private void sendReport() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String[] emails = preferences.getString(SettingsActivity.KEY_REPORT_EMAILS, null).split("\\,");

        Log.i(MailReportService.class.getSimpleName(), "Sending daily reports to " + Arrays.toString(emails));
        sendReports(this, emails);
        Log.i(MailReportService.class.getSimpleName(), "Emails has been sent.");
    }

    public static void scheduleReport(Context context) {
        cancelReport(context);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean sendReport = preferences.getBoolean(SettingsActivity.KEY_SEND_REPORT, false);
        if (sendReport) {
            int time = preferences.getInt(SettingsActivity.KEY_REPORT_TIME, 2359);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, time / 100);
            calendar.set(Calendar.MINUTE, time % 100);

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1);
            }
            AlarmManager alarmManager = getAlarmManager(context);
            PendingIntent alarmIntent = buildIntent(context);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    1000 * 60 * 60 * 24, alarmIntent);
        }
    }

    private static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public static void cancelReport(Context context) {
        PendingIntent intent = buildIntent(context);
        AlarmManager alarmManager = getAlarmManager(context);
        alarmManager.cancel(intent);
    }

    private static PendingIntent buildIntent(Context context) {
        Intent intent = new Intent(context, MailReportService.class);
        return PendingIntent.getService(context, 0, intent, 0);
    }
}
