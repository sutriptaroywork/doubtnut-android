package com.doubtnutapp.newglobalsearch.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.FragmentIasAllFilterBinding
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.doubtnutapp.hide
import com.doubtnutapp.newglobalsearch.model.SearchTabsItem
import com.doubtnutapp.newglobalsearch.ui.adapter.IasAllFilterAdapter
import com.doubtnutapp.newglobalsearch.viewmodel.IasAllFilterFragmentViewModel
import com.doubtnutapp.newglobalsearch.viewmodel.InAppSearchViewModel
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingFragment
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable

class IasAllFilterFragment : BaseBindingFragment<IasAllFilterFragmentViewModel,FragmentIasAllFilterBinding>(),
    ActionPerformer {

    private lateinit var parentViewModel: InAppSearchViewModel

    private var filters: ArrayList<SearchFilter> = arrayListOf()
    private var isYoutube: Boolean = false

    private lateinit var facet: SearchTabsItem
    private lateinit var adapter: IasAllFilterAdapter

    private var appliedFilters: HashMap<String, String> = hashMapOf()
    private var selectedFilter: HashMap<String, String> = hashMapOf()

    private var resultsCount: Int = 0
    private var disposable: Disposable? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        parentViewModel = activityViewModelProvider(viewModelFactory)

        arguments?.let {
            facet = it.getParcelable(ARG_FACET)!!
            filters = parentViewModel.tabMap[facet.key]?.filterList ?: arrayListOf()
            isYoutube = it.getBoolean(IS_YOUTUBE, false)
        }

        init()
    }

    fun init() {
        for (filter in filters) {
            if (filter.isSelected && !filter.key.equals("sort", true)) {
                for (filterValue in filter.filters) {
                    if (filterValue.isSelected) {
                        selectedFilter.put(filter.key, filterValue.value)
                    }
                }
            }
        }
        setClearButtonEnabled()
        adapter = IasAllFilterAdapter(this, isYoutube)
        binding.rvFilter.adapter = adapter
        binding.rvFilter.itemAnimator = null
        for (filter in filters) {
            filter.previousSelectedState = filter.isSelected
            for (item in filter.filters) {
                item.previousSelectedState = item.isSelected
            }

            if (filter.key.equals("class", true)) {
                filter.filters = ArrayList(filter.filters.sortedWith(compareBy {
                    it.value.toInt()
                }))
            }
        }
        adapter.updateData(facet, filters)

        binding.closeAllFilterScreen.setOnClickListener {
            restoreAppliedFilter()
            activity?.onBackPressed()
        }
        binding.btnApplyFilter.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.clearFiters.setOnClickListener {
            for (filter in filters) {
                filter.isSelected = false
                for (filterValue in filter.filters) {
                    filterValue.isSelected = false
                }
            }
            selectedFilter.clear()
            appliedFilters.clear()
            adapter.updateData(facet, filters)
            DoubtnutApp.INSTANCE.bus()?.send(IasClearAllFilters(facet, isYoutube))
            DoubtnutApp.INSTANCE.bus()?.send(IasFilterSelected(facet, appliedFilters, isYoutube))
            binding.progressFilter.show()
        }

        disposable = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            when (it) {
                is YoutubeResultsFetched -> {
                    binding.progressFilter.hide()
                }
            }
        }
    }

    private fun applyFilters() {
        binding.progressFilter.show()
        if (selectedFilter.size == 0) {
            appliedFilters.clear()
            DoubtnutApp.INSTANCE.bus()?.send(IasClearAllFilters(facet, isYoutube))
        } else {
            appliedFilters.clear()
            appliedFilters.putAll(selectedFilter)
        }
        DoubtnutApp.INSTANCE.bus()?.send(IasFilterSelected(facet, appliedFilters, isYoutube))
    }

    private fun restoreAppliedFilter() {
        for (filter in filters) {
            filter.isSelected = filter.previousSelectedState
            for (item in filter.filters) {
                item.isSelected = item.previousSelectedState
            }
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is IasFilterValueSelected -> {
                filters[action.parentPosition].isSelected = true
                if (!filters[action.parentPosition].key.equals("sort", true)) {
                    selectedFilter.put(filters[action.parentPosition].key, action.filterValue.value)
                    applyFilters()
                } else {
                    DoubtnutApp.INSTANCE.bus()
                        ?.send(IasSortByFilterSelected(facet, action.filterValue))
                }
            }
            is IasFilterValueDeselected -> {
                filters[action.parentPosition].isSelected = false
                if (!filters[action.parentPosition].key.equals("sort", true)) {
                    selectedFilter.remove(filters[action.parentPosition].key)
                    applyFilters()
                } else {
                    DoubtnutApp.INSTANCE.bus()
                        ?.send(IasSortByFilterSelected(facet, action.filterValue))
                }
            }
        }
        setClearButtonEnabled()
    }

    fun setClearButtonEnabled() {
        binding.clearFiters.isEnabled = selectedFilter.size > 0
        binding.btnApplyFilter.isEnabled = selectedFilter.size > 0
    }

    fun updateFilters(resultsCount: Int) {
        binding.progressFilter.hide()
        this.resultsCount = resultsCount
        filters = parentViewModel.tabMap[facet.key]?.filterList ?: arrayListOf()

        for (filter in filters) {
            if (filter.isSelected && !filter.key.equals("sort", true)) {
                for (filterValue in filter.filters) {
                    if (filterValue.isSelected) {
                        selectedFilter.put(filter.key, filterValue.value)
                    }
                }
            }
            if (filter.key.equals("class", true)) {
                filter.filters = ArrayList(filter.filters.sortedWith(compareBy {
                    it.value.toInt()
                }))
            }
        }
        adapter?.updateData(facet, filters)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    companion object {
        private const val TAG = "IasAllFilterFragment"
        private const val ARG_FILTERS = "filters"
        private const val ARG_FACET = "facet"
        private const val IS_YOUTUBE = "isYoutube"


        @JvmStatic
        fun newInstance(
            facet: SearchTabsItem,
            filters: ArrayList<SearchFilter>,
            isYoutube: Boolean
        ) =
            IasAllFilterFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_FILTERS, filters)
                    putParcelable(ARG_FACET, facet)
                    putBoolean(IS_YOUTUBE, isYoutube)
                }
            }
    }

    override fun provideViewBinding(inflater: LayoutInflater, container: ViewGroup?):
            FragmentIasAllFilterBinding = FragmentIasAllFilterBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): IasAllFilterFragmentViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
    }
}