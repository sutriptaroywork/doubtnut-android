package com.doubtnutapp.gamification.dailyattendance.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.gamification.dailyattendance.model.DailyAttendanceDataModel
import com.doubtnutapp.gamification.dailyattendance.ui.viewholder.CurrentStreakViewHolder

class CurrentStreakListAdapter(private val requiredWidth: Int,
                               private var currentStreakList: List<DailyAttendanceDataModel.CurrentStreakDataModel>,
                               private val actionPerformer: ActionPerformer
)
    : RecyclerView.Adapter<CurrentStreakViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentStreakViewHolder {
        return CurrentStreakViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_current_streak, parent, false), requiredWidth).also {
            it.actionPerformer = this@CurrentStreakListAdapter.actionPerformer
        }
    }


    override fun getItemCount() = currentStreakList.size

    override fun onBindViewHolder(holder: CurrentStreakViewHolder, position: Int) {
        holder.bind(currentStreakList[position])
    }

    fun updateList(items: List<DailyAttendanceDataModel.CurrentStreakDataModel>) {
        currentStreakList = items
    }
}