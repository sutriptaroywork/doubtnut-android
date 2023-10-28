package com.doubtnutapp.doubtfeed2.reward.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemDoubtFeedRewardKnowMoreBinding
import com.doubtnutapp.doubtfeed2.reward.data.model.KnowMoreItem

/**
 * Created by devansh on 14/7/21.
 */

class KnowMoreRewardViewHolder(itemView: View) : BaseViewHolder<KnowMoreItem>(itemView) {

    val binding = ItemDoubtFeedRewardKnowMoreBinding.bind(itemView)

    override fun bind(data: KnowMoreItem) {
        binding.tvLevelNumber.text = (bindingAdapterPosition + 1).toString()
        binding.tvLevelTitle.text = data.title
        binding.tvLevelDescription.text = data.description
    }
}
