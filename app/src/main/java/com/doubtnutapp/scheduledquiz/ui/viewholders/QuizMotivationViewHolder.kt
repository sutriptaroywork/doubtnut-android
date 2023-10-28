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
import com.doubtnutapp.databinding.LayoutQuizMotivationBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.scheduledquiz.di.model.ScheduledQuizNotificationModel
import com.doubtnutapp.utils.UserUtil
import javax.inject.Inject

class QuizMotivationViewHolder(itemView: View) :
    BaseViewHolder<ScheduledQuizNotificationModel>(itemView) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    val binding = LayoutQuizMotivationBinding.bind(itemView)

    override fun bind(data: ScheduledQuizNotificationModel) {
        binding.btnQuizMotivation.text = data.btnText.orEmpty()
        binding.headingQuizMotivation.text = data.heading.orEmpty()

        binding.btnQuizMotivation.setOnClickListener {
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

        binding.rootMotivation.setOnClickListener {
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

        binding.cvThumbnailMotivation.setOnClickListener {
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

        binding.skipMotivation.isVisible = (data.isSkippable == true)

        binding.skipMotivation.setOnClickListener {
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

        binding.thumbnailQuizMotivation.loadImageOrMathView(
            data.imageUrl.orEmpty(),
            binding.mathViewQuizMotivation,
            data.ocrText,
            placeholderId = R.drawable.placeholder_thumbnail_quiz
        )
        binding.headingIconQuizMotivation.loadImageOrMathView(
            data.headingIcon.orEmpty(),
            placeholderId = R.drawable.placeholder_quiz_header
        )
        binding.overlayQuizMotivation.loadImageOrMathView(
            data.overlayImage.orEmpty(),
            placeholderId = R.drawable.placeholder_thumbnail_quiz
        )
        defaultPrefs().edit {
            putString(Constants.QUIZ_CURRENT_DAY, data.currentDay.toString())
        }
    }

}
