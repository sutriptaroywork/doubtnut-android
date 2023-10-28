package com.doubtnutapp.newglobalsearch.ui.dialog

import android.content.DialogInterface
import android.graphics.Point
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.databinding.FragmentFilterSelectionDialogBinding
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilterItem
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.newglobalsearch.model.SearchTabsItem
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchFilterValueDropDownAdapter
import com.doubtnutapp.ui.base.BaseBindingDialogFragment

class IasFilterSelectionDialogFragment : BaseBindingDialogFragment<DummyViewModel,FragmentFilterSelectionDialogBinding>() {

    private val locationOnScreen: Point
        get() = requireArguments().getParcelable(KEY_LOCATION_ON_SCREEN)!!

    private val facet: SearchTabsItem
        get() = requireArguments().getParcelable(KEY_FACET)!!

    private val isYoutube: Boolean
        get() = requireArguments().getBoolean(KEY_IS_FROM_YOUTUBE, false)!!

    private val isFromAllChapters: Boolean
        get() = requireArguments().getBoolean(KEY_IS_FROM_ALL_CHAPTERS, false)!!

    private var filterValues: ArrayList<SearchFilterItem> = arrayListOf()

    var actionPerformer: ActionPerformer? = null

    private fun init() {
        arguments?.let {
            filterValues =
                it.getParcelableArrayList<SearchFilterItem>(KEY_FILTER_VALUES) as ArrayList<SearchFilterItem>
        }

        binding.rvFilterSelection.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        val adapter =
            SearchFilterValueDropDownAdapter(actionPerformer, facet, isYoutube, isFromAllChapters)
        adapter.updateData(
            filterValues,
            requireArguments().getInt(KEY_IS_FROM_ALL_CHAPTERS, 0)!!,
            facet
        )
        binding.rvFilterSelection.adapter = adapter
        binding.rvFilterSelection.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        dialog?.window?.let { window ->
            window.attributes = window.attributes.apply {
                gravity = Gravity.TOP or Gravity.START
                x = locationOnScreen.x
                y = locationOnScreen.y
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        const val TAG = "FilterSelectionDialogFragment"
        const val KEY_FILTER_VALUES = "key_filter_values"
        const val KEY_FACET = "facet"
        const val KEY_LOCATION_ON_SCREEN = "location_on_screen"
        const val KEY_IS_FROM_YOUTUBE = "is_from_youtube"
        const val KEY_IS_FROM_ALL_CHAPTERS = "is_from_all_chapters"
        const val KEY_FILTER_TYPE_POSITION = "key_filter_type_position"

        fun newInstance(
            filterValues: ArrayList<SearchFilterItem>,
            facet: SearchTabsItem,
            filterTypePosition: Int,
            locationOnScreen: Point,
            isYoutube: Boolean = false,
            isFromAllChapters: Boolean = false
        ) =
            IasFilterSelectionDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(KEY_FILTER_VALUES, filterValues)
                    putParcelable(KEY_LOCATION_ON_SCREEN, locationOnScreen)
                    putParcelable(KEY_FACET, facet)
                    putBoolean(KEY_IS_FROM_YOUTUBE, isYoutube)
                    putBoolean(KEY_IS_FROM_ALL_CHAPTERS, isFromAllChapters)
                    putInt(KEY_FILTER_TYPE_POSITION, filterTypePosition)
                }
            }
    }

    override fun provideViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentFilterSelectionDialogBinding
    = FragmentFilterSelectionDialogBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel =  viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        activity ?: return
        init()
    }
}

