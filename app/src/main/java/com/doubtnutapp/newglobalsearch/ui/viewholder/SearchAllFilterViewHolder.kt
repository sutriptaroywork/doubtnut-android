package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.content.Context
import android.view.View
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.ShowAllFilters
import com.doubtnutapp.newglobalsearch.model.SearchTabsItem
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchFilterValueAdapter

class SearchAllFilterViewHolder(
    val context: Context,
    val view: View,
    val facet: SearchTabsItem,
    val actionPerformer: ActionPerformer,
    val isYoutube: Boolean
) : SearchFilterValueAdapter.IASFilterValueViewHolder(view) {

    fun bind() {
        view.setOnClickListener {
            actionPerformer.performAction(ShowAllFilters(facet, isYoutube))
        }
    }

}