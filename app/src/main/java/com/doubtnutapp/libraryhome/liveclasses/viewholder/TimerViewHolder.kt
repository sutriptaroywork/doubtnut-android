package com.doubtnutapp.libraryhome.liveclasses.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemLiveClassTimerBinding
import com.doubtnutapp.libraryhome.liveclasses.model.TimerViewItem

class TimerViewHolder(val binding: ItemLiveClassTimerBinding, val timerInterface: TimerInterface?) :
    BaseViewHolder<TimerViewItem>(binding.root) {

    interface TimerInterface {
        fun startTimer(time: String, view: View, title: String)
        fun setToolbarTitle(title: String)
    }

    override fun bind(data: TimerViewItem) {
        binding.timerFeed = data
        binding.totalVideos.text = "${data.title} (${data.videoCount} Videos)"
        timerInterface?.setToolbarTitle(data.title)
        if (data.timer.isNotBlank()) {
            timerInterface?.startTimer(data.timer, binding.root, data.title)
        }
    }
}