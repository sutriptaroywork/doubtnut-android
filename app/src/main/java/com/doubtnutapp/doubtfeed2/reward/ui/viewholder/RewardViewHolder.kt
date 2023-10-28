package com.doubtnutapp.doubtfeed2.reward.ui.viewholder

import android.view.View
import androidx.core.view.isVisible
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.RewardClicked
import com.doubtnutapp.databinding.ItemDoubtFeedRewardBinding
import com.doubtnutapp.doubtfeed2.reward.data.model.Reward
import com.doubtnutapp.utils.DebouncedOnClickListener

/**
 * Created by devansh on 14/7/21.
 */

class RewardViewHolder(itemView: View) : BaseViewHolder<Reward>(itemView) {

    private val binding = ItemDoubtFeedRewardBinding.bind(itemView)

    override fun bind(data: Reward) {
        with(binding) {
            lockView.isVisible = data.isUnlocked.not()
            tvKnowMore.isVisible = data.isUnlocked.not()
            tvLevel.text = root.context.getString(R.string.level, data.level)

            tvRupeesIcon.hide()
            tvRewardAmount.hide()
            animationScratchCard.hide()

            if (data.isScratched) {
                if (data.rewardType == Reward.WALLET) {
                    tvRupeesIcon.show()
                    tvRewardAmount.show()
                    tvRewardAmount.text = data.walletAmount.toString()
                }
                ivReward.loadImage(data.scratchedImageLink)
                tvShortDescription.text = data.scratchDescription
            } else {
                if (data.isUnlocked) {
                    animationScratchCard.show()
                } else {
                    ivReward.load(R.drawable.scratch_card_unopen)
                }

                tvShortDescription.text =
                    if (data.isUnlocked) data.shortDescription else data.lockedShortDescription
            }

            root.setOnClickListener(object : DebouncedOnClickListener(500) {
                override fun onDebouncedClick(v: View?) {
                    performAction(RewardClicked(data.level))
                }
            })
        }
    }
}
