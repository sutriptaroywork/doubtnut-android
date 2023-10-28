package com.doubtnutapp.topicboostergame2.ui.viewholder

import android.annotation.SuppressLint
import android.view.View
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.remote.models.topicboostergame2.LeaderboardStudent
import com.doubtnutapp.databinding.ItemTbgLeaderboardStudentBinding
import com.doubtnutapp.loadImage
import javax.inject.Inject

/**
 * Created by devansh on 23/06/21.
 */

class LeaderboardViewHolder(itemView: View) :
    BaseViewHolder<IndexedValue<LeaderboardStudent>>(itemView) {

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val binding = ItemTbgLeaderboardStudentBinding.bind(itemView)

    @SuppressLint("SetTextI18n")
    override fun bind(data: IndexedValue<LeaderboardStudent>) {
        with(binding) {
            val student = data.value
            tvRank.text = "${data.index + 4}" //+4 as top 3 items are shown in separate UI at top
            ivStudent.apply {
                loadImage(student.image)
                setOnClickListener {
                    openStudentProfile(student.studentId)
                }
            }
            tvName.apply {
                text = student.name
                setOnClickListener {
                    openStudentProfile(student.studentId)
                }
            }
            tvWins.text = student.subtitle
        }
    }

    private fun openStudentProfile(studentId: String) {

        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.TOPIC_BOOSTER_GAME_PROFILE_VISIT_LEADERBOARD, ignoreSnowplow = true))

        FragmentWrapperActivity.userProfile(
            binding.root.context,
            studentId, "khelo_jeeto"
        )
    }
}