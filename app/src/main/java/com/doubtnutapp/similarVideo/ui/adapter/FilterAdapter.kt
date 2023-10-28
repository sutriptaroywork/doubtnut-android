package com.doubtnutapp.similarVideo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.matchquestion.model.SubjectTabViewItem
import com.doubtnutapp.similarVideo.ui.FilterViewHolder

/**
 * Created by Anand Gaurav on 2019-12-02.
 */
class FilterAdapter(
    private val tabList: List<SubjectTabViewItem>,
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