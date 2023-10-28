package com.doubtnutapp.newglobalsearch.ui.adapter

import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R
import com.doubtnutapp.base.RemoveFilter
import com.doubtnutapp.databinding.ItemAppliedIasFilterBinding
import com.doubtnutapp.hide
import com.doubtnutapp.show

class IasAppliedFilterAdapter(private val actionPerformer: ActionPerformer) :
    RecyclerView.Adapter<IasAppliedFilterAdapter.IasAppliedFilterViewHolder>() {

    private var searchFilterList: ArrayList<String> = arrayListOf()
    private var canClearFilter: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IasAppliedFilterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_applied_ias_filter, null, false)
        return IasAppliedFilterViewHolder(view, actionPerformer)
    }

    override fun getItemCount(): Int =
        if (searchFilterList.isNullOrEmpty()) 0 else searchFilterList.size

    override fun onBindViewHolder(holder: IasAppliedFilterViewHolder, position: Int) {
        holder.bind(searchFilterList[position], canClearFilter)
    }

    fun updateData(selectedFilters: List<String>, canClearFilter: Boolean = true) {
        this.canClearFilter = canClearFilter
        searchFilterList = selectedFilters as ArrayList<String>
        notifyDataSetChanged()
    }

    class IasAppliedFilterViewHolder(val view: View, val actionPerformer: ActionPerformer) :
        RecyclerView.ViewHolder(view) {

        val binding = ItemAppliedIasFilterBinding.bind(view)

        fun bind(filter: String, canClearFilter: Boolean) {
            binding.tvAppliedFilter.apply {
                text = filter
            }
            if (canClearFilter) {
                binding.removeFilter.show()
                view.setOnClickListener {
                    actionPerformer.performAction(RemoveFilter(filter))
                }
            } else {
                binding.tvAppliedFilter.filters =
                    arrayOf<InputFilter>(InputFilter.LengthFilter(Int.MAX_VALUE))
                binding.removeFilter.hide()
            }
        }
    }
}
