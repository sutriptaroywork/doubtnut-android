package com.doubtnutapp.widgets.mathview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.File;

public class ASCIIMathView extends WebView {
    private String text;
    private boolean isTextSet = false;
    private String color = "white";
    private String fontSize = "13px";
    private String textAlign = "left";
    private ProgressBar progressBar;
    private static final String TAG = "ASCIIMathView";
    private volatile boolean pageLoaded;
    private OnMathViewClickListener onMathViewClickListener;
    private OnMathViewRenderListener onMathViewRenderListener;

    private boolean isLoadingFinished = false;

    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (onMathViewClickListener != null) {
                onMathViewClickListener.onClick();
            }
            return super.onSingleTapUp(e);
        }
    };

    private GestureDetector gestureDetector;

    public ASCIIMathView(Context var1) {
        super(var1);
        this.init(var1);
    }

    public ASCIIMathView(Context var1, AttributeSet var2) {
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
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
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
        this.getSettings().setLoadWithOverviewMode(true);
        this.getSettings().setUseWideViewPort(true);
//        setInitialScale(1);
        this.loadUrl("file:///android_asset/www/MathTemplate.html");
        this.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (onMathViewRenderListener != null) {
                    onMathViewRenderListener.onRenderStarted();
                }
                super.onPageStarted(view, url, favicon);
            }

            public void onPageFinished(WebView var1, String var2) {
                ASCIIMathView.this.pageLoaded = true;

                if (!isTextSet)
                   loadText();

                if (ASCIIMathView.this.progressBar != null)
                    ASCIIMathView.this.progressBar.setVisibility(GONE);

                if (onMathViewRenderListener != null) {
                    onMathViewRenderListener.onRenderEnd();
                }

//                if (!isTextSet && text != null) {
//                    setText(text);
//                }
                super.onPageFinished(var1, var2);
            }
        });
        gestureDetector = new GestureDetector(var1, gestureListener);
    }

    public void setOnMathViewClickListener(OnMathViewClickListener onMathViewClickListener) {
        this.onMathViewClickListener = onMathViewClickListener;
    }

    public void setOnMathViewRenderStartedListener(OnMathViewRenderListener onMathViewRenderListener) {
        this.onMathViewRenderListener = onMathViewRenderListener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        gestureDetector.onTouchEvent(event);
        return false;
    }

    public void setText(String var1) {
        this.text = var1;
        if (this.pageLoaded) {
            isTextSet = true;
            loadText();
        } else {
            Log.e(TAG, "Page is not loaded yet.");
        }
    }

    private void loadText() {
        ASCIIMathView.this.loadUrl("" +
                "javascript:" +
                "document.body.style.setProperty(\"color\", \"" + color + "\");" +
                "document.body.style.setProperty(\"font-size\", \"" + fontSize + "\");" +
                "document.body.style.setProperty(\"text-align\", \"" + textAlign + "\");" +
                "showFormula('" + ASCIIMathView.this.text + "')");
    }

    public void setTextAlign(String textAlign) {
        this.textAlign = textAlign;
    }

    public void setTextColor(String color) {
        this.color = color;
    }

    public void setFontSize(String size) {
        this.fontSize = size;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public String getText() {
        return this.text.substring(1, this.text.length() - 1);
    }
}
