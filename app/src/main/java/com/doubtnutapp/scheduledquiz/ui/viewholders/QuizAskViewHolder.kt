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
import com.doubtnutapp.databinding.LayoutAskQuestionBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.scheduledquiz.di.model.ScheduledQuizNotificationModel
import com.doubtnutapp.utils.UserUtil
import javax.inject.Inject

class QuizAskViewHolder(itemView: View) : BaseViewHolder<ScheduledQuizNotificationModel>(itemView) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val MIDDLE_IMAGE_POSITION = 0
        const val RIGHTMOST_IMAGE_POSITION = 1
        const val LEFTMOST_IMAGE_POSITION = 2
        const val SECOND_LEFT_IMAGE_POSITION = 3
        const val SECOND_RIGHT_IMAGE_POSITION = 4
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    val binding = LayoutAskQuestionBinding.bind(itemView)

    override fun bind(data: ScheduledQuizNotificationModel) {
        binding.headingQuizAsk.text = data.heading.orEmpty()
        binding.btnQuizAsk.text = data.btnText.orEmpty()
        binding.subtitleQuizAsk.text = data.subTitle.orEmpty()
        binding.titleQuizAsk.text = data.title.orEmpty()

        binding.rootAsk.setOnClickListener {
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
        binding.skipQuizAsk.isVisible = (data.isSkippable == true)

        binding.skipQuizAsk.setOnClickListener {
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
        binding.btnQuizAsk.setOnClickListener {
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
        if (data.imageUrls != null) {
            val imageListSize = data.imageUrls.size - 1
            for (i in 0..imageListSize) {
                when (i) {
                    MIDDLE_IMAGE_POSITION -> {
                        binding.ivAskMiddle.loadImageOrMathView(data.imageUrls[i].orEmpty())
                    }
                    SECOND_RIGHT_IMAGE_POSITION -> {
                        binding.ivAsk2.loadImageOrMathView(data.imageUrls[i].orEmpty())
                    }
                    SECOND_LEFT_IMAGE_POSITION -> {
                        binding.ivAsk2Left.loadImageOrMathView(data.imageUrls[i].orEmpty())
                    }
                    RIGHTMOST_IMAGE_POSITION -> {
                        binding.ivAsk3.loadImageOrMathView(data.imageUrls[i].orEmpty())
                    }
                    LEFTMOST_IMAGE_POSITION -> {
                        binding.ivAsk3Left.loadImageOrMathView(data.imageUrls[i].orEmpty())
                    }
                }
            }
        }

        binding.headingIconQuizAsk.loadImageOrMathView(
            data.headingIcon.orEmpty(),
            placeholderId = R.drawable.placeholder_quiz_header
        )
        defaultPrefs().edit {
            putString(Constants.QUIZ_CURRENT_DAY, data.currentDay.toString())
        }
    }

}
