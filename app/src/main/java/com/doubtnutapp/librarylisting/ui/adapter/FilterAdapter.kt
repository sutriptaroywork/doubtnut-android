package com.doubtnutapp.librarylisting.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.librarylisting.model.FilterInfo
import com.doubtnutapp.librarylisting.viewholder.FilterViewHolder

/**
 * Created by Anand Gaurav on 2019-10-14.
 */
class FilterAdapter(
    private val tabList: List<FilterInfo>,
    private val actionPerformer: ActionPerformer,
    private val recyclerViewChildren: RecyclerView
) : RecyclerView.Adapter<FilterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        return FilterViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_filter_library,
                parent,
                false
            ), recyclerViewChildren
        ).also {
            it.actionPerformer = this@FilterAdapter.actionPerformer
        }
    }

    override fun getItemCount() = tabList.size

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(tabList[position])
    }

    fun updateTagSelection(position: Int) {
        tabList.forEachIndexed { index, subjectTagViewItem ->
            subjectTagViewItem.isSelected = index == position
        }
        notifyDataSetChanged()
    }
}
