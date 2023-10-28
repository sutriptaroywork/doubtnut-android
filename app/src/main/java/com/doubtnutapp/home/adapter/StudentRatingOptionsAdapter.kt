package com.doubtnutapp.home.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.home.HomeFeedViewHolderFactory

class StudentRatingOptionsAdapter(private val actionsPerformer: ActionPerformer?) :
    RecyclerView.Adapter<BaseViewHolder<String>>() {

    private val viewHolderFactory: HomeFeedViewHolderFactory = HomeFeedViewHolderFactory()
    private val feeds = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<String> {
        return (viewHolderFactory.getViewHolderFor(
            parent,
            viewType
        ) as BaseViewHolder<String>).apply {
            actionPerformer = this@StudentRatingOptionsAdapter.actionsPerformer
        }
    }

    override fun getItemCount(): Int = feeds.size

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_student_rating_options
    }

    override fun onBindViewHolder(holder: BaseViewHolder<String>, position: Int) {
        holder.bind(feeds[position])
    }

    fun updateFeeds(recentFeeds: List<String>) {
        feeds.clear()
        feeds.addAll(recentFeeds)
        notifyDataSetChanged()
    }
}