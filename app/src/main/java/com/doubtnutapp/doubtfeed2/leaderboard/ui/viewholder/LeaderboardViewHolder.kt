package com.doubtnutapp.doubtfeed2.leaderboard.ui.viewholder

import android.annotation.SuppressLint
import android.view.View
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.databinding.ItemDfLeaderboardStudentBinding
import com.doubtnutapp.doubtfeed2.leaderboard.data.model.LeaderboardStudent
import com.doubtnutapp.loadImage
import javax.inject.Inject

/**
 * Created by devansh on 23/06/21.
 */

class LeaderboardViewHolder(itemView: View) :
    BaseViewHolder<LeaderboardStudent>(itemView) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val binding = ItemDfLeaderboardStudentBinding.bind(itemView)

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    @SuppressLint("SetTextI18n")
    override fun bind(data: LeaderboardStudent) {
        with(binding) {
            tvRank.text = "${bindingAdapterPosition + 4}" // +4 as top 3 items are shown in separate UI at top
            ivStudent.loadImage(data.image)
            tvName.text = data.name
            tvWins.text = data.subtitle

            root.setOnClickListener {
                openStudentProfile(data.studentId)
            }
        }
    }

    private fun openStudentProfile(studentId: String) {
        FragmentWrapperActivity.userProfile(binding.root.context, studentId, "daily_goal")
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.DG_PROFILE_VISIT_LEADERBOARD, ignoreSnowplow = true))
    }
}
