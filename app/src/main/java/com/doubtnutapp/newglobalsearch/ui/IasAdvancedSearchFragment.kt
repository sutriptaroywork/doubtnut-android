package com.doubtnutapp.newglobalsearch.ui

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.base.FilterOptionClick
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.FragmentIasAdvancedSearchBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.hide
import com.doubtnutapp.newglobalsearch.model.ApiSearchAdvanceFilterResponse
import com.doubtnutapp.newglobalsearch.model.NewSearchDataItem
import com.doubtnutapp.newglobalsearch.viewmodel.InAppSearchViewModel
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingFragment
import dagger.android.support.AndroidSupportInjection

class IasAdvancedSearchFragment : BaseBindingFragment<DummyViewModel, FragmentIasAdvancedSearchBinding>(),
    ActionPerformer, FilterOptionsBottomSheetFragment.OnFilterSelectedListener {

    private var isClassSelected: Boolean = false
    private var isFilterCalled: Boolean = false

    private lateinit var mApiSearchAdvanceFilterResponse: ApiSearchAdvanceFilterResponse
    private lateinit var parentViewModel: InAppSearchViewModel

    companion object {
        private const val TAG = "IasAdvancedSearchFragment"
        private const val IS_YOUTUBE = "isYoutube"


        @JvmStatic
        fun newInstance(
                isYoutube: Boolean
        ) =
                IasAdvancedSearchFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean(IS_YOUTUBE, isYoutube)
                    }
                }
    }

    private var isYoutube: Boolean = false
    private var filterType: String = ""

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun provideViewBinding(inflater: LayoutInflater, container: ViewGroup?):
            FragmentIasAdvancedSearchBinding = FragmentIasAdvancedSearchBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel = activityViewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            isYoutube = it.getBoolean(IasAdvancedSearchFragment.IS_YOUTUBE, false)
        }
        parentViewModel = activityViewModelProvider(viewModelFactory)
        setUpListener()
        setUpObserver()
    }

    private fun setUpListener() {
        binding.btnApplyFilter.setOnClickListener {
            binding.progressFilter.show()
            parentViewModel.postAdvanceFilterAppliedEvent()
            parentViewModel.getAdvancedSearchNewResults("", false, false,
                    null, mApiSearchAdvanceFilterResponse.tabType.orEmpty())
        }

        binding.closeAllFilterScreen.setOnClickListener {
            parentViewModel.filterOptionsMap.clear()
            parentViewModel.isAdvancedSearchPopupShown = false
            activity?.onBackPressed()
        }

        binding.clearFiters.setOnClickListener {
            parentViewModel.filterOptionsMap.clear()
            setUI(mApiSearchAdvanceFilterResponse)
        }

    }

    override fun onResume() {
        super.onResume()
        if (view == null) {
            return
        }
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { v, keyCode, event ->
            if (event.action === KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                parentViewModel.filterOptionsMap.clear()
                parentViewModel.isAdvancedSearchPopupShown = false
                activity?.onBackPressed()
                true
            } else false
        }
    }

    private fun setUpObserver() {
        parentViewModel.advancedSearchResponse.observeK(
                this,
                ::onAdvancedSearchSuccess
        )

        parentViewModel.filterOptionResponse.observeK(
                this,
                ::onFilterOptionSuccess
        )

        parentViewModel.userNewSearchLiveData.observeK(
                this,
                ::onUserSearchSuccessNew

        )
    }

    private fun onUserSearchSuccessNew(newSearchDataItem: NewSearchDataItem) {
        binding.progressFilter.hide()
        if (parentViewModel.isAdvancedSerachDataRequest) {
            parentViewModel.filterOptionsMap.clear()
            activity?.onBackPressed()
        }
    }

    private fun onFilterOptionSuccess(apiSearchAdvanceFilterResponse: ApiSearchAdvanceFilterResponse) {
        if (isFilterCalled) {
            if (apiSearchAdvanceFilterResponse?.list!!.isNotEmpty()) {
                val filterOptionsBottomSheetFragment =
                        FilterOptionsBottomSheetFragment.newInstance(filterType)
                filterOptionsBottomSheetFragment.setFilterSelectedListener(this)
                filterOptionsBottomSheetFragment.show(
                        childFragmentManager,
                        FilterOptionsBottomSheetFragment.TAG
                )
            }
        }
    }

    private fun onAdvancedSearchSuccess(apiSearchAdvanceFilterResponse: ApiSearchAdvanceFilterResponse) {
        mApiSearchAdvanceFilterResponse = apiSearchAdvanceFilterResponse
        if (apiSearchAdvanceFilterResponse?.list?.isEmpty() == false) {
            setUI(apiSearchAdvanceFilterResponse)
        } else {
            Toast.makeText(requireActivity(), "No filters available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUI(apiSearchAdvanceFilterResponse: ApiSearchAdvanceFilterResponse) {
        binding.tvHeader.text = apiSearchAdvanceFilterResponse.title
        binding.filterContainer.removeAllViews()
        if (!isClassSelected) {
            for (item in apiSearchAdvanceFilterResponse.list!!) {
                for (filter in item?.list!!) {
                    if (filter?.isSelected) {
                        parentViewModel.filterOptionsMap.put(item?.key.toString(), filter?.value)
                        break
                    }
                }
            }
        }

        for (item in apiSearchAdvanceFilterResponse.list!!) {
            val layoutToInflate =
                    this.layoutInflater.inflate(R.layout.filter_dropdown_item,
                            null)
            val tvQuestion = layoutToInflate.findViewById<TextView>(R.id.tvQuestion);
            val rlOption = layoutToInflate.findViewById<RelativeLayout>(R.id.rlOption);
            val tvOption = layoutToInflate.findViewById<TextView>(R.id.tvOption);

            if (!parentViewModel.filterOptionsMap.isNullOrEmpty()
                    && parentViewModel.filterOptionsMap[item.key] != null
                    && parentViewModel.filterOptionsMap[item.key]?.toString()?.length!! > 0) {
                tvOption.text = parentViewModel.filterOptionsMap.get(item.key)?.toString()
            } else {
                tvOption.text = item.display.orEmpty()
            }
            tvQuestion.text = item.value

            rlOption.setOnClickListener {
                isFilterCalled = true
                filterType = item.key
                parentViewModel.getFilterListOptions(apiSearchAdvanceFilterResponse.tabType.toString(), item.key)
            }
            binding.filterContainer.addView(layoutToInflate)
        }

    }

    override fun performAction(action: Any) {
        when (action) {
            is FilterOptionClick -> {
            }
            else -> {
            }
        }
    }

    override fun onFilterSelected(type: String) {
        if (type.equals("class")) {
            isClassSelected = true
        }
        setUI(mApiSearchAdvanceFilterResponse)
    }

}