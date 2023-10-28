package com.doubtnutapp.home.recyclerdecorator

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

class HorizontalBannerSpaceItemDecoration(
    private val spacing: Int,
    private val snapHelper: PagerSnapHelper,
    private var addHorizontalSpacing: Boolean = true,
    private val indicatorRadius: Float = 10f
) : RecyclerView.ItemDecoration() {

    private val colorActive = Color.parseColor("#6A6A6A")
    private val colorInactive = Color.parseColor("#CECDCD")

    private val mIndicatorHeight = (DP * 16).toInt()

    private val mIndicatorStrokeWidth = DP * 2

    private val mIndicatorItemLength = DP * 8

    private val mIndicatorItemPadding = DP * 4

    private val mPaint = Paint()

    init {
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeWidth = mIndicatorStrokeWidth
        mPaint.style = Paint.Style.FILL
        mPaint.isAntiAlias = true
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildViewHolder(view).bindingAdapterPosition
        val itemCount = state.itemCount
        setSpacingForDirection(outRect, position, itemCount)
    }

    private fun setSpacingForDirection(
        outRect: Rect,
        position: Int,
        itemCount: Int
    ) {

        outRect.left = if (position == 0 && addHorizontalSpacing) spacing else 0
        outRect.right = if ((position == itemCount - 1) && addHorizontalSpacing) spacing else 0
        outRect.top = spacing
        outRect.bottom = spacing

    }

    /**
     * Remove spacings from left and right sides.
     * Call [RecyclerView.invalidateItemDecorations] after this to update item decorations.
     */
    fun removeHorizontalSpacing() {
        addHorizontalSpacing = false
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val itemCount = parent.adapter?.itemCount ?: 0

        if (itemCount < ITEM_COUNT_THRESHOLD) return

        val totalLength = mIndicatorItemLength * itemCount
        val paddingBetweenItems = max(0, itemCount - 1) * mIndicatorItemPadding
        val indicatorTotalWidth = totalLength + paddingBetweenItems
        val indicatorStartX = (parent.width - indicatorTotalWidth) / 2f

        val indicatorPosY = parent.height - mIndicatorHeight / 2f

        drawInactiveIndicators(c, indicatorStartX, indicatorPosY, itemCount)

        val layoutManagerNew = parent.layoutManager as LinearLayoutManager
        val snapView = snapHelper.findSnapView(layoutManagerNew)
        val snapPosition = layoutManagerNew.getPosition(snapView!!)

        if (snapPosition == RecyclerView.NO_POSITION) {
            return
        }

        drawHighlights(c, indicatorStartX, indicatorPosY, snapPosition)
    }

    private fun drawInactiveIndicators(
        c: Canvas,
        indicatorStartX: Float,
        indicatorPosY: Float,
        itemCount: Int
    ) {
        mPaint.color = colorInactive

        val itemWidth = mIndicatorItemLength + mIndicatorItemPadding

        var start = indicatorStartX
        for (i in 0 until itemCount) {
            c.drawCircle(start, indicatorPosY, indicatorRadius, mPaint)
            start += itemWidth
        }
    }

    private fun drawHighlights(
        c: Canvas, indicatorStartX: Float, indicatorPosY: Float,
        highlightPosition: Int
    ) {
        mPaint.color = colorActive

        val itemWidth = mIndicatorItemLength + mIndicatorItemPadding

        var highlightStart = indicatorStartX + itemWidth * highlightPosition

        c.drawCircle(
            highlightStart, indicatorPosY, indicatorRadius,
            mPaint
        )

        if (highlightPosition < 0) {
            highlightStart += itemWidth
            c.drawCircle(
                highlightStart, indicatorPosY, indicatorRadius,
                mPaint
            )
        }
    }

    companion object {
        private val DP = Resources.getSystem().displayMetrics.density

        private const val ITEM_COUNT_THRESHOLD = 2
    }
}
