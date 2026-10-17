package com.easybudget.app;

import android.app.Activity;
import android.content.Context;
import android.webkit.JavascriptInterface;

public class ReminderBridge {
    private final Activity activity;
    public ReminderBridge(Context context) { this.activity = (Activity) context; }
    @JavascriptInterface public void scheduleDailyReminder(int hour, int minute, boolean enabled) {
        ReminderReceiver.schedule(activity.getApplicationContext(), hour, minute, enabled);
    }
    @JavascriptInterface public void exitApp() {
        activity.runOnUiThread(() -> activity.finish());
    }
}
