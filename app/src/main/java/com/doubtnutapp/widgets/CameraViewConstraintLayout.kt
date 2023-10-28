package com.doubtnutapp.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.doubtnut.core.utils.ViewUtils
import com.doubtnutapp.R


class CameraViewConstraintLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {


    private val seeThroughPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val cornerAngle: Paint = Paint().apply {
        strokeWidth = 15f
        color = Color.WHITE
        style = Paint.Style.STROKE
    }

    private val leftTopPath = Path()
    private val leftBottomPath = Path()
    private val rightTopPath = Path()
    private val rightBottomPath = Path()

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        setBackgroundColor(ContextCompat.getColor(context, R.color.black_70))
        setUpViewAtCenter(context)
    }

    lateinit var seeThroughView: View

    private fun setUpViewAtCenter(context: Context) {

        inflate(context, com.doubtnutapp.R.layout.view_see_through, this)

        seeThroughView = findViewById(R.id.seeThroughView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(this)

        constraintSet.connect(R.id.captureQuestionPhotoText, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
        constraintSet.connect(R.id.captureQuestionPhotoText, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
        constraintSet.connect(R.id.captureQuestionPhotoText, ConstraintSet.BOTTOM, R.id.divider, ConstraintSet.TOP, ViewUtils.dpToPx(10f, context).toInt())

        constraintSet.connect(R.id.divider, ConstraintSet.START, R.id.noHandWrittenImage, ConstraintSet.START, 0)
        constraintSet.connect(R.id.divider, ConstraintSet.END, R.id.noHandWrittenImage, ConstraintSet.END, 0)
        constraintSet.connect(R.id.divider, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, ViewUtils.dpToPx(10f, context).toInt())
        constraintSet.connect(R.id.divider, ConstraintSet.BOTTOM, R.id.seeThroughView, ConstraintSet.TOP, ViewUtils.dpToPx(10f, context).toInt())

        constraintSet.connect(R.id.noHandWrittenImage, ConstraintSet.TOP, R.id.divider, ConstraintSet.BOTTOM, ViewUtils.dpToPx(10f, context).toInt())
        constraintSet.connect(R.id.noHandWrittenImage, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
        constraintSet.connect(R.id.noHandWrittenImage, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)


        constraintSet.connect(R.id.seeThroughView, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
        constraintSet.connect(R.id.seeThroughView, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        constraintSet.connect(R.id.seeThroughView, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, ViewUtils.dpToPx(30f, context).toInt())
        constraintSet.connect(R.id.seeThroughView, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, ViewUtils.dpToPx(30f, context).toInt())
        constraintSet.setVerticalBias(R.id.seeThroughView, 0.40f)



        constraintSet.connect(R.id.trickyQuestionCaption, ConstraintSet.TOP, R.id.seeThroughView, ConstraintSet.BOTTOM, ViewUtils.dpToPx(30f, context).toInt())
        constraintSet.connect(R.id.trickyQuestionCaption, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
        constraintSet.connect(R.id.trickyQuestionCaption, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)

        constraintSet.connect(R.id.trickyQuestionLayout, ConstraintSet.TOP, R.id.trickyQuestionCaption, ConstraintSet.BOTTOM, ViewUtils.dpToPx(20f, context).toInt())
        constraintSet.connect(R.id.trickyQuestionLayout, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
        constraintSet.connect(R.id.trickyQuestionLayout, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)

        constraintSet.applyTo(this)

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        leftTopPath.moveTo(seeThroughView.left.toFloat(), seeThroughView.top.toFloat() + 50)
        leftTopPath.lineTo(seeThroughView.left.toFloat(), seeThroughView.top.toFloat())
        leftTopPath.lineTo(seeThroughView.left.toFloat() + 50, seeThroughView.top.toFloat())

        canvas?.drawPath(leftTopPath, cornerAngle)

        leftBottomPath.moveTo(seeThroughView.left.toFloat(), seeThroughView.bottom.toFloat() - 50)
        leftBottomPath.lineTo(seeThroughView.left.toFloat(), seeThroughView.bottom.toFloat())
        leftBottomPath.lineTo(seeThroughView.left.toFloat() + 50, seeThroughView.bottom.toFloat())

        canvas?.drawPath(leftBottomPath, cornerAngle)

        rightTopPath.moveTo(seeThroughView.right.toFloat() - 50, seeThroughView.top.toFloat())
        rightTopPath.lineTo(seeThroughView.right.toFloat(), seeThroughView.top.toFloat())
        rightTopPath.lineTo(seeThroughView.right.toFloat(), seeThroughView.top.toFloat() + 50)

        canvas?.drawPath(rightTopPath, cornerAngle)

        rightBottomPath.moveTo(seeThroughView.right.toFloat(), seeThroughView.bottom.toFloat() - 50)
        rightBottomPath.lineTo(seeThroughView.right.toFloat(), seeThroughView.bottom.toFloat())
        rightBottomPath.lineTo(seeThroughView.right.toFloat() - 50, seeThroughView.bottom.toFloat())

        canvas?.drawPath(rightBottomPath, cornerAngle)

        canvas?.drawRect(
                seeThroughView.left.toFloat(),
                seeThroughView.top.toFloat(),
                seeThroughView.right.toFloat(),
                seeThroughView.bottom.toFloat(),
                seeThroughPaint)
    }

}