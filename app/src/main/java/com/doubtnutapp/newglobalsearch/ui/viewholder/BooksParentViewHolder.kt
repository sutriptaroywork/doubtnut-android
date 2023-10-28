package com.doubtnutapp.newglobalsearch.ui.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.BooksNewLayoutBinding
import com.doubtnutapp.newglobalsearch.model.NewSearchCategorizedDataItem
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchResultAdapter

class BooksParentViewHolder(val binding: BooksNewLayoutBinding) : BaseViewHolder<NewSearchCategorizedDataItem>(binding.root) {

    override fun bind(data: NewSearchCategorizedDataItem) {
        binding.searchCategory = data

        val childAdapter = SearchResultAdapter(actionPerformer, null)
        binding.gridList.adapter = childAdapter
        binding.gridList.isNestedScrollingEnabled = false
        childAdapter.updateData(data.dataList, data.chapterDetails, "")
        binding.executePendingBindings()

    }
}