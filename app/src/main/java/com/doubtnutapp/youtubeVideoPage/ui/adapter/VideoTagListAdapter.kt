package com.doubtnutapp.youtubeVideoPage.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.libraryhome.liveclasses.model.LiveClassesFeedViewItem
import com.doubtnutapp.libraryhome.liveclasses.ui.adapter.DetailLiveClassesViewHolderFactory

class VideoTagListAdapter (private val actionsPerformer: ActionPerformer?) :
        RecyclerView.Adapter<BaseViewHolder<LiveClassesFeedViewItem>>() {

    private val recyclerViewPool = RecyclerView.RecycledViewPool()
    private val viewHolderFactory: DetailLiveClassesViewHolderFactory =
            DetailLiveClassesViewHolderFactory(recyclerViewPool, null)
    private val feeds = mutableListOf<LiveClassesFeedViewItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<LiveClassesFeedViewItem> {
        return (viewHolderFactory.getViewHolderFor(parent, viewType) as BaseViewHolder<LiveClassesFeedViewItem>).apply {
            actionPerformer = this@VideoTagListAdapter.actionsPerformer
        }
    }

    override fun getItemCount(): Int = feeds.size

    override fun getItemViewType(position: Int): Int {
        return feeds[position].viewType
    }

    override fun onBindViewHolder(holder: BaseViewHolder<LiveClassesFeedViewItem>, position: Int) {
        holder.bind(feeds[position])
    }

    fun updateFeeds(recentFeeds: List<LiveClassesFeedViewItem>) {
        feeds.clear()
        feeds.addAll(recentFeeds)
        notifyDataSetChanged()
    }
}