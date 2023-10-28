package com.doubtnutapp.matchquestion.ui.viewholder

import android.view.View
import com.bumptech.glide.Glide
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenLibraryPlayListActivity
import com.doubtnutapp.databinding.ItemMatchFeedbackBookBinding
import com.doubtnutapp.matchquestion.model.MatchFeedbackEntity

/**
 * Created by Sachin Saxena on 2020-05-04.
 */
class MatchFeedbackBookViewHolder(
    private val binding: ItemMatchFeedbackBookBinding
) : BaseViewHolder<MatchFeedbackEntity.MatchFeedbackDataEntity>(binding.root) {

    override fun bind(data: MatchFeedbackEntity.MatchFeedbackDataEntity) {

        Glide.with(binding.root.context)
            .load(data._source.image)
            .into(binding.bookImage)

        binding.bookName.text = data._source.bookName

        if (data._source.author.isEmpty()) {
            binding.authorName.visibility = View.GONE
        } else {
            binding.authorName.visibility = View.VISIBLE
            binding.authorName.text = data._source.author
        }

        binding.root.setOnClickListener {
            actionPerformer?.performAction(
                OpenLibraryPlayListActivity(
                    data._id,
                    data._source.bookName,
                    position = adapterPosition
                )
            )
        }

        binding.executePendingBindings()
    }
}