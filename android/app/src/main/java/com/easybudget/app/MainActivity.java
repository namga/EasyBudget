package com.easybudget.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.content.pm.PackageManager;
import android.view.View;
import android.graphics.Color;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient.FileChooserParams;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {
    private WebView web;
    private WebViewAssetLoader assetLoader;
    private static final int REQUEST_CREATE_BACKUP = 4101;
    private static final int REQUEST_OPEN_BACKUP = 4102;
    private static final int REQUEST_CAPTURE_IMAGE = 4104;
    private String pendingBackupJson = null;
    private ValueCallback<Uri[]> pendingFileChooser;
    private Uri pendingCameraUri;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(237,245,236));
        getWindow().setNavigationBarColor(Color.rgb(237,245,236));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        assetLoader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
            .build();
        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(237,245,236));
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);
        web.addJavascriptInterface(new ReminderBridge(this), "EasyBudgetNative");
        web.addJavascriptInterface(new BackupBridge(), "EasyBudgetBackup");
        web.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return assetLoader.shouldInterceptRequest(Uri.parse(url));
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (pendingFileChooser != null) pendingFileChooser.onReceiveValue(null);
                pendingFileChooser = filePathCallback;
                boolean wantsImage = false;
                String[] types = fileChooserParams.getAcceptTypes();
                if (types != null) for (String type : types) if (type != null && (type.contains("image/") || type.equals("image/*"))) wantsImage = true;
                // HTML input capture="environment" is not consistently honored by Android WebView.
                // Explicitly launch the native camera when capture is requested for an image.
                if (fileChooserParams.isCaptureEnabled() && wantsImage) {
                    Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (camera.resolveActivity(getPackageManager()) != null) {
                        try {
                            File dir = new File(getCacheDir(), "camera");
                            if (!dir.exists() && !dir.mkdirs()) throw new java.io.IOException("Cannot create camera cache");
                            File image = File.createTempFile("easybudget_receipt_", ".jpg", dir);
                            pendingCameraUri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".fileprovider", image);
                            camera.putExtra(MediaStore.EXTRA_OUTPUT, pendingCameraUri);
                            camera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            startActivityForResult(camera, REQUEST_CAPTURE_IMAGE);
                            return true;
                        } catch (Exception e) {
                            pendingCameraUri = null;
                            // Fall through to the system picker if camera launch fails.
                        }
                    }
                }
                try {
                    Intent intent = fileChooserParams.createIntent();
                    startActivityForResult(intent, 4103);
                } catch (Exception e) {
                    pendingFileChooser = null;
                    filePathCallback.onReceiveValue(null);
                    return false;
                }
                return true;
            }
        });
        setContentView(web);
        ReminderReceiver.createChannel(this);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 71);
        }
        web.loadUrl("https://appassets.androidplatform.net/assets/web/index.html");
    }

    public class BackupBridge {
        @JavascriptInterface public void saveBackup(String json, String suggestedName) {
            runOnUiThread(() -> {
                pendingBackupJson = json;
                Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("application/json");
                i.putExtra(Intent.EXTRA_TITLE, suggestedName == null ? "easybudget-backup.json" : suggestedName);
                startActivityForResult(i, REQUEST_CREATE_BACKUP);
            });
        }
        @JavascriptInterface public void chooseBackup() {
            runOnUiThread(() -> {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/json", "text/json", "text/plain"});
                startActivityForResult(i, REQUEST_OPEN_BACKUP);
            });
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == REQUEST_CAPTURE_IMAGE) {
            if (pendingFileChooser != null) {
                Uri[] results = resultCode == RESULT_OK && pendingCameraUri != null ? new Uri[]{pendingCameraUri} : null;
                pendingFileChooser.onReceiveValue(results);
                pendingFileChooser = null;
            }
            pendingCameraUri = null;
            return;
        }
        if (requestCode == 4103) {
            if (pendingFileChooser != null) {
                Uri[] results = null;
                if (resultCode == RESULT_OK && result != null) {
                    if (result.getData() != null) results = new Uri[]{result.getData()};
                    else if (Build.VERSION.SDK_INT >= 21 && result.getClipData() != null) {
                        int count = result.getClipData().getItemCount();
                        results = new Uri[count];
                        for (int i = 0; i < count; i++) results[i] = result.getClipData().getItemAt(i).getUri();
                    }
                }
                pendingFileChooser.onReceiveValue(results);
                pendingFileChooser = null;
            }
            return;
        }
        if (requestCode != REQUEST_CREATE_BACKUP && requestCode != REQUEST_OPEN_BACKUP) return;
        if (resultCode != RESULT_OK || result == null || result.getData() == null) {
            pendingBackupJson = null;
            return;
        }
        Uri uri = result.getData();
        if (requestCode == REQUEST_CREATE_BACKUP) {
            try {
                OutputStream out = getContentResolver().openOutputStream(uri, "wt");
                if (out == null) throw new Exception("Unable to open backup destination");
                out.write((pendingBackupJson == null ? "" : pendingBackupJson).getBytes(StandardCharsets.UTF_8));
                out.flush(); out.close();
                pendingBackupJson = null;
                web.evaluateJavascript("window.onNativeBackupSaved && window.onNativeBackupSaved(true)", null);
            } catch (Exception e) {
                pendingBackupJson = null;
                web.evaluateJavascript("window.onNativeBackupSaved && window.onNativeBackupSaved(false)", null);
            }
        } else {
            try {
                InputStream in = getContentResolver().openInputStream(uri);
                if (in == null) throw new Exception("Unable to read backup");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[8192]; int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                in.close();
                String content = new String(out.toByteArray(), StandardCharsets.UTF_8);
                web.evaluateJavascript("window.onNativeBackupChosen && window.onNativeBackupChosen(" + org.json.JSONObject.quote(content) + ")", null);
            } catch (Exception e) {
                web.evaluateJavascript("window.onNativeBackupChosen && window.onNativeBackupChosen(null)", null);
            }
        }
    }

    @Override public void onBackPressed() {
        if (web != null) {
            web.evaluateJavascript("window.handleAppBackFromNative && window.handleAppBackFromNative()", null);
        } else {
            super.onBackPressed();
        }
    }
}
