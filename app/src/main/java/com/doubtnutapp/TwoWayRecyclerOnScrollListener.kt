package com.doubtnutapp

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

abstract class TwoWayRecyclerOnScrollListener(private val mLayoutManager: RecyclerView.LayoutManager?)
    : RecyclerView.OnScrollListener() {
    private var loading = false
    private var lastPageReached = false
    private var prevLastPageReached = false
    var currentPage = 0
        private set

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (mLayoutManager == null) return
        val visibleItemCount = recyclerView.childCount
        val totalItemCount = mLayoutManager.itemCount
        val lastVisibleItem = (mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
        val firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition()
        val visibleThreshold = 3
        if (dy <= 0) {
            val isScrollable = isScrollable(recyclerView)
            if (!loading && (!prevLastPageReached || (!isScrollable && !lastPageReached))) {
                if (!isScrollable && !lastPageReached) {
                    currentPage++
                    loading = true
                    onLoadMore()
                } else if (lastVisibleItem < 3 || firstVisibleItem < 1) {
                    currentPage++
                    loading = true
                    onLoadMorePrev()
                }
            }
        } else {
            if (!loading && !lastPageReached
                    && totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold) {
                currentPage++
                loading = true
                onLoadMore()
            }
        }
    }

    fun setDataLoading(loading: Boolean) {
        this.loading = loading
    }

    fun setLastPageReached(lastPageReached: Boolean) {
        this.lastPageReached = lastPageReached
    }

    fun setPrevLastPageReached(prevLastPageReached: Boolean) {
        this.prevLastPageReached = prevLastPageReached
    }

    fun setStartPage(page: Int) {
        currentPage = page
    }

    fun incrementCurrentPage() {
        ++currentPage
    }

    fun refresh() {
        loading = false
        lastPageReached = false
        currentPage = 0
    }

    private fun isScrollable(recyclerView: RecyclerView): Boolean {
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

    abstract fun onLoadMore()
    abstract fun onLoadMorePrev()

}