package com.doubtnutapp.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.roundToInt

class AutofitRecyclerView : RecyclerView {
    private var manager: GridLayoutManager? = null
    private var columnWidth = -1

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val attrsArray = intArrayOf(android.R.attr.columnWidth)
            val array = context.obtainStyledAttributes(attrs, attrsArray)
            columnWidth = array.getDimensionPixelSize(0, -1)
            array.recycle()
        }
        manager = CenteredGridLayoutManager(getContext(), 1)
        layoutManager = manager
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        if (columnWidth > 0) {
            val spanCount = max(1, measuredWidth / columnWidth)
            manager!!.spanCount = spanCount
        }
    }

    private inner class CenteredGridLayoutManager : GridLayoutManager {
        constructor(
            context: Context?,
            attrs: AttributeSet?,
            defStyleAttr: Int,
            defStyleRes: Int
        ) : super(context, attrs, defStyleAttr, defStyleRes) {
        }

        constructor(context: Context?, spanCount: Int) : super(context, spanCount) {}
        constructor(
            context: Context?,
            spanCount: Int,
            orientation: Int,
            reverseLayout: Boolean
        ) : super(context, spanCount, orientation, reverseLayout) {
        }

        override fun getPaddingLeft(): Int {
            val totalItemWidth = columnWidth * spanCount
            return if (totalItemWidth >= this@AutofitRecyclerView.measuredWidth) {
                super.getPaddingLeft() // do nothing
            } else {
                (this@AutofitRecyclerView.measuredWidth / (1f + spanCount) - totalItemWidth / (1f + spanCount)).roundToInt()
            }
        }

        override fun getPaddingRight(): Int {
            return paddingLeft
        }
    }
}