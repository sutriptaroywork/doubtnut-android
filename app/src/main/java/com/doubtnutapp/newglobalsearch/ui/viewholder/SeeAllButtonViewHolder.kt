package com.doubtnutapp.newglobalsearch.ui.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.SeeAllSearchResults
import com.doubtnutapp.databinding.ItemSeeAllButtonBinding
import com.doubtnutapp.newglobalsearch.model.SeeAllButtonViewItem

class SeeAllButtonViewHolder(val binding: ItemSeeAllButtonBinding) :
        BaseViewHolder<SeeAllButtonViewItem>(binding.root) {

    override fun bind(data: SeeAllButtonViewItem) {
        binding.seeAllButton = data
        binding.executePendingBindings()
        binding.button.setOnClickListener {
            performAction(SeeAllSearchResults("", "",0))
        }
    }
}