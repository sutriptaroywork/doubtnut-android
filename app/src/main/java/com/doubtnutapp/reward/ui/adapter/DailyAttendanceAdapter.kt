package com.doubtnutapp.reward.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.data.remote.models.reward.AttendanceItem
import com.doubtnutapp.reward.ui.viewholder.DailyAttendanceViewHolder

class DailyAttendanceAdapter(private val actionsPerformer: ActionPerformer?) :
    RecyclerView.Adapter<DailyAttendanceViewHolder>() {

    private val attendanceItems = mutableListOf<AttendanceItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyAttendanceViewHolder {
        return DailyAttendanceViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_daily_attendance, parent, false)
        ).apply { actionPerformer = actionsPerformer }
    }

    override fun onBindViewHolder(holder: DailyAttendanceViewHolder, position: Int) {
        holder.bind(attendanceItems[position])
    }

    override fun getItemCount(): Int = attendanceItems.size

    fun updateList(attendanceItems: List<AttendanceItem>) {
        this.attendanceItems.clear()
        this.attendanceItems.addAll(attendanceItems)
        notifyDataSetChanged()
    }

}