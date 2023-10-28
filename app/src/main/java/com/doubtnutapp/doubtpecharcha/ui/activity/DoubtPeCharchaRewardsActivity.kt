package com.doubtnutapp.doubtpecharcha.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnut.core.utils.visible
import com.doubtnutapp.databinding.ActivityDoubtPackageBinding
import com.doubtnutapp.databinding.ActivityDoubtPeCharchaRewardsBinding
import com.doubtnutapp.doubtpecharcha.ui.fragment.DoubtPeCharchaRewardInfoBottomSheetFragment
import com.doubtnutapp.doubtpecharcha.viewmodel.DoubtPeCharchaRewardsViewModel
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.GradientBannerWithActionButtonWidget

class DoubtPeCharchaRewardsActivity :
    BaseBindingActivity<DoubtPeCharchaRewardsViewModel, ActivityDoubtPeCharchaRewardsBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "DoubtPeCharchaRewardsActivity"
    }

    val widgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(this, this, "")
    }

    private var rewardData: GradientBannerWithActionButtonWidget.RewardsData? = null

    override fun provideViewBinding(): ActivityDoubtPeCharchaRewardsBinding {
        return ActivityDoubtPeCharchaRewardsBinding.inflate(layoutInflater, null, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DoubtPeCharchaRewardsViewModel {
        return viewModelProvider<DoubtPeCharchaRewardsViewModel>(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        viewModel.getRewardsResponse()

        binding.tvFAQ.setOnClickListener {
            rewardData?.let {
                val rewardsBottomSheet = DoubtPeCharchaRewardInfoBottomSheetFragment.newInstance(it)
                rewardsBottomSheet.show(
                    supportFragmentManager,
                    DoubtPeCharchaRewardInfoBottomSheetFragment.TAG
                )
            }
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.ctaButton.setOnClickListener {

        }

    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.liveDataRewardsResponse.observe(this, {
            when (it) {
                is Outcome.Success -> {
                    binding.rvMain.layoutManager =
                        LinearLayoutManager(this@DoubtPeCharchaRewardsActivity)
                    binding.rvMain.adapter = widgetLayoutAdapter
                    it.data.widgets?.let { widgets ->
                        widgetLayoutAdapter.addWidgets(widgets)
                    }
                    binding.tvTitle.text = it.data.title.orEmpty()
                    rewardData = it.data.rewardFaqData
                    binding.tvFAQ.visibility = View.VISIBLE
                }

                is Outcome.Progress -> {
                    binding.progressBar.isVisible = it.loading
                }

                else -> {
                    showToast(this@DoubtPeCharchaRewardsActivity, "Error found")
                }
            }
        })
    }

    override fun performAction(action: Any) {

    }


}