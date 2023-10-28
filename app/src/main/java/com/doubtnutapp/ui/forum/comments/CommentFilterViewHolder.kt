package com.doubtnutapp.ui.forum.comments

import android.view.View
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.CommentFilterSelected
import com.doubtnutapp.data.remote.models.CommentFilter
import com.doubtnutapp.databinding.ItemCommentFilterBinding

class CommentFilterViewHolder(itemView: View) : BaseViewHolder<CommentFilter>(itemView) {

    val binding = ItemCommentFilterBinding.bind(itemView)

    override fun bind(data: CommentFilter) {
        binding.textView.text = data.title
        if (data.isSelected == true) {
            binding.textView.background =
                itemView.context.getDrawable(R.drawable.capsule_stroke_tomato)
        } else {
            binding.textView.background =
                itemView.context.getDrawable(R.drawable.capsule_stroke_grey_solid_white)
        }
        itemView.setOnClickListener {
            if (data.isSelected != true) {
                actionPerformer?.performAction(CommentFilterSelected(data))
            }
        }
    }

}