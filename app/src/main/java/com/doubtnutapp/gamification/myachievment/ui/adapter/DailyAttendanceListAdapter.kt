package com.doubtnutapp.gamification.myachievment.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.gamification.myachievment.ui.viewholder.DailyAttendanceViewHolder
import com.doubtnutapp.gamification.userProfileData.model.DailyAttendanceDataModel

class DailyAttendanceListAdapter(
    private val requiredWidth: Int,
    private val dailyStreakProgress: List<DailyAttendanceDataModel>,
    private val actionPerformer: ActionPerformer
)
    : RecyclerView.Adapter<DailyAttendanceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyAttendanceViewHolder {
        return DailyAttendanceViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_profile_dailystreak_badgenormal, parent, false), requiredWidth).also {
            it.actionPerformer = this@DailyAttendanceListAdapter.actionPerformer
        }
    }


    override fun getItemCount() = dailyStreakProgress.size

    override fun onBindViewHolder(holder: DailyAttendanceViewHolder, position: Int) {
        holder.bind(dailyStreakProgress[position])
    }
}