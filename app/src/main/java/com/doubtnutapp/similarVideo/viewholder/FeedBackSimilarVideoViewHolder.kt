package com.doubtnutapp.similarVideo.viewholder

import android.graphics.Color
import android.view.View
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.addEventNames
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.SubmitFeedBack
import com.doubtnutapp.databinding.ItemSimilarQuestionsFeedbackBinding
import com.doubtnutapp.similarVideo.model.FeedbackSimilarViewItem
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId

/**
 * Created by pradip on
 * 21, May, 2019
 **/
class FeedBackSimilarVideoViewHolder(val binding: ItemSimilarQuestionsFeedbackBinding) :
    BaseViewHolder<FeedbackSimilarViewItem>(binding.root), View.OnClickListener {

    private val FEEDBACK_YES = 1
    private val FEEDBACK_NO = 0

    override fun bind(feedbackViewItem: FeedbackSimilarViewItem) {
        binding.feedbackData = feedbackViewItem
        binding.executePendingBindings()
        color(feedbackViewItem.bgColor)
        binding.feedbackYes.setOnClickListener(this)
        binding.feedbackNo.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.feedbackYes -> {
                performAction(SubmitFeedBack(FEEDBACK_YES, binding.feedbackData?.bgColor))
                sendEvent(EventConstants.FEEDBACK_LIKE_CLICKED)

            }
            binding.feedbackNo -> {
                performAction(SubmitFeedBack(FEEDBACK_NO, binding.feedbackData?.bgColor))
                sendEvent(EventConstants.FEEDBACK_LIKE_CLICKED)

            }
        }
    }

    fun color(bgColor: String) {
        binding.clFeedback.setBackgroundColor(Color.parseColor(bgColor))
    }

    private fun sendEvent(eventName: String) {
        val context = binding.root.context

        context?.apply {
            (context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(context!!).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_VIDEO_VIEW_ACTIVITY)
                .track()
        }
    }

}