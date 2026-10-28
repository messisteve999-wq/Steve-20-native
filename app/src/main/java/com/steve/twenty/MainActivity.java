package com.steve.twenty;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    EditText key, contacts;
    Switch auto;
    SharedPreferences prefs;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("steve20", MODE_PRIVATE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(0xFF101014);

        TextView title = new TextView(this);
        title.setText("STEVE-20");
        title.setTextSize(28);
        title.setTextColor(0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView desc = new TextView(this);
        desc.setText("Assistant flottant Gemini • WhatsApp et WhatsApp Business");
        desc.setTextColor(0xFFCCCCD4);
        desc.setGravity(Gravity.CENTER);
        desc.setPadding(0, 8, 0, 18);
        root.addView(desc);

        key = new EditText(this);
        key.setSingleLine(true);
        key.setHint("Clé API Gemini (AI Studio)");
        key.setHintTextColor(0xFF888892);
        key.setTextColor(0xFFFFFFFF);
        key.setInputType( android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        key.setText(prefs.getString("api_key", ""));
        root.addView(key, new LinearLayout.LayoutParams(-1, -2));

        contacts = new EditText(this);
        contacts.setHint("Contacts autorisés, séparés par des virgules");
        contacts.setHintTextColor(0xFF888892);
        contacts.setTextColor(0xFFFFFFFF);
        contacts.setText(prefs.getString("allowed_contacts", ""));
        contacts.setMinLines(2);
        contacts.setGravity(Gravity.TOP);
        root.addView(contacts, new LinearLayout.LayoutParams(-1, -2));

        TextView help = new TextView(this);
        help.setText("Exemple : Marie, Papa, Kevin\nLe nom doit correspondre au titre de la notification. Le mode automatique est désactivé par défaut.");
        help.setTextColor(0xFFB8B8C2);
        help.setPadding(0, 8, 0, 8);
        root.addView(help);

        auto = new Switch(this);
        auto.setText("Réponse automatique (contacts autorisés uniquement)");
        auto.setTextColor(0xFFFFFFFF);
        auto.setChecked(prefs.getBoolean("auto_reply", false));
        root.addView(auto);

        Button save = new Button(this);
        save.setText("Enregistrer les réglages");
        root.addView(save);
        save.setOnClickListener(v -> {
            prefs.edit().putString("api_key", key.getText().toString().trim())
                .putString("allowed_contacts", contacts.getText().toString().trim())
                .putBoolean("auto_reply", auto.isChecked()).apply();
            Toast.makeText(this, "Réglages enregistrés sur ce téléphone", Toast.LENGTH_SHORT).show();
        });

        Button notif = new Button(this);
        notif.setText("1. Autoriser l'accès aux notifications");
        root.addView(notif);
        notif.setOnClickListener(v -> startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));

        Button overlay = new Button(this);
        overlay.setText("2. Autoriser l'affichage par-dessus les applis");
        root.addView(overlay);
        overlay.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(i);
            } else Toast.makeText(this, "Permission déjà accordée", Toast.LENGTH_SHORT).show();
        });

        Button start = new Button(this);
        start.setText("Lancer la bulle flottante");
        root.addView(start);
        start.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Autorise d'abord l'affichage par-dessus les applis", Toast.LENGTH_LONG).show();
                return;
            }
            if (android.os.Build.VERSION.SDK_INT >= 26) startForegroundService(new Intent(this, FloatingService.class));\n            else startService(new Intent(this, FloatingService.class));
            Toast.makeText(this, "Bulle Steve-20 lancée", Toast.LENGTH_SHORT).show();
        });

        TextView privacy = new TextView(this);
        privacy.setText("Confidentialité : les messages autorisés sont envoyés à l'API Gemini pour générer une réponse. Ne partage pas l'APK avec ta clé personnelle.");
        privacy.setTextColor(0xFF9999A4);
        privacy.setTextSize(12);
        privacy.setPadding(0, 16, 0, 0);
        root.addView(privacy);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }
}
