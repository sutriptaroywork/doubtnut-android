package com.doubtnutapp.widgets.itemdecorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacesItemDecoration(
    private val leftSpace: Int,
    private val topSpace: Int,
    private val rightSpace: Int,
    private val bottomSpace: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.left = leftSpace
        outRect.right = rightSpace
        outRect.bottom = bottomSpace
        outRect.top = topSpace
    }
}