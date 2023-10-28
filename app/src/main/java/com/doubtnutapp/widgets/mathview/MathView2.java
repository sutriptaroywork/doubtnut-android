package com.doubtnutapp.widgets.mathview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.File;

public class MathView2 extends WebView {
    private String text;
    private String color = "black";
    private String fontSize = "13px";
    private ProgressBar progressBar;
    private static final String TAG = MathView2.class.getSimpleName();
    private volatile boolean pageLoaded;

    public MathView2(Context var1) {
        super(var1);
        this.init(var1);
    }

    public MathView2(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.init(var1);
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    private void init(Context var1) {
        this.setBackgroundColor(0);
        this.text = "";
        this.pageLoaded = false;
        this.getSettings().setLoadWithOverviewMode(true);
        this.getSettings().setJavaScriptEnabled(true);
        File var2 = var1.getCacheDir();
        if (!var2.exists()) {
            Log.d(TAG, "directory does not exist");
            boolean var3 = var2.mkdirs();
            if (!var3) {
                Log.e(TAG, "directory creation failed");
            }
        }

        this.getSettings().setAppCachePath(var2.getPath());
        this.getSettings().setAppCacheEnabled(true);
        this.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        this.setClickable(false);
        this.setLongClickable(false);
        this.getSettings().setLoadWithOverviewMode(true);
        this.getSettings().setUseWideViewPort(true);
//        setInitialScale(1);
        this.loadUrl("file:///android_asset/www/MathTemplate.html");
        this.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView var1, String var2) {
                MathView2.this.pageLoaded = true;
                MathView2.this.loadUrl("javascript:document.body.style.setProperty(\"color\", \""+ color +"\");");
                MathView2.this.loadUrl("javascript:document.body.style.setProperty(\"font-size\", \""+ fontSize +"\");");
                MathView2.this.loadUrl("javascript:showFormula('" + MathView2.this.text + "')");
                if (MathView2.this.progressBar != null)
                    MathView2.this.progressBar.setVisibility(GONE);
                super.onPageFinished(var1, var2);
            }
        });
    }

    public void setText(String var1) {
        this.text = var1;
        if (this.pageLoaded) {
            this.loadUrl("javascript:showFormula('" + this.text + "')");
        } else {
            Log.e(TAG, "Page is not loaded yet.");
        }
    }

    public void setTextColor(String color) {
        this.color = color;
    }

    public void setFontSize(int size) {
        this.fontSize = String.valueOf(size) + "px";
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public String getText() {
        return this.text.substring(1, this.text.length() - 1);
    }
}