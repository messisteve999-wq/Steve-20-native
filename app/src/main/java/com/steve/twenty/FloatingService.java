package com.steve.twenty;

import android.app.Service;\nimport android.app.Notification;\nimport android.app.NotificationChannel;\nimport android.app.NotificationManager;\nimport android.os.Build;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

public class FloatingService extends Service {
    WindowManager wm;
    View bubble, panel;
    WindowManager.LayoutParams bubbleParams, panelParams;
    boolean opened = false;

    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(new NotificationChannel("steve20_running", "Steve-20 actif", NotificationManager.IMPORTANCE_LOW));
            Notification notification = new Notification.Builder(this, "steve20_running")
                .setContentTitle("Steve-20 est actif")
                .setContentText("Touchez la bulle pour ouvrir l'assistant")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true).build();
            startForeground(20, notification);
        }
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return; }
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        int type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        bubble = new TextView(this);
        ((TextView)bubble).setText("S");
        ((TextView)bubble).setTextColor(0xFFFFFFFF);
        ((TextView)bubble).setTextSize(24);
        ((TextView)bubble).setGravity(Gravity.CENTER);
        bubble.setBackground(makeCircle());
        bubbleParams = new WindowManager.LayoutParams(58, 58, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        bubbleParams.gravity = Gravity.TOP | Gravity.LEFT;
        bubbleParams.x = 20; bubbleParams.y = 260;
        bubble.setOnTouchListener(new View.OnTouchListener() {
            float sx, sy; int ox, oy; boolean moved;
            public boolean onTouch(View v, android.view.MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    sx=e.getRawX(); sy=e.getRawY(); ox=bubbleParams.x; oy=bubbleParams.y; moved=false; return true;
                }
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    int dx=(int)(e.getRawX()-sx), dy=(int)(e.getRawY()-sy);
                    if (Math.abs(dx)>8 || Math.abs(dy)>8) moved=true;
                    bubbleParams.x=ox+dx; bubbleParams.y=oy+dy; wm.updateViewLayout(bubble,bubbleParams); return true;
                }
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    if (!moved) togglePanel();
                    return true;
                }
                return true;
            }
        });
        wm.addView(bubble, bubbleParams);
    }

    private android.graphics.drawable.GradientDrawable makeCircle() {
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        d.setColor(0xFF17171D); d.setStroke(3, 0xFF8B5CF6); return d;
    }

    private void togglePanel() {
        if (opened) { removePanel(); return; }
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(18,18,18,18);
        box.setBackgroundColor(0xFF202027);
        TextView heading = new TextView(this);
        heading.setText("STEVE-20 • Gemini");
        heading.setTextColor(0xFFFFFFFF); heading.setTextSize(18);
        box.addView(heading);
        TextView info = new TextView(this);
        android.content.SharedPreferences prefs = getSharedPreferences("steve20", MODE_PRIVATE);
        String contact = prefs.getString("last_contact", "");
        String message = prefs.getString("last_message", "");
        if (contact.isEmpty() || message.isEmpty()) {
            info.setText("Aucun message récent détecté. Autorise l'accès aux notifications, puis reçois un message WhatsApp ou WhatsApp Business.");
        } else {
            info.setText("Dernier message de : " + contact + "\\n\\n" + message + "\\n\\nMode automatique : " + (prefs.getBoolean("auto_reply", false) ? "ACTIF" : "désactivé"));
        }
        info.setTextColor(0xFFE2E2EA); info.setPadding(0,12,0,12);
        box.addView(info);
        Button settings = new Button(this); settings.setText("Réglages / contacts autorisés");
        box.addView(settings);
        settings.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i);
        });
        Button close = new Button(this); close.setText("Fermer la fenêtre");
        box.addView(close); close.setOnClickListener(v -> removePanel());
        panel = box;
        panelParams = new WindowManager.LayoutParams(
            Math.min(getResources().getDisplayMetrics().widthPixels - 32, 360),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT);
        panelParams.gravity = Gravity.CENTER;
        wm.addView(panel, panelParams); opened=true;
    }

    private void removePanel() {
        if (panel != null) { try { wm.removeView(panel); } catch(Exception ignored) {} panel=null; }
        opened=false;
    }

    @Override public int onStartCommand(Intent i, int f, int id) { return START_STICKY; }
    @Override public void onDestroy() {
        removePanel();
        if (bubble != null) { try { wm.removeView(bubble); } catch(Exception ignored) {} }
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent i) { return null; }
}
