package com.doubtnutapp.matchquestion.ui.fragment.bottomsheet

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.core.utils.immediateParentViewModelStoreOwner
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentMatchFilterBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.matchquestion.listener.FilterDataListener
import com.doubtnutapp.matchquestion.model.MatchFilterFacetViewItem
import com.doubtnutapp.matchquestion.ui.adapter.MatchFilterAdapter
import com.doubtnutapp.matchquestion.viewmodel.MatchQuestionViewModel
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment

/**
 * Created by Sachin Saxena on 2020-06-01.
 */
class AdvanceSearchBottomSheetFragment :
    BaseBindingBottomSheetDialogFragment<DummyViewModel, FragmentMatchFilterBinding>(),
    FilterDataListener {

    companion object {
        const val TAG = "AdvanceSearchBottomSheetFragment"
        fun newInstance(): AdvanceSearchBottomSheetFragment = AdvanceSearchBottomSheetFragment()
    }

    private lateinit var matchQuestionViewModel: MatchQuestionViewModel

    private var updateList = mutableListOf<MatchFilterFacetViewItem>()

    private var isAdvanceSearchOptionSelected = false

    private val filterAdapter: MatchFilterAdapter by lazy { MatchFilterAdapter(this) }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMatchFilterBinding {
        return FragmentMatchFilterBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel = viewModelProvider(viewModelFactory)

    override fun provideActivityViewModel() {
        matchQuestionViewModel =
            ViewModelProvider(immediateParentViewModelStoreOwner, viewModelFactory).get(
                MatchQuestionViewModel::class.java
            )
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpRecyclerView()
        setUpClickListeners()
        dialog?.setOnShowListener(activity as? DialogInterface.OnShowListener)
    }

    override fun setupObservers() {
        super.setupObservers()
        matchQuestionViewModel.filterFacetLiveData.observe(
            viewLifecycleOwner,
            Observer { filterList ->
                filterList.forEachIndexed { index, facet ->
                    updateList.add(index, facet.copy())
                    val topicList = facet.data.map { topic ->
                        topic.copy()
                    }
                    updateList[index].data = topicList
                }
                filterAdapter.updateFilter(updateList)
            })
    }

    private fun setUpClickListeners() {
        mBinding?.search?.setOnClickListener {
            if (!isAdvanceSearchOptionSelected) {
                toast(getString(R.string.err_option_not_selected))
                return@setOnClickListener
            }
            matchQuestionViewModel.filterMatchResult(updateList)
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        matchQuestionViewModel.sendEventForFilterSelection(updateList)
        (activity as? DialogInterface.OnDismissListener)?.onDismiss(dialog)
    }

    private fun setUpRecyclerView() {
        mBinding?.filterRecyclerView?.adapter = filterAdapter
    }

    override fun onUpdate(
        topicPosition: Int,
        isTopicSelected: Boolean,
        toRefresh: Boolean,
        facetPosition: Int
    ) {
        updateFilterList(topicPosition, isTopicSelected, facetPosition)
    }

    private fun updateFilterList(topicPosition: Int, isTopicSelected: Boolean, facetPosition: Int) {

        if (updateList.size <= facetPosition) return

        if (updateList[facetPosition].data.size <= topicPosition) return

        if (updateList[facetPosition].isMultiSelect) {
            updateList[facetPosition].data[topicPosition].isSelected = isTopicSelected
        } else {
            if (isTopicSelected) {
                updateList[facetPosition].data.forEachIndexed { index, matchFilterTopic ->
                    updateList[facetPosition].data[index].isSelected = index == topicPosition
                }
            } else {
                updateList[facetPosition].data[topicPosition].isSelected = isTopicSelected
            }
        }
        updateList.forEach { facet ->
            val topic = facet.data.find { topic ->
                topic.isSelected
            }
            facet.isSelected = topic != null
        }
        isAdvanceSearchOptionSelected = true
    }
}
