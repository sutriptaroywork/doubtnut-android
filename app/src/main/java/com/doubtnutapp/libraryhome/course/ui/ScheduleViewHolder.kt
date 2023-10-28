package com.doubtnutapp.libraryhome.course.ui

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemScheduleBinding
import com.doubtnutapp.libraryhome.course.data.Schedule

class ScheduleViewHolder(itemView: View) : BaseViewHolder<Schedule>(itemView) {

    val binding = ItemScheduleBinding.bind(itemView)

    override fun bind(data: Schedule) {
        binding.textViewDate.text = data.date.orEmpty()
        binding.textViewDay.text = data.day.orEmpty()
        val scheduleResourceAdapter = ScheduleResourceAdapter(null)
        binding.recyclerView.run {
            adapter = scheduleResourceAdapter
            isNestedScrollingEnabled = false
        }
        scheduleResourceAdapter.updateList(data.resources.orEmpty())
    }

}