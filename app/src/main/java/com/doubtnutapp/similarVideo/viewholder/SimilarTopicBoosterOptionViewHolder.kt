package com.doubtnutapp.similarVideo.viewholder

import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OnTopicBoosterOptionClick
import com.doubtnutapp.databinding.ItemSimilarTopicBoosterOptionBinding
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.similarVideo.model.SimilarTopicBoosterOptionViewItem

class SimilarTopicBoosterOptionViewHolder (val binding: ItemSimilarTopicBoosterOptionBinding,
                                           private val childActionPerformer: ActionPerformer?) : BaseViewHolder<SimilarTopicBoosterOptionViewItem>(binding.root) {

    override fun bind(data: SimilarTopicBoosterOptionViewItem) {
        binding.option = data

        when (data.optionStatus) {
            0 -> { // Default
                binding.optionLayout.setBackgroundColor(binding.root.context.resources.getColor(R.color.white))
                binding.answerIcon.hide()
            }
            1 -> { // Correct Answer
                binding.optionLayout.setBackgroundColor(binding.root.context.resources.getColor(R.color.color_challenge_option_correct_answer))
                binding.answerIcon.show()
                binding.answerIcon.setImageResource(R.drawable.ic_correct_option)
            }
            2 -> { // Wrong Answer
                binding.optionLayout.setBackgroundColor(binding.root.context.resources.getColor(R.color.color_challenge_option_wrong_answer))
                binding.answerIcon.show()
                binding.answerIcon.setImageResource(R.drawable.ic_wrong_option)
            }
        }

        binding.optionCardView.setOnClickListener {
            childActionPerformer?.performAction(
                    OnTopicBoosterOptionClick(data)
            )
        }

        binding.executePendingBindings()
    }
}