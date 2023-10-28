package com.doubtnutapp.feed.view.viewholders

import android.app.ProgressDialog
import android.content.Context
import android.text.util.Linkify
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.PostDeletedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.databinding.ItemFeedBinding
import com.doubtnutapp.feed.FeedPostTypes
import com.doubtnutapp.feed.view.FeedAdapter
import com.doubtnutapp.feed.view.LinkPreviewView
import com.doubtnutapp.feed.view.TopicFeedActivity
import com.doubtnutapp.ui.forum.comments.CommentsActivity
import com.doubtnutapp.utils.BannerActionUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.saket.bettermovementmethod.BetterLinkMovementMethod

class FeedItemViewHolder(
    view: View,
    fm: FragmentManager,
    private val isNested: Boolean,
    private val showTopic: Boolean,
    private val source: String,
    val analyticsPublisher: AnalyticsPublisher
) : FeedAdapter.FeedViewHolder(view, fm) {

    val binding = ItemFeedBinding.bind(itemView)

    fun bind(feedItem: FeedPostItem, feedItemPosition: Int) {

        feedItem.videoObj?.autoPlay =
            feedItem.videoObj?.autoPlay ?: false && (!isNested || source == "post_detail")
        bind(FeedAdapter.AutoplayFeedItem(feedItem))

        if (!feedItem.message.isNullOrEmpty()) {
            binding.layoutUserInfo.tvMessage.show()
            binding.layoutUserInfo.tvMessage.text = feedItem.message
            BetterLinkMovementMethod
                .linkify(Linkify.ALL, binding.layoutUserInfo.tvMessage)
                .setOnLinkClickListener { textView: TextView, url: String ->
                    LinkPreviewView.linkClickAction(textView.context, url)
                }
            if (feedItem.type == "link") {
                feedItem.attachments = arrayListOf()
                if (binding.layoutUserInfo.tvMessage.urls.isNotEmpty()) {
                    feedItem.attachments = arrayListOf(binding.layoutUserInfo.tvMessage.urls[0].url)
                }
            }
        } else binding.layoutUserInfo.tvMessage.hide()

        if (showTopic && feedItem.topic != null && feedItem.topic.isNotEmpty()) {
            binding.tvTopic.show()
            binding.tvTopic.text = feedItem.topic
            binding.tvTopic.setOnClickListener {
                TopicFeedActivity.getStartIntent(it.context, feedItem.topic, true)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_NAME_FEED_POST_TOPIC_CLICK,
                        hashMapOf(Pair(EventConstants.TOPIC, feedItem.topic)),
                        ignoreSnowplow = true
                    )
                )
            }
        } else {
            binding.tvTopic.hide()
        }

        binding.viewFeedAttachment.bindData(feedItem, source)

        if (feedItem.disableLcsfBar) {
            binding.viewPostActions.visibility = View.GONE
        } else {
            binding.viewPostActions.visibility = View.VISIBLE
            binding.viewPostActions.bindData(feedItem, source)
        }

        if (feedItem.featuredComment != null) {
            binding.viewFeedComment.show()
            binding.viewFeedComment.bindData(feedItem.featuredComment, feedItem.id)
        } else binding.viewFeedComment.hide()

        binding.layoutUserInfo.tvTimestamp.text = Utils.formatTime(itemView.context, feedItem.createdAt)


        if (feedItem.type == "poll") {
            binding.viewFeedPoll.show()
            binding.viewFeedPoll.bindData(feedItem)
        } else {
            binding.viewFeedPoll.hide()
        }

        if (feedItem.type == "live") {
            binding.viewLiveInfo.show()
            binding.viewLiveInfo.bindData(feedItem)
        } else {
            binding.viewLiveInfo.hide()
        }

        binding.layoutUserInfo.tvUsername.apply {
            text = feedItem.username
            setOnClickListener {
                FragmentWrapperActivity.userProfile(context, feedItem.studentId, "feed_post")
            }
            if (feedItem.isVerified != null && feedItem.isVerified) {
                Utils.setVerifiedTickTextView(this)
            } else {
                setCompoundDrawables(null, null, null, null)
            }
        }

        binding.layoutUserInfo.ivProfileImage.apply {
            loadImage(
                feedItem.studentImageUrl,
                R.drawable.ic_default_one_to_one_chat,
                R.drawable.ic_default_one_to_one_chat
            )
            setOnClickListener {
                FragmentWrapperActivity.userProfile(context, feedItem.studentId, "feed_post")
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_NAME_FEED_POST_PROFILE_CLICK,
                        ignoreSnowplow = true
                    )
                )
            }
        }

        if (feedItem.studentId == defaultPrefs(binding.root.context).getString(
                Constants.STUDENT_ID,
                ""
            ) || source.contains("home")
        ) {
            binding.layoutUserInfo.btnFollow.hide()
        } else {
            binding.layoutUserInfo.btnFollow.show()
            if (feedItem.followRelationship == 1) {
                binding.layoutUserInfo.btnFollow.isSelected = true
                binding.layoutUserInfo.btnFollow.text =
                    binding.root.context.resources.getString(R.string.following)

            } else {
                binding.layoutUserInfo.btnFollow.isSelected = false
                binding.layoutUserInfo.btnFollow.text =
                    binding.root.context.resources.getString(R.string.follow)
            }

            binding.layoutUserInfo.btnFollow.setOnClickListener {
                if (feedItem.followRelationship != 1) {
                    DataHandler.INSTANCE.teslaRepository.followUser(feedItem.studentId)
                        .subscribeOn(Schedulers.io()).subscribe()
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.EVENT_NAME_FEED_POST_FOLLOW,
                            ignoreSnowplow = true
                        )
                    )
                    binding.layoutUserInfo.btnFollow.isSelected = true
                    feedItem.followRelationship = 1
                    binding.layoutUserInfo.btnFollow.text =
                        binding.root.context.resources.getString(R.string.following)
                } else {
                    DataHandler.INSTANCE.teslaRepository.unfollowUser(feedItem.studentId)
                        .subscribeOn(Schedulers.io()).subscribe()
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.EVENT_NAME_FEED_POST_UNFOLLOW,
                            ignoreSnowplow = true
                        )
                    )
                    binding.layoutUserInfo.btnFollow.isSelected = false
                    feedItem.followRelationship = 0
                    binding.layoutUserInfo.btnFollow.text =
                        binding.root.context.resources.getString(R.string.follow)
                }
            }
        }

        binding.ivSelfProfileImage.loadImage(
            userProfileImage(itemView.context),
            R.drawable.ic_default_one_to_one_chat,
            R.drawable.ic_default_one_to_one_chat
        )

        if (feedItem.button?.actionActivity != null) {
            binding.postButton.show()
            binding.postButton.text = feedItem.button.btnText
            feedItem.button.bgColor?.let {
                binding.postButton.setBackgroundColor(Utils.parseColor(it))
            }
            binding.postButton.setOnClickListener {
                BannerActionUtils.performAction(
                    it.context,
                    feedItem.button.actionActivity,
                    feedItem.button.actionData
                )
            }
        } else binding.postButton.hide()

        if (!feedItem.type.equals(FeedPostTypes.TYPE_DN_ACTIVITY)) {
            if (feedItem.commentCount > 0) {
                binding.tvAllComments.show()
                binding.tvAllComments.setOnClickListener {
                    openComments(itemView.context, feedItem.id, feedItemPosition)
                }
            } else {
                binding.tvAllComments.hide()
            }
            binding.addCommentContainer.setOnClickListener {
                openComments(itemView.context, feedItem.id, feedItemPosition)
            }
            if (feedItem.showComments == true) {
                binding.addCommentContainer.show()
            } else {
                binding.addCommentContainer.hide()
            }
        } else {
            binding.addCommentContainer.hide()
            binding.tvAllComments.hide()
        }

        if (feedItem.studentId != defaultPrefs().getString(
                Constants.STUDENT_ID,
                ""
            ) && feedItem.type.equals(FeedPostTypes.TYPE_DN_ACTIVITY)
        ) {
            binding.layoutUserInfo.overflowMenu.hide()
        } else {
            binding.layoutUserInfo.overflowMenu.show()
            binding.layoutUserInfo.overflowMenu.setOnClickListener {
                showPopUpMenu(feedItem, it)
            }
        }
    }

    private fun showPopUpMenu(feedItem: FeedPostItem, anchor: View) {
        val popupMenu = PopupMenu(anchor.context, anchor)
        val menu = R.menu.menu_feed_post
        popupMenu.inflate(menu)

        popupMenu.show()
        if (feedItem.studentId != defaultPrefs(anchor.context).getString(
                Constants.STUDENT_ID,
                ""
            )
        ) {
            popupMenu.menu.removeItem(R.id.itemDelete)
        }
        if (feedItem.type.equals(FeedPostTypes.TYPE_DN_ACTIVITY)) {
            popupMenu.menu.removeItem(R.id.itemReport)
            popupMenu.menu.removeItem(R.id.itemMute)
        }
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.itemReport -> {
                    DataHandler.INSTANCE.teslaRepository.reportPost(feedItem.id)
                        .subscribeOn(Schedulers.io()).subscribe()
                    showToast(anchor.context, "Post reported")
                    analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_POST_REPORT, ignoreSnowplow = true))
                }
                R.id.itemDelete -> {
                    deletePost(anchor.context, feedItem.id)
                }
                R.id.itemMute -> {
                    DataHandler.INSTANCE.teslaRepository.muteFeedPostNotification(hashMapOf<String, String>().apply {
                        put("entity_id", feedItem.id)
                        put("entity_type", "new_feed_type")
                    }.toRequestBody()).subscribeOn(Schedulers.io()).subscribe()
                    showToast(anchor.context, "Post muted")
                    analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_POST_MUTE, ignoreSnowplow = true))
                }
            }
            true
        }
    }

    private fun deletePost(context: Context, postId: String) {
        AlertDialog.Builder(context)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                val progressDialog: ProgressDialog = ProgressDialog.show(
                    context, "",
                    "Deleting post...", true, true
                )
                DataHandler.INSTANCE.teslaRepository.deletePost(postId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe { data, error ->
                        progressDialog.dismiss()
                        if (error == null) {
                            showToast(context, "Post deleted")
                            DoubtnutApp.INSTANCE.bus()?.send(PostDeletedEvent(postId))
                            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_POST_DELETE, ignoreSnowplow = true))
                        } else {
                            showToast(context, "Error deleting post")
                        }
                    }
                progressDialog.show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openComments(context: Context, entityId: String, position: Int) {
        CommentsActivity.startActivityForResult(
            context as AppCompatActivity,
            entityId,
            "new_feed_type",
            position,
            EventConstants.CATEGORY_FEED,
            null
        )
    }

}
