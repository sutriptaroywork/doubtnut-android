package com.doubtnutapp.widgets.mathview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;


public class TextSolutionMathView extends WebView {
    private String question;
    private ProgressBar progressBar;
    private static final String TAG = TextSolutionMathView.class.getSimpleName();
    private volatile boolean pageLoaded;
    private String fontSize = "13px";
    private String color = "black";

    private AppCompatActivity javaInterfaceForAppCompatActivity;


    public TextSolutionMathView(Context var1) {
        super(var1);
        this.init(var1);
    }

    public TextSolutionMathView(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.init(var1);
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void init(Context var1) {
        this.setBackgroundColor(0);


        this.pageLoaded = false;
        this.getSettings().setLoadWithOverviewMode(true);
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

        this.loadUrl("file:///android_asset/www/TextSolution.html");
        this.getSettings().setJavaScriptEnabled(true);

        //for inspecting the webview in chrome tab
        WebView.setWebContentsDebuggingEnabled(true);

        this.setWebViewClient(new WebViewClient() {
            @SuppressLint("JavascriptInterface")
            public void onPageFinished(WebView var1, String var2) {
                TextSolutionMathView.this.pageLoaded = true;
                try {
                    if (question != null) {
                        loadTextSolutionUrl(question);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (TextSolutionMathView.this.progressBar != null)
                    TextSolutionMathView.this.progressBar.setVisibility(GONE);
                super.onPageFinished(var1, var2);
            }
        });
    }

    private void loadTextSolutionUrl(String questionText) {
        TextSolutionMathView.this.clearCache(true);
        TextSolutionMathView.this.clearHistory();
        TextSolutionMathView.this.loadUrl("javascript:document.body.style.setProperty(\"color\", \"" + color + "\");");
        TextSolutionMathView.this.loadUrl("javascript:document.body.style.setProperty(\"font-size\", \"" + fontSize + "\");");
        TextSolutionMathView.this.loadUrl("javascript:showQuestion('" + questionText + "')");
    }


    // Question
    public void setQuestion(String var1) {
        this.question = var1;
        if (pageLoaded) {
            loadTextSolutionUrl(question);
        }
    }

    public String getQuestion() {
        return this.question;
    }


    // text color
    public void setTextColor(String color) {
        this.color = color;
    }

    // font size
    public void setFontSize(int size) {
        this.fontSize = String.valueOf(size) + "px";
    }

    // progress bar
    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }


    public void setJavaInterfaceForAppCompatActivity(AppCompatActivity javaInterfaceForAppCompatActivity) {
        addJavascriptInterface(javaInterfaceForAppCompatActivity, "Android");
    }


    public AppCompatActivity getJavaInterfaceForAppCompatActivity() {
        return javaInterfaceForAppCompatActivity;
    }

}

