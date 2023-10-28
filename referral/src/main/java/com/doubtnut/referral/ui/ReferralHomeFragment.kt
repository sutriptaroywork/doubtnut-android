package com.doubtnut.referral.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.doubtnut.core.actions.ItemClick
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.ui.base.CoreBindingFragment
import com.doubtnut.core.ui.listeners.TagsEndlessRecyclerOnScrollListener
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.IWidgetLayoutAdapter
import com.doubtnut.referral.R
import com.doubtnut.referral.data.entity.ButtonData
import com.doubtnut.referral.databinding.FragmentReferralHomeBinding
import java.net.URLEncoder
import javax.inject.Inject

class ReferralHomeFragment :
    CoreBindingFragment<ReferralHomeFragmentVM, FragmentReferralHomeBinding>(), ActionPerformer {

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    private var page = 0
    private var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener? = null
    private var activityViewModel: ReferralActivityVM? = null

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentReferralHomeBinding {
        return FragmentReferralHomeBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): ReferralHomeFragmentVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun provideActivityViewModel() {
        activityViewModel = activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        (binding.rvMain.adapter as? IWidgetLayoutAdapter)?.actionPerformer = this
        (binding.rvMain.adapter as? IWidgetLayoutAdapter)?.source = TAG

        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(binding.rvMain.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    page++
                    getReferralInfo()
                }
            }
        binding.rvMain.addOnScrollListener(infiniteScrollListener ?: return)
        getReferralInfo()
    }

    fun getReferralInfo() {
        viewModel.getReferralInfo(
            page = page
        )
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.referralInfoResponse.observeNonNull(viewLifecycleOwner) { outcome ->

            mBinding ?: return@observeNonNull

            when (outcome) {
                is Outcome.Progress -> {
                    infiniteScrollListener?.setDataLoading(outcome.loading)
                    binding.progressBar.isVisible = outcome.loading
                }
                is Outcome.Success -> {
                    val response = outcome.data

                    if (outcome.data.widgets.isNullOrEmpty()) {
                        infiniteScrollListener?.isLastPageReached = true
                    }

                    if (response.deeplink.isNullOrEmpty().not()) {
                        deeplinkAction.performAction(requireContext(), response.deeplink)
                        if (response.finishActivity == true) {
                            requireActivity().finish()
                        }
                        return@observeNonNull
                    }

                    (binding.rvMain.adapter as? IWidgetLayoutAdapter)?.addWidgets(outcome.data.widgets.orEmpty())

                    activityViewModel?.updateTitle(outcome.data.title)
                    activityViewModel?.updateMobile(outcome.data.mobile)

                    binding.clFooter.isVisible = outcome.data.buttonData != null
                    outcome.data.buttonData?.let { buttonData ->
                        binding.btnCta.setTextFromHtml(buttonData.text.orEmpty())
                        binding.btnCta.applyBackgroundTint(buttonData.bgColor)
                        if (buttonData.icon.isNullOrEmpty()) {
                            binding.btnCta.icon = null
                        } else {
                            binding.btnCta.loadIntoCustomViewTarget(
                                url = buttonData.icon,
                                _onResourceReady = {
                                    lifecycleScope.launchWhenResumed {
                                        binding.btnCta.icon = it
                                    }
                                },
                                _onLoadFailed = {
                                    lifecycleScope.launchWhenResumed {
                                        binding.btnCta.icon = it
                                    }

                                },
                                _onResourceCleared = {
                                    lifecycleScope.launchWhenResumed {
                                        binding.btnCta.icon = it
                                    }
                                }
                            )
                        }

                        binding.btnCta.setOnClickListener {
                            var hasPermission = false
                            var isWhatsappLaunched = false
                            var isPermissionRequested = false

                            when {
                                ContextCompat.checkSelfPermission(
                                    requireContext(),
                                    Manifest.permission.READ_CONTACTS
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    hasPermission = true
                                    ShareYourReferralCodeBottomSheetDialogFragment.isCalledAfterGrantPermission =
                                        false
                                    launchShareYourReferralCodeBottomSheetDialogFragment(buttonData)
                                }
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                    requireActivity(),
                                    Manifest.permission.READ_CONTACTS
                                ) -> {
                                    isWhatsappLaunched = true
                                    val message = URLEncoder.encode(
                                        buttonData.shareMessage ?: return@setOnClickListener,
                                        "UTF-8"
                                    )
                                    launchWhatsapp(
                                        deeplink = "doubtnutapp://whatsapp?external_url=https://api.whatsapp.com/send?text=$message",
                                        isGranted = null
                                    )
                                }
                                else -> {
                                    isPermissionRequested = true
                                    requestContactPermission.launch(Manifest.permission.READ_CONTACTS)
                                }
                            }

                            analyticsPublisher.publishEvent(
                                AnalyticsEvent(
                                    "${EVENT_TAG}_${CoreEventConstants.CTA_CLICKED}",
                                    hashMapOf<String, Any>(
                                        CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                                        CoreEventConstants.CTA_TEXT to buttonData.text.orEmpty(),
                                        CoreEventConstants.HAS_PERMISSION to hasPermission,
                                        CoreEventConstants.IS_WA_LAUNCHED to isWhatsappLaunched,
                                        CoreEventConstants.IS_PERMISSION_REQUESTED to isPermissionRequested,
                                    ).apply {
                                        putAll(outcome.data.extraParams.orEmpty())
                                    }, ignoreMoengage = false
                                )
                            )
                        }
                    }

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            "${EVENT_TAG}_${CoreEventConstants.VIEWED}",
                            hashMapOf<String, Any>(
                                CoreEventConstants.ID to id,
                                CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                                CoreEventConstants.PAGE to page,
                            ).apply {
                                putAll(outcome.data.extraParams.orEmpty())
                            }
                        )
                    )
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
    }

    private val requestContactPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val buttonData =
                (viewModel.referralInfoResponse.value as? Outcome.Success)?.data?.buttonData
                    ?: return@registerForActivityResult
            when {
                isGranted -> {
                    ShareYourReferralCodeBottomSheetDialogFragment.isCalledAfterGrantPermission =
                        true
                    launchShareYourReferralCodeBottomSheetDialogFragment(buttonData)
                }
                else -> {
                    val message = URLEncoder.encode(
                        buttonData.shareMessage ?: return@registerForActivityResult,
                        "UTF-8"
                    )
                    launchWhatsapp(
                        deeplink = "doubtnutapp://whatsapp?external_url=https://api.whatsapp.com/send?text=$message",
                        isGranted = false
                    )
                }
            }
        }

    private fun launchShareYourReferralCodeBottomSheetDialogFragment(buttonData: ButtonData) {
        ShareYourReferralCodeBottomSheetDialogFragment.newInstance(
            buttonData
        )
            .show(
                requireActivity().supportFragmentManager,
                ShareYourReferralCodeBottomSheetDialogFragment.TAG
            )
    }

    private fun launchWhatsapp(deeplink: String, isGranted: Boolean?) {
        deeplinkAction.performAction(
            requireContext(),
            deeplink
        )

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                "${EVENT_TAG}_${CoreEventConstants.WHATSAPP_LAUNCHED}",
                hashMapOf(
                    CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                    CoreEventConstants.IS_PERMISSION_GRANTED to isGranted.toString(),
                )
            )
        )
    }

    private fun shakeCtaButton() {
        binding.btnCta.startAnimation(
            AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.anim_shake
            )
        )
    }

    companion object {
        private const val TAG = "ReferralHomeFragment"
        private const val EVENT_TAG = "referral_home_fragment"

        const val ACTION_SHAKE_CTA = "action_shake_cta"
    }

    override fun performAction(action: Any) {
        when (action) {
            is ItemClick -> {
                if (action.item == ACTION_SHAKE_CTA) {
                    shakeCtaButton()
                }
            }
        }
    }
}