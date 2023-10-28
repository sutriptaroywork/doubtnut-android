package com.doubtnutapp.newglobalsearch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.FilterOptionClick
import com.doubtnutapp.databinding.ItemFilterOptionBinding
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilterItem

class FilterOptionAdapter(private val mActionPerformer: ActionPerformer): RecyclerView.Adapter<FilterOptionAdapter.ViewHolder>(){

    private lateinit var actionPerformer : ActionPerformer
    var classes: ArrayList<SearchFilterItem>? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{

        return ViewHolder(DataBindingUtil.inflate<ItemFilterOptionBinding>(LayoutInflater.from(parent.context),
                R.layout.item_filter_option, parent, false)).apply {
                    actionPerformer = mActionPerformer
        }
    }

    override fun getItemCount(): Int {
        return classes?.size ?: 0
    }

    override fun onBindViewHolder(holder:ViewHolder, position: Int) {
        holder.bind(classes!![position],actionPerformer)
    }
    class ViewHolder constructor(var binding: ItemFilterOptionBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: SearchFilterItem, actionPerformer: ActionPerformer) {
            binding.studentclass = course
            binding.executePendingBindings()
            binding.root.setOnClickListener {
                actionPerformer?.performAction(
                        FilterOptionClick(course.key.orEmpty(),course.value.orEmpty())
                )
            }
        }
    }

    fun updateData(classes: ArrayList<SearchFilterItem>) {
        this.classes = classes
        notifyDataSetChanged()
    }


}