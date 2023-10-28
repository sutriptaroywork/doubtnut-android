package com.doubtnutapp.freeclasses.bottomsheets

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.Keep
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.addIfNotPresentElseRemove
import com.doubtnutapp.base.ApplyFilters
import com.doubtnutapp.databinding.FragmentFilterListBottomSheetDialogBinding
import com.doubtnutapp.freeclasses.viewmodels.FilterListBottomSheetDialogVM
import com.doubtnutapp.freeclasses.widgets.FilterSortWidget
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName

/**
 * Created by Akshat Jindal on 31/01/22.
 */
class FilterListBottomSheetDialogFragment :
    BaseBindingBottomSheetDialogFragment<FilterListBottomSheetDialogVM,
            FragmentFilterListBottomSheetDialogBinding>() {

    companion object {
        const val TAG = "FilterListBottomSheetDi"
        const val TYPE = "type"
        const val FILTERS = "filters"
        const val ASSORTMENT_ID = "assortment_id"
        fun newInstance(
            type: FilterSortWidget.FilterType,
            assortmentId: String,
            filters: HashMap<String, MutableList<String>>
        ) = FilterListBottomSheetDialogFragment().apply {
            arguments = bundleOf(
                TYPE to type,
                ASSORTMENT_ID to assortmentId,
                FILTERS to filters
            )
        }
    }

    val filtersSelected = mutableListOf<String>()
    private var filtersMap: HashMap<String, List<String>> = hashMapOf()
    private var actionPerformer: ActionPerformer? = null
    private var type: FilterSortWidget.FilterType = FilterSortWidget.FilterType.NONE
    private var assortmentId: String = ""

    fun setActionPerformer(actionPerformer: ActionPerformer) {
        this.actionPerformer = actionPerformer
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        type = arguments?.getSerializable(TYPE) as? FilterSortWidget.FilterType
            ?: FilterSortWidget.FilterType.NONE
        assortmentId = arguments?.getString(ASSORTMENT_ID) ?: ""
        filtersMap =
            arguments?.getSerializable(FILTERS) as? HashMap<String, List<String>> ?: hashMapOf()
        viewModel.getData(type, assortmentId, filtersMap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFilterListBottomSheetDialogBinding.inflate(layoutInflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): FilterListBottomSheetDialogVM =
        viewModelProvider(viewModelFactory)

    override fun setupObservers() {
        viewModel.filterLiveData.observe(viewLifecycleOwner, {
            setUpData(it)
        })
        viewModel.progressState.observe(viewLifecycleOwner, {
            binding.progressBar.setVisibleState(it)
        })
    }

    @SuppressLint("ResourceType")
    private fun setUpData(data: FilterListData) {
        binding.apply {
            tvTitle.text = data.title
            buttonDone.text = data.cta
            buttonDone.setOnClickListener {
                actionPerformer?.performAction(
                    ApplyFilters(
                        type,
                        data.filterId.orEmpty(),
                        filtersSelected
                    )
                )
                dismiss()
            }
            when (type) {
                FilterSortWidget.FilterType.SORT,
                FilterSortWidget.FilterType.SUBJECT -> {
                    val layoutManager =
                        LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                    val adapter =
                        FilterListLinearAdapter(data.list.orEmpty()) { pos ->
                            handleListItemClicked(data.isMultiSelect, data.list.orEmpty(), pos)
                        }
                    recyclerView.layoutManager = layoutManager
                    recyclerView.adapter = adapter
                }
                FilterSortWidget.FilterType.CHAPTER -> {
                    val layoutManager = GridLayoutManager(requireContext(), 2)
                    val adapter =
                        FilterListGridAdapter(data.list.orEmpty()) { pos ->
                            handleListItemClicked(data.isMultiSelect, data.list.orEmpty(), pos)
                        }
                    recyclerView.layoutManager = layoutManager
                    recyclerView.adapter = adapter
                }
                else -> {
                    showMessage("Invalid Type")
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleListItemClicked(
        isMultiSelect: Boolean,
        list: List<FilterListData.FilterListItem>,
        pos: Int
    ) {
        if (isMultiSelect) {
            filtersSelected.addIfNotPresentElseRemove(list[pos].filterId.orEmpty())
            list[pos].isSelected = !list[pos].isSelected
        } else {
            filtersSelected.clear()
            filtersSelected.add(list[pos].filterId.orEmpty())
            list.forEachIndexed { index, filterListItem ->
                filterListItem.isSelected = (index == pos)
            }
        }
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }
}

@Keep
data class FilterListData(
    @SerializedName("title") val title: String?,
    @SerializedName("filter_id") val filterId: String?,
    @SerializedName("list") val list: List<FilterListItem>?,
    @SerializedName("cta") val cta: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("is_multi_select") val isMultiSelect: Boolean = false
) {
    data class FilterListItem(
        @SerializedName("title") val title: String?,
        @SerializedName("filter_id") val filterId: String?,
        @SerializedName("is_selected") var isSelected: Boolean = false,
    )
}