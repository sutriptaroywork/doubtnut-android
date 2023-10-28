package com.doubtnutapp.payment.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.domain.payment.entities.BreakThrough

/**
 * Created by Anand Gaurav on 2020-01-22.
 */
class BreakThroughAdapter(private val actionsPerformer: ActionPerformer?) : RecyclerView.Adapter<BaseViewHolder<BreakThrough>>() {

    val listings = mutableListOf<BreakThrough>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<BreakThrough> {
        return (BreakThroughViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_break_through, parent, false)
        ) as BaseViewHolder<BreakThrough>).also {
            it.actionPerformer = actionsPerformer
        }
    }

    override fun getItemCount(): Int = listings.size

    override fun onBindViewHolder(holder: BaseViewHolder<BreakThrough>, position: Int) {
        holder.bind(listings[position])
    }

    fun updateList(recentListings: List<BreakThrough>) {
        listings.clear()
        listings.addAll(recentListings)
        notifyDataSetChanged()
    }
}