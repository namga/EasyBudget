package com.easybudget.app;

import android.app.Activity;
import android.content.Context;
import android.webkit.JavascriptInterface;

public class ReminderBridge {
    private final Context context;
    public ReminderBridge(Context context) { this.context = context.getApplicationContext(); }
    @JavascriptInterface public void scheduleDailyReminder(int hour, int minute, boolean enabled) {
        ReminderReceiver.schedule(context, hour, minute, enabled);
    }
}
