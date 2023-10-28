package com.doubtnutapp.ui.forum.comments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.OnCommentTagClicked
import com.doubtnutapp.data.remote.models.PreComment
import kotlinx.android.synthetic.main.item_filter_chip_subject.view.*

class CommentTagsAdapter(
    private val tags: List<PreComment> = mutableListOf(),
    private val actionPerformer: ActionPerformer
) : RecyclerView.Adapter<CommentTagsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentTagsViewHolder {
        return CommentTagsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_comment_tags, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CommentTagsViewHolder, position: Int) {
        if (tags[position].isDoubt == "1" || tags[position].isDoubt == "true") {
            holder.itemView.tvFilter.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.white
                )
            )
            holder.itemView.tvFilter.background = AppCompatResources.getDrawable(
                holder.itemView.context,
                R.drawable.bg_comment_tag_doubt
            )
        } else {
            holder.itemView.tvFilter.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.black
                )
            )
            holder.itemView.tvFilter.background =
                AppCompatResources.getDrawable(holder.itemView.context, R.drawable.bg_comment_tag)
        }

        holder.itemView.tvFilter.text = tags[position].title.orEmpty()
        holder.itemView.setOnClickListener {
            actionPerformer.performAction(OnCommentTagClicked(tags[position]))
        }
    }

    override fun getItemCount(): Int {
        return tags.size
    }
}

class CommentTagsViewHolder(view: View) : RecyclerView.ViewHolder(view)
