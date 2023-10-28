package com.doubtnut.referral.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.core.ui.base.CoreBindingFragment
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.gone
import com.doubtnut.core.utils.toastApiError
import com.doubtnut.core.widgets.IWidgetLayoutAdapter
import com.doubtnut.referral.databinding.FragmentReferAndEarnHomeBinding
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

class ReferAndEarnHomeFragment :
    CoreBindingFragment<ReferAndEarnViewModel, FragmentReferAndEarnHomeBinding>() {

    @Inject
    lateinit var analyticsPublisher:IAnalyticsPublisher

    var isShareSheetShown = false
    var lastOpenedTSheetTime:Long = 0L

    companion object {
        const val TAG = "refer_and_earn_landing_page"
        const val EVENT_TAG_PAGE_VISIT="ReferQA_page_visit"
        const val EVENT_TAG_REFER_BUTTON_CLICKED="ReferQA_refer_friend_click"
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentReferAndEarnHomeBinding {
        return FragmentReferAndEarnHomeBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): ReferAndEarnViewModel {
        return activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        viewModel.getReferAndEarnData()
        binding.imgBack.setOnClickListener {
            requireActivity().finish()
        }

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EVENT_TAG_PAGE_VISIT,
                hashMapOf("source" to TAG)
            )
        )

        binding.imgBack.setColorFilter(Color.WHITE)
    }


    override fun setupObservers() {
        super.setupObservers()

        viewModel.liveDataReferAndEarn.observe(
            viewLifecycleOwner, { outcome ->
                when (outcome) {
                    is Outcome.Progress -> {
                        if (outcome.loading) {
                            binding.progressBar.visibility = View.VISIBLE
                        } else {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                    is Outcome.Success -> {
                        val response = outcome.data
                        (binding.rvWidgets.adapter as? IWidgetLayoutAdapter)?.addWidgets(outcome.data.listWidgets.orEmpty())
                        if (outcome.data.cta != null) {
                            binding.buttonReferFriend.text = outcome.data.cta?.title.orEmpty()
                            binding.buttonReferFriend.setOnClickListener {
                                if(!isShareSheetShown || System.currentTimeMillis()-lastOpenedTSheetTime>1500) {
                                    UiHelper.launchShareSheetWithImage(
                                        requireContext(),
                                        response.cta!!.messageWhatsapp.orEmpty(),
                                        response.cta.imageShare
                                    )
                                    analyticsPublisher.publishEvent(
                                        AnalyticsEvent(
                                            EVENT_TAG_REFER_BUTTON_CLICKED,
                                            hashMapOf("source" to TAG)
                                        )
                                    )
                                    isShareSheetShown=true
                                    lastOpenedTSheetTime = System.currentTimeMillis()
                                }
                            }
                        }

                    }
                    is Outcome.ApiError -> {
                        binding.progressBar.gone()
                        toastApiError(outcome.e)
                    }
                    is Outcome.BadRequest -> {
                        binding.progressBar.gone()
                    }
                    is Outcome.Failure -> {
                        binding.progressBar.gone()
                        toastApiError(outcome.e)
                    }
                }
            }
        )


    }
}