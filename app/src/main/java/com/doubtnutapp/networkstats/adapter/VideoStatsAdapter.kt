package com.doubtnutapp.networkstats.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.utils.DateTimeUtils
import com.doubtnutapp.databinding.ItemVideoNetworkBinding
import com.doubtnutapp.networkstats.models.VideoStatsData
import java.util.concurrent.TimeUnit

class VideoStatsAdapter(
    private val tasks: List<VideoStatsData>,
) :
    ListAdapter<VideoStatsData, VideoStatsAdapter.StatsViewHolder>(DiffUtils()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {

        val binding =
            ItemVideoNetworkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StatsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount() = tasks.size

    inner class StatsViewHolder(private val binding: ItemVideoNetworkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: VideoStatsData) {
            binding.tvVideoName.text = item.videoName
            val data = getFormattedBytesData(item.videoBytes)

            binding.tvDataConsumed.text = (data.first.toString() + data.second)

            binding.tvPlayedOn.text = "Played on " + DateTimeUtils.getEventTimeDate(item.date)

            binding.tvQid.text = "qid: " + item.questionId

            binding.tvSeekTime.text = when {
                item.seekTime in 0..60000 -> {
                    "Video Was Played For " + TimeUnit.MILLISECONDS.toSeconds(item.seekTime) + " seconds"
                }
                item.seekTime > 60000 -> {
                    "Video Was Played For " + TimeUnit.MILLISECONDS.toMinutes(item.seekTime) + " minutes"
                }
                else -> {
                    "Video Was Played For " + TimeUnit.MILLISECONDS.toHours(item.seekTime) + " hours"
                }
            }

            // time spent by the user on the video
            binding.tvEngagementTime.text = when {
                item.engagementTime in 0..60000 -> {
                    "Time Spent on the Video " + TimeUnit.MILLISECONDS.toSeconds(item.engagementTime) + " seconds"
                }
                item.engagementTime > 60000 -> {
                    "Time Spent on the Video " + TimeUnit.MILLISECONDS.toMinutes(item.engagementTime) + " minutes"
                }
                else -> {
                    "Time Spent on the Video " + TimeUnit.MILLISECONDS.toHours(item.engagementTime) + " hours"
                }
            }

            if (item.contentType.isNotEmpty()) {
                binding.tvContentType.visibility = VISIBLE
                binding.tvContentType.text = item.contentType
            } else {
                binding.tvContentType.visibility = GONE
            }
        }
    }

    private fun getFormattedBytesData(data: Long): Pair<Long, String> {
        val unit: String
        val formattedValue: Long
        when {
            data in 1025..1048576 -> {
                formattedValue = (data / 1024)
                unit = "KB"
            }
            data > 1048576 -> {
                formattedValue = (data / 1048576)
                unit = "MB"
            }
            else -> {
                formattedValue = data
                unit = "Bytes"
            }
        }
        return Pair(formattedValue, unit)
    }

    class DiffUtils :
        DiffUtil.ItemCallback<VideoStatsData>() {
        override fun areItemsTheSame(
            oldItem: VideoStatsData,
            newItem: VideoStatsData
        ) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: VideoStatsData,
            newItem: VideoStatsData
        ) =
            oldItem == newItem
    }

}