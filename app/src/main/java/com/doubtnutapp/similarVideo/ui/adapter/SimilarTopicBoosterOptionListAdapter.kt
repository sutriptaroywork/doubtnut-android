package com.doubtnutapp.similarVideo.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.similarVideo.viewholder.SimilarVideoViewHolderFactory

class SimilarTopicBoosterOptionListAdapter (private val actionsPerformer: ActionPerformer) :
        RecyclerView.Adapter<BaseViewHolder<RecyclerViewItem>>() {

    private val recyclerViewPool = RecyclerView.RecycledViewPool()
    private val viewHolderFactory: SimilarVideoViewHolderFactory = SimilarVideoViewHolderFactory()
    private val feeds = mutableListOf<RecyclerViewItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<RecyclerViewItem> {
        return (viewHolderFactory.getViewHolderFor(parent, viewType, actionsPerformer) as BaseViewHolder<RecyclerViewItem>).apply {
            actionPerformer = this@SimilarTopicBoosterOptionListAdapter.actionsPerformer
        }
    }

    override fun getItemCount(): Int = feeds.size

    override fun getItemViewType(position: Int): Int {
        return feeds[position].viewType
    }

    override fun onBindViewHolder(holder: BaseViewHolder<RecyclerViewItem>, position: Int) {
        holder.bind(feeds[position])
    }

    fun updateFeeds(recentFeeds: List<RecyclerViewItem>) {
        feeds.clear()
        feeds.addAll(recentFeeds)
        notifyDataSetChanged()
    }

    fun updateItemAtPosition(position: Int, recyclerViewItem: RecyclerViewItem) {
        feeds[position] = recyclerViewItem
        notifyItemChanged(position)
    }
}