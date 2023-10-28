package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.SearchSuggestionClicked
import com.doubtnutapp.base.SearchSuggestionClicked.Companion.ACTION_ITEM_SELECTED
import com.doubtnutapp.databinding.ItemTydSuggestionsBinding
import com.doubtnutapp.newglobalsearch.model.SearchSuggestionItem

class SearchTydSuggestionViewHolder(val binding: ItemTydSuggestionsBinding) :
        BaseViewHolder<SearchSuggestionItem>(binding.root) {

    override fun bind(data: SearchSuggestionItem) {
        binding.suggestionItem = data

        if (adapterPosition == 0)
            binding.searchDivider.visibility = View.GONE

        binding.root.setOnClickListener {
            performAction(SearchSuggestionClicked(ACTION_ITEM_SELECTED, data.displayText, adapterPosition, "", "",data))
        }
    }
}