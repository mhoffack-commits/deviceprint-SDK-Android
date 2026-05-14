package com.iovation.mobile.android.sample.sampleapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.iovation.mobile.android.FraudForceManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by trevorchapman on 4/13/15.
 */
public class WebViewActivity extends Activity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);
        final WebView wv = (WebView) findViewById(R.id.webView);
        wv.getSettings().setJavaScriptEnabled(true);
        String url = "file:///android_asset/JsInjectionIntegration.html";
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                FraudForceManager.INSTANCE.refresh(wv.getContext());
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                String[] ref = url.split("#");
                if (url.startsWith("iov://") && ref.length > 1 && ref[1] != null) {
                    String elementId = ref[1].replaceAll("[^a-zA-Z0-9_\\-]", "");
                    executor.execute(() -> {
                        String bb = FraudForceManager.INSTANCE.getBlackbox(getApplicationContext());
                        String escapedBb = bb.replace("\\", "\\\\").replace("'", "\\'");
                        String js = "javascript:(function() { " +
                                "document.getElementById('" + elementId + "').value = '" + escapedBb +
                                "';})()";
                        mainHandler.post(() -> wv.loadUrl(js));
                    });
                    return true;
                }
                return false;
            }
        });

        wv.loadUrl(url);
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
