package com.doubtnutapp.widgets.curvedtopbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.doubtnutapp.R

class CurvedTopBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val mPath: Path = Path()
    private val mPaint: Paint = Paint()

    // Navigation bar bounds (width & height)
    private var mNavigationBarWidth: Int = 0
    private var mNavigationBarHeight: Int = 0

    private var curvedBackgroundColor: Int = 0
    private var curvedRadius: Float = 0f
    private var offset: Int = 0

    init {

        with(context.obtainStyledAttributes(attrs, R.styleable.CurvedTopBar)) {
            curvedRadius = getDimension(R.styleable.CurvedTopBar_curved_corner_radius, 0f)
            curvedBackgroundColor = getInt(R.styleable.CurvedTopBar_curved_background_color, 0)
        }


        with(mPaint) {
            style = Paint.Style.FILL_AND_STROKE
            color = if (curvedBackgroundColor != 0) curvedBackgroundColor else Color.BLUE
        }

        setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val validHeight = measuredHeight >= curvedRadius.toInt()

        check(validHeight) {
            "Height needs to larger than Curved Radius"
        }

        mNavigationBarHeight = height
        mNavigationBarWidth = width
        offset = width / 100 * 20

        mPath.run {
            reset()
            moveTo(0f, mNavigationBarHeight - curvedRadius)
            cubicTo(
                    (mNavigationBarWidth / 2 - offset).toFloat(), mNavigationBarHeight.toFloat(),
                    (mNavigationBarWidth / 2 + offset).toFloat(), mNavigationBarHeight.toFloat(),
                    mNavigationBarWidth.toFloat(), mNavigationBarHeight - curvedRadius
            )

            lineTo(mNavigationBarWidth.toFloat(), 0f)
            lineTo(0f, 0f)
            close()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(mPath, mPaint)
    }
}