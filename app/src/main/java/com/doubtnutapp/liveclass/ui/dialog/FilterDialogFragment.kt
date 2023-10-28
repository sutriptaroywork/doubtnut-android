package com.doubtnutapp.liveclass.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.course.widgets.FilterButtonWidgetModel
import com.doubtnutapp.databinding.DialogFiltersBinding
import com.doubtnutapp.liveclass.viewmodel.FilterViewModel
import com.doubtnutapp.model.FilterData
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class FilterDialogFragment :
    BaseBindingBottomSheetDialogFragment<FilterViewModel, DialogFiltersBinding>(),
    ActionPerformer {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var actionPerformer: ActionPerformer? = null

    companion object {
        const val TAG = "FilterDialogFragment"
        const val FILTERS = "filters"
        const val SOURCE = "source"
        const val ASSORTMENT_ID = "assortment_id"

        fun newInstance(
            filtersMap: HashMap<String, MutableList<String>>,
            source: String = "CLP",
            assortmentId: String = "",
        ): FilterDialogFragment =
            FilterDialogFragment().apply {
                arguments = bundleOf(
                    FILTERS to filtersMap,
                    ASSORTMENT_ID to assortmentId,
                    SOURCE to source
                )
            }
    }

    private lateinit var adapter: WidgetLayoutAdapter
    private var filtersMap = hashMapOf<String, MutableList<String>>()
    private var source: String = ""
    private var assortmentId: String = ""

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFiltersBinding {
        return DialogFiltersBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName() = TAG

    override fun provideViewModel(): FilterViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        filtersMap =
            arguments?.getSerializable(FILTERS) as? HashMap<String, MutableList<String>>
                ?: hashMapOf()
        source = arguments?.getString(SOURCE) ?: "CLP"
        assortmentId = arguments?.getString(ASSORTMENT_ID) ?: ""

        setUpRecyclerView()
        binding.tvApply.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.VIDEO_PAGE_PLAYLIST_CLOSE,
                    hashMapOf()
                )
            )
            //TODO("remove")
            if (actionPerformer == null) showMessage("Apply filter not working")
            actionPerformer?.performAction(OnCategoryFilterApplied(filtersMap))
            dismiss()
        }
        viewModel.getFilters(source, assortmentId, filtersMap)
        filtersMap.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    fun setActionPerformer(actionPerformer: ActionPerformer) {
        this.actionPerformer = actionPerformer
    }

    private fun setUpRecyclerView() {
        adapter = WidgetLayoutAdapter(requireContext(), this)
        binding.rvWidgets.adapter = adapter
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.filterLiveData.observeK(
            this,
            this::onSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )
    }

    private fun onSuccess(filterData: FilterData?) {
        binding.tvApply.text = filterData?.submitText.orEmpty()
        binding.tvApply.setTextColor(
            Utils.parseColor(filterData?.submitTextColor ?: "#ea532c")
        )
        binding.tvApply.textSize = filterData?.submitTextSize?.toFloat() ?: 14f
        binding.tvTitle.text = filterData?.title.orEmpty()
        binding.tvTitle.setTextColor(
            Utils.parseColor(filterData?.titleColor ?: "#000000")
        )
        binding.tvTitle.textSize = filterData?.titleSize?.toFloat() ?: 16f
        binding.tvClear.text = filterData?.clearText.orEmpty()
        binding.tvClear.textSize = filterData?.clearTextSize ?: 12f
        binding.tvClear.setTextColor(
            Utils.parseColor(filterData?.clearTextColor ?: "#000000")
        )
        binding.tvClear.setOnClickListener {
            filtersMap.clear()
            viewModel.getFilters(source, assortmentId, filtersMap)
        }
        adapter.setWidgets(filterData?.widgets.orEmpty())
        filterData?.widgets?.filterIsInstance<FilterButtonWidgetModel>().apply {
            this?.forEach {
                it.data.filterItems?.forEach { item ->
                    if (item.isSelected == true && it.data.filterKey.isNotNullAndNotEmpty()
                        && item.filterId.isNotNullAndNotEmpty()
                    ) {
                        handleMultiSelectUnSelectFilterLogic(it.data.filterKey!!, item.filterId!!)
                    }
                }
            }
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        context?.let {
            if (NetworkUtils.isConnected(it).not()) {
                toast(it.getString(R.string.string_noInternetConnection))
            } else {
                toast(it.getString(R.string.somethingWentWrong))
            }
        }
    }

    private fun updateProgress(state: Boolean) {
        binding.frameLayout.setVisibleState(state)
        binding.progressBar.setVisibleState(state)
    }

    override fun performAction(action: Any) {
        when (action) {
            is OnFilterButtonClicked -> {
                filtersMap[action.key] = mutableListOf(action.value)
            }
            is OnMultiSelectFilterButtonClicked -> {
                handleMultiSelectUnSelectFilterLogic(action.key, action.value)
            }
        }
        viewModel.getFilters(source, assortmentId, filtersMap)
        filtersMap.clear()
    }

    private fun handleMultiSelectUnSelectFilterLogic(key: String, value: String) {
        if (filtersMap.containsKey(key)) {
            if (filtersMap[key]!!.contains(value))
                filtersMap[key]!!.remove(value)
            else
                filtersMap[key]!!.add(value)
        } else {
            filtersMap[key] = mutableListOf(value)
        }
    }
}