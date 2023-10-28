package com.doubtnutapp.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.doubtnutapp.R

class WaveRipple @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(context, attrs, defStyleAttr) {

    private var waveAnimator: ValueAnimator? = null
    private var waveRadiusOffset = 0f
        set(value) {
            field = value
            postInvalidateOnAnimation()
        }

    private var center = PointF(0f, 0f)

    private var maxRadius: Float = 0f
    private var initialRadius: Float = 0f
    private var waveGap: Float = 0f
    private var waveColor: Int = 0

    private var paint = Paint()

    init {

        with(context.obtainStyledAttributes(attrs, R.styleable.WaveRipple)) {
            waveColor = getColor(R.styleable.WaveRipple_wave_color, Color.BLUE)
            waveGap = getDimension(R.styleable.WaveRipple_wave_gap, 20f)
        }

        paint.apply {
            color = waveColor
            style = Paint.Style.STROKE
            strokeWidth = waveGap
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        waveAnimator = ValueAnimator.ofFloat(0f, waveGap).apply {

            addUpdateListener {
                waveRadiusOffset = it.animatedValue as Float
            }

            duration = 1500L
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        center.set(w / 2f, h / 2f)
        maxRadius = (h / 2).toFloat()
        initialRadius = w / waveGap
    }

    override fun onDetachedFromWindow() {
        waveAnimator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        var currentRadius = initialRadius + waveRadiusOffset
        while (currentRadius < maxRadius) {
            paint.alpha = getAplhaValueForRadius(currentRadius)
            canvas?.drawCircle(center.x, center.y, currentRadius, paint)
            currentRadius += waveGap
        }
    }

    private fun getAplhaValueForRadius(currentRadius: Float) = 255 - (255 / maxRadius * currentRadius).toInt()
}