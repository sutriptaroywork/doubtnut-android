package com.doubtnutapp.newglobalsearch.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilterItem
import com.doubtnutapp.newglobalsearch.model.SearchTabsItem
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchFilterValueDropDownAdapter

class IASFilterValuesDropDownMenu(
    private val context: Context,
    private val actionPerformer: ActionPerformer,
    var facet: SearchTabsItem,
    private val isYoutube: Boolean,
    val isFromAllChapters: Boolean,
    var filterTypePosition: Int,
    var filterValueList: ArrayList<SearchFilterItem>
) : PopupWindow(context) {
    private var rvFilterValues: RecyclerView? = null
    private var dropdownAdapter: SearchFilterValueDropDownAdapter? = null

    fun setupView() {
        val view: View = LayoutInflater.from(context).inflate(R.layout.ias_pop_up_filter, null)
        setBackgroundDrawable(context.getDrawable(R.drawable.capsule_stroke_grey_solid_white))
        rvFilterValues = view.findViewById(R.id.rvCategory)
        rvFilterValues?.setHasFixedSize(true)
        rvFilterValues?.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvFilterValues?.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )
        dropdownAdapter = SearchFilterValueDropDownAdapter(
            actionPerformer,
            facet,
            isYoutube,
            isFromAllChapters
        )
        rvFilterValues?.adapter = dropdownAdapter
        dropdownAdapter!!.updateData(filterValueList, filterTypePosition, facet)
        contentView = view
    }

    fun updateData(filters: ArrayList<SearchFilterItem>?, filterTypePosition: Int) {
        filterValueList = filters ?: arrayListOf()
        dropdownAdapter?.updateData(filterValueList, filterTypePosition, facet)
    }

    init {
        setupView()
    }
}