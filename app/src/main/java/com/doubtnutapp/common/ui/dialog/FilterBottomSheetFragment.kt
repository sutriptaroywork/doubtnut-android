package com.doubtnutapp.common.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R
import com.doubtnutapp.base.OnFilterButtonClicked
import com.doubtnutapp.base.OnFilterSelected
import com.doubtnutapp.common.model.FilterListData
import com.doubtnutapp.databinding.FragmentFilterListBottomSheetDialogBinding
import com.doubtnutapp.doubtpecharcha.model.Filter
import com.doubtnutapp.doubtpecharcha.model.FilterData
import com.doubtnutapp.doubtpecharcha.model.FilterType
import com.doubtnutapp.liveclasshome.ui.FilterItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_sg_chat.*

class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "FilterBottomSheetFragment"
        const val FILTER_TYPE = "filter_type"
        const val FILTER_ITEMS = "items"
        const val SELECTED_ITEMS = "selected_items"
        const val TITLE = "title"

        fun newInstance(
            title: String, listItems: java.util.ArrayList<FilterListData.FilterListItem>,
            filterType: FilterType, selectedFilters: java.util.ArrayList<Filter>
        ) =
            FilterBottomSheetFragment().apply {
                val bundle = Bundle()
                bundle.putString(TITLE, title)
                bundle.putParcelable(FILTER_TYPE, filterType)
                bundle.putParcelableArrayList(FILTER_ITEMS, listItems)
                bundle.putParcelableArrayList(SELECTED_ITEMS, selectedFilters)
                arguments = bundle
            }
    }

    fun setActionPerformer(actionPerformer: ActionPerformer) {
        this.actionPerformer = actionPerformer
    }

    val title: String? by lazy {
        arguments?.getString(TITLE)
    }

    val filterType: FilterType? by lazy {
        arguments?.getParcelable(FILTER_TYPE)
    }
    val filters: java.util.ArrayList<FilterListData.FilterListItem>? by lazy {
        arguments?.getStringArrayList(FILTER_ITEMS) as java.util.ArrayList<FilterListData.FilterListItem>
    }

    val selectedFilters: java.util.ArrayList<Filter>? by lazy {
        arguments?.getParcelableArrayList(SELECTED_ITEMS)
    }

    private var actionPerformer: ActionPerformer? = null

    private var currentlySelectedFilters: ArrayList<FilterListData.FilterListItem> = ArrayList(0)

    private var binding: FragmentFilterListBottomSheetDialogBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFilterListBottomSheetDialogBinding.inflate(inflater, container, false)

        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        setUpView()
    }

   private fun setUpView() {

        val layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
        val selectedFiltersCopy = ArrayList<Filter>(selectedFilters)
        val adapter =
            FilterListDataAdapter(filters!!, selectedFiltersCopy) { pos ->
                val itemSelected = filters!![pos]
                if (currentlySelectedFilters.contains(itemSelected)) {
                    currentlySelectedFilters.remove(itemSelected)
                    filters!![pos].isSelected = false
                } else {
                    currentlySelectedFilters.add(itemSelected)
                    filters!![pos].isSelected = true
                }
                if (doesContainFilter(selectedFiltersCopy, itemSelected.filterId)) {
                    removeFilter(selectedFiltersCopy, itemSelected.filterId!!)
                } else {
                    selectedFiltersCopy.add(Filter(itemSelected.title, itemSelected.filterId))
                }
                filters!!.forEachIndexed { position, listItem ->
                    listItem.isSelected = currentlySelectedFilters.contains(listItem)
                }
                binding?.recyclerView?.adapter?.notifyDataSetChanged()
            }
        selectedFilters?.forEach {
            currentlySelectedFilters.add(
                FilterListData.FilterListItem(
                    title = it.title,
                    filterId = it.id,
                    true
                )
            )
        }
        binding?.recyclerView?.layoutManager = layoutManager
        binding?.recyclerView?.adapter = adapter
        binding?.tvTitle?.text = title

        binding?.buttonCta?.visibility = View.VISIBLE
        binding?.buttonCta?.setOnClickListener {
            if (currentlySelectedFilters.size >= 0) {
                actionPerformer?.performAction(
                    OnFilterSelected(
                        filterType,
                        currentlySelectedFilters
                    )
                )
            }
            dismiss()
        }

        binding?.buttonDone?.text = getString(R.string.reset)
        binding?.buttonDone?.setOnClickListener {
            currentlySelectedFilters.clear()
            actionPerformer?.performAction(
                OnFilterSelected(
                    filterType,
                    currentlySelectedFilters
                )
            )
            dismiss()
        }
    }

    private fun removeFilter(listFilters: ArrayList<Filter>, filterId: String) {
        var position = -1
        for (i in 0 until listFilters.size) {
            if (listFilters[i].id == filterId) {
                position = i
            }
        }
        if (position >= 0) {
            listFilters.removeAt(position)
        }
    }

    private fun doesContainFilter(listFilters: ArrayList<Filter>, filterId: String?): Boolean {
        if (filterId == null) {
            return false
        }
        var found = false
        listFilters.forEach {
            if (it.id == filterId) {
                found = true
                return@forEach
            }
        }
        return found
    }
}