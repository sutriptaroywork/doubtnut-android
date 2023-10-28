package com.doubtnutapp.similarVideo.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenInAppSearch
import com.doubtnutapp.databinding.ItemSimilarSearchTopicBinding
import com.doubtnutapp.similarVideo.model.SimilarTopicSearchViewItem

class SimilarTopicSearchViewHolder(val binding: ItemSimilarSearchTopicBinding) : BaseViewHolder<SimilarTopicSearchViewItem>(binding.root) {

    override fun bind(data: SimilarTopicSearchViewItem) {
        val item = data.copy(
                searchText = data.searchText?.replace("_", " ")
        )
        binding.similarItem = item
        binding.executePendingBindings()
        binding.root.setOnClickListener {
            performAction(OpenInAppSearch(isVoiceSearch = false, searchQuery = data.searchText?.capitalize()))
        }
        binding.button.setOnClickListener {
            performAction(OpenInAppSearch(isVoiceSearch = false, searchQuery = data.searchText?.capitalize()))
        }
    }

}