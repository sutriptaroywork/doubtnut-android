package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.content.Context
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.WidgetIasFilterV2Binding
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilterItem
import com.doubtnutapp.newglobalsearch.model.SearchTabsItem
import com.doubtnutapp.newglobalsearch.ui.IASFilterValuesDropDownMenu
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchFilterTypeAdapterNew

class SearchFilterViewHolderNew(
    val context: Context,
    val view: View,
    val actionPerformer: ActionPerformer,
    val isYoutube: Boolean,
    val isFromAllChapters: Boolean,
    val appliedFilter: HashMap<String, String>
) : RecyclerView.ViewHolder(view), ActionPerformer {

    private val KEY_SORT: String = "sort"

    private lateinit var typeAdapter: SearchFilterTypeAdapterNew

    private var tab: SearchTabsItem? = null
    private var filterArr: ArrayList<SearchFilter> = arrayListOf()
    private var selectedFilterTypePosition = 0

    private var filterValuePopup: IASFilterValuesDropDownMenu? = null

    val binding = WidgetIasFilterV2Binding.bind(itemView)

    fun bind(
        filter: ArrayList<SearchFilter>,
        facet: SearchTabsItem,
        selectedFilterTypePosition: Int
    ) {
        tab = facet
        filterArr.clear()
        filterArr.addAll(filter)
        typeAdapter = SearchFilterTypeAdapterNew(this)
        binding.rvFilterType.itemAnimator = null
        binding.rvFilterType.adapter = typeAdapter
        binding.rvFilterType.isNestedScrollingEnabled = true
        this.selectedFilterTypePosition = selectedFilterTypePosition
        typeAdapter.updateData(filter, selectedFilterTypePosition)
    }

    override fun performAction(action: Any) {
        when (action) {
            is IasFilterTypeSelected -> {
                selectedFilterTypePosition = action.position
                showFilterValueDropDown(action.anchorView, filterArr[action.position].filters)
            }
            is IasFilterTypeDeselected -> {
                closeFilterValueDropDown()
            }
            is IasFilterValueDeselected -> {
                closeFilterValueDropDown()
                actionPerformer.performAction(action)
                onFilterValueDeselected(action)

            }

            is IasFilterValueSelected -> {
                closeFilterValueDropDown()
                actionPerformer.performAction(action)
                onFilterValueSelected(action)
            }
            else -> actionPerformer.performAction(action)
        }
    }

    private fun showFilterValueDropDown(anchorView: View, filters: ArrayList<SearchFilterItem>) {
        if (filterValuePopup?.isShowing == true) {
            filterValuePopup?.dismiss()
        }
        if (filterValuePopup == null) {
            filterValuePopup = IASFilterValuesDropDownMenu(
                itemView.context,
                this,
                tab!!,
                isYoutube,
                isFromAllChapters,
                selectedFilterTypePosition,
                filters
            )

            filterValuePopup!!.setOnDismissListener {
                actionPerformer.performAction(IasFilterValuePopupStateChanged(false))
            }
        } else {
            filterValuePopup!!.updateData(filters, selectedFilterTypePosition)
        }

        filterValuePopup!!.height = WindowManager.LayoutParams.WRAP_CONTENT
        filterValuePopup!!.width = WindowManager.LayoutParams.WRAP_CONTENT
        filterValuePopup!!.isOutsideTouchable = true
        filterValuePopup!!.isFocusable = true
        filterValuePopup!!.elevation = 4.0f

        filterValuePopup!!.showAsDropDown(anchorView)
        actionPerformer.performAction(IasFilterValuePopupStateChanged(true))
    }

    private fun closeFilterValueDropDown() {
        if (filterValuePopup != null && filterValuePopup!!.isShowing) {
            filterValuePopup!!.dismiss()
        }
    }

    private fun onFilterValueSelected(action: IasFilterValueSelected) {
        if (isFromAllChapters) {
            appliedFilter.clear()
        }

        if (selectedFilterTypePosition >= 0 && selectedFilterTypePosition < filterArr.size) {
            val filterType = filterArr[selectedFilterTypePosition]
            filterType.isSelected = true

            if (KEY_SORT.equals(filterType.key, true)) {
                actionPerformer.performAction(IasSortByFilterSelected(tab!!, action.filterValue))
            } else {
                if (filterType.key.equals("course", true)) {
                    actionPerformer.performAction(CourseV2FilterApplied())
                    appliedFilter.clear()
                }
                appliedFilter.put(filterType.key, action.filterValue.value)
                actionPerformer.performAction(IasFilterSelected(tab!!, appliedFilter, isYoutube))
            }
            filterType.isExpanded = false
            filterType.appliedLabel = action.filterValue.display()
            typeAdapter.notifyItemChanged(selectedFilterTypePosition)
        }
    }

    private fun onFilterValueDeselected(action: IasFilterValueDeselected) {
        try {
            if (selectedFilterTypePosition >= 0 && selectedFilterTypePosition < filterArr.size) {
                filterArr[selectedFilterTypePosition].isSelected = false
            }
            if (!KEY_SORT.equals(filterArr[selectedFilterTypePosition].key, true)) {
                val filterType = filterArr[selectedFilterTypePosition]
                appliedFilter.remove(filterType.key)
                if (isFromAllChapters) {
                    appliedFilter.clear()
                }
                filterType.appliedLabel = filterType.label
                actionPerformer.performAction(IasFilterSelected(tab!!, appliedFilter, isYoutube))
            }
            filterArr[selectedFilterTypePosition].isExpanded = false
            typeAdapter.notifyItemChanged(selectedFilterTypePosition)
        } catch (e: Exception) {
            // https://console.firebase.google.com/u/0/project/doubtnut-e000a/crashlytics/app/android:com.doubtnutapp/issues/076f0cdf5386a53a077aa81bb527841e?time=last-seven-days&sessionEventKey=614AB62B030700016BB121DF74E8085D_1589061867123889256
        }
    }

}