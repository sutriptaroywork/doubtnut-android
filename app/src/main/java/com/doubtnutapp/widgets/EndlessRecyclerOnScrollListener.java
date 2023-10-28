package com.doubtnutapp.widgets;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 3; // The minimum amount of items to have below your current scroll position before loading more.
    int firstVisibleItem, visibleItemCount, totalItemCount;

    private int current_page = 0;

    private RecyclerView.LayoutManager mLayoutManager;
    private boolean lastPageReached = false; // True if server has no data to send.


    public EndlessRecyclerOnScrollListener(RecyclerView.LayoutManager linearLayoutManager) {
        this.mLayoutManager = linearLayoutManager;
    }


    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLayoutManager.getItemCount();

        if (mLayoutManager instanceof LinearLayoutManager)
            firstVisibleItem = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
        else if (mLayoutManager instanceof GridLayoutManager)
            firstVisibleItem = ((GridLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
        else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            int[] array = new int[2];
            ((StaggeredGridLayoutManager) mLayoutManager).findFirstVisibleItemPositions(array);
            firstVisibleItem = array[1];
        }


        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount)
                <= (firstVisibleItem + visibleThreshold)) {
            // End has been reached

            // Do something
            current_page++;

            onLoadMore(current_page);

            loading = true;
        }
    }

    public void setDataLoading(boolean loading) {
        this.loading = loading;
    }

    public void setLastPageReached(boolean lastPageReached) {
        this.lastPageReached = lastPageReached;
    }


    public abstract void onLoadMore(int current_page);
}