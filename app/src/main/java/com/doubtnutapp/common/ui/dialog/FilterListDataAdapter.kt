package com.doubtnutapp.common.ui.dialog

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.common.model.FilterListData
import com.doubtnutapp.databinding.ItemFilterListBinding
import com.doubtnutapp.doubtpecharcha.model.Filter

/**
 * Created by Amit Gupta on 10/05/22.
 */
class FilterListDataAdapter(
    val list: List<FilterListData.FilterListItem>,
    val selectedFilters: java.util.ArrayList<Filter>?,
    val onItemClicked: (pos: Int) -> Unit

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var hasUserInteracted = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FilterListSingleColumnViewHolder(
            ItemFilterListBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as FilterListSingleColumnViewHolder).bind(
            list[position],
            selectedFilters,
            position,
            onItemClicked
        )
    }

    inner class FilterListSingleColumnViewHolder(
        val binding: ItemFilterListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            data: FilterListData.FilterListItem,
            selectedFilters: ArrayList<Filter>?,
            position: Int,
            onItemClicked: (pos: Int) -> Unit
        ) {
            binding.apply {
                text.text = data.title
                selectedFilters?.let {
                    data.isSelected = doesContainFilter(selectedFilters, data.filterId)
                }
                radioButton.isChecked = data.isSelected
                if (data.isSelected) {
                    text.setTextColor(Color.parseColor("#424242"))
                } else {
                    text.setTextColor(Color.parseColor("#808080"))
                }
                root.setOnClickListener {
                    onItemClicked(position)
                }
            }
        }
    }

    private fun doesContainFilter(listFilters: ArrayList<Filter>, filterId: String?): Boolean {
        if (filterId == null) {
            return false
        }
        var found = false
        listFilters.forEach {
            if (it.id == filterId) {
                found = true
                return@forEach
            }
        }
        return found
    }
}