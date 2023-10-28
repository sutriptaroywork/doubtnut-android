package com.doubtnutapp.feed.view

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnutapp.live.ui.JoinLivePaymentDialog
import com.doubtnutapp.live.ui.LiveActivity
import com.doubtnutapp.utils.showToast
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.feed_live_info_view.view.*
import javax.inject.Inject

class FeedLiveInfoView(ctx: Context, attrs: AttributeSet) : LinearLayout(ctx, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.feed_live_info_view, this, true)
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    fun bindData(feedItem: FeedPostItem) {
        if (feedItem.isLive && !feedItem.isEnded) {
            // currently live
            tvLive.text = "Live Now"
            tvLiveInfo.text = "${feedItem.viewerCount} watching now"
            tvLiveInfo.setCompoundDrawables(context.resources.getDrawable(R.drawable.ic_viewer_black),
                    null, null, null)

            btnJoinLive.text = "Join Now"

            if (feedItem.isPaid) {
                if (!feedItem.isBooked) {
                    btnJoinLive.setOnClickListener {
                        bookLiveStream(feedItem)
                    }
                } else {
                    btnJoinLive.setOnClickListener {
                        joinLiveStream(feedItem)
                    }
                }
            } else {
                btnJoinLive.setOnClickListener {
                    joinLiveStream(feedItem)
                }
            }
        } else if (feedItem.isEnded) {
            // live session has finished
            tvLive.text = "Live session ended"
            tvLiveInfo.text = "${feedItem.viewerCount} viewers"
            tvLiveInfo.setCompoundDrawables(context.resources.getDrawable(R.drawable.ic_viewer_black),
                    null, null, null)

            btnJoinLive.text = "Watch"

            if (feedItem.isPaid) {
                if (feedItem.isBooked) {
                    btnJoinLive.setOnClickListener {
                        joinLiveStream(feedItem)
                    }
                } else {
                    btnJoinLive.setOnClickListener {
                        bookLiveStream(feedItem)
                    }
                }
            } else {
                btnJoinLive.setOnClickListener {
                    joinLiveStream(feedItem)
                }
            }
        } else {
            // scheduled and not started yet
            if (feedItem.streamDate != null)
                tvLive.text = "${feedItem.streamDate} at ${feedItem.streamStartTime}"

            tvLiveInfo.setCompoundDrawables(null, null, null, null)


            if (feedItem.isPaid) {
                // scheduled and paid, use can book the session by paying
                tvLiveInfo.text = "${feedItem.bookedCount} booked"
                if (!feedItem.isBooked) {
                    btnJoinLive.text = "Book Now"
                    btnJoinLive.setOnClickListener {
                        bookLiveStream(feedItem)
                    }
                } else {
                    btnJoinLive.text = "Live session booked"
                    btnJoinLive.setOnClickListener {
                        notifyStreamTime(feedItem)
                    }
                }
            } else {
                // scheduled and free, user can bookmark the session
                tvLiveInfo.text = "${feedItem.bookmarkCount} booked"
                if (feedItem.isStarred.toBoolean()) {
                    btnJoinLive.text = "Bookmarked"
                    btnJoinLive.setOnClickListener {
                        notifyStreamTime(feedItem)
                    }
                } else {
                    btnJoinLive.text = "Bookmark"
                    btnJoinLive.setOnClickListener {
                        bookmarkLiveSession(feedItem)
                    }
                }
            }
        }
        if (feedItem.isPaid) {
            tvPrice.text = "₹ ${feedItem.streamFee}"
        } else {
            tvPrice.text = "FREE"
        }

        if (feedItem.studentId == defaultPrefs().getString(Constants.STUDENT_ID, "")) {
            if (feedItem.isEnded) {
                btnJoinLive.text = "Watch"
                btnJoinLive.setOnClickListener {
                    joinLiveStream(feedItem)
                }
            } else {
                btnJoinLive.text = "Start live session"
                btnJoinLive.setOnClickListener {
                    startLiveStreamPublish(feedItem)
                }
            }
        }
    }

    private fun joinLiveStream(feedItem: FeedPostItem) {
        val streamUrl = (if (feedItem.isEnded) feedItem.vodLink else feedItem.streamLink)
        if (streamUrl == null || streamUrl.isEmpty()) {
            showToast(context, "Video is currently being generated and will be available soon.")
            return
        }
        startLiveStreamJoin(feedItem.id, feedItem.isEnded, streamUrl)
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_LIVE_POST_BUTTON_CLICK,
                EventConstants.TYPE, if (feedItem.isEnded) "watch" else "join"))
    }

    private fun bookLiveStream(feedItem: FeedPostItem) {
        JoinLivePaymentDialog(feedItem) {
            // bookmark the post also in case user books live stream
            DataHandler.INSTANCE.teslaRepository.starPost(feedItem.id, feedItem.topic).subscribeOn(Schedulers.io()).subscribe()
            bindData(feedItem.apply {
                isStarred = 1
                bookmarkCount += 1
                isBooked = true
            })
            if (feedItem.isLive) {
                showToast(context, "Payment successful, joining live stream")
                startLiveStreamJoin(feedItem.id, feedItem.isEnded, feedItem.streamLink!!)
            } else {
                showToast(context, "Payment successful, live session booked")
            }
        }.show((context as FragmentActivity).supportFragmentManager,
                "JoinLivePayment")
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_LIVE_POST_BUTTON_CLICK,
                EventConstants.TYPE, "book"))
    }

    private fun notifyStreamTime(feedItem: FeedPostItem) {
        if (feedItem.streamDate != null) {
            showToast(context, "Live session will start on ${feedItem.streamDate} at ${feedItem.streamStartTime}")
        } else {
            showToast(context, "Live session has not started yet." +
                    " We will notify you once the live session starts.")
        }
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_LIVE_POST_BUTTON_CLICK,
                EventConstants.TYPE, "notify_time"))
    }

    private fun bookmarkLiveSession(feedItem: FeedPostItem) {
        DataHandler.INSTANCE.teslaRepository.starPost(feedItem.id, feedItem.topic).subscribeOn(Schedulers.io()).subscribe()
        bindData(feedItem.apply {
            isStarred = 1
            bookmarkCount += 1
        })
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_LIVE_POST_BUTTON_CLICK,
                EventConstants.TYPE, "bookmark"))
    }

    private fun startLiveStreamPublish(feedItem: FeedPostItem) {
        context.startActivity(LiveActivity.getStartIntent(context, LiveActivity.TYPE_LIVE_STREAM_PUBLISH,
                Bundle().apply { putString(Constants.POST_ID, feedItem.id) }))
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_LIVE_POST_BUTTON_CLICK,
                EventConstants.TYPE, "start"))
    }

    private fun startLiveStreamJoin(postId: String, isEnded: Boolean, url: String) {
        context.startActivity(LiveActivity.getStartIntent(context, LiveActivity.TYPE_LIVE_STREAM,
                Bundle().apply {
                    putString(Constants.POST_ID, postId)
                    putBoolean(Constants.IS_VOD, isEnded)
                    putString(Constants.URL, url)
                }))
    }

}