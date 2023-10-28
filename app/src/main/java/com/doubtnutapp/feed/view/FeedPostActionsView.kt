package com.doubtnutapp.feed.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnutapp.sharing.FEED_POST_CHANNEL
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.toBoolean
import com.doubtnutapp.ui.forum.comments.CommentsActivity
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.view_post_actions.view.*
import javax.inject.Inject

class FeedPostActionsView(ctx: Context, attrs: AttributeSet) : LinearLayout(ctx, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_post_actions, this, true)
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var whatsAppSharing: WhatsAppSharing

    fun bindData(feedItem: FeedPostItem, source: String = Constants.NA) {
        btnLike.text = "${feedItem.likeCount} Likes"
        btnComment.text = feedItem.commentCount.toString() + " Comments"
        //tvStarCount.text = feedItem.bookmarkCount.toString()
//        btnShare.text = feedItem.shareCount.toString() + " Shares"

        btnLike.isSelected = feedItem.isLiked.toBoolean()
        btnStar.isSelected = feedItem.isStarred.toBoolean()

        btnStar.setOnClickListener {
            if (!btnStar.isSelected) {
                btnStar.isSelected = true
                //tvStarCount.text = (tvStarCount.text.toString().toInt() + 1).toString()
                DataHandler.INSTANCE.teslaRepository.starPost(feedItem.id, feedItem.topic)
                    .subscribeOn(Schedulers.io()).subscribe()
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_NAME_FEED_POST_STAR,
                        hashMapOf(Pair(Constants.SOURCE, source))
                    )
                )
                analyticsPublisher.publishEvent(
                    StructuredEvent(
                        EventConstants.CATEGORY_FEED,
                        EventConstants.EVENT_NAME_FEED_POST_STAR, label = feedItem.id,
                        eventParams = hashMapOf(Pair(Constants.SOURCE, source))
                    )
                )
            } else {
                btnStar.isSelected = false
                //tvStarCount.text = (tvStarCount.text.toString().toInt() - 1).toString()
                DataHandler.INSTANCE.teslaRepository.unstarPost(feedItem.id, feedItem.topic)
                    .subscribeOn(Schedulers.io()).subscribe()
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_NAME_FEED_POST_UNSTAR,
                        hashMapOf(Pair(Constants.SOURCE, source))
                    )
                )
                analyticsPublisher.publishEvent(
                    StructuredEvent(
                        EventConstants.CATEGORY_FEED,
                        EventConstants.EVENT_NAME_FEED_POST_UNSTAR, label = feedItem.id,
                        eventParams = hashMapOf(Pair(Constants.SOURCE, source))
                    )
                )
            }
        }
        btnLike.setOnClickListener {
            if (!btnLike.isSelected) {
                btnLike.isSelected = true
                feedItem.likeCount = feedItem.likeCount + 1
                btnLike.text = "${feedItem.likeCount} Likes"
                DataHandler.INSTANCE.teslaRepository.likePost(feedItem.id, feedItem.topic)
                    .subscribeOn(Schedulers.io()).subscribe()
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_NAME_FEED_POST_LIKE,
                        hashMapOf(Pair(Constants.SOURCE, source))
                    )
                )
                analyticsPublisher.publishEvent(
                    StructuredEvent(
                        EventConstants.CATEGORY_FEED,
                        EventConstants.EVENT_NAME_FEED_POST_LIKE, label = feedItem.id,
                        eventParams = hashMapOf(Pair(Constants.SOURCE, source))
                    )
                )
            } else {
                btnLike.isSelected = false
                feedItem.likeCount = feedItem.likeCount - 1
                btnLike.text = "${feedItem.likeCount} Likes"
                DataHandler.INSTANCE.teslaRepository.unlikePost(feedItem.id, feedItem.topic)
                    .subscribeOn(Schedulers.io()).subscribe()
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_NAME_FEED_POST_UNLIKE,
                        hashMapOf(Pair(Constants.SOURCE, source))
                    )
                )
                analyticsPublisher.publishEvent(
                    StructuredEvent(
                        EventConstants.CATEGORY_FEED,
                        EventConstants.EVENT_NAME_FEED_POST_UNLIKE, label = feedItem.id,
                        eventParams = hashMapOf(Pair(Constants.SOURCE, source))
                    )
                )
            }
        }

        btnComment.setOnClickListener {
            openComments(feedItem.id, source)
        }
        btnShare.setOnClickListener {
            whatsAppSharing.shareOnWhatsApp(
                ShareOnWhatApp(
                    FEED_POST_CHANNEL,
                    featureType = Constants.FEATURE_TYPE_FEED_POST,
                    imageUrl = if (feedItem.type == "image") feedItem.cdnPath + feedItem.attachments[0] else "",
                    controlParams = hashMapOf(
                        Constants.POST_ID to feedItem.id
                    ),
                    bgColor = "#000000",
                    sharingMessage = "Hey! check out this post on Doubtnut -\n${feedItem.message}",
                    questionId = feedItem.id
                )
            )
            whatsAppSharing.startShare(context)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.EVENT_NAME_FEED_POST_SHARE,
                    hashMapOf(Pair(Constants.SOURCE, source))
                )
            )
            analyticsPublisher.publishEvent(
                StructuredEvent(
                    EventConstants.CATEGORY_FEED,
                    EventConstants.EVENT_NAME_FEED_POST_SHARE, label = feedItem.id,
                    eventParams = hashMapOf(Pair(Constants.SOURCE, source))
                )
            )
        }
    }

    private fun openComments(id: String, source: String) {
        CommentsActivity.startActivityForResult(
            context as AppCompatActivity,
            id,
            "new_feed_type",
            0,
            EventConstants.CATEGORY_FEED,
            null
        )
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_NAME_FEED_POST_COMMENT_VIEW,
                hashMapOf(Pair(Constants.SOURCE, source)), ignoreSnowplow = true
            )
        )
    }
}