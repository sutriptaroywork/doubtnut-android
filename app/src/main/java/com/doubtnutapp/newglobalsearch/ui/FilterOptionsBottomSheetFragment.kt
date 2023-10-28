package com.doubtnutapp.newglobalsearch.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.base.FilterOptionClick
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.BottomSheetFilterOptionsBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilterItem
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.newglobalsearch.model.ApiSearchAdvanceFilterResponse
import com.doubtnutapp.newglobalsearch.ui.adapter.FilterOptionAdapter
import com.doubtnutapp.newglobalsearch.viewmodel.InAppSearchViewModel
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import javax.inject.Inject


class FilterOptionsBottomSheetFragment() :
        BaseBindingBottomSheetDialogFragment<DummyViewModel, BottomSheetFilterOptionsBinding>(),
    ActionPerformer {

    private lateinit var mApiSearchAdvanceFilterResponse: ApiSearchAdvanceFilterResponse
    private lateinit var onFilterSelectedListener: OnFilterSelectedListener

    companion object {

        const val TAG = "FilterOptionsBottomSheetFragment"

        private const val ARG_TYPE = "type"

        fun newInstance(type: String) = FilterOptionsBottomSheetFragment().apply {
            val bundle = Bundle()
            bundle.putString(ARG_TYPE, type)
            arguments = bundle
        }
    }

    private val type: String? by lazy { arguments?.getString(ARG_TYPE) }

    private lateinit var parentViewModel: InAppSearchViewModel

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val filterOptionAdapter: FilterOptionAdapter by lazy {
        FilterOptionAdapter(this)
    }

    override fun provideViewBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ): BottomSheetFilterOptionsBinding =
            BottomSheetFilterOptionsBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel = viewModelProvider(viewModelFactory)

    override fun provideActivityViewModel() {
        parentViewModel = activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUI()
    }

    private fun setUI() {

        binding.editSearchBox.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
            ) {
                if (s.toString().isNotEmpty()) {
                    val filteredList = (mApiSearchAdvanceFilterResponse?.list?.get(0)?.list as ArrayList<SearchFilterItem>).filter {
                        it.value.toLowerCase().contains(s.toString().toLowerCase())
                    }
                    if (filteredList.isNotEmpty()) {
                        filterOptionAdapter.updateData(filteredList as ArrayList<SearchFilterItem>)
                    }
                } else {
                    filterOptionAdapter.updateData(mApiSearchAdvanceFilterResponse?.list?.get(0)?.list as ArrayList<SearchFilterItem>)
                }
            }
        })
    }

    override fun setupObservers() {
        parentViewModel.filterOptionResponse.observeK(
                this,
                ::onFilterOptionSuccess
        )
    }

    private fun onFilterOptionSuccess(apiSearchAdvanceFilterResponse: ApiSearchAdvanceFilterResponse) {
        mApiSearchAdvanceFilterResponse = apiSearchAdvanceFilterResponse
        binding.editSearchBox.hint = "Search "
        setUpList(apiSearchAdvanceFilterResponse)

    }

    private fun setUpList(apiSearchAdvanceFilterResponse: ApiSearchAdvanceFilterResponse) {
        if (apiSearchAdvanceFilterResponse?.list?.get(0)?.list?.isEmpty() == true) {
            dismiss()
        } else {
            mBinding?.apply {
                val linearLayoutManager = LinearLayoutManager(requireContext())
                val dividerItemDecoration =
                        DividerItemDecoration(requireContext(), linearLayoutManager.orientation)
                mBinding?.gridOtherList?.addItemDecoration(dividerItemDecoration)
                mBinding?.gridOtherList?.layoutManager = linearLayoutManager
                mBinding?.gridOtherList?.adapter = filterOptionAdapter
                filterOptionAdapter.updateData(apiSearchAdvanceFilterResponse?.list?.get(0)?.list as ArrayList<SearchFilterItem>)
                bottomSheetHeader?.text = apiSearchAdvanceFilterResponse.list?.get(0).display.orEmpty()

            }
        }
    }


    override fun performAction(action: Any) {
        when (action) {
            is FilterOptionClick -> {
                parentViewModel.filterOptionsMap.put(type.orEmpty(), action.key)
                onFilterSelectedListener.onFilterSelected(type.orEmpty())
                dismiss()
            }
            else -> {
            }
        }
    }

    fun setFilterSelectedListener(onFilterSelectedListener: OnFilterSelectedListener) {
        this.onFilterSelectedListener = onFilterSelectedListener
    }

    interface OnFilterSelectedListener {
        fun onFilterSelected(type: String)
    }

}