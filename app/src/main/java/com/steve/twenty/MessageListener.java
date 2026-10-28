package com.steve.twenty;

import android.app.Notification;
import android.app.RemoteInput;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageListener extends NotificationListenerService {
    private final ExecutorService pool = Executors.newSingleThreadExecutor();
    private String lastFingerprint = "";

    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();
        if (!pkg.equals("com.whatsapp") && !pkg.equals("com.whatsapp.w4b")) return;
        Bundle extras = sbn.getNotification().extras;
        if (extras == null) return;
        String title = String.valueOf(extras.getCharSequence(Notification.EXTRA_TITLE, ""));
        CharSequence textCs = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        if (TextUtils.isEmpty(textCs)) textCs = extras.getCharSequence(Notification.EXTRA_TEXT);
        String message = textCs == null ? "" : textCs.toString().trim();
        if (title.trim().isEmpty() || message.isEmpty() || message.equals("This message was deleted")) return;

        SharedPreferences p = getSharedPreferences("steve20", MODE_PRIVATE);
        p.edit().putString("last_contact", title).putString("last_message", message).apply();
        if (!p.getBoolean("auto_reply", false)) return;
        String apiKey = p.getString("api_key", "").trim();
        if (apiKey.isEmpty() || !isAllowed(title, p.getString("allowed_contacts", ""))) return;

        String fingerprint = pkg + "|" + sbn.getKey() + "|" + message;
        synchronized (this) {
            if (fingerprint.equals(lastFingerprint)) return;
            lastFingerprint = fingerprint;
        }
        final String contact = title;
        final String incoming = message;
        pool.execute(() -> {
            try {
                String reply = generateReply(apiKey, contact, incoming);
                if (!reply.isEmpty()) sendQuickReply(sbn, reply);
            } catch (Exception ignored) { }
        });
    }

    private boolean isAllowed(String title, String raw) {
        if (raw == null || raw.trim().isEmpty()) return false;
        for (String name : raw.split(",")) {
            if (title.trim().equalsIgnoreCase(name.trim())) return true;
        }
        return false;
    }

    private String generateReply(String key, String contact, String incoming) throws Exception {
        URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + java.net.URLEncoder.encode(key, "UTF-8"));
        HttpURLConnection c = (HttpURLConnection)url.openConnection();
        c.setRequestMethod("POST"); c.setConnectTimeout(12000); c.setReadTimeout(20000);
        c.setDoOutput(true); c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        JSONObject part = new JSONObject().put("text",
            "Tu es Steve-20, assistant de messagerie. Réponds en français naturel, brièvement (1 ou 2 phrases), sans prétendre être l'utilisateur ni inventer des faits. " +
            "Message reçu de " + contact + ": " + incoming + "\nPropose uniquement le texte de réponse, sans guillemets.");
        JSONObject body = new JSONObject().put("contents", new JSONArray().put(new JSONObject().put("parts", new JSONArray().put(part))))
            .put("generationConfig", new JSONObject().put("temperature", 0.7).put("maxOutputTokens", 120));
        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = c.getOutputStream()) { os.write(bytes); }
        int code = c.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream in = is) { byte[] buf = new byte[2048]; int n; while((n=in.read(buf))!=-1) out.write(buf,0,n); }
        String response = new String(out.toByteArray(), StandardCharsets.UTF_8);
        c.disconnect();
        if (code < 200 || code >= 300) return "";
        JSONObject json = new JSONObject(response);
        return json.getJSONArray("candidates").getJSONObject(0)
            .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text").trim();
    }

    private void sendQuickReply(StatusBarNotification sbn, String reply) {
        Notification n = sbn.getNotification();
        if (n.actions == null) return;
        for (Notification.Action action : n.actions) {
            if (action == null || action.getRemoteInputs() == null || action.getRemoteInputs().length == 0) continue;
            try {
                Bundle results = new Bundle();
                for (RemoteInput input : action.getRemoteInputs()) results.putCharSequence(input.getResultKey(), reply);
                IntentFill.fill(this, action.actionIntent, action.getRemoteInputs(), results);
                return;
            } catch (Exception ignored) { }
        }
    }

    private static class IntentFill {
        static void fill(android.content.Context ctx, PendingIntent pending, RemoteInput[] inputs, Bundle results) throws Exception {
            android.content.Intent intent = new android.content.Intent();
            android.content.ClipData clip = android.content.ClipData.newIntent("Steve-20 reply", intent);
            intent.setClipData(clip);
            RemoteInput.addResultsToIntent(inputs, intent, results);
            pending.send(ctx, 0, intent);
        }
    }

    @Override public void onDestroy() {
        pool.shutdownNow();
        super.onDestroy();
    }
}
