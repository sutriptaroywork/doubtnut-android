package com.doubtnutapp.ui.forum.comments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.remote.models.CommentFilter

class CommentFilterAdapter(private val actionsPerformer: ActionPerformer?) :
    RecyclerView.Adapter<BaseViewHolder<CommentFilter>>() {

    val listings = mutableListOf<CommentFilter>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<CommentFilter> {
        return CommentFilterViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_comment_filter,
                parent, false
            )
        )
            .apply { actionPerformer = actionsPerformer }
    }

    override fun getItemCount(): Int = listings.size

    override fun onBindViewHolder(holder: BaseViewHolder<CommentFilter>, position: Int) {
        holder.bind(listings[position])
    }

    fun updateList(recentListings: List<CommentFilter>) {
        listings.addAll(recentListings)
        notifyDataSetChanged()
    }

}