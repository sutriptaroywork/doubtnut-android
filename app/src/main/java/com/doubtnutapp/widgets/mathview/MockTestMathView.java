package com.doubtnutapp.widgets.mathview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.doubtnutapp.data.remote.models.MockTestQuestionDataOptions;
import com.doubtnutapp.data.remote.models.QuestionwiseResult;
import com.doubtnutapp.ui.mockTest.MockTestQuestionFragment;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;


public class MockTestMathView extends WebView {
    private String questionNumber, question, questionType, imageLink;
    private String submitChangeName, skipChangeName, colorBtn, optionReportCorrectActiveQuiz, colorReportCorrectActiveQuiz;

    private String inputTextFeild;

    private ArrayList<String> checkBoxOptionList, radioOptionList;


    private Boolean isReport;
    private Boolean isImage;

    private ProgressBar progressBar;
    private static final String TAG = MockTestMathView.class.getSimpleName();
    private volatile boolean pageLoaded;
    private String fontSize = "13px";
    private String color = "black";

    private MockTestQuestionFragment javaInterfaceForMockTest;
    private String jsonStr, jsonResStr;


    public MockTestQuestionDataOptions questionData;
    public QuestionwiseResult resultQuestionData;

    public MockTestMathView(Context var1) {
        super(var1);
        this.init(var1);
    }

    public MockTestMathView(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.init(var1);
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void init(Context var1) {
        this.setBackgroundColor(0);
        this.questionData = null;
        this.resultQuestionData = null;
        this.checkBoxOptionList = null;
        this.radioOptionList = null;
        this.questionNumber = "";
        this.question = "";
        this.questionType = "";

        this.imageLink = "";
        this.submitChangeName = "";
        this.skipChangeName = "";
        this.inputTextFeild = "";
        this.colorBtn = "";
        this.colorReportCorrectActiveQuiz = "";
        this.optionReportCorrectActiveQuiz = "";

        this.pageLoaded = false;

        this.pageLoaded = false;

        this.isImage = false;
        this.isReport = false;
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

        this.loadUrl("file:///android_asset/www/MockTest.html");
        this.getSettings().setJavaScriptEnabled(true);

        //for inspecting the webview in chrome tab
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }


        this.setWebViewClient(new WebViewClient() {
            @SuppressLint("JavascriptInterface")
            public void onPageFinished(WebView var1, String var2) {
                MockTestMathView.this.pageLoaded = true;

                Gson gson = new Gson();
                try {
                    // get Employee object as a json string
                    if(questionData!=null) {
                        jsonStr = gson.toJson(questionData.getOptions());
                    }
                    System.out.println(jsonStr);

                    if(resultQuestionData!=null) {
                        jsonResStr = gson.toJson(resultQuestionData.getOptions());
                    }
                    System.out.println(jsonResStr);


                    MockTestMathView.this.loadUrl("javascript:document.body.style.setProperty(\"color\", \"" + color + "\");");
                    MockTestMathView.this.loadUrl("javascript:document.body.style.setProperty(\"font-size\", \"" + fontSize + "\");");
                    if (isReport) {
                        MockTestMathView.this.loadUrl("javascript:showReportQuestionData('" + jsonResStr + "' , \"" + resultQuestionData.getType() + "\", \"" + MockTestMathView.this.radioOptionList + "\", \"" + MockTestMathView.this.inputTextFeild + "\", \"" + MockTestMathView.this.checkBoxOptionList + "\" );");
                    } else {
                        MockTestMathView.this.loadUrl("javascript:showQuestionData('" + jsonStr+ "' , \"" + questionData.getType() + "\", \"" + MockTestMathView.this.radioOptionList + "\", \"" + MockTestMathView.this.inputTextFeild + "\", \"" + MockTestMathView.this.checkBoxOptionList + "\" );");
                    }

                    MockTestMathView.this.loadUrl("javascript:showQuestionNumber('" + MockTestMathView.this.questionNumber + "')");
                    MockTestMathView.this.loadUrl("javascript:showQuestion('" + MockTestMathView.this.question + "')");
                    MockTestMathView.this.loadUrl("javascript:showQuestionType('" + MockTestMathView.this.questionType + "')");

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                if (MockTestMathView.this.progressBar != null)
                    MockTestMathView.this.progressBar.setVisibility(GONE);
                super.onPageFinished(var1, var2);
            }
        });
    }

    public void reload(){
        this.loadUrl("file:///android_asset/www/MockTest.html");
    }
    // Question Number
    public void setQuestionNumber(String var1) {
        this.questionNumber = var1;
    }

    public String getQuestionNumber() {
        return this.questionNumber.substring(1, this.questionNumber.length() - 1);
    }


    @NotNull
    public MockTestQuestionDataOptions getQuestionData() {
        return questionData;
    }

    public void setQuestionData(@NotNull MockTestQuestionDataOptions questionData) {
        this.questionData = questionData;
    }

    @NotNull
    public QuestionwiseResult getResultQuestionData() {
        return resultQuestionData;
    }

    public void setResultQuestionData(@NotNull QuestionwiseResult resultQuestionData) {
        this.resultQuestionData = resultQuestionData;
    }


    // Question
    public void setQuestion(String var1) {
        this.question = var1;
    }

    public String getQuestion() {
        return this.question.substring(1, this.question.length() - 1);
    }


    // Question Type
    public void setQuestionType(String var1) {
        this.questionType = var1;
    }

    public String getQuestionType() {
        return this.questionType.substring(1, this.questionType.length() - 1);
    }


    public void setIsImage() {
        this.isImage = true;
    }




    // submit change name
    public void setSubmitChangeName(String var1) {
        this.submitChangeName = var1;
    }

    public String getSubmitChangeName() {
        return this.submitChangeName.substring(1, this.submitChangeName.length() - 1);
    }

    // submit change bg color
    public void setSubmitChangeColor(String colorBtn) {
        this.colorBtn = colorBtn;
    }

    public String getSubmitChangeColor() {
        return colorBtn;
    }





    public void setMatrixCheckedOptions(ArrayList<String> checkBoxOptionList) {
        this.checkBoxOptionList = checkBoxOptionList;
    }

    public void setRadioCheckedOptions(ArrayList<String> radioOptionList) {
        this.radioOptionList = radioOptionList;
    }

    public void setInputTextFeild(String inputTextFeild) {
        this.inputTextFeild = inputTextFeild;
    }



    // skip change name
    public void setSkipChangeName(String var1) {
        this.skipChangeName = var1;
    }

    public String getSkipChangeName() {
        return this.skipChangeName.substring(1, this.skipChangeName.length() - 1);
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
        if (this.pageLoaded) {
            MockTestMathView.this.loadUrl("javascript:showImage('" + MockTestMathView.this.imageLink + "')");
        }
    }

    public String getImageLink() {
        return imageLink;
    }



    public void setIsReport() {
        this.isReport = true;
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


    public void setJavaInterfaceForMockTest(MockTestQuestionFragment javaInterfaceForMockTest) {
        addJavascriptInterface(javaInterfaceForMockTest, "Android");
    }


    public MockTestQuestionFragment getJavaInterfaceForMockTest() {
        return javaInterfaceForMockTest;
    }

}

