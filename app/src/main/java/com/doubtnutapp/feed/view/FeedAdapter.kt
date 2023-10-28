package com.doubtnutapp.feed.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.base.AutoplayRecyclerViewItem
import com.doubtnutapp.base.AutoplayVideoViewHolder
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnutapp.feed.FeedPostTypes
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.feed.view.viewholders.CreatePostHeaderViewHolder
import com.doubtnutapp.feed.view.viewholders.FeedItemViewHolder
import com.doubtnutapp.feed.view.viewholders.FeedVideoItemViewHolder
import com.doubtnutapp.feed.view.viewholders.FeedWidgetViewHolder
import com.doubtnutapp.feed.view.widgets.FeedPostWidgetModel
import com.doubtnutapp.model.Video
import com.doubtnutapp.widgetmanager.WidgetFactory
import com.doubtnutapp.widgetmanager.WidgetTypes
import javax.inject.Inject

class FeedAdapter(
    val fm: FragmentManager,
    private val isNested: Boolean,
    private val showTopic: Boolean = true,
    private val source: String = Constants.NA,
    val mActionPerformer: ActionPerformer? = null
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    val feedItems: ArrayList<WidgetEntityModel<*, *>> = arrayListOf()

    private var trackingBus: ViewTrackingBus? = null

    private val VIEW_TYPE_CREATE_POST_HEADER = 0
    private val VIEW_TYPE_FEED_POST = 1
    private val VIEW_TYPE_PAID_VIDEO_FEED_POST = 2
    private val widgetMap = hashMapOf<Int, String>()

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var userPreference: UserPreference

    private var showCreatePostHeader: Boolean

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
        showCreatePostHeader = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        if (widgetMap.containsKey(viewType)) {
            return FeedWidgetViewHolder(
                WidgetFactory.createViewHolder(
                    parent.context,
                    parent,
                    widgetMap[viewType]!!,
                    mActionPerformer,
                    source
                )!!
            )
        }
        return when (viewType) {
            VIEW_TYPE_CREATE_POST_HEADER ->
                CreatePostHeaderViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.create_post_header, parent, false),
                    analyticsPublisher, source, userPreference
                ).apply {
                    actionPerformer = mActionPerformer
                }
            VIEW_TYPE_FEED_POST ->
                FeedItemViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_feed, parent, false),
                    fm, isNested, showTopic, source, analyticsPublisher
                )
            VIEW_TYPE_PAID_VIDEO_FEED_POST -> FeedVideoItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_feed_video, parent, false),
                fm, isNested, showTopic, source, analyticsPublisher
            ).apply {
                actionPerformer = this@FeedAdapter.mActionPerformer
            }
            else ->
                FeedItemViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_feed, parent, false),
                    fm, isNested, showTopic, source, analyticsPublisher
                )
        }
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val feedItemPosition = feedItemPosition(position)

        if (feedItemPosition == -1 && getItemViewType(position) == VIEW_TYPE_CREATE_POST_HEADER) {
            (holder as CreatePostHeaderViewHolder).bind()
            return
        }

        val feedItem = feedItems[feedItemPosition]

        when (getItemViewType(position)) {
            VIEW_TYPE_FEED_POST -> (holder as FeedItemViewHolder).bind(
                (feedItem as FeedPostWidgetModel).data,
                feedItemPosition
            )

            VIEW_TYPE_PAID_VIDEO_FEED_POST -> (holder as FeedVideoItemViewHolder).bind(
                (feedItem as FeedPostWidgetModel).data,
                feedItemPosition
            )
        }

        if (widgetMap.containsKey(getItemViewType(position))) {
            (holder as FeedWidgetViewHolder).bind(feedItem)
        }
    }

    override fun getItemCount(): Int {
        return if (showCreatePostHeader) feedItems.size + 1 else feedItems.size
    }

    override fun getItemViewType(position: Int): Int {
        if (showCreatePostHeader && position == 0) return VIEW_TYPE_CREATE_POST_HEADER
        val feedItem = this.feedItems[feedItemPosition(position)]
        return when (feedItem.type) {
            WidgetTypes.WIDGET_TYPE_FEED_POST -> getFeedPostViewType(feedItem)
            else -> feedItem.type.hashCode()
        }
    }

    private fun getFeedPostViewType(feedItem: WidgetEntityModel<*, *>): Int {
        if ((feedItem.data is FeedPostItem) &&
            ((feedItem.data as FeedPostItem).type.equals(FeedPostTypes.TYPE_DN_PAID_VIDEO, true) ||
                    (feedItem.data as FeedPostItem).type.equals(FeedPostTypes.TYPE_DN_VIDEO, true))
        ) {
            return VIEW_TYPE_PAID_VIDEO_FEED_POST
        }

        return VIEW_TYPE_FEED_POST
    }

    fun updateData(feedItems: List<WidgetEntityModel<*, *>>) {
        for (widget in feedItems) {
            this.feedItems.add(widget)
            widgetMap[widget.type.hashCode()] = widget.type
        }
        notifyDataSetChanged()
    }

    fun addItems(feedItems: List<WidgetEntityModel<*, *>>, index: Int) {
        for (widget in feedItems) {
            widgetMap[widget.type.hashCode()] = widget.type
        }
        this.feedItems.addAll(index, feedItems)
        notifyItemRangeInserted(index, feedItems.size)
    }

    fun addItem(feedItem: WidgetEntityModel<*, *>, index: Int) {
        this.feedItems.add(index, feedItem)
        widgetMap[feedItem.type.hashCode()] = feedItem.type
        notifyDataSetChanged()
    }

    fun addPostItem(feedItem: FeedPostItem, index: Int) {
        this.feedItems.add(index, FeedPostWidgetModel(WidgetTypes.WIDGET_TYPE_FEED_POST, feedItem))
        notifyDataSetChanged()
    }

    fun updatePostItem(updatedItem: FeedPostItem) {
        val position = feedItems.indexOfFirst {
            it.type == WidgetTypes.WIDGET_TYPE_FEED_POST &&
                    (it as FeedPostWidgetModel).data.id == updatedItem.id
        }
        if (position != -1) {
            this.feedItems[position] =
                FeedPostWidgetModel(WidgetTypes.WIDGET_TYPE_FEED_POST, updatedItem)
            notifyDataSetChanged()
        }
    }

    fun removeItem(id: String) {
        val feedItemToBeRemoved =
            feedItems.find { it.type == WidgetTypes.WIDGET_TYPE_FEED_POST && (it as FeedPostWidgetModel).data!!.id == id }
        this.feedItems.remove(feedItemToBeRemoved)
        notifyDataSetChanged()
    }

    fun removeItemAt(index: Int) {
        this.feedItems.removeAt(index)
        notifyItemRemoved(index)
    }

    @Deprecated(
        replaceWith = ReplaceWith("removeWidgetUsingType()"),
        message = "Use removeWidgetUsingType() instead"
    )
    fun removeWidget(type: String) {
        updateData(this.feedItems.filterNot {
            it.type == type
        })
    }

    fun removeWidgetUsingType(type: String) {
        this.feedItems.indexOfFirst { it._widgetType == type }
            .takeIf { it >= 0 }
            ?.let {
                removeItemAt(it)
            }
    }

    fun removeWidget(widget: WidgetEntityModel<*, *>) {
        if (feedItems.contains(widget)) {
            val index = feedItems.indexOf(widget)
            feedItems.remove(widget)
            notifyItemRemoved(index)
        }
    }

    fun registerViewTracking(trackingBus: ViewTrackingBus) {
        this.trackingBus = trackingBus
    }

    override fun onViewAttachedToWindow(holder: FeedViewHolder) {
        super.onViewAttachedToWindow(holder)
        trackViewAdded(holder)
        val autoplayHolder = holder as? AutoplayVideoViewHolder<AutoplayFeedItem>
        autoplayHolder?.onStartAutoplay()
    }

    override fun onViewDetachedFromWindow(holder: FeedViewHolder) {
        super.onViewDetachedFromWindow(holder)
        trackViewRemoved(holder)
        val autoplayHolder = holder as? AutoplayVideoViewHolder<AutoplayFeedItem>
        autoplayHolder?.onStopAutoplay()
    }

    private fun feedItemPosition(position: Int): Int {
        if (position == -1) return position
        return if (showCreatePostHeader) position - 1 else position
    }

    private fun trackViewAdded(holder: FeedViewHolder?) {
        if (holder == null) return
        if (holder is FeedItemViewHolder) {
            val position = feedItemPosition(holder.adapterPosition)
            if (position >= 0) {
                (feedItems[position] as? FeedPostWidgetModel)?.let {
                    trackingBus?.trackViewAdded(position, it.data.id,
                        hashMapOf<String, Any>(EventConstants.WIDGET_TYPE to it.type)
                            .apply {
                                putAll(it.extraParams ?: HashMap())
                            })
                }
            }
        } else if (holder is FeedWidgetViewHolder) {
            val position = feedItemPosition(holder.adapterPosition)
            if (position >= 0 && holder.trackingViewId != null) {
                (feedItems[position] as? WidgetEntityModel<*, *>)?.let {
                    trackingBus?.trackViewAdded(position, holder.trackingViewId.orEmpty(),
                        hashMapOf<String, Any>(EventConstants.WIDGET_TYPE to it.type)
                            .apply {
                                putAll(it.extraParams ?: HashMap())
                            }
                    )

                }
            }
        }
    }

    fun trackViewRemoved(holder: FeedViewHolder?) {
        if (holder == null) return
        if (holder is FeedItemViewHolder) {
            val position = feedItemPosition(holder.adapterPosition)
            if (position >= 0) {
                (feedItems[position] as? FeedPostWidgetModel)?.let {
                    trackingBus?.trackViewRemoved(position, it.data.id,
                        hashMapOf<String, Any>(EventConstants.WIDGET_TYPE to it.type)
                            .apply {
                                putAll(it.extraParams ?: HashMap())
                            })
                }
            }
        } else if (holder is FeedWidgetViewHolder) {
            val position = feedItemPosition(holder.adapterPosition)
            if (position >= 0 && holder.trackingViewId != null) {
                (feedItems[position] as? WidgetEntityModel<*, *>)?.let {
                    trackingBus?.trackViewRemoved(position, holder.trackingViewId.orEmpty(),
                        hashMapOf<String, Any>(EventConstants.WIDGET_TYPE to it.type)
                            .apply {
                                putAll(it.extraParams ?: HashMap())
                            })

                }
            }
        }
    }

    fun showCreatePostHeaderView(){
        if (!isNested) {
            showCreatePostHeader = true
            notifyItemChanged(0)
        }
    }

    open class FeedViewHolder(view: View, fm: FragmentManager? = null) :
        AutoplayVideoViewHolder<AutoplayFeedItem>(fm, view) {
        override fun toggleShowAutoplayVideo(showVideo: Boolean, data: AutoplayFeedItem) {
            //handled in FeedAttachmentView
        }

        override fun getAutoplayVideoPage(): String {
            return "COMMUNITY"
        }
    }

    data class AutoplayFeedItem(val feedItem: FeedPostItem) : AutoplayRecyclerViewItem {
        override val videoObj: Video?
            get() = if (feedItem.type == "dn_video" && feedItem.videoObj != null && feedItem.videoObj.autoPlay ?: false) Video.fromVideoObj(
                feedItem.videoObj
            ) else null
        override val viewType: Int
            get() = 1
    }
}