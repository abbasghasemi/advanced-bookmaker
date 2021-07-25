/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.text.Bidi;

import androidx.annotation.Nullable;
import ghasemi.abbas.book.components.LineProgressBar;
import ghasemi.abbas.book.components.TextView;
import ghasemi.abbas.book.general.AndroidUtilities;

public class WebViewActivity extends BaseActivity {
    private LineProgressBar lineProgressBar;
    private WebView webView;
    private TextView title;
    private boolean showProgress = true;
    private String webUrl;
    private Bitmap icon;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        AndroidUtilities.setWindowFullScreen(this);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(webUrl));
                try {
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    //
                }
            }
        });
        lineProgressBar = findViewById(R.id.progressBarToolbar);
        title = findViewById(R.id.title);
        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                showProgress = false;
                if (TextUtils.isEmpty(view.getTitle())) {
                    title.setText(getIntent().getStringExtra("web_title"));
                } else {
                    title.setText(view.getTitle());
                }
                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                showProgress = true;

                if (TextUtils.isEmpty(view.getTitle())) {
                    title.setText(getIntent().getStringExtra("web_title"));
                } else {
                    title.setText(view.getTitle());
                }
                createIcon(icon);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("https://") || url.startsWith("http://")) {
                    showProgress = true;
                    view.loadUrl(url);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        //
                    }
                }
                return false;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (!showProgress) {
                    return;
                }
                lineProgressBar.setProgress(newProgress);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
                createIcon(icon);
            }
        });
        title.setText(getIntent().getStringExtra("web_title"));
        webUrl = getIntent().getStringExtra("web_url");
        if (webUrl != null) {
            if (!webUrl.startsWith("http://") && !webUrl.startsWith("https://")) {
                webUrl = "http://" + webUrl;
            }
        }
        webView.loadUrl(webUrl);
    }

    private void createIcon(Bitmap _icon) {
        icon = _icon;
        if (icon != null) {
            int width = AndroidUtilities.dp(16);
            if (icon.getHeight() != width || icon.getWidth() != width) {
                icon = Bitmap.createScaledBitmap(icon, width, width, false);
            }
            Bidi bidi = new Bidi(title.getText().toString(), Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT);
            if (bidi.getBaseLevel() == 1) {
                title.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, new BitmapDrawable(getResources(), icon), null);
            } else {
                title.setCompoundDrawablesRelativeWithIntrinsicBounds(new BitmapDrawable(getResources(), icon), null, null, null);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView == null || !webView.canGoBack()) {
            finish();
        } else {
            webView.goBack();
        }
    }

}
