package com.doubtnutapp.doubtpecharcha.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentDialogBadgeRewardDoubtPeCharchaBinding
import com.doubtnutapp.doubtpecharcha.model.FilterData
import com.doubtnutapp.doubtpecharcha.viewmodel.DoubtCollectionViewModel
import com.doubtnutapp.hide
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class DoubtPeCharchaBadgeRewardDialog : BaseBindingBottomSheetDialogFragment<DoubtCollectionViewModel,FragmentDialogBadgeRewardDoubtPeCharchaBinding>() {

    companion object {
        const val TAG = "DoubtPeCharchaRewardDialog"
        const val BADGE_REWARD_RESPONSE = "badge_reward_response"

        fun newInstance() =
            DoubtPeCharchaBadgeRewardDialog().apply {
                val bundle = Bundle()
                this.arguments = bundle
            }
    }

    val widgetLayoutAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(requireContext(), null, "")
    }

    private lateinit var parentViewModel: DoubtCollectionViewModel

    override fun providePageName(): String {
        return  TAG
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDialogBadgeRewardDoubtPeCharchaBinding {
        return FragmentDialogBadgeRewardDoubtPeCharchaBinding.inflate(inflater, container, false)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = widgetLayoutAdapter
    }

    override fun provideViewModel(): DoubtCollectionViewModel {
       return activityViewModelProvider<DoubtCollectionViewModel>(viewModelFactory)
    }

    override fun setupObservers() {
        super.setupObservers()



        viewModel.userDoubtsLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {

                }
                is Outcome.Success -> {

                }
            }
        }

    }

}