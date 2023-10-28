package com.doubtnutapp.widgets.mathview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.doubtnut.core.utils.ToastUtils;
import com.doubtnutapp.R;

import org.jetbrains.annotations.NotNull;

public class MathView extends LinearLayout {
    private final String TAG_ML_MATH_VIEW = "ml_math_view";
    private final String TAG_ASCII_MATH_VIEW = "ascii_math_view";
    private String text;
    private String color = "white";
    private String fontSize = "13px";
    private String textAlign = "left";
    private ProgressBar progressBar;
    private static final String TAG = MathView.class.getSimpleName();
    private volatile boolean pageLoaded;

    public MathView(Context var1) {
        super(var1);
        this.init(var1);
    }

    public MathView(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.init(var1);
    }

    private void init(Context var1) {


    }


    public void setOnMathViewClickListener(OnMathViewClickListener onMathViewClickListener) {
        setOnMathViewClickListenerToChild(onMathViewClickListener);
    }

    private void setOnMathViewClickListenerToChild(OnMathViewClickListener onMathViewClickListener) {
        View asciiMathView = findViewWithTag(TAG_ASCII_MATH_VIEW);
        if (asciiMathView instanceof ASCIIMathView) {
            ((ASCIIMathView) asciiMathView).setOnMathViewClickListener(onMathViewClickListener);
        }
    }

    public void setText(String var1) {
        this.text = var1;
        try {
            if (text != null && text.contains("<math")) {
                MLMathView mlMathView = this.findViewWithTag(TAG_ML_MATH_VIEW);
                if (mlMathView == null) {
                    mlMathView = getMlMathView();
                    this.addView(mlMathView);
                }
                mlMathView.setText(text);
            } else {
                ASCIIMathView asciiMathView = this.findViewWithTag(TAG_ASCII_MATH_VIEW);
                if (asciiMathView == null) {
                    asciiMathView = getAsciiMathView();
                    this.addView(asciiMathView);
                }
                // Set color here because it may have changed before setText() is called
                asciiMathView.setTextColor(color);
                asciiMathView.setText(text);
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("webview")) {
                ToastUtils.makeText(getContext(), R.string.browser_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public void setTextAlign(String textAlign) {
        this.textAlign = textAlign;
    }

    public void setTextColor(String color) {
        setTextColor(color, false);
    }

    public void setTextColor(String color, @NotNull Boolean invalidate) {
        this.color = color;
        if (invalidate) {
            setText(text);
        }
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

    private MLMathView getMlMathView() {
        MLMathView mlMathView = new MLMathView(getContext(), null);
        mlMathView.setTag(TAG_ML_MATH_VIEW);
        mlMathView.setVerticalScrollBarEnabled(false);
        mlMathView.setHorizontalScrollBarEnabled(false);
        return mlMathView;
    }

    private ASCIIMathView getAsciiMathView() {
        ASCIIMathView asciiMathView = new ASCIIMathView(getContext());
        asciiMathView.setTag(TAG_ASCII_MATH_VIEW);
        asciiMathView.setBackgroundColor(0);
        asciiMathView.setFontSize(fontSize);
        asciiMathView.setProgressBar(progressBar);
        asciiMathView.setTextAlign(textAlign);
        asciiMathView.setTextColor(color);
        asciiMathView.setVerticalScrollBarEnabled(false);
        asciiMathView.setHorizontalScrollBarEnabled(false);
        return asciiMathView;
    }
}
