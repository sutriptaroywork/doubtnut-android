package com.doubtnutapp.libraryhome.liveclasses.viewholder

import android.view.View
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.ClickAction
import com.doubtnutapp.databinding.ItemLiveClassVideoBinding
import com.doubtnutapp.hide
import com.doubtnutapp.libraryhome.liveclasses.model.VideoViewItem
import com.doubtnutapp.show
import com.doubtnutapp.utils.showToast

class VideoViewHolder(val binding: ItemLiveClassVideoBinding) :
    BaseViewHolder<VideoViewItem>(binding.root) {

    override fun bind(data: VideoViewItem) {
        binding.videoFeed = data

        if (data.duration.isNotEmpty()) {

            binding.ivWatch.show()
            binding.tvTopicMinute.show()

            if (data.duration.toInt() < 60) {
                binding.tvTopicMinute.text =
                    data.duration + " " + binding.root.context.getString(R.string.item_topic_video_second_text_title)
            } else {
                binding.tvTopicMinute.text =
                    Math.ceil((data.duration.toDouble() / 60.toDouble())).toInt()
                        .toString() + " " + binding.root.context.getString(R.string.item_topic_video_minute_text_title)
            }

        } else {
            binding.ivWatch.hide()
            binding.tvTopicMinute.hide()
        }

        if (data.topicList.isEmpty()) {
            binding.topicList.hide()
        } else {
            binding.topicList.show()
            if (data.topicList.isEmpty()) {
                binding.topicListHeader.hide()
            } else {
                binding.topicListHeader.show()
                data.topicList.forEachIndexed { index, s ->
                    binding.topicList.append((index + 1).toString() + ". " + s + "\n")
                }
            }
        }


        when (data.status) {
            1 -> {
                binding.liveClassStatus.visibility = View.VISIBLE
                binding.playCourse.visibility = View.VISIBLE

                binding.ivTestStatus.setImageResource(R.drawable.ic_live_icon)
                binding.tvTestStatus.text = binding.root.context.resources.getString(R.string.live)
                binding.tvTestStatus.setTextColor(binding.root.context.resources.getColor(R.color.color_watch_now))
            }
            2, 3 -> {
                binding.liveClassStatus.visibility = View.VISIBLE
                binding.playCourse.visibility = View.VISIBLE

                binding.ivTestStatus.setImageResource(R.drawable.ic_completed_status)
                binding.tvTestStatus.text = binding.root.resources.getString(R.string.completed)
                binding.tvTestStatus.setTextColor(binding.root.resources.getColor(R.color.color_completed))
            }
            4 -> {
                binding.liveClassStatus.visibility = View.GONE
                binding.playCourse.visibility = View.VISIBLE
            }
            5 -> {
                binding.liveClassStatus.visibility = View.VISIBLE
                binding.playCourse.visibility = View.GONE

                binding.ivTestStatus.setImageResource(R.drawable.ic_topic_upcoming)
                binding.tvTestStatus.text = binding.root.resources.getString(R.string.upcoming)
                binding.tvTestStatus.setTextColor(binding.root.resources.getColor(R.color.color_upcoming))
            }
        }

        binding.root.setOnClickListener {
            when {
                data.status == 5 -> {
                    showToast(
                        binding.root.context,
                        binding.root.context.getString(R.string.coming_soon)
                    )
                }
                else -> {
                    if (data.pageData != null) {
                        data.event?.let {
                            actionPerformer?.performAction(ClickAction(data.event, data.pageData))
                        }
                    }
                }
            }

        }
    }
}
