package com.easybudget.app;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("easybudget_reminder", Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("enabled", false);
        int hour = prefs.getInt("hour", 20);
        int minute = prefs.getInt("minute", 0);
        ReminderReceiver.schedule(context, hour, minute, enabled);
    }
}
