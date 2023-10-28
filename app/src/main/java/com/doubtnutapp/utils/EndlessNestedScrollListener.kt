package com.doubtnutapp.utils

import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.isOnScreen

abstract class EndlessNestedScrollListener : RecyclerView.OnScrollListener(), NestedScrollView.OnScrollChangeListener {
    private var loading = false // True if we are still waiting for the last set of data to load.
    private var lastPageReached = false // True if server has no data to send.
    private var firstVisibleItem = 0
    var currentPage = 0
        private set
    private var childRecyclerView: RecyclerView? = null
    fun setChildRecyclerView(childRecyclerView: RecyclerView?) {
        this.childRecyclerView = childRecyclerView
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (childRecyclerView == null) return

        (childRecyclerView?.layoutManager as? LinearLayoutManager)?.let {
            if (it.findLastVisibleItemPosition() > -1) {
                it.findViewByPosition(it.findLastVisibleItemPosition())?.let { lastView ->
                    if (lastView.isOnScreen) {
                        if (!loading) //check for scroll down
                        {
                            if (!lastPageReached) {
                                currentPage++
                                onLoadMore(currentPage)
                                loading = true
                            }
                        }
                    }
                }
            }

        }
    }

    override fun onScrollChange(v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
            if (!loading) //check for scroll down
            {
                if (!lastPageReached) {
                    currentPage++
                    onLoadMore(currentPage)
                    loading = true
                }
            }
        }
    }

    fun setDataLoading(loading: Boolean) {
        this.loading = loading
    }

    fun setLastPageReached(lastPageReached: Boolean) {
        this.lastPageReached = lastPageReached
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
        firstVisibleItem = 0
        currentPage = 0
    }

    abstract fun onLoadMore(current_page: Int)
}