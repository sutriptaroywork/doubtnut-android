package com.doubtnutapp.payment.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.domain.payment.entities.DoubtPackageInfo

/**
 * Created by Anand Gaurav on 2020-01-21.
 */
class ChoosePlanAdapter(private val actionsPerformer: ActionPerformer?)
    : RecyclerView.Adapter<BaseViewHolder<DoubtPackageInfo>>() {

    val listings = mutableListOf<DoubtPackageInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<DoubtPackageInfo> {

        return if (viewType == 1) {
            (ChoosePlanSelectedViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_choose_plan_selected, parent, false)
            ) as BaseViewHolder<DoubtPackageInfo>).also {
                it.actionPerformer = actionsPerformer
            }
        } else {
            (ChoosePlanViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_choose_plan_doubt, parent, false)
            ) as BaseViewHolder<DoubtPackageInfo>).also {
                it.actionPerformer = actionsPerformer
            }
        }
    }

    override fun getItemCount(): Int = listings.size

    override fun getItemViewType(position: Int): Int = if (listings[position].selected != null && listings[position].selected == true) {
        1
    } else {
        2
    }


    override fun onBindViewHolder(holder: BaseViewHolder<DoubtPackageInfo>, position: Int) {
        holder.bind(listings[position])
    }

    fun updateList(recentListings: List<DoubtPackageInfo>) {
        listings.clear()
        listings.addAll(recentListings)
        notifyDataSetChanged()
    }

    fun updatePackageOnSelection(packageInfo: DoubtPackageInfo) {
        listings.forEach {
            it.selected = it.id == packageInfo.id
        }
        notifyDataSetChanged()
    }
}