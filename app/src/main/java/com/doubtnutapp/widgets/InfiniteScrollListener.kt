package com.doubtnutapp.widgets

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager


abstract class InfiniteScrollListener : RecyclerView.OnScrollListener() {

    private var firstVisibleItem: Int = 0


    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val layoutManager = recyclerView.layoutManager
        val visibleItemCount = recyclerView.childCount
        val totalItemCount = layoutManager?.itemCount ?: 0

        if (dy <= 0) {

            if (isRecyclerScrollable(recyclerView).not()) {
                onLoadMore()
            }

            return //if user is scrolling upwards we are rejecting that call, as it is not required.
        }

        firstVisibleItem = when (layoutManager) {
            is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition()
            is GridLayoutManager -> layoutManager.findFirstVisibleItemPosition()
            is StaggeredGridLayoutManager -> {
                val array = IntArray(2)
                layoutManager.findFirstVisibleItemPositions(array)
                array[1]
            }
            else -> throw IllegalArgumentException("unidentified instance of LayoutManager")
        }

        val visibleThreshold = 3
        if (totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold) {
            onLoadMore()
        }
    }

    private fun isRecyclerScrollable(recyclerView: RecyclerView): Boolean {
        val layoutManager = recyclerView.layoutManager

        val lastCompletelyVisiblePosition = when (layoutManager) {
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

    abstract fun onLoadMore()
}