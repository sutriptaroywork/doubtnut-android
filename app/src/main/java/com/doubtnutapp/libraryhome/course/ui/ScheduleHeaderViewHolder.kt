package com.doubtnutapp.libraryhome.course.ui

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemScheduleHeaderBinding
import com.doubtnutapp.libraryhome.course.data.ScheduleHeader

class ScheduleHeaderViewHolder(itemView: View) : BaseViewHolder<ScheduleHeader>(itemView) {

    val binding = ItemScheduleHeaderBinding.bind(itemView)

    override fun bind(data: ScheduleHeader) {
        binding.textView.text = data.title.orEmpty()
    }

}