package com.doubtnutapp.similarVideo.viewholder

import android.view.View
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenCameraFragment
import com.doubtnutapp.databinding.ItemSimilarVideoAskquestionBinding
import com.doubtnutapp.similarVideo.model.AskNowViewItem
import com.doubtnutapp.utils.NetworkUtils
import kotlinx.android.extensions.LayoutContainer

/**
 * Created by pradip on
 * 21, May, 2019
 **/
class AskNowViewHolder(override val containerView: View) :
    BaseViewHolder<AskNowViewItem>(containerView), LayoutContainer, View.OnClickListener {

    val binding = ItemSimilarVideoAskquestionBinding.bind(containerView)

    override fun bind(matchedQuestion: AskNowViewItem) {
        binding.askNowButton.text = containerView.context.getText(matchedQuestion.buttonText)
        binding.askNowButton.setOnClickListener(this)
        binding.askQuestionTitle.text = containerView.context.getText(matchedQuestion.title)
    }

    override fun onClick(v: View?) {
        performAction(OpenCameraFragment)
        sendEvent(EventConstants.EVENT_NAME_ASK_AGAIN_FROM_FEEDBACK)
    }

    private fun sendEvent(@Suppress("SameParameterValue") eventName: String) {
        val context = containerView.context
        context?.apply {
            (context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(context).toString())
                .addStudentId(
                    defaultPrefs(context)
                        .getString(Constants.STUDENT_ID, "").orDefaultValue()
                )
                .addScreenName(EventConstants.PAGE_VIDEO_VIEW_ACTIVITY)
                .track()
        }
    }

}