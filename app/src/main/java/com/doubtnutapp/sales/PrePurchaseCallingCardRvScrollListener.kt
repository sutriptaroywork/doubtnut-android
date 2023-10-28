package com.doubtnutapp.sales

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

open class PrePurchaseCallingCardRvScrollListener : RecyclerView.OnScrollListener() {

    private var isScrollBackToTop = false
    private var scrollCounter = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        var firstVisibleItemPosition = 0

        when (val layoutManager = recyclerView.layoutManager) {
            is LinearLayoutManager -> {
                firstVisibleItemPosition =
                    layoutManager.findFirstVisibleItemPosition()
            }
            is GridLayoutManager -> {
                firstVisibleItemPosition =
                    layoutManager.findFirstVisibleItemPosition()
            }
        }

        if (!isScrollBackToTop && scrollCounter >= 4 && firstVisibleItemPosition == 0) {
            scrollBackToTop()
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            scrollCounter++
        }
    }

    open fun scrollBackToTop() {
        isScrollBackToTop = true
    }
}