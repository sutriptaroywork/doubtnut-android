package com.doubtnutapp.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.core.view.doOnPreDraw
import com.bumptech.glide.Glide
import com.doubtnutapp.R
import com.doubtnutapp.hide
import com.doubtnutapp.setVisibleState

class AskQuestionProgressView : RelativeLayout {

    private var scannerAnimator: ValueAnimator? = null

    private lateinit var mBarPaint: Paint
    private lateinit var mOverlayPaint: Paint

    private var imageView: ImageView? = null
    private var progressBar: ProgressBar? = null

    private var childX: Float = -1f
    private var childY: Float = -1f
    private var childW: Float = -1f
    private var childH: Float = -1f

    private var changingX: Float = -1f

    private var isBitmapSet = false

    private var questionBitmap: Bitmap? = null

    init {
        inflate(context, R.layout.item_imageaskquestion, this)
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }


    private fun init() {

        setWillNotDraw(false)

        imageView = findViewById(R.id.askQuestionImage)
        progressBar = findViewById(R.id.progressBar)

        imageView?.doOnPreDraw {
            imageView?.let {
                childX = it.x
                childY = it.y
                changingX = childX
                childH = it.height.toFloat()
                childW = it.width.toFloat()
            }

            if (childH > 0) {
                startScannerAnimation()
            }
        }


        mBarPaint = Paint().apply {
            color = Color.parseColor("#ff7d42")
            style = Paint.Style.FILL
            strokeWidth = 10f
        }

        mOverlayPaint = Paint().apply {
            color = Color.parseColor("#50ff7d42")
            style = Paint.Style.FILL
        }
    }

    private fun startScannerAnimation() {
        if (scannerAnimator != null && scannerAnimator?.isRunning == true) return

        scannerAnimator = ValueAnimator.ofFloat(childX, childX + childW).apply {

            addUpdateListener { valueAnimator ->
                changingX = valueAnimator.animatedValue as Float
                postInvalidateOnAnimation()
            }
            duration = 1500
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }

    }

    override fun dispatchDraw(canvas: Canvas?) {
        // bitmap is recycled in some cases (e.g. on low end devices,
        // when screen is turned off and turned back on, bitmap is recycled)
        // if bitmap is recycled, remove image and continue the loading animation
        if (isBitmapSet && questionBitmap!!.isRecycled) {
            imageView?.setImageBitmap(null)
        }

        super.dispatchDraw(canvas)

        if (isBitmapSet) {
            canvas?.drawRect(childX, childY, changingX, childY + childH, mOverlayPaint)

            canvas?.drawLine(changingX, childY - 20f, changingX, childY + childH + 20, mBarPaint)
        }
    }

    fun setImageBitmap(askQuestionBitmap: Bitmap) {
        questionBitmap = askQuestionBitmap
        isBitmapSet = true
        imageView?.setImageBitmap(askQuestionBitmap)
        progressBar?.hide()
    }

    fun setProgressBarState(state: Boolean) {
        progressBar?.setVisibleState(state)
        setVisibleState(state)
    }

}
