package com.doubtnutapp.newglobalsearch.ui.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemSearchHeaderBinding
import com.doubtnutapp.hide
import com.doubtnutapp.newglobalsearch.model.SearchHeaderViewItem
import com.doubtnutapp.show

class SearchHeaderViewHolder(val binding: ItemSearchHeaderBinding) :
        BaseViewHolder<SearchHeaderViewItem>(binding.root) {

    override fun bind(data: SearchHeaderViewItem) {
        binding.searchHeader = data

        if(adapterPosition == 0)
            binding.searchDivider.hide()
        else
            binding.searchDivider.show()

        if(data.imageUrl.isEmpty())
            binding.searchHeaderImage.hide()
        else
            binding.searchHeaderImage.show()

    }
}