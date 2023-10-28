package com.doubtnutapp.widgets.mathview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.doubtnutapp.ui.test.TestQuestionFragment;

import java.io.File;

/**
 * Created by akshaynandwana on
 * 24, January, 2019
 **/
public class NewMathViewTest extends WebView {
    private String questionNumber, question, questionType, option1, option2, option3, option4, imageLink;
    private String submitChangeName, skipChangeName, colorBtn, optionReportCorrectActiveQuiz, colorReportCorrectActiveQuiz;

    private Boolean tickOne, tickTwo, tickThree, tickFour;
    private Boolean wrongOne, wrongTwo, wrongThree, wrongFour;

    private Boolean option1ReportCorrectBool, option2ReportCorrectBool, option3ReportCorrectBool, option4ReportCorrectBool;

    private Boolean oneOptionGreenCorrectActiveQuizBool, twoOptionGreenCorrectActiveQuizBool,
            threeOptionGreenCorrectActiveQuizBool, fourOptionGreenCorrectActiveQuizBool;
    private Boolean oneOptionBlueCorrectActiveQuizBool, twoOptionBlueCorrectActiveQuizBool,
            threeOptionBlueCorrectActiveQuizBool, fourOptionBlueCorrectActiveQuizBool;

    private Boolean option1ReportBool, option2ReportBool, option3ReportBool, option4ReportBool;
    private Boolean option1ReportDefRightBool, option2ReportDefRightBool, option3ReportDefRightBool, option4ReportDefRightBool;
    private Boolean isReport;
    private Boolean isImage;

    private TestQuestionFragment javaInterfaceForTest;
    private ProgressBar progressBar;
    private static final String TAG = NewMathViewTest.class.getSimpleName();
    private volatile boolean pageLoaded;
    private String fontSize = "13px";
    private String color = "black";


    public NewMathViewTest(Context var1) {
        super(var1);
        this.init(var1);
    }

