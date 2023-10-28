package com.doubtnutapp.liveclass.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.OnCommentTagClicked
import com.doubtnutapp.data.remote.models.PreComment
import com.doubtnutapp.databinding.ItemCommentTagsLandscapeBinding

class CommentTagsAdapter(
    private val tags: List<PreComment> = mutableListOf(),
    private val actionPerformer: ActionPerformer
) : RecyclerView.Adapter<CommentTagsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentTagsViewHolder {
        return CommentTagsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_comment_tags_landscape, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CommentTagsViewHolder, position: Int) {
        if (tags[position].isDoubt == "1" || tags[position].isDoubt == "true") {
            holder.binding.tvFilter.background =
                holder.itemView.context.getDrawable(R.drawable.bg_comment_tag_doubt)
        } else {
            holder.binding.tvFilter.background =
                holder.itemView.context.getDrawable(R.drawable.bg_comment_tag_landscape)
        }
        holder.binding.tvFilter.text = tags[position].title.orEmpty()
        holder.itemView.setOnClickListener {
            actionPerformer.performAction(OnCommentTagClicked(tags[position]))
        }
    }

    override fun getItemCount(): Int {
        return tags.size
    }
}

class CommentTagsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val binding = ItemCommentTagsLandscapeBinding.bind(view)
}
