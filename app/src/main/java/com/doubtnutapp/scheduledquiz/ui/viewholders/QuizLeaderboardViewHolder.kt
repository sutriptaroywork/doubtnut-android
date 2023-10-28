package com.doubtnutapp.scheduledquiz.ui.viewholders

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.ScheduledQuizNotificationClicked
import com.doubtnutapp.databinding.LayoutQuizLeaderboardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.scheduledquiz.di.model.ScheduledQuizNotificationModel
import com.doubtnutapp.utils.UserUtil
import javax.inject.Inject

class QuizLeaderboardViewHolder(itemView: View) :
    BaseViewHolder<ScheduledQuizNotificationModel>(itemView) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    val binding = LayoutQuizLeaderboardBinding.bind(itemView)

    val rankOnePosition = 0
    val rankTwoPosition = 1
    val rankThreePosition = 2

    override fun bind(data: ScheduledQuizNotificationModel) {
        binding.btnQuizLeaderboard.text = data.btnText.orEmpty()
        binding.headingQuizLeaderboard.text = data.heading.orEmpty()

        binding.btnQuizLeaderboard.setOnClickListener {
            deeplinkAction.performAction(itemView.context, data.deeplink, Bundle().apply {
                putBoolean(Constants.CLEAR_TASK, false)
                putString(Constants.SOURCE, EventConstants.PAGE_DEEPLINK_CLICK)
            })
            performAction(
                ScheduledQuizNotificationClicked(
                    EventConstants.CLICK_QUIZ_NOTIFICATION, hashMapOf(
                        EventConstants.QUIZ_NOTIFICATION_TITLE to data.heading.orDefaultValue("new_quiz"),
                        EventConstants.CLICK_QUIZ_TYPE to "cta"
                    )
                )
            )
            performAction(
                ScheduledQuizNotificationClicked(
                    EventConstants.CLICK_QUIZ_EVENT_V2, hashMapOf(
                        EventConstants.QUIZ_CLICKED_TYPE to "new",
                        EventConstants.QUIZ_NOTIFICATION_TITLE to data.heading.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId()
                    )
                )
            )
            (itemView.context as Activity).finish()
        }
        binding.rootLeaderboard.setOnClickListener {
            performAction(
                ScheduledQuizNotificationClicked(
                    EventConstants.CLICK_QUIZ_EVENT_V2, hashMapOf(
                        EventConstants.QUIZ_CLICKED_TYPE to "new",
                        EventConstants.QUIZ_NOTIFICATION_TITLE to data.heading.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId()
                    )
                )
            )
        }
        binding.skipQuizLeaderboard.isVisible = (data.isSkippable == true)

        binding.skipQuizLeaderboard.setOnClickListener {
            performAction(
                ScheduledQuizNotificationClicked(
                    EventConstants.CLICK_QUIZ_NOTIFICATION, hashMapOf(
                        EventConstants.QUIZ_NOTIFICATION_TITLE to data.heading.orDefaultValue("new_quiz"),
                        EventConstants.CLICK_QUIZ_TYPE to "skip"
                    )
                )
            )
            performAction(
                ScheduledQuizNotificationClicked(
                    EventConstants.CLICK_QUIZ_EVENT_V2, hashMapOf(
                        EventConstants.QUIZ_CLICKED_TYPE to "new",
                        EventConstants.QUIZ_NOTIFICATION_TITLE to data.heading.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId()
                    )
                )
            )
            (itemView.context as Activity).finish()
        }

        binding.headingIconQuizLeaderboard.loadImageOrMathView(
            data.headingIcon.orEmpty(),
            placeholderId = R.drawable.placeholder_quiz_header
        )
        if (data.profiles != null) {
            for (rank in rankOnePosition..(data.profiles.size ?: rankOnePosition)) {
                when (rank) {
                    rankOnePosition -> {
                        binding.ivRankOne.loadImageOrMathView(data.profiles[rank].profileUrl.orEmpty())
                        binding.tvNameRankOne.text = data.profiles[rank].name
                    }
                    rankTwoPosition -> {
                        binding.ivRankTwo.loadImageOrMathView(data.profiles[rank].profileUrl.orEmpty())
                        binding.tvNameRankTwo.text = data.profiles[rank].name
                    }
                    rankThreePosition -> {
                        binding.ivRankThree.loadImageOrMathView(data.profiles[rank].profileUrl.orEmpty())
                        binding.tvNameRankThree.text = data.profiles[rank].name
                    }
                }
            }
        }
        defaultPrefs().edit {
            putString(Constants.QUIZ_CURRENT_DAY, data.currentDay.toString())
        }

    }

}
