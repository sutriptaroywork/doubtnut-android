package com.doubtnutapp.feed.view.viewholders

import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.HandleDeeplink
import com.doubtnutapp.base.UnbanRequested
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.feed.view.CreatePostActivity
import com.doubtnutapp.feed.view.FeedAdapter
import com.doubtnutapp.profile.social.CommunityGuidelinesActivity
import com.doubtnutapp.ui.userstatus.CreateStatusActivity
import kotlinx.android.synthetic.main.create_post_header.view.*
import kotlinx.android.synthetic.main.layout_feed_ban_state.view.*
import kotlinx.android.synthetic.main.layout_feed_unban_rejected.view.*

class CreatePostHeaderViewHolder(
    view: View,
    val analyticsPublisher: AnalyticsPublisher,
    val source: String?,
    private val userPreference: UserPreference
) : FeedAdapter.FeedViewHolder(view) {

    private var isUserBanned = false

    fun bind() {

        isUserBanned = defaultPrefs().getBoolean(Constants.USER_COMMUNITY_BAN, false)

        if (isUserBanned) {
            itemView.createPostHeader.hide()
            itemView.createPostOption.hide()
            setupBanStatus()
        } else {
            itemView.createPostHeader.show()
            itemView.createPostOption.show()
            itemView.layoutUnban.hide()
            itemView.layoutUnbanRejected.hide()
            itemView.layoutUnbanInReview.hide()

            val studyGroupData = userPreference.getStudyGroupData()
            val isStudyGroupFeatureEnabled = studyGroupData != null
            if (isStudyGroupFeatureEnabled) {
                itemView.layoutCreateStudyGroup.show()
                itemView.layoutCreateStudyGroup.setOnClickListener {
                    val deeplink = studyGroupData?.deeplink ?: return@setOnClickListener
                    actionPerformer?.performAction(HandleDeeplink(deeplink))
                }
            } else {
                itemView.layoutCreateStudyGroup.hide()
            }

            itemView.layoutCreateStory.setOnClickListener {
                CreateStatusActivity.getStartIntent(itemView.context, "CreatePostHeader").apply {
                    itemView.context.startActivity(this)
                }
            }
        }

        itemView.layoutCreatePost.setOnClickListener {
            openCreatePostScreen("top_tray")
        }

    }

    private fun showCommunityGuideLines() {
        itemView.context.startActivity(CommunityGuidelinesActivity.getIntent(itemView.context, source = source))
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_COMMUNITY_GUIDELINE_CLICK,
                EventConstants.SOURCE, "feed_header", ignoreSnowplow = true))
    }

    private fun setupBanStatus() {
        val unbanRequestState = defaultPrefs().getString(Constants.USER_UNABN_REQUEST_STATE, "")
        when (unbanRequestState) {
            "Request Reviewed" -> {
                itemView.layoutUnban.hide()
                itemView.layoutUnbanRejected.show()
                itemView.layoutUnbanInReview.hide()
                val communityGuidelinesSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) { /* do something */
                        showCommunityGuideLines()
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                        ds.color = ContextCompat.getColor(itemView.context, R.color.Black)
                        ds.density = 2.0f
                    }
                }
                val text: String = itemView.layoutUnbanRejected.tvRejectedUnbanMessage.text.toString()
                val spanBuilder = SpannableString(text)
                spanBuilder.setSpan(communityGuidelinesSpan, text.indexOf("View Community Guidelines."), text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                itemView.layoutUnbanRejected.tvRejectedUnbanMessage.text = spanBuilder
                itemView.layoutUnbanRejected.tvRejectedUnbanMessage.movementMethod = LinkMovementMethod.getInstance();
            }

            "Request under Review" -> {
                itemView.layoutUnban.hide()
                itemView.layoutUnbanRejected.hide()
                itemView.layoutUnbanInReview.show()
            }
            else -> {
                itemView.layoutUnban.show()
                itemView.layoutUnbanRejected.hide()
                itemView.layoutUnbanInReview.hide()
                itemView.layoutUnban.tvRequestUnban.setOnClickListener {
                    analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.REQUEST_TO_REMOVE_BAN_CLICK, hashMapOf(Pair(EventConstants.SOURCE, source.orEmpty())),
                    ignoreSnowplow = true))
                    performAction(UnbanRequested)
                }
            }
        }
    }

    private fun openCreatePostScreen(source: String, action: String? = null) {
        if (!isUserBanned) {
            if (action.isNullOrEmpty()) {
                CreatePostActivity.startActivity(itemView.context)
            } else {
                CreatePostActivity.startActivity(itemView.context, action)
            }
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.POST_CREATE_CLICK,
                    hashMapOf(Pair(EventConstants.SOURCE, source)), ignoreSnowplow = true))
        }
    }
}
