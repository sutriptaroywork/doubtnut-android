package com.doubtnutapp.home.recyclerdecorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceItemDecoration constructor(private val spacing: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildViewHolder(view).adapterPosition
        val itemCount = state.itemCount
        setSpacingForDirection(outRect, position, itemCount)
    }

    private fun setSpacingForDirection(
        outRect: Rect,
        position: Int,
        itemCount: Int
    ) {

        outRect.left = if (position == 0) spacing else 0
        outRect.right = if (position == itemCount - 1) spacing else 0
        outRect.top = spacing
        outRect.bottom = spacing

    }
}
