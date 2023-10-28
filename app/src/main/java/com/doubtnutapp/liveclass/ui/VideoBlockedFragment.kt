package com.doubtnutapp.liveclass.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.FragmentVideoBlockedBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.liveclass.viewmodel.VideoBlockedViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.videoPage.model.ViewAnswerData
import javax.inject.Inject

class VideoBlockedFragment :
    BaseBindingFragment<VideoBlockedViewModel, FragmentVideoBlockedBinding>() {

    companion object {
        private const val TAG = "VideoBlockedFragment"
        const val BLOCKED_DATA = "blocked_data"
        const val PAGE = "page"
        fun newInstance(viewAnswerData: ViewAnswerData, page: String)
                : VideoBlockedFragment = VideoBlockedFragment()
                .apply {
                    arguments = Bundle().apply {
                        putParcelable(BLOCKED_DATA, viewAnswerData)
                        putString(PAGE, page)
                    }
                }
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var viewAnswerData: ViewAnswerData? = null
    var page: String = ""
    private var isFirstTime: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = viewModelProvider(viewModelFactory)
        if (arguments?.getParcelable<ViewAnswerData>(BLOCKED_DATA) != null) {
            viewAnswerData = arguments?.getParcelable(BLOCKED_DATA)
            page = arguments?.getString(PAGE, "").toString()
        }
        initView()

    }

    private fun initView() {
        val premiumVideoBlockedData = viewAnswerData?.premiumVideoBlockedData

            mBinding?.ivBackFromVideoBlocked?.setOnClickListener {
            viewModel.postEvent(hashMapOf<String, Any>().apply {
                put(EventConstants.VIDEO_VIEW_ID, viewAnswerData?.viewId.orEmpty())
                put(EventConstants.COURSE_ID, viewAnswerData?.premiumVideoBlockedData?.courseId
                        ?: 0)
                put(EventConstants.PAID_USER, viewAnswerData?.isPremium == true && viewAnswerData?.isVip == true)
                put(EventConstants.CTA_VIEWED, true)
                put(EventConstants.CTA_CLICKED, 0)
                put(EventConstants.VIEW_FROM, page)
            })

            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.PAID_CONTENT_SEARCH_EVENTS, hashMapOf<String, Any>().apply {
                put(EventConstants.VIDEO_VIEW_ID, viewAnswerData?.viewId.orEmpty())
                put(EventConstants.COURSE_ID, viewAnswerData?.premiumVideoBlockedData?.courseId
                        ?: 0)
                put(EventConstants.PAID_USER, viewAnswerData?.isPremium == true && viewAnswerData?.isVip == true)
                put(EventConstants.CTA_VIEWED, true)
                put(EventConstants.CTA_CLICKED, 0)
                put(EventConstants.VIEW_FROM, page)
            }))
            if (activity is LiveClassActivity) {
                activity?.onBackPressed()
            } else {
                activity?.finish()
            }
        }
        mBinding?.textViewHeader?.text = premiumVideoBlockedData?.title.orEmpty()
        mBinding?.textViewSubHeader?.text = premiumVideoBlockedData?.description.orEmpty()
        mBinding?.button?.text = premiumVideoBlockedData?.courseDetailsButtonText.orEmpty()
        mBinding?.button?.background = Utils.getShape(
                premiumVideoBlockedData?.courseDetailsButtonBgColor ?: "##000000",
                premiumVideoBlockedData?.courseDetailsButtonCornerColor ?: "#eb532c",
                5f, 3)
        mBinding?.button?.setTextColor(Utils.parseColor(premiumVideoBlockedData?.courseDetailsButtonTextColor
                ?: "#eb532c"))

        mBinding?.btnAdLink?.text = premiumVideoBlockedData?.coursePurchaseButtonText.orEmpty()
        mBinding?.btnAdLink?.background = Utils.getShape(
                premiumVideoBlockedData?.coursePurchaseButtonBgColor ?: "#eb532c",
                premiumVideoBlockedData?.coursePurchaseButtonBgColor ?: "#eb532c",
                5f)
        mBinding?.btnAdLink?.setTextColor(Utils.parseColor(premiumVideoBlockedData?.coursePurchaseButtonTextColor
                ?: "#ffffff"))

        mBinding?.button?.setOnClickListener {
            viewModel.postEvent(hashMapOf<String, Any>().apply {
                put(EventConstants.VIDEO_VIEW_ID, viewAnswerData?.viewId.orEmpty())
                put(EventConstants.COURSE_ID, viewAnswerData?.premiumVideoBlockedData?.courseId
                        ?: 0)
                put(EventConstants.PAID_USER, viewAnswerData?.isPremium == true && viewAnswerData?.isVip == true)
                put(EventConstants.CTA_VIEWED, true)
                put(EventConstants.CTA_CLICKED, 1)
                put(EventConstants.VIEW_FROM, page)
            })


            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.PAID_CONTENT_SEARCH_EVENTS, hashMapOf<String, Any>().apply {
                put(EventConstants.VIDEO_VIEW_ID, viewAnswerData?.viewId.orEmpty())
                put(EventConstants.COURSE_ID, viewAnswerData?.premiumVideoBlockedData?.courseId
                        ?: 0)
                put(EventConstants.PAID_USER, viewAnswerData?.isPremium == true && viewAnswerData?.isVip == true)
                put(EventConstants.CTA_VIEWED, true)
                put(EventConstants.CTA_CLICKED, 1)
                put(EventConstants.VIEW_FROM, page)
            }))

            deeplinkAction.performAction(requireContext(), premiumVideoBlockedData?.courseDetailsButtonDeeplink)
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVideoBlockedBinding {
        return FragmentVideoBlockedBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): VideoBlockedViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (isFirstTime) {

                    viewModel.postEvent(hashMapOf<String, Any>().apply {
                        put(EventConstants.VIDEO_VIEW_ID, viewAnswerData?.viewId.orEmpty())
                        put(
                            EventConstants.COURSE_ID,
                            viewAnswerData?.premiumVideoBlockedData?.courseId
                                ?: 0
                        )
                        put(
                            EventConstants.PAID_USER,
                            viewAnswerData?.isPremium == true && viewAnswerData?.isVip == true
                        )
                        put(EventConstants.CTA_VIEWED, true)
                        put(EventConstants.CTA_CLICKED, 0)
                        put(EventConstants.VIEW_FROM, page)
                    })

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.PAID_CONTENT_SEARCH_EVENTS,
                            hashMapOf<String, Any>().apply {
                                put(EventConstants.VIDEO_VIEW_ID, viewAnswerData?.viewId.orEmpty())
                                put(
                                    EventConstants.COURSE_ID,
                                    viewAnswerData?.premiumVideoBlockedData?.courseId
                                        ?: 0
                                )
                                put(
                                    EventConstants.PAID_USER,
                                    viewAnswerData?.isPremium == true && viewAnswerData?.isVip == true
                                )
                                put(EventConstants.CTA_VIEWED, true)
                                put(EventConstants.CTA_CLICKED, 0)
                                put(EventConstants.VIEW_FROM, page)
                            })
                    )
                    isFirstTime = false
                }
                false
            } else false
        }
    }
}