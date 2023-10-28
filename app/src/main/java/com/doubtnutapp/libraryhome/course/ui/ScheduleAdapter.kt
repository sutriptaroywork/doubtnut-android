package com.doubtnutapp.libraryhome.course.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.libraryhome.course.data.Schedule
import com.doubtnutapp.libraryhome.course.data.ScheduleHeader
import com.doubtnutapp.libraryhome.course.data.ScheduleNoData

@Suppress("UNCHECKED_CAST")
class ScheduleAdapter(private val actionsPerformer: ActionPerformer?) :
    RecyclerView.Adapter<BaseViewHolder<Any>>() {

    val listings = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Any> {
        return when (viewType) {
            R.layout.item_schedule -> {
                ScheduleViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_schedule,
                        parent, false
                    )
                )
                    .apply { actionPerformer = actionsPerformer } as BaseViewHolder<Any>
            }
            R.layout.item_schedule_header -> {
                ScheduleHeaderViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_schedule_header,
                        parent, false
                    )
                )
                    .apply { actionPerformer = actionsPerformer } as BaseViewHolder<Any>
            }
            R.layout.item_schedule_no_data -> {
                ScheduleNoDataViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_schedule_no_data,
                        parent, false
                    )
                )
                    .apply { actionPerformer = actionsPerformer } as BaseViewHolder<Any>
            }
            else -> {
                throw IllegalArgumentException("Invalid viewType")
            }
        }
    }

    override fun getItemCount(): Int = listings.size

    override fun getItemViewType(position: Int): Int {
        return when (val item = listings[position]) {
            is Schedule -> R.layout.item_schedule
            is ScheduleHeader -> R.layout.item_schedule_header
            is ScheduleNoData -> R.layout.item_schedule_no_data
            else -> throw IllegalStateException("Unknown Item type: ${item::class.java.simpleName}")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<Any>, position: Int) {
        holder.bind(listings[position])
    }

    fun updateList(recentListings: List<Any>) {
        listings.addAll(recentListings)
        notifyDataSetChanged()
    }

}