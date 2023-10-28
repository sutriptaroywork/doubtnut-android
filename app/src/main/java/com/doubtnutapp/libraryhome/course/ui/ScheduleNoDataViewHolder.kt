package com.doubtnutapp.libraryhome.course.ui

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemScheduleNoDataBinding
import com.doubtnutapp.libraryhome.course.data.ScheduleNoData

class ScheduleNoDataViewHolder(itemView: View) : BaseViewHolder<ScheduleNoData>(itemView) {

    val binding = ItemScheduleNoDataBinding.bind(itemView)

    override fun bind(data: ScheduleNoData) {
        binding.textView.text = data.title.orEmpty()
    }

}