package com.doubtnutapp.matchquestion.ui.viewholder

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ItemMatchFilterTopicBinding
import com.doubtnutapp.matchquestion.listener.FilterTopicClickListener
import com.doubtnutapp.matchquestion.model.MatchFilterTopicViewItem

/**
 * Created by Sachin Saxena on 2020-06-02.
 */
class MatchFilterTopicViewHolder(
    containerView: View,
    private val facetPosition: Int,
    private val isFromFragment: Boolean,
    private val isMultiSelect: Boolean,
    topicClickListener: FilterTopicClickListener
) : MatchFilterTopicBaseViewHolder(containerView, topicClickListener) {

    val binding = ItemMatchFilterTopicBinding.bind(itemView)

    override fun bind(data: MatchFilterTopicViewItem) {

        binding.apply {
            topicName.text = data.display

            val layoutParams = topicCard.layoutParams as ViewGroup.MarginLayoutParams

            topicNameLayout.isEnabled = true
            when (data.isSelected) {
                true -> {
                    topicName.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.fragment_match_filter_selected_text
                        )
                    )
                    topicCard.background =
                        ContextCompat.getDrawable(
                            binding.root.context,
                            R.drawable.background_selected_filter
                        )
                }
                else -> {
                    topicName.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.fragment_match_filter_unselected_text
                        )
                    )
                    topicCard.background =
                        ContextCompat.getDrawable(
                            binding.root.context,
                            R.drawable.background_unselected_filter
                        )
                }
            }

            if (isFromFragment) {
                layoutParams.setMargins(0, 0, 15, 20)
                topicCard.layoutParams = layoutParams

            } else {
                layoutParams.setMargins(0, 0, 10, 0)
                topicCard.layoutParams = layoutParams
            }

            topicNameLayout.setOnClickListener {
                if (data.isAllTopic.not() || (data.isAllTopic && data.isSelected.not())) {
                    topicClickListener.onTopicClick(
                        adapterPosition,
                        data.isSelected,
                        !isFromFragment,
                        facetPosition,
                        isMultiSelect
                    )
                }
            }
        }
    }
}