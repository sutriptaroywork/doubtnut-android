package com.doubtnut.core.ui.listeners;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.doubtnut.core.utils.RecyclerViewUtils;

import org.jetbrains.annotations.NotNull;

public abstract class TagsEndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

    private boolean loading = false; // True if we are still waiting for the last set of data to load.
    private boolean lastPageReached = false; // True if server has no data to send.
    private int firstVisibleItem;

    private int currentPage = 0;
    private int visibleThreshold = 3;

    private final RecyclerView.LayoutManager mLayoutManager;

    public TagsEndlessRecyclerOnScrollListener(RecyclerView.LayoutManager linearLayoutManager) {
        this.mLayoutManager = linearLayoutManager;
    }

    public void setVisibleThreshold(int visibleThreshold) {
        this.visibleThreshold = visibleThreshold;
    }

    @Override
    public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (mLayoutManager == null) {
            return;
        }

        if (dy <= 0) {
            if (!loading && !lastPageReached && !RecyclerViewUtils.INSTANCE.isRecyclerScrollable(recyclerView)) {
                // End has been reached
                // Do something
                currentPage++;
                onLoadMore(currentPage);
                loading = true;
            }
            return;//if user is scrolling upwards we are rejecting that call, as it is not required.
        }

        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = mLayoutManager.getItemCount();

        if (mLayoutManager instanceof LinearLayoutManager) {
            firstVisibleItem = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
        } else if (mLayoutManager instanceof GridLayoutManager) {
            firstVisibleItem = ((GridLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
        } else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            int[] array = new int[2];
            ((StaggeredGridLayoutManager) mLayoutManager).findFirstVisibleItemPositions(array);
            firstVisibleItem = array[1];
        }

        if (!loading && !lastPageReached && ((totalItemCount - visibleItemCount)
                <= (firstVisibleItem + visibleThreshold) || !RecyclerViewUtils.INSTANCE.isRecyclerScrollable(recyclerView))) {
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

    public boolean isLastPageReached() {
        return lastPageReached;
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