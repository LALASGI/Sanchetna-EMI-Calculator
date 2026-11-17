package com.abviva.loancalculator;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.content.Intent;
import android.net.Uri;
import android.app.DownloadManager;
import android.os.Environment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    private static final String VERSION_URL =
            "https://raw.githubusercontent.com/LALASGI/Sanchetna-EMI-Calculator/main/update.json";

    private WebView webView;
    private LinearLayout statusLayout;
    private TextView statusText;
    private ProgressBar progressBar;
    private Button actionButton;
    private String latestApkUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(16, 16, 16));
        getWindow().setNavigationBarColor(Color.rgb(16, 16, 16));
        checkForUpdate();
    }

    private void showStatus(String message, boolean loading, String buttonText, Runnable action) {
        statusLayout = new LinearLayout(this);
        statusLayout.setOrientation(LinearLayout.VERTICAL);
        statusLayout.setGravity(Gravity.CENTER);
        statusLayout.setPadding(32, 32, 32, 32);
        statusLayout.setBackgroundColor(Color.rgb(244, 241, 235));

        TextView title = new TextView(this);
        title.setText("ABVIVA Loan Calculator");
        title.setTextColor(Color.rgb(35, 31, 25));
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        statusLayout.addView(title);

        if (loading) {
            progressBar = new ProgressBar(this);
            LinearLayout.LayoutParams progressParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            progressParams.topMargin = 28;
            statusLayout.addView(progressBar, progressParams);
        }

        statusText = new TextView(this);
        statusText.setText(message);
        statusText.setTextColor(Color.rgb(75, 68, 58));
        statusText.setTextSize(16);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 22, 0, 22);
        statusLayout.addView(statusText);

        if (buttonText != null && action != null) {
            actionButton = new Button(this);
            actionButton.setText(buttonText);
            actionButton.setOnClickListener(v -> action.run());
            statusLayout.addView(actionButton);
        }

        setContentView(statusLayout);
    }

    private void checkForUpdate() {
        showStatus("नवीनतम अपडेट की जाँच हो रही है…", true, null, null);

        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(VERSION_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(12000);
                connection.setReadTimeout(12000);
                connection.setUseCaches(false);
                connection.setRequestProperty("Cache-Control", "no-cache");
                int response = connection.getResponseCode();
                if (response != HttpURLConnection.HTTP_OK) {
                    throw new Exception("Update server returned HTTP " + response);
                }

                StringBuilder body = new StringBuilder();
                try (InputStream input = connection.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) body.append(line);
                }

                JSONObject data = new JSONObject(body.toString());
                int latestVersion = data.getInt("versionCode");
                String latestName = data.optString("versionName", "नया संस्करण");
                String releaseNotes = data.optString("releaseNotes", "नए संस्करण में सुधार उपलब्ध हैं।");
                latestApkUrl = data.optString("apkUrl", "");

                runOnUiThread(() -> {
                    if (latestVersion > BuildConfig.VERSION_CODE) {
                        showStatus("नया अपडेट उपलब्ध है: " + latestName + "\n\n"
                                        + releaseNotes + "\n\nपुराने संस्करण में ऐप इस्तेमाल नहीं किया जा सकेगा।",
                                false, "DOWNLOAD UPDATE", this::downloadUpdate);
                    } else {
                        openCalculator();
                    }
                });
            } catch (Exception error) {
                runOnUiThread(() -> showStatus(
                        "अपडेट की जाँच नहीं हो सकी। ऐप इस्तेमाल करने से पहले इंटरनेट कनेक्शन जाँचें और फिर कोशिश करें।",
                        false, "RETRY", this::checkForUpdate));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private void downloadUpdate() {
        try {
            if (latestApkUrl == null || !latestApkUrl.startsWith("https://")) {
                throw new IllegalArgumentException("Invalid APK download URL");
            }
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(latestApkUrl));
            request.setTitle("ABVIVA Loan Calculator Update");
            request.setDescription("नया अपडेट डाउनलोड हो रहा है। डाउनलोड पूरा होने पर नोटिफिकेशन खोलें।");
            request.setMimeType("application/vnd.android.package-archive");
            request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, "ABVIVA_Loan_Calculator_Update.apk");

            DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (manager == null) throw new Exception("Download service unavailable");
            manager.enqueue(request);
            showStatus("अपडेट डाउनलोड शुरू हो गया है। डाउनलोड पूरा होने पर फोन के नोटिफिकेशन में इसे खोलें और Install दबाएँ।",
                    false, "CHECK AGAIN", this::checkForUpdate);
        } catch (Exception error) {
            showStatus("अपडेट डाउनलोड शुरू नहीं हो सका। इंटरनेट कनेक्शन जाँचकर फिर कोशिश करें।",
                    false, "RETRY", this::downloadUpdate);
        }
    }

    private void openCalculator() {
        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(244, 241, 235));
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setSupportZoom(false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && url.startsWith("mailto:")) {
                    try { startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url))); }
                    catch (Exception ignored) { }
                    return true;
                }
                if (url != null && url.startsWith("file:///android_asset/")) return false;
                return true;
            }
        });
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
