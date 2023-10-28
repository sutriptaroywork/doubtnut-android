package com.doubtnutapp.newglobalsearch.viewmodel


import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.IasFilterValueDeselected
import com.doubtnutapp.base.IasFilterValueSelected
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class IasAllFilterFragmentViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {


    private var filters: ArrayList<SearchFilter> = arrayListOf()
    private var originalFilters: ArrayList<SearchFilter> = arrayListOf()

    private var appliedFilters: HashMap<String, String> = hashMapOf()
    private var selectedFilter: HashMap<String, String> = hashMapOf()

    fun preserveOldSelection() {
        for (filter in originalFilters) {
            if (filter.isSelected && !filter.key.equals("sort", true)) {
                for (filterValue in filter.filters) {
                    if (filterValue.isSelected) {
                        selectedFilter.put(filter.key, filterValue.value)
                    }
                }
            }
        }
    }

    fun handleAcrtion(action: Any) {
        when (action) {
            is IasFilterValueSelected -> {
                filters[action.parentPosition].isSelected = true
                if (!filters[action.parentPosition].key.equals("sort", true)) {
                    selectedFilter.put(filters[action.parentPosition].key, action.filterValue.value)
                }
            }
            is IasFilterValueDeselected -> {
                filters[action.parentPosition].isSelected = true
                if (!filters[action.parentPosition].key.equals("sort", true)) {
                    selectedFilter.remove(filters[action.parentPosition].key)
                }
            }
        }
    }
}
