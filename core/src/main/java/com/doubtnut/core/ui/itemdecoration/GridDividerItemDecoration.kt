package com.doubtnut.core.ui.itemdecoration

import android.graphics.Canvas
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView

class GridDividerItemDecoration(
    private val dividerColor: Int,
    private val _strokeWidth: Float = 2f
) : RecyclerView.ItemDecoration() {

    private val dividerPaint: Paint = Paint().apply {
        this.strokeWidth = _strokeWidth
        color = dividerColor
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawHorizontalDividers(canvas, parent)
        drawVerticalDividers(canvas, parent)
    }

    private fun drawHorizontalDividers(canvas: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            try {
                val childView = parent.getChildAt(i)
                canvas.drawLine(
                    childView.left.toFloat(),
                    childView.bottom.toFloat(),
                    childView.right.toFloat(),
                    childView.bottom.toFloat(),
                    dividerPaint
                )
            } catch (ex: ClassCastException) {
                throw(RuntimeException("LayoutManager is not a subtype of GridlayoutManager"))
            }
        }
    }

    private fun drawVerticalDividers(canvas: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            try {
                val childView = parent.getChildAt(i)
                canvas.drawLine(
                    childView.right.toFloat(),
                    childView.top.toFloat(),
                    childView.right.toFloat(),
                    childView.bottom.toFloat(),
                    dividerPaint
                )
            } catch (ex: ClassCastException) {
                throw(RuntimeException("LayoutManager is not a subtype of GridlayoutManager"))
            }
        }

    }

}