    public NewMathViewTest(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.init(var1);
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void init(Context var1) {
        this.setBackgroundColor(0);
        this.questionNumber = "";
        this.question = "";
        this.questionType = "";
        this.option1 = "";
        this.option2 = "";
        this.option3 = "";
        this.option4 = "";
        this.imageLink = "";
        this.submitChangeName = "";
        this.skipChangeName = "";
        this.colorBtn = "";
        this.colorReportCorrectActiveQuiz = "";
        this.optionReportCorrectActiveQuiz = "";
        this.tickFour = false;
        this.tickOne = false;
        this.tickThree = false;
        this.tickTwo = false;
        this.pageLoaded = false;
        this.wrongFour = false;
        this.wrongOne = false;
        this.wrongThree = false;
        this.wrongTwo = false;
        this.pageLoaded = false;
        this.option1ReportCorrectBool = false;
        this.option2ReportCorrectBool = false;
        this.option3ReportCorrectBool = false;
        this.option4ReportCorrectBool = false;
        this.option1ReportDefRightBool = false;
        this.option2ReportDefRightBool = false;
        this.option3ReportDefRightBool = false;
        this.option4ReportDefRightBool = false;
        this.oneOptionBlueCorrectActiveQuizBool = false;
        this.twoOptionBlueCorrectActiveQuizBool = false;
        this.threeOptionBlueCorrectActiveQuizBool = false;
        this.fourOptionBlueCorrectActiveQuizBool = false;
        this.oneOptionGreenCorrectActiveQuizBool = false;
        this.twoOptionGreenCorrectActiveQuizBool = false;
        this.threeOptionGreenCorrectActiveQuizBool = false;
        this.fourOptionGreenCorrectActiveQuizBool = false;
        this.option1ReportBool = false;
        this.option2ReportBool = false;
        this.option3ReportBool = false;
        this.option4ReportBool = false;
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

        this.loadUrl("file:///android_asset/www/index.html");
        this.getSettings().setJavaScriptEnabled(true);


        this.setWebViewClient(new WebViewClient() {
            @SuppressLint("JavascriptInterface")
            public void onPageFinished(WebView var1, String var2) {
                NewMathViewTest.this.pageLoaded = true;

                NewMathViewTest.this.loadUrl("javascript:document.body.style.setProperty(\"color\", \"" + color + "\");");
                NewMathViewTest.this.loadUrl("javascript:document.body.style.setProperty(\"font-size\", \"" + fontSize + "\");");
                NewMathViewTest.this.loadUrl("javascript:showQuestionNumber('" + NewMathViewTest.this.questionNumber + "')");
                NewMathViewTest.this.loadUrl("javascript:showQuestion('" + NewMathViewTest.this.question + "')");
                NewMathViewTest.this.loadUrl("javascript:showQuestionType('" + NewMathViewTest.this.questionType + "')");
                NewMathViewTest.this.loadUrl("javascript:showOption1('" + NewMathViewTest.this.option1 + "')");
                NewMathViewTest.this.loadUrl("javascript:showOption2('" + NewMathViewTest.this.option2 + "')");
                NewMathViewTest.this.loadUrl("javascript:showOption3('" + NewMathViewTest.this.option3 + "')");
                NewMathViewTest.this.loadUrl("javascript:showOption4('" + NewMathViewTest.this.option4 + "')");
                NewMathViewTest.this.loadUrl("javascript:showSubmitChangeName('" + NewMathViewTest.this.submitChangeName + "')");
                NewMathViewTest.this.loadUrl("javascript:showSkipChangeName('" + NewMathViewTest.this.skipChangeName + "')");
                NewMathViewTest.this.loadUrl("javascript:submitChangeBgColor('" + NewMathViewTest.this.colorBtn + "')");

                if (isImage) {
                    Log.d("img", NewMathViewTest.this.imageLink);
                    NewMathViewTest.this.loadUrl("javascript:showImage('" + NewMathViewTest.this.imageLink + "')");
                } else {
                    NewMathViewTest.this.loadUrl("javascript:hideImage('" + NewMathViewTest.this.imageLink + "')");
                }

                if (oneOptionGreenCorrectActiveQuizBool) {
                    NewMathViewTest.this.loadUrl("javascript:option1ChangeBgColorGreen()");
                }
                if (twoOptionGreenCorrectActiveQuizBool) {
                    NewMathViewTest.this.loadUrl("javascript:option2ChangeBgColorGreen()");
                }
                if (threeOptionGreenCorrectActiveQuizBool) {
                    NewMathViewTest.this.loadUrl("javascript:option3ChangeBgColorGreen()");
                }
                if (fourOptionGreenCorrectActiveQuizBool) {
                    NewMathViewTest.this.loadUrl("javascript:option4ChangeBgColorGreen()");
                }

                if (oneOptionBlueCorrectActiveQuizBool) {
                    NewMathViewTest.this.loadUrl("javascript:option1ChangeBgColorBlue()");
                }
                if (twoOptionBlueCorrectActiveQuizBool) {
                    NewMathViewTest.this.loadUrl("javascript:option2ChangeBgColorBlue()");
                }
                if (threeOptionBlueCorrectActiveQuizBool) {
                    NewMathViewTest.this.loadUrl("javascript:option3ChangeBgColorBlue()");
                }
                if (fourOptionBlueCorrectActiveQuizBool) {
                    NewMathViewTest.this.loadUrl("javascript:option4ChangeBgColorBlue()");
                }

                if (tickOne) {
                    NewMathViewTest.this.loadUrl("javascript:showTickOne()");
                }
                if (tickTwo) {
                    NewMathViewTest.this.loadUrl("javascript:showTickTwo()");
                }
                if (tickThree) {
                    NewMathViewTest.this.loadUrl("javascript:showTickThree()");
                }
                if (tickFour) {
                    NewMathViewTest.this.loadUrl("javascript:showTickFour()");
                }


                if (wrongOne) {
                    NewMathViewTest.this.loadUrl("javascript:showWrongOne()");
                }
                if (wrongTwo) {
                    NewMathViewTest.this.loadUrl("javascript:showWrongTwo()");
                }
                if (wrongThree) {
                    NewMathViewTest.this.loadUrl("javascript:showWrongThree()");
                }
                if (wrongFour) {
                    NewMathViewTest.this.loadUrl("javascript:showWrongFour()");
                }


                if (option1ReportCorrectBool) {
                    NewMathViewTest.this.loadUrl("javascript:option1ChangeBgColorGreen()");
                }
                if (option2ReportCorrectBool) {
                    NewMathViewTest.this.loadUrl("javascript:option2ChangeBgColorGreen()");
                }
                if (option3ReportCorrectBool) {
                    NewMathViewTest.this.loadUrl("javascript:option3ChangeBgColorGreen()");
                }
                if (option4ReportCorrectBool) {
                    NewMathViewTest.this.loadUrl("javascript:option4ChangeBgColorGreen()");
                }


                if (option1ReportDefRightBool) {
                    NewMathViewTest.this.loadUrl("javascript:option1ChangeBgColorGreen()");
                }
                if (option2ReportDefRightBool) {
                    NewMathViewTest.this.loadUrl("javascript:option2ChangeBgColorGreen()");
                }
                if (option3ReportDefRightBool) {
                    NewMathViewTest.this.loadUrl("javascript:option3ChangeBgColorGreen()");
                }
                if (option4ReportDefRightBool) {
                    NewMathViewTest.this.loadUrl("javascript:option4ChangeBgColorGreen()");
                }


                if (option1ReportBool) {
                    NewMathViewTest.this.loadUrl("javascript:option1ChangeBgColorRed()");
                }
                if (option2ReportBool) {
                    NewMathViewTest.this.loadUrl("javascript:option2ChangeBgColorRed()");
                }
                if (option3ReportBool) {
                    NewMathViewTest.this.loadUrl("javascript:option3ChangeBgColorRed()");
                }
                if (option4ReportBool) {
                    NewMathViewTest.this.loadUrl("javascript:option4ChangeBgColorRed()");
                }

                if (NewMathViewTest.this.progressBar != null)
                    NewMathViewTest.this.progressBar.setVisibility(GONE);
                super.onPageFinished(var1, var2);
            }
        });
    }

    // Question Number
    public void setQuestionNumber(String var1) {
        this.questionNumber = var1;
    }

    public String getQuestionNumber() {
        return this.questionNumber.substring(1, this.questionNumber.length() - 1);
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
    public void setOption1(String var1) {
        this.option1 = var1;
    }

    public String getOption1() {
        return this.option1.substring(1, this.option1.length() - 1);
    }

    // option 2
    public void setOption2(String var1) {
        this.option2 = var1;
    }

    public void setIsImage() {
        this.isImage = true;
    }

    // Option 1

    public String getOption2() {
        return this.option2.substring(1, this.option2.length() - 1);
    }

    //option 3
    public void setOption3(String var1) {
        this.option3 = var1;
    }

    public String getOption3() {
        return this.option3.substring(1, this.option3.length() - 1);
    }

    // option 4
    public void setOption4(String var1) {
        this.option4 = var1;
    }

    public String getOption4() {
        return this.option4.substring(1, this.option4.length() - 1);
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

    // option change bg color
    public void setOption1ChangeColor() {
        this.option1ReportBool = true;
    }

    // option change bg color
    public void setOption2ChangeColor() {
        this.option2ReportBool = true;
    }

    // option change bg color
    public void setOption3ChangeColor() {
        this.option3ReportBool = true;
    }

    // option change bg color
    public void setOption4ChangeColor() {
        this.option4ReportBool = true;
    }

    public void setOption1ChangeColorCorrect() {
        this.option1ReportCorrectBool = true;
    }

    public void setOption2ChangeColorCorrect() {
        this.option2ReportCorrectBool = true;
    }

    public void setOption3ChangeColorCorrect() {
        this.option3ReportCorrectBool = true;
    }

    public void setOption4ChangeColorCorrect() {
        this.option4ReportCorrectBool = true;
    }

    public void setOneOptionGreenCorrectActiveQuiz() {
        this.oneOptionGreenCorrectActiveQuizBool = true;
        if (pageLoaded) {
            NewMathViewTest.this.loadUrl("javascript:option1ChangeBgColorGreen()");
        }
    }

    public void setTwoOptionGreenCorrectActiveQuiz() {
        this.twoOptionGreenCorrectActiveQuizBool = true;
        if (pageLoaded) {
            NewMathViewTest.this.loadUrl("javascript:option2ChangeBgColorGreen()");
        }
    }

    public void setThreeOptionGreenCorrectActiveQuiz() {
        this.threeOptionGreenCorrectActiveQuizBool = true;
        if (pageLoaded) {
            NewMathViewTest.this.loadUrl("javascript:option3ChangeBgColorGreen()");
        }
    }

    public void setFourOptionGreenCorrectActiveQuiz() {
        this.fourOptionGreenCorrectActiveQuizBool = true;
        if (pageLoaded) {
            NewMathViewTest.this.loadUrl("javascript:option4ChangeBgColorGreen()");
        }
    }

    public void setOneOptionBlueCorrectActiveQuiz() {
        this.oneOptionBlueCorrectActiveQuizBool = true;
        if (pageLoaded) {
            NewMathViewTest.this.loadUrl("javascript:option1ChangeBgColorBlue()");
        }
    }

    public void setTwoOptionBlueCorrectActiveQuiz() {
        this.twoOptionBlueCorrectActiveQuizBool = true;
        if (pageLoaded) {
            NewMathViewTest.this.loadUrl("javascript:option2ChangeBgColorBlue()");
        }
    }

    public void setThreeOptionBlueCorrectActiveQuiz() {
        this.threeOptionBlueCorrectActiveQuizBool = true;
        if (pageLoaded) {
            NewMathViewTest.this.loadUrl("javascript:option3ChangeBgColorBlue()");
        }
    }

    public void setFourOptionBlueCorrectActiveQuiz() {
        this.fourOptionBlueCorrectActiveQuizBool = true;
        if (pageLoaded) {
            NewMathViewTest.this.loadUrl("javascript:option4ChangeBgColorBlue()");
        }
    }

    public void setOption1ChangeColorDefRight() {
        this.option1ReportDefRightBool = true;
    }

    public void setOption2ChangeColorDefRight() {
        this.option2ReportDefRightBool = true;
    }

    public void setOption3ChangeColorDefRight() {
        this.option3ReportDefRightBool = true;
    }

    public void setOption4ChangeColorDefRight() {
        this.option4ReportDefRightBool = true;
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
            NewMathViewTest.this.loadUrl("javascript:showImage('" + NewMathViewTest.this.imageLink + "')");
        }
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setWrongOne(Boolean var) {
        this.wrongOne = var;
    }

    public Boolean getWrongOne() {
        return wrongOne;
    }

    public void setWrongTwo(Boolean var) {
        this.wrongTwo = var;
    }

    public Boolean getWrongTwo() {
        return wrongTwo;
    }

    public void setWrongThree(Boolean var) {
        this.wrongThree = var;
    }

    public Boolean getWrongThree() {
        return wrongThree;
    }

    public void setWrongFour(Boolean var) {
        this.wrongFour = var;
    }

    public Boolean getWrongFour() {
        return wrongFour;
    }


    public void setTickOne(Boolean var) {
        this.tickOne = var;
    }

    public Boolean getTickOne() {
        return tickOne;
    }

    public void setTickTwo(Boolean var) {
        this.tickTwo = var;
    }

    public Boolean getTickTwo() {
        return tickTwo;
    }

    public void setTickThree(Boolean var) {
        this.tickThree = var;
    }

    public Boolean getTickThree() {
        return tickThree;
    }

    public void setTickFour(Boolean var) {
        this.tickFour = var;
    }

    public Boolean getTickFour() {
        return tickFour;
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

    public void setJavaInterfaceForTest(TestQuestionFragment javaInterfaceForTest) {
        addJavascriptInterface(javaInterfaceForTest, "Android");
    }
    

    public TestQuestionFragment getJavaInterfaceForTest() {
        return javaInterfaceForTest;
    }

}

