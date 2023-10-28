/*
package com.doubtnutapp.textsolution;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.doubtnutapp.data.remote.models.MockTestQuestionDataOptions;
import com.doubtnutapp.data.remote.models.QuestionwiseResult;
import com.doubtnutapp.textsolution.model.ResourceData;
import com.doubtnutapp.textsolution.model.TextAnswerData;
import com.doubtnutapp.ui.mockTest.MockTestQuestionFragment;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class TextSoiutionQuestionWithOptionView extends WebView {
    private String question;
    private ArrayList<String>  radioOptionList;


    private ProgressBar progressBar;
    private static final String TAG = TextSoiutionQuestionWithOptionView.class.getSimpleName();
    private volatile boolean pageLoaded;
    private String fontSize = "13px";
    private String color = "black";

    private AppCompatActivity javaInterfaceForAppCompatActivity;
    private String jsonStr, jsonResStr;


    public List<ResourceData.OptionsList> optionList;

    public TextSoiutionQuestionWithOptionView(Context var1) {
        super(var1);
        this.init(var1);
    }

    public TextSoiutionQuestionWithOptionView(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.init(var1);
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void init(Context var1) {
        this.setBackgroundColor(0);
        this.optionList = null;
        this.question = "";
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

        this.loadUrl("file:///android_asset/www/TextQuestionForOptions.html");
        this.getSettings().setJavaScriptEnabled(true);

        //for inspecting the webview in chrome tab
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }


        this.setWebViewClient(new WebViewClient() {
            @SuppressLint("JavascriptInterface")
            public void onPageFinished(WebView var1, String var2) {
                TextSoiutionQuestionWithOptionView.this.pageLoaded = true;

                Gson gson = new Gson();
                try {
                    // get Employee object as a json string
                    TextSoiutionQuestionWithOptionView.this.loadUrl("javascript:document.body.style.setProperty(\"color\", \"" + color + "\");");
                    TextSoiutionQuestionWithOptionView.this.loadUrl("javascript:document.body.style.setProperty(\"font-size\", \"" + fontSize + "\");");
                    if(optionList!=null) {
                        jsonStr = gson.toJson(optionList);
                        TextSoiutionQuestionWithOptionView.this.loadUrl("javascript:showQuestionData('" + jsonStr +"')");
                    }
                    TextSoiutionQuestionWithOptionView.this.loadUrl("javascript:showQuestion('" + TextSoiutionQuestionWithOptionView.this.question + "')");

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                if (TextSoiutionQuestionWithOptionView.this.progressBar != null)
                    TextSoiutionQuestionWithOptionView.this.progressBar.setVisibility(GONE);
                super.onPageFinished(var1, var2);
            }
        });
    }


    @NotNull
    public List<ResourceData.OptionsList> getQuestionData() {
        return optionList;
    }

    public void setQuestionData(@NotNull List<ResourceData.OptionsList> optionList) {
        this.optionList = optionList;
    }



    // Question
    public void setQuestion(String var1) {
        this.question = var1;
    }

    public String getQuestion() {
        return this.question.substring(1, this.question.length() - 1);
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


 */