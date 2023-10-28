package com.doubtnutapp.newglobalsearch.ui.viewholder

import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.SearchSuggestionClicked
import com.doubtnutapp.base.SearchSuggestionClicked.Companion.ACTION_ITEM_SELECTED
import com.doubtnutapp.base.SearchSuggestionClicked.Companion.ACTION_ITEM_TEXT_SUBMITTED
import com.doubtnutapp.databinding.ItemSearchSuggestionBinding
import com.doubtnutapp.newglobalsearch.model.SearchSuggestionItem

class SearchSuggestionViewHolder(val binding: ItemSearchSuggestionBinding, val resultCount:Int) :
        BaseViewHolder<SearchSuggestionItem>(binding.root) {

    override fun bind(data: SearchSuggestionItem) {
        binding.searchSuggestion = data
        binding.ivSearchIcon.setImageResource(R.drawable.ic_search_suggestion)
        binding.ivSubmitText.setImageResource(R.drawable.ic_suggestions_arrow)

        binding.root.setOnClickListener {
            performAction(SearchSuggestionClicked(ACTION_ITEM_SELECTED, data.displayText, adapterPosition, data.id, data.version,data))
        }

        binding.ivSubmitText.setOnClickListener {
            performAction(SearchSuggestionClicked(ACTION_ITEM_TEXT_SUBMITTED, data.displayText, adapterPosition, data.id, data.version,data))
        }
    }
}