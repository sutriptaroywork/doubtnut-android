package com.doubtnut.referral.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.core.actions.OnReferNowCtaClicked
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.ui.base.CoreBindingFragment
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.gone
import com.doubtnut.core.utils.isNotNullAndNotEmpty2
import com.doubtnut.core.utils.toastApiError
import com.doubtnut.core.widgets.IWidgetLayoutAdapter
import com.doubtnut.referral.databinding.FragmentReferAndEarnFaqBinding
import javax.inject.Inject

class ReferAndEarnFAQFragment :
    CoreBindingFragment<ReferAndEarnViewModel, FragmentReferAndEarnFaqBinding>(), ActionPerformer {

    companion object {
        const val TAG = "ReferAndEarnFAQPage"
        const val EVENT_TAG_PAGE_VISIT = "ReferQA_page_visit"
    }

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    var isShareSheetShown = false
    var lastOpenedTSheetTime: Long = 0L

    private var message: String? = null
    private var imageShare: String? = null

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentReferAndEarnFaqBinding {
        return FragmentReferAndEarnFaqBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): ReferAndEarnViewModel {
        return activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        viewModel.getReferAndEarnFAQData()

        binding.imgBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EVENT_TAG_PAGE_VISIT,
                hashMapOf("source" to TAG)
            )
        )

        binding.imgBack.setColorFilter(Color.WHITE)


    }


    private fun launchShareSheet(messageStr: String?, imageShare: String?) {
        if (!isShareSheetShown || System.currentTimeMillis() - lastOpenedTSheetTime > 1500) {
            if(messageStr.isNotNullAndNotEmpty2()) {
                UiHelper.launchShareSheetWithImage(
                    requireContext(),
                    messageStr!!,
                    imageShare
                )
                isShareSheetShown = true
                lastOpenedTSheetTime = System.currentTimeMillis()
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.liveDataReferAndEarnFAQ.observe(
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
                        (binding.rvWidgets.adapter as? IWidgetLayoutAdapter)?.actionPerformer = this
                        if (outcome.data.cta != null) {
                            binding.buttonReferFriend.text = outcome.data.cta?.title.orEmpty()
                            binding.buttonReferFriend.setOnClickListener {
                                launchShareSheet(
                                    response.cta?.messageWhatsapp!!,
                                    response.cta.imageShare
                                )

                                analyticsPublisher.publishEvent(
                                    AnalyticsEvent(
                                        ReferAndEarnHomeFragment.EVENT_TAG_REFER_BUTTON_CLICKED,
                                        hashMapOf("source" to TAG)
                                    )
                                )
                            }
                        }
                        message = response.cta?.messageWhatsapp
                        imageShare = response.cta?.imageShare

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

    override fun performAction(action: Any) {
        if (action is OnReferNowCtaClicked) {
            launchShareSheet(
                message,
                imageShare
            )
        }
    }

}