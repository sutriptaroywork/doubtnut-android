package com.doubtnutapp.newlibrary.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.FragmentLibrarySortByYearBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.newlibrary.model.FilterTabData
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearFilterResponse
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearPapersFilter
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearSelectionResponse
import com.doubtnutapp.newlibrary.ui.adapter.LibraryPreviousYearPapersDateAdapter
import com.doubtnutapp.newlibrary.viewmodel.LibrarySortByYearFragmentViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class LibrarySortByYearFragment :
    BaseBindingFragment<LibrarySortByYearFragmentViewModel, FragmentLibrarySortByYearBinding>() {

    companion object {
        const val TAG = "LibrarySortByYearFragment"
        const val TAB_ID = "tab_id"
        const val FILTER_DATA = "filter_data"
        const val EXAM_ID = "exam_id"

        fun newInstance(
            tabId: String,
            filterData: FilterTabData,
            examId: String
        ) =
            LibrarySortByYearFragment().apply {
                arguments = bundleOf(
                    TAB_ID to tabId,
                    FILTER_DATA to filterData,
                    EXAM_ID to examId
                )
            }
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val adapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(context = requireContext(), source = TAG)
    }

    private val tabId: String? by lazy { arguments?.getString(TAB_ID) }

    private val filterData: FilterTabData? by lazy {
        arguments?.getParcelable(FILTER_DATA)
    }

    private val examId: String? by lazy { arguments?.getString(EXAM_ID) }

    private var dateAdapter: LibraryPreviousYearPapersDateAdapter? = null

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLibrarySortByYearBinding =
        FragmentLibrarySortByYearBinding.inflate(layoutInflater)

    override fun provideViewModel(): LibrarySortByYearFragmentViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBinding ?: return
        binding.rvShifts.adapter = adapter
        val isSingleItem = filterData?.filter?.size == 1
        binding.dropdown.setVisibleState(!isSingleItem)
        viewModel.getFilterData(
            examId.forceUnWrap(),
            tabId.forceUnWrap(),
            filterData?.filter?.find { it.isSelected }?.filterId.forceUnWrap(),
            filterData?.filter?.find { it.isSelected }?.filterDataType.forceUnWrap(),
            filterData?.filter?.find { it.isSelected }?.filterText
        )
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.filterData.observeK(
            viewLifecycleOwner,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgress
        )

        viewModel.selectionData.observeK(
            viewLifecycleOwner,
            ::onSelection,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgress
        )
    }

    private fun onSuccess(data: LibraryPreviousYearFilterResponse) {
        setupFilterUI(data)
    }

    private fun onSelection(data: LibraryPreviousYearSelectionResponse) {
        setupShiftsUI(data)
        if (data.examWidgetData.isEmpty() && data.deeplink.isNotNullAndNotEmpty()) {
            deeplinkAction.performAction(requireContext(), data.deeplink)
        }
    }

    private fun setupFilterUI(data: LibraryPreviousYearFilterResponse) {
        mBinding ?: return
        binding.cardTitle.text = filterData?.filter?.find { it.isSelected }?.filterText

        if (data.selectedFilterData.isEmpty()) {
            binding.guideline.hide()
            binding.divider.hide()
            binding.rvDates.hide()
        } else {
            binding.guideline.show()
            binding.divider.show()
            binding.rvDates.show()

            dateAdapter = LibraryPreviousYearPapersDateAdapter { id ->
                id?.let {
                    viewModel.getSelectionData(
                        examId = examId.forceUnWrap(),
                        tabId = tabId.forceUnWrap(),
                        filterId = filterData?.filter?.find { filterItem ->
                            filterItem.isSelected
                        }?.filterId.forceUnWrap(),
                        filterDataType = data.filterDataType,
                        filterText = data.filterText,
                        id = id
                    )
                    // Update selection
                    val updatedFilterData = data.selectedFilterData
                    updatedFilterData.forEach { selectedFilterItem ->
                        selectedFilterItem.isSelected = selectedFilterItem.id == id
                    }
                    dateAdapter?.submitData(updatedFilterData)
                }
            }
            dateAdapter?.submitData(data.selectedFilterData)
            binding.rvDates.clearDecorations()
            binding.rvDates.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    (binding.rvDates.layoutManager as LinearLayoutManager).orientation
                )
            )
            binding.rvDates.adapter = dateAdapter
        }

        binding.dropdown.setOnClickListener {
            if (filterData != null) {
                val dropDownMenu = getFilterPopup(
                    filterData!!.filter,
                    data.examId,
                    data.tabId
                )
                dropDownMenu.showAsDropDown(binding.dropdown)
            }
        }
    }

    private fun setupShiftsUI(data: LibraryPreviousYearSelectionResponse) {
        adapter.setWidgets(data.examWidgetData)
    }

    private fun getFilterPopup(
        list: List<LibraryPreviousYearPapersFilter>,
        examId: String,
        tabId: String
    ): DropDownMenu {

        val menu = DropDownMenu(requireContext(), list)
        menu.height = WindowManager.LayoutParams.WRAP_CONTENT
        menu.width = Utils.getWidthFromScrollSize(requireContext(), "1x")
        menu.isOutsideTouchable = true
        menu.isFocusable = true
        menu.setLanguageSelectedListener(object :
            DropDownAdapter.FilterListener {

            override fun onSelected(position: Int, data: LibraryPreviousYearPapersFilter) {
                menu.dismiss()
                if (!list.isNullOrEmpty()) {
                    filterData?.filter?.forEach {
                        it.isSelected = it.filterId == data.filterId
                    }
                    viewModel.getFilterData(
                        examId,
                        tabId,
                        data.filterId,
                        data.filterDataType,
                        data.filterText
                    )
                }
            }
        })
        return menu
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        this.let { currentContext ->
            if (NetworkUtils.isConnected(requireContext())) {
                toast(getString(R.string.somethingWentWrong))
            } else {
                toast(getString(R.string.string_noInternetConnection))
            }
        }
    }

    private fun unAuthorizeUserError() {
        childFragmentManager.beginTransaction()
            .add(BadRequestDialog.newInstance("unauthorized"), "BadRequestDialog").commit()
    }

    private fun updateProgress(state: Boolean) {
        mBinding?.progressbar?.setVisibleState(state)
    }
}