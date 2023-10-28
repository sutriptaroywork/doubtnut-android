package com.doubtnutapp.liveclass.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnVideoOffsetClicked
import com.doubtnutapp.course.widgets.VideoOffsetWidget
import com.doubtnutapp.databinding.ItemVideoTagBinding

class VideoTagsAdapter(
    private val tags: List<VideoOffsetWidget.VideoOffsetItem>? = mutableListOf(),
    private val actionPerformer: ActionPerformer,
    private val analyticsPublisher: AnalyticsPublisher
) : RecyclerView.Adapter<VideoTagsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoTagsViewHolder {
        return VideoTagsViewHolder(
            ItemVideoTagBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VideoTagsViewHolder, position: Int) {
        holder.binding.tvOffset.text = tags?.getOrNull(position)?.offsetTitle
        holder.binding.tvVideoTitle.text = tags?.getOrNull(position)?.title
        holder.binding.mainLayout.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.VIDEO_PAGE_HORIZONTAL_TIMESTAMP_CLICK,
                    hashMapOf<String, Any>()
                )
            )
            actionPerformer.performAction(OnVideoOffsetClicked(tags?.getOrNull(position)?.offset))
        }
    }

    override fun getItemCount(): Int {
        return tags?.size ?: 0
    }
}

class VideoTagsViewHolder(val binding: ItemVideoTagBinding) : RecyclerView.ViewHolder(binding.root)
