package com.doubtnutapp.doubtfeed2.reward.ui.viewholder

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isInvisible
import androidx.core.view.updateLayoutParams
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.MarkAttendance
import com.doubtnutapp.base.ShowDayDescription
import com.doubtnutapp.databinding.ItemDoubtFeedRewardStreakBinding
import com.doubtnutapp.doubtfeed2.reward.data.model.Streak
import com.doubtnutapp.hide
import com.doubtnutapp.load
import com.doubtnutapp.show
import com.doubtnutapp.utils.Utils

/**
 * Created by devansh on 14/7/21.
 */

class RewardStreakViewHolder(itemView: View) : BaseViewHolder<Streak>(itemView) {

    val binding = ItemDoubtFeedRewardStreakBinding.bind(itemView)

    override fun bind(data: Streak) {
        binding.apply {

            root.setOnClickListener(null)

            ivReward.isInvisible = data.showGift.not()
            ivScratchCard.hide()
            ivTick.hide()
            animationCircleStreak.hide()
            ivTick.imageTintList = null

            val dayText = root.context.getString(R.string.day_number, data.dayNumber)
            tvDay.text = if (data.isCurrentDay) {
                buildSpannedString { bold { append("${root.context.getString(R.string.today)}\n$dayText") } }
            } else {
                dayText
            }

            when (data.state) {
                Streak.FUTURE -> {
                    ivBackground.load(R.drawable.reward_grey_white)
                    root.setOnClickListener {
                        performAction(ShowDayDescription(data.dayNumber, data.showGift))
                    }
                }
                Streak.MARKED -> {
                    ivBackground.load(R.drawable.reward_green_ring)
                    ivTick.show()
                    ivTick.imageTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(root.context, R.color.white))
                }
                Streak.UNMARKED -> {
                    ivBackground.load(R.drawable.reward_grey_white)
                    animationCircleStreak.show()
                    ivTick.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            root.context,
                            R.color.reward_ring_dark_green
                        )
                    )
                    root.setOnClickListener {
                        performAction(MarkAttendance())
                    }
                }
                Streak.SCRATCHED -> {
                    ivBackground.load(R.drawable.reward_green_ring)
                    ivScratchCard.show()
                    ivScratchCard.load(R.drawable.bg_wallet_reward)
                }
                Streak.UNSCRATCHED -> {
                    ivBackground.load(R.drawable.reward_green_ring)
                    ivScratchCard.show()
                    ivScratchCard.load(R.drawable.scratch_card_unopen)
                }
            }

            root.post {
                root.updateLayoutParams {
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    width = Utils.getWidthFromScrollSize(root.context, "6")
                }
            }
        }
    }
}
