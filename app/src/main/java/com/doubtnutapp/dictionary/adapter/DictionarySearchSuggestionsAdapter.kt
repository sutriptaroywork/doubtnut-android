package com.doubtnutapp.dictionary.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.newglobalsearch.model.SearchListViewItem
import com.doubtnutapp.newglobalsearch.ui.viewholder.SearchResultViewHolderFactory

class DictionarySearchSuggestionsAdapter(
    private val actionPerformer: ActionPerformer?
) :
    RecyclerView.Adapter<BaseViewHolder<SearchListViewItem>>() {

    private val viewHolderFactory = SearchResultViewHolderFactory()

    private var searchSuggestionsList = listOf<SearchListViewItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<SearchListViewItem> {
        return (viewHolderFactory.getViewHolderFor(parent, viewType, null, searchSuggestionsList.size) as BaseViewHolder<SearchListViewItem>).apply {
            actionPerformer = this@DictionarySearchSuggestionsAdapter.actionPerformer
        }
    }

    override fun getItemViewType(position: Int): Int {
        return searchSuggestionsList[position].viewType
    }

    override fun getItemCount(): Int = searchSuggestionsList.size

    override fun onBindViewHolder(holder: BaseViewHolder<SearchListViewItem>, position: Int) =
        holder.bind(searchSuggestionsList[position])

    fun updateData(data: List<SearchListViewItem>) {
        searchSuggestionsList = data
        notifyDataSetChanged()
    }
}
