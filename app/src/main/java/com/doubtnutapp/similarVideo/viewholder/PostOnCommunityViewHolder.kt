package com.doubtnutapp.similarVideo.viewholder

import android.view.View
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.PostQuestion
import com.doubtnutapp.databinding.ItemSimilarVideoPostcommunityBinding
import com.doubtnutapp.similarVideo.model.PostCommunityViewItem
import com.doubtnutapp.utils.NetworkUtils
import kotlinx.android.extensions.LayoutContainer

/**
 * Created by pradip on
 * 21, May, 2019
 **/
class PostOnCommunityViewHolder(override val containerView: View) :
    BaseViewHolder<PostCommunityViewItem>(containerView), LayoutContainer, View.OnClickListener {

    val binding = ItemSimilarVideoPostcommunityBinding.bind(itemView)

    override fun bind(matchedQuestion: PostCommunityViewItem) {
        binding.postOnCommunityButton.text =
            containerView.context.getText(matchedQuestion.buttonText)
        binding.postOnCommunityButton.setOnClickListener(this)

        binding.postOnCommunityTitle.text =
            containerView.context.getText(matchedQuestion.title)
    }

    override fun onClick(v: View?) {
        performAction(PostQuestion)
        sendEvent(EventConstants.EVENT_NAME_POST_IN_COMMUNITY_CLICK)
    }

    private fun sendEvent(eventName: String) {
        val context = containerView.context

        context?.apply {
            (context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(context!!).toString())
                .addStudentId(
                    defaultPrefs(context!!)
                        .getString(Constants.STUDENT_ID, "").orDefaultValue()
                )
                .addScreenName(EventConstants.PAGE_MATCH_ACTIVITY)
                .track()
        }
    }

}