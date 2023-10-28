package com.doubtnutapp.doubtpecharcha.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.applyTextColor
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentP2pDoubtCollectionBinding
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.doubtpecharcha.viewmodel.DoubtCollectionViewModel
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.OnDoubtPeCharchaRewardCtaClicked
import com.doubtnutapp.base.OnFilterSelected
import com.doubtnutapp.common.model.FilterListData
import com.doubtnutapp.common.ui.dialog.FilterBottomSheetFragment
import com.doubtnutapp.doubtpecharcha.model.*
import com.doubtnutapp.doubtpecharcha.ui.activity.P2PDoubtCollectionActivity
import com.doubtnutapp.doubtpecharcha.ui.adapter.SecondaryTabsAdapter
import com.doubtnutapp.freeclasses.bottomsheets.FilterListBottomSheetDialogFragment
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlin.math.sign

class P2PDoubtCollectionFragment :
    BaseBindingFragment<DoubtCollectionViewModel, FragmentP2pDoubtCollectionBinding>(),
    ActionPerformer {

    companion object {

        const val DOUBT_P2P_COLLECTION = "p2p_collection"
        const val SUBJECT_FILTERS = "subject_filters"
        const val CLASS_FILTERS = "class_filters"
        const val LANGUAGE_FILTERS = "language_filters"
        const val SECONDARY_TAB_ID = "secondary_tab_id"
        const val DEFAULT_LAST_SELECTED_PAGE_VALUE = 1
        fun newInstance(
            type: Int,
            secondaryTabId: String?,
            subjectFilters: ArrayList<String>?,
            classesFilters: ArrayList<String>?, languageFilters: ArrayList<String>?
        ) = P2PDoubtCollectionFragment().apply {
            arguments = Bundle().apply {
                putInt(DoubtCollectionViewModel.DOUBT_TYPE, type)
                putString(SECONDARY_TAB_ID, secondaryTabId)
                putStringArrayList(SUBJECT_FILTERS, subjectFilters)
                putStringArrayList(CLASS_FILTERS, classesFilters)
                putStringArrayList(LANGUAGE_FILTERS, languageFilters)
            }
        }
    }

    private var filters: java.util.ArrayList<FilterData>? = null

    private lateinit var subjectFilters: java.util.ArrayList<Filter>
    private lateinit var classesFilters: java.util.ArrayList<Filter>
    private lateinit var languageFilters: java.util.ArrayList<Filter>
    private var secondaryTabId = 0

    private var lastSelectedPage = 1

    private val primaryTabID: Int? by lazy {
        arguments?.getInt(DoubtCollectionViewModel.DOUBT_TYPE, 1)
    }

    private val secondaryTabIdFromBundle: String? by lazy {
        arguments?.getString(P2PDoubtCollectionActivity.SECONDARY_TAB_ID, "")
    }

    private val subjectFiltersFromBundle: ArrayList<String>? by lazy {
        arguments?.get(P2PDoubtCollectionActivity.SUBJECT_FILTERS) as ArrayList<String>?
    }

    private val classFiltersFromBundle: ArrayList<String>? by lazy {
        arguments?.get(P2PDoubtCollectionActivity.CLASS_FILTERS) as ArrayList<String>?
    }

    private val languageFiltersFromBundle: ArrayList<String>? by lazy {
        arguments?.get(P2PDoubtCollectionActivity.LANGUAGE_FILTERS) as ArrayList<String>?
    }

    private val widgetAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            requireContext(),
            source = DOUBT_P2P_COLLECTION
        )
    }

    private var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener? = null

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentP2pDoubtCollectionBinding =
        FragmentP2pDoubtCollectionBinding.inflate(layoutInflater)

    override fun providePageName(): String = DOUBT_P2P_COLLECTION

    override fun provideViewModel(): DoubtCollectionViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            primaryTabID?.let {
                setUpScreen(it, true)
            }
        }
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        val key = arguments?.get(DoubtCollectionViewModel.DOUBT_TYPE) as Int

        subjectFilters = ArrayList()
        classesFilters = ArrayList()
        languageFilters = ArrayList()

        secondaryTabIdFromBundle?.let {
            if (it.isNotEmpty()) {
                secondaryTabId = Integer.parseInt(it)
            }
        }

        setUpP2pCollectionList(key)

        binding.layoutFilters.visibility = View.GONE
        binding.layoutSecondaryTab.visibility = View.GONE

        binding.layoutFilter1.setOnClickListener {
            showFilterDialog(filters!![0], SubjectFilter())
        }

        binding.layoutFilter2.setOnClickListener {
            showFilterDialog(filters!![1], ClassesFilter())
        }

        binding.layoutFilter3.setOnClickListener {
            showFilterDialog(filters!![2], LanguageFilter())
        }

        subjectFiltersFromBundle?.let { subjectFilterFromBundle ->
            if (subjectFilterFromBundle.size > 0) {
                subjectFilterFromBundle.forEach {
                    subjectFilters.add(Filter(title = null, id = it))
                }
                setFilters(SubjectFilter(), subjectFilters)
            }
        }

        classFiltersFromBundle?.let { classesFiltersFromBundle ->
            if (classesFiltersFromBundle.size > 0) {
                classesFiltersFromBundle.forEach {
                    subjectFilters.add(Filter(title = null, id = it))
                }
                setFilters(ClassesFilter(), classesFilters)
            }
        }

        languageFiltersFromBundle?.let { languageFiltersFromBundle ->
            if (languageFiltersFromBundle.size > 0) {
                languageFiltersFromBundle.forEach {
                    languageFilters.add(Filter(title = null, id = it))
                }
                setFilters(LanguageFilter(), languageFilters)
            }
        }

        if (subjectFilters.isNotEmpty() || classesFilters.isNotEmpty() || languageFilters.isNotEmpty()) {
            binding.btnClearAllFilters.isEnabled = true
            binding.btnClearAllFilters.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        }

        binding.btnClearAllFilters.setOnClickListener {
            clearAllFilters()
        }


        setGreyBorderBackground(binding.layoutFilter1)
        setGreyBorderBackground(binding.layoutFilter2)
        setGreyBorderBackground(binding.layoutFilter3)
        setGreyBorderBackground(binding.layoutFilter4)

        binding.recyclerviewSecondaryTabs.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)


    }

    private fun clearAllFilters() {
        if (subjectFilters.isEmpty() && classesFilters.isEmpty() && languageFilters.isEmpty()) {
            showToast(requireContext(), "Filters already empty")
            return
        }
        subjectFilters.clear()
        languageFilters.clear()
        classesFilters.clear()
        binding.tvValueFilter1.text = getString(R.string.all)
        binding.tvValueFilter2.text = getString(R.string.all)
        binding.tvValueFilter3.text = getString(R.string.all)
        resetClearAllFilterButton()
        fetchDoubts(primaryTabId = primaryTabID!!, secondaryTabId)
    }

    private fun fetchDoubts(
        primaryTabId: Int = 1, secondaryTabId: Int = 0
    ) {
        viewModel.getUserDoubts(
            primaryTabId, secondaryTabId, subjectFilters, classesFilters,
            languageFilters
        )
        lastSelectedPage = DEFAULT_LAST_SELECTED_PAGE_VALUE
        infiniteScrollListener?.isLastPageReached = false
    }

    private fun fetchPaginatedDoubts(primaryTabId: Int = 1, secondaryTabId: Int = 0) {
        viewModel.getDoubtsPagination(
            primaryTabId, secondaryTabId, subjectFilters, classesFilters,
            languageFilters, lastSelectedPage
        )
    }

    private fun setUpP2pCollectionList(key: Int) {
        mBinding?.rvDoubts?.adapter = widgetAdapter
        widgetAdapter.actionPerformer = this
        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(mBinding?.rvDoubts?.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    fetchPaginatedDoubts(primaryTabId = key, secondaryTabId = secondaryTabId)
                }
            }
        infiniteScrollListener?.setStartPage(1)
        mBinding?.rvDoubts?.addOnScrollListener(infiniteScrollListener!!)
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.userDoubtsLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    infiniteScrollListener?.setDataLoading(it.loading)
                    mBinding?.progressBarLoader?.isVisible = it.loading
                }
                is Outcome.Success -> {
                    mBinding?.progressBarLoader?.hide()
                    infiniteScrollListener?.setDataLoading(false)
                    val doubts = it.data.second.doubtData
                    if (doubts != null && doubts.isNotEmpty()) {
                        binding.rvDoubts.show()
                        binding.emptyStateLayout.hide()
                        updateData(doubts)
                        filters = it.data.second.filters as java.util.ArrayList<FilterData>?
                        filters?.let { filters ->
                            if (filters.size > 0) {
                                binding.layoutFilters.visibility = View.VISIBLE
                                setVisibilityOfFilters(filters)
                            }

                        }
                    } else {
                        showEmptyStateData(it.data.second.noDoubtsData)
                    }

                    val secondaryTabs = it.data.second.secondaryTabs
                    if (secondaryTabs != null && secondaryTabs.isNotEmpty()) {
                        showSecondaryTabs(
                            it.data.second.secondaryTabs,
                            it.data.second.activeSecondaryTabId
                        )
                        binding.layoutSecondaryTab.visibility = View.VISIBLE
                    } else {
                        binding.layoutSecondaryTab.visibility = View.GONE
                    }


                }
                is Outcome.Failure -> {
                    infiniteScrollListener?.setDataLoading(false)
                    mBinding?.progressBarLoader?.hide()
                    apiErrorToast(it.e)
                }
                else -> {
                }
            }
        }

        viewModel.paginatedUserDoubtsLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    infiniteScrollListener?.setDataLoading(it.loading)
                    mBinding?.progressBarLoader?.isVisible = it.loading
                }
                is Outcome.Success -> {
                    val doubts = it.data.second.doubtData
                    val page = it.data.second.page
                    page?.let { pageNo ->
                        lastSelectedPage = pageNo.toInt()
                    }
                    updatePaginatedData(doubts)
                }
                else -> {

                }
            }
        }
    }
    private fun showEmptyStateData(noDoubtsData: NoDoubtsData?) {
        noDoubtsData?.let { noDoubtEmptyStateData ->
            binding.emptyStateLayout.show()
            binding.apply {
                emptyStateLayout.show()
                tvEmpty.text = noDoubtEmptyStateData.description
                ivEmpty.loadImage(noDoubtEmptyStateData.imageUrl)
            }
        }
        binding.rvDoubts.hide()

    }

    private fun updatePaginatedData(data: List<WidgetEntityModel<*, *>>?) {
        if (data != null) {
            widgetAdapter.addWidgets(data)
            if (data.isEmpty()) {
                infiniteScrollListener?.setLastPageReached(true)
            }
        } else {
            binding.rvDoubts.hide()
        }
    }

    private fun setVisibilityOfFilters(filters: java.util.ArrayList<FilterData>?) {
        filters?.let { listFilters ->
            val size = listFilters.size
            when (size) {
                1 -> {
                    binding.layoutFilter4.hide()
                    binding.layoutFilter3.hide()
                    binding.layoutFilter2.hide()
                }
                2 -> {
                    binding.layoutFilter4.hide()
                    binding.layoutFilter3.hide()
                }
                3 -> {
                    binding.layoutFilter4.hide()
                }
            }
        }
    }

    private fun setGreyBorderBackground(view: View) {
        val radius = resources.getDimension(R.dimen.dimen_6dp)
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, radius)
            .build()
        val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        materialShapeDrawable.strokeWidth = 1.5f
        materialShapeDrawable.strokeColor =
            ContextCompat.getColorStateList(requireContext(), R.color.grey_808080)
        materialShapeDrawable.fillColor =
            ContextCompat.getColorStateList(requireContext(), R.color.colorTransparent)
        view.background = materialShapeDrawable
    }

    private fun setUpScreen(key: Int, doRefresh: Boolean) {
        if (doRefresh) {
            fetchDoubts(key, secondaryTabId)
            return
        }
        val doubts = viewModel.doubtsCollection[key]
        if (doubts != null) {
            updateData(viewModel.doubtsCollection[key])
        } else {
            fetchDoubts(primaryTabId = key)
        }
    }

    private fun updateData(data: List<WidgetEntityModel<*, *>>?) {
        if (data != null) {
            widgetAdapter.setWidgets(data)
            if (data.isEmpty()) {
                infiniteScrollListener?.isLastPageReached = true
            }
        } else {
            binding.rvDoubts.hide()
        }
    }

    private fun setSelectedKeyForFilter(
        filter: FilterData,
        position: Int
    ) {
        if (filter.selectedOptionId.isNullOrEmpty()) {
            return
        }
        filter.filterItemData?.let { filterData ->
            for (item in filterData) {
                if (item.filterId == filter.selectedOptionId) {
                    when (position) {
                        0 -> {
                            binding.tvValueFilter1.text = item.title
                            binding.tvLabelFilter1.text = filter.titleFilter
                        }
                        1 -> {
                            binding.tvValueFilter2.text = item.title
                            binding.tvLabelFilter2.text = filter.titleFilter
                        }
                        2 -> {
                            binding.tvValueFilter3.text = item.title
                            binding.tvLabelFilter3.text = filter.titleFilter
                        }
                        3 -> binding.tvValueFilter4.text = item.title
                    }
                }
            }
        }
    }

    private fun showFilterDialog(filter: FilterData, filterType: FilterType) {
        if (requireActivity().supportFragmentManager.findFragmentByTag(
                FilterListBottomSheetDialogFragment.TAG
            ) != null
        ) {
            return
        }
        val dialog =
            FilterBottomSheetFragment.newInstance(
                filter.titleFilter!!,
                filter.filterItemData!!,
                filterType,
                getSelectedFiltersForType(filterType)
            )
                .apply {
                    setActionPerformer(this@P2PDoubtCollectionFragment)
                }
        dialog.show(
            requireActivity().supportFragmentManager,
            FilterListBottomSheetDialogFragment.TAG
        )
    }

    private fun getSelectedFiltersForType(filterType: FilterType): java.util.ArrayList<Filter> {
        return when (filterType) {
            is SubjectFilter -> subjectFilters
            is ClassesFilter -> classesFilters
            is LanguageFilter -> languageFilters
        }
    }

    private fun showSecondaryTabs(listSecondaryTabs: List<TabData>?, activeSecondaryTab: Int?) {

        val adapter = SecondaryTabsAdapter(
            requireContext(),
            listSecondaryTabs as ArrayList<TabData>,
            activeSecondaryTab
        ) {
            secondaryTabId = it
            fetchDoubts(primaryTabId = primaryTabID!!, secondaryTabId = it)
        }
        binding.recyclerviewSecondaryTabs.adapter = adapter
    }

    override fun performAction(action: Any) {
        if (action is OnFilterSelected) {
            selectFilters(action.type, action.values)
        }
        if (action is OnDoubtPeCharchaRewardCtaClicked) {
            action.rewardsData?.let {
                val rewardsBottomSheet = DoubtPeCharchaRewardInfoBottomSheetFragment.newInstance(it)
                rewardsBottomSheet.show(
                    requireActivity().supportFragmentManager,
                    DoubtPeCharchaRewardInfoBottomSheetFragment.TAG
                )
            }
        }


    }

    fun resetClearAllFilterButton() {
        binding.btnClearAllFilters.isEnabled = false
        binding.btnClearAllFilters.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.grey_808080
            )
        )
    }

    private fun selectFilters(
        filterType: FilterType?,
        listFilter: java.util.ArrayList<FilterListData.FilterListItem>
    ) {
        if (filterType == null) {
            return
        }
        when (filterType) {
            is SubjectFilter -> {
                subjectFilters.clear()
                val stringBuilder = StringBuilder()
                listFilter.forEach {
                    it.filterId?.let { filterId ->
                        subjectFilters.add(Filter(title = it.title, filterId))
                    }
                    it.title?.let { title ->
                        stringBuilder.append(title).append(" , ")
                    }
                }
                if (listFilter.size > 0) {
                    val filters =
                        stringBuilder.toString().substring(0, stringBuilder.toString().length - 2)
                    binding.tvValueFilter1.text = filters

                } else {
                    subjectFilters.clear()
                    binding.tvValueFilter1.text = getString(R.string.all)
                    resetClearAllFilterButton()
                }
            }
            is ClassesFilter -> {
                val stringBuilder = StringBuilder()
                classesFilters.clear()
                listFilter.forEach {
                    it.filterId?.let { filterId ->
                        classesFilters.add(Filter(it.title, filterId))
                    }
                    it.title?.let { title ->
                        stringBuilder.append(title).append(" , ")
                    }
                }
                if (listFilter.size > 0) {
                    val filters =
                        stringBuilder.toString().substring(0, stringBuilder.toString().length - 2)
                    binding.tvValueFilter2.text = filters
                } else {
                    classesFilters.clear()
                    binding.tvValueFilter2.text = getString(R.string.all)
                    resetClearAllFilterButton()
                }
            }
            is LanguageFilter -> {
                val stringBuilder = StringBuilder()
                languageFilters.clear()
                listFilter.forEach {
                    it.filterId?.let { filterId ->
                        languageFilters.add(Filter(title = it.title, id = filterId))
                    }
                    it.title?.let { title ->
                        stringBuilder.append(title).append(" , ")
                    }
                }
                if (listFilter.size > 0) {
                    val filters =
                        stringBuilder.toString().substring(0, stringBuilder.toString().length - 2)
                    binding.tvValueFilter3.text = filters
                } else {
                    languageFilters.clear()
                    binding.tvValueFilter3.text = getString(R.string.all)
                    resetClearAllFilterButton()
                }
            }

        }

        if (subjectFilters.isNotEmpty() || classesFilters.isNotEmpty() || languageFilters.isNotEmpty()) {
            binding.btnClearAllFilters.isEnabled = true
            binding.btnClearAllFilters.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        }

        fetchDoubts(
            primaryTabId = primaryTabID!!,
            secondaryTabId = secondaryTabId
        )
    }

    private fun setFilters(filterType: FilterType, values: ArrayList<Filter>) {
        when (filterType) {
            is SubjectFilter -> {
                val stringBuilder = StringBuilder()
                values.forEach {
                    stringBuilder.append(it.id).append(" , ")
                }
                stringBuilder.substring(0, stringBuilder.length - 2)
                binding.tvValueFilter1.text = stringBuilder.toString()
            }
            is ClassesFilter -> {
                val stringBuilder = StringBuilder()
                values.forEach {
                    stringBuilder.append(it.id).append(" , ")
                }
                stringBuilder.substring(0, stringBuilder.length - 2)
                binding.tvValueFilter2.text = stringBuilder.toString()
            }
            is LanguageFilter -> {
                val stringBuilder = StringBuilder()
                values.forEach {
                    stringBuilder.append(it.id).append(" , ")
                }
                stringBuilder.substring(0, stringBuilder.length - 2)
                binding.tvValueFilter3.text = stringBuilder.toString()
            }
        }

    }


}
