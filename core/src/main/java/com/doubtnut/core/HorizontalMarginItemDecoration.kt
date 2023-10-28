package com.doubtnut.core

import android.graphics.Rect
import android.view.View
import androidx.annotation.Dimension
import androidx.recyclerview.widget.RecyclerView

/**
 * Refernce: https://stackoverflow.com/a/58088398/2437655
 */
class HorizontalMarginItemDecoration(@Dimension val horizontalMarginInPx: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.right = horizontalMarginInPx
        outRect.left = horizontalMarginInPx
    }
}
