package com.doubtnutapp.widgets.mathview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.doubtnut.core.utils.ToastUtils;
import com.doubtnutapp.R;

public class MathViewSimilar extends LinearLayout {
    private final String TAG_ML_MATH_VIEW = "ml_math_view";
    private final String TAG_ASCII_MATH_VIEW = "ascii_math_view";
    private String text;
    private String color = "#2f2f2df";
    private String fontSize = "17px";
    private String textAlign = "left";
    private ProgressBar progressBar;
    private static final String TAG = MathViewSimilar.class.getSimpleName();
    private volatile boolean pageLoaded;
    private OnMathViewRenderListener onMathViewRenderListener;

    private ASCIIMathView asciiMathView;

    public MathViewSimilar(Context var1) {
        super(var1);
        this.init(var1);
    }

    public MathViewSimilar(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.init(var1);
    }

    private void init(Context var1) {
        asciiMathView = getAsciiMathView();
        this.addView(asciiMathView);
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

    // Call this before setText(String) to get callback
    public void setOnMathViewRenderStartedListener(OnMathViewRenderListener onMathViewRenderListener) {
        this.onMathViewRenderListener = onMathViewRenderListener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public void setText(String var1) {
        setText(var1, null);
    }

    public void setText(String var1, OnMathViewRenderListener onMathViewRenderListener) {
        this.text = var1;
        try {
            if (text != null && text.contains("<math")) {
                MLMathView mlMathView = this.findViewWithTag(TAG_ML_MATH_VIEW);
                if (mlMathView == null) {
                    mlMathView = getMlMathView();
                    this.addView(mlMathView);
                }
                mlMathView.setText(text);
                mlMathView.setOnMathViewRenderStartedListener(onMathViewRenderListener);

            } else {
                if (text != null && text.contains("'")) {
                   this.text =  text.replace("'", "");
                }
                asciiMathView.setFontSize(fontSize);
                asciiMathView.setText(text);
                asciiMathView.setOnMathViewRenderStartedListener(onMathViewRenderListener);
            }
        } catch (NullPointerException e) {
            if (e.getMessage() != null && e.getMessage().contains("webview")) {
                ToastUtils.makeText(getContext(), R.string.browser_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void setTextAlign(String textAlign) {
        this.textAlign = textAlign;
    }

    public void setTextColor(String color) {
        this.color = color;
    }

    public void setFontSize(int size) {
        this.fontSize = size + "px";
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
        mlMathView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
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
        asciiMathView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        return asciiMathView;
    }
}
