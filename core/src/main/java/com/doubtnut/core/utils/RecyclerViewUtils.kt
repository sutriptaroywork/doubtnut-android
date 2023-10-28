package com.doubtnut.core.utils

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.doubtnut.core.ui.helpers.RvRequestDisallowInterceptTouchEventListener

object RecyclerViewUtils {

    private val rvRequestDisallowInterceptTouchEventListener =
        RvRequestDisallowInterceptTouchEventListener()

    fun isRecyclerScrollable(recyclerView: RecyclerView): Boolean {
        val lastCompletelyVisiblePosition =
            when (val layoutManager = recyclerView.layoutManager) {
                is LinearLayoutManager -> layoutManager.findLastCompletelyVisibleItemPosition()
                is GridLayoutManager -> layoutManager.findLastCompletelyVisibleItemPosition()
                is StaggeredGridLayoutManager -> {
                    val array = IntArray(2)
                    layoutManager.findLastCompletelyVisibleItemPositions(array)
                    array[1]
                }
                else -> throw IllegalArgumentException("unidentified instance of LayoutManager")
            }
        val adapter = recyclerView.adapter
        return if (adapter == null) false else lastCompletelyVisiblePosition < adapter.itemCount - 1
    }

    fun RecyclerView.addRvRequestDisallowInterceptTouchEventListener() {
//        removeOnItemTouchListener(rvRequestDisallowInterceptTouchEventListener)
//        addOnItemTouchListener(rvRequestDisallowInterceptTouchEventListener)
    }

}

/**
 * Removes all recyclerview item decorations
 */
fun RecyclerView.removeItemDecorations2() {
    while (this.itemDecorationCount > 0) {
        this.removeItemDecorationAt(0)
    }
}

