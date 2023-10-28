package com.doubtnutapp.reward.ui.viewholder

import android.view.View
import androidx.core.view.isVisible
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.RewardClicked
import com.doubtnutapp.data.remote.models.reward.Reward
import com.doubtnutapp.databinding.ItemRewardBinding
import com.doubtnutapp.utils.DebouncedOnClickListener

class RewardViewHolder(itemView: View) : BaseViewHolder<Reward>(itemView) {

    val binding = ItemRewardBinding.bind(itemView)

    override fun bind(data: Reward) {
        binding.apply {
            lockView.isVisible = data.isUnlocked.not()
            tvKnowMore.isVisible = data.isUnlocked.not()
            tvLevel.text = root.context.getString(R.string.level, data.level)

            tvRupeesIcon.hide()
            tvRewardAmount.hide()

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