package com.doubtnutapp.libraryhome.library;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

    private boolean loading = false; // True if we are still waiting for the last set of data to load.
    private boolean lastPageReached = false; // True if server has no data to send.
    private int firstVisibleItem;

    private int currentPage = 0;

    private final RecyclerView.LayoutManager mLayoutManager;

    public EndlessRecyclerOnScrollListener(RecyclerView.LayoutManager linearLayoutManager) {
        this.mLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = mLayoutManager.getItemCount();

        if (mLayoutManager instanceof LinearLayoutManager)
            firstVisibleItem = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
        else if (mLayoutManager instanceof GridLayoutManager)
            firstVisibleItem = ((GridLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
        else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            int[] array = new int[2];
            ((StaggeredGridLayoutManager) mLayoutManager).findFirstVisibleItemPositions(array);
            firstVisibleItem = array[1];
        }

        int visibleThreshold = 3;
        if (!loading && !lastPageReached && (totalItemCount - visibleItemCount)
                <= (firstVisibleItem + visibleThreshold)) {
            // End has been reached

            // Do something
            currentPage++;

            onLoadMore(currentPage);

            loading = true;
        }
    }

    public void setDataLoading(boolean loading) {
        this.loading = loading;
    }

    public void setLastPageReached(boolean lastPageReached) {
        this.lastPageReached = lastPageReached;
    }

    public void setStartPage(int page) {
        currentPage = page;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void incrementCurrentPage() {
        ++currentPage;
    }

    public void refresh() {
        loading = false;
        lastPageReached = false;
        firstVisibleItem = 0;
        currentPage = 0;
    }

    public abstract void onLoadMore(int current_page);
}