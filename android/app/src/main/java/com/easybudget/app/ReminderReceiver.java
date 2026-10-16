package com.easybudget.app;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL = "easybudget_savings";
    private static final int ALARM_ID = 2401;
    public static void createChannel(Context c) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Nhắc nhở tiết kiệm", NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription("Nhắc bạn dành một khoản nhỏ cho mục tiêu tương lai.");
            NotificationManager nm = c.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }
    static PendingIntent pending(Context c) {
        Intent i = new Intent(c, ReminderReceiver.class);
        return PendingIntent.getBroadcast(c, ALARM_ID, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    public static void schedule(Context c, int hour, int minute, boolean enabled) {
        c.getSharedPreferences("easybudget_reminder", Context.MODE_PRIVATE).edit()
            .putBoolean("enabled", enabled).putInt("hour", hour).putInt("minute", minute).apply();
        AlarmManager am = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = pending(c);
        am.cancel(pi);
        if (!enabled) return;
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, Math.max(0, Math.min(23, hour)));
        next.set(Calendar.MINUTE, Math.max(0, Math.min(59, minute)));
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (!next.after(Calendar.getInstance())) next.add(Calendar.DAY_OF_YEAR, 1);
        // Inexact repeating alarms are battery-friendly and do not require exact-alarm special access.
        if (Build.VERSION.SDK_INT >= 23) am.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        else am.setRepeating(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }
    @Override public void onReceive(Context context, Intent intent) {
        createChannel(context);
        Intent open = new Intent(context, MainActivity.class);
        PendingIntent content = PendingIntent.getActivity(context, 2402, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(context, CHANNEL) : new Notification.Builder(context);
        b.setSmallIcon(android.R.drawable.ic_dialog_info)
         .setContentTitle("EasyBudget 🌱")
         .setContentText("Một khoản nhỏ hôm nay sẽ giúp bạn tiến gần hơn đến mục tiêu.")
         .setAutoCancel(true).setContentIntent(content);
        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(2401, b.build());
    }
}
