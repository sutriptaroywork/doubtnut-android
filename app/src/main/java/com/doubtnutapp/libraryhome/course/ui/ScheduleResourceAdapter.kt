package com.doubtnutapp.libraryhome.course.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.libraryhome.course.data.Schedule

@Suppress("UNCHECKED_CAST")
class ScheduleResourceAdapter(private val actionsPerformer: ActionPerformer?) :
    RecyclerView.Adapter<BaseViewHolder<Schedule.Resource>>() {

    val listings = mutableListOf<Schedule.Resource>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<Schedule.Resource> {
        return ScheduleResourceViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_schedule_resource,
                parent, false
            )
        )
            .apply { actionPerformer = actionsPerformer }
    }

    override fun getItemCount(): Int = listings.size

    override fun onBindViewHolder(holder: BaseViewHolder<Schedule.Resource>, position: Int) {
        holder.bind(listings[position])
    }

    fun updateList(recentListings: List<Schedule.Resource>) {
        listings.addAll(recentListings)
        notifyDataSetChanged()
    }
}