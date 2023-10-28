package com.doubtnutapp.videoPage.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ItemVideoFeedbackBinding
import com.doubtnutapp.videoPage.model.VideoDislikeFeedbackOption


class VideoDislikeFeedbackAdapter : RecyclerView.Adapter<VideoDislikeFeedbackAdapter.ViewHolder>() {

    var options: List<VideoDislikeFeedbackOption> = listOf()

    override fun getItemCount(): Int = options.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(DataBindingUtil.inflate<ItemVideoFeedbackBinding>(LayoutInflater.from(parent.context),
                R.layout.item_video_feedback, parent, false))

    override fun onBindViewHolder(holder:ViewHolder, position: Int) = holder.bind(options[position])

    class ViewHolder constructor(var binding: ItemVideoFeedbackBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(option: VideoDislikeFeedbackOption) {
            binding.option = option
            binding.executePendingBindings()
        }

    }

    fun updateData(options: List<VideoDislikeFeedbackOption>) {
        this.options = options
        notifyDataSetChanged()
    }
}