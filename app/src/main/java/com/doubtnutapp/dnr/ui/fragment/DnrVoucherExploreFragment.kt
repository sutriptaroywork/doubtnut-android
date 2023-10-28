package com.doubtnutapp.dnr.ui.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.copy
import com.doubtnutapp.R
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.databinding.FragmentDnrVoucherExploreBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.dnr.model.DnrVoucherSource
import com.doubtnutapp.dnr.model.VoucherLoadingData
import com.doubtnutapp.dnr.model.VoucherState
import com.doubtnutapp.dnr.model.VoucherStateLayoutVisibility
import com.doubtnutapp.dnr.ui.adapter.DnrVoucherInfoItemAdapter
import com.doubtnutapp.dnr.viewmodel.DnrVoucherExploreViewModel
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.loadImage
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.extension.observeEvent
import javax.inject.Inject

class DnrVoucherExploreFragment :
    BaseBindingFragment<DnrVoucherExploreViewModel, FragmentDnrVoucherExploreBinding>() {

    companion object {
        private const val TAG = "DnrWidgetListFragment"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val navController by findNavControllerLazy()
    private val args by navArgs<DnrVoucherExploreFragmentArgs>()
    private val voucherId by lazy { args.voucherId ?: "" }
    private val redeemId by lazy { args.redeemId ?: "" }
    private val source by lazy { args.source ?: "" }

    private lateinit var countDownTimer: CountDownTimer
    private var totalTimerDuration = 0L
    private var counter = 0L

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDnrVoucherExploreBinding =
        FragmentDnrVoucherExploreBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DnrVoucherExploreViewModel {
        return activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        viewModel.sendEvent(
            EventConstants.DNR_VOUCHER_DETAIL_OPEN,
            hashMapOf(
                EventConstants.SOURCE to source,
                EventConstants.VOUCHER_ID to voucherId,
                EventConstants.REDEEM_ID to redeemId,
            ),
            ignoreSnowplow = true
        )
        when (source) {
            DnrVoucherSource.SPIN_THE_WHEEL.type, DnrVoucherSource.MYSTERY_BOX.type -> {
                if (viewModel.isVoucherRedeemed.not()) {
                    viewModel.redeemVoucher(
                        voucherId = voucherId,
                        source = source
                    )
                    viewModel.isVoucherRedeemed = true
                } else {
                    viewModel.getVoucherStateData(
                        voucherId = voucherId,
                        redeemId = redeemId,
                        source = source
                    )
                }
            }
            DnrVoucherSource.BETTER_LUCK_NEXT_TIME.type -> {
                viewModel.showBetterLuckNextTime()
                if (viewModel.isVoucherRedeemed.not()) {
                    viewModel.redeemVoucher(
                        voucherId = voucherId,
                        source = source
                    )
                    viewModel.isVoucherRedeemed = true
                } else {
                    viewModel.getVoucherStateData(
                        voucherId = voucherId,
                        redeemId = redeemId,
                        source = source
                    )
                }
            }

            else -> {
                viewModel.getVoucherStateData(
                    voucherId = voucherId,
                    redeemId = redeemId,
                    source = source
                )
            }
        }

        binding.toolbar.ivBack.setOnClickListener {
            mayNavigate {
                navController.navigateUp()
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.voucherStateLiveData.observeEvent(this) {
            updateVoucherState(it)
        }

        viewModel.initialInfoLiveData.observeEvent(this) {
            binding.toolbar.apply {
                val toolbarData = it.toolbarData
                tvTitle.text = toolbarData.title
                endLayout.isVisible = toolbarData.dnr.isNotNullAndNotEmpty()
                endLayout.setOnClickListener { v ->
                    if (it.toolbarData.deeplink != null) {
                        mayNavigate {
                            val deeplinkUri = Uri.parse(it.toolbarData.deeplink)
                            if (navController.graph.hasDeepLink(deeplinkUri)) {
                                navController.navigate(deeplinkUri)
                            }
                        }
                    }
                }
                ivEnd.loadImage(toolbarData.dnrImage)
                tvEndTitle.text = toolbarData.dnr
                tvEndTitle.isVisible = toolbarData.dnr.isNotNullAndNotEmpty()
            }
            binding.ivVoucherImage.apply {
                isVisible = it.voucherImageUrl.isNotNullAndNotEmpty()
                loadImage(it.voucherImageUrl)
            }
        }
    }

    private fun updateVoucherState(state: VoucherState) {

        val dnrVoucherDetailBinding = binding.dnrVoucherDetailLayout
        val dnrVoucherErrorBinding = binding.dnrVoucherErrorLayout
        val dnrVoucherLoadingBinding = binding.dnrVoucheLoadingLayout
        var voucherStateLayoutVisibility: VoucherStateLayoutVisibility? = null

        when (state) {
            is VoucherState.Error -> {
                val voucherErrorData = state.data
                voucherStateLayoutVisibility = voucherErrorData.voucherStateLayoutVisibility
                dnrVoucherErrorBinding.apply {
                    title.text = voucherErrorData.title
                    title.isVisible = voucherErrorData.title.isNotNullAndNotEmpty()

                    noResultImageView.loadImage(voucherErrorData.imageUrl)
                    noResultImageView.isVisible = voucherErrorData.imageUrl.isNotNullAndNotEmpty()

                    noResultTvTitle.text = voucherErrorData.subtitle
                    noResultTvTitle.isVisible = voucherErrorData.subtitle.isNotNullAndNotEmpty()

                    noResultTvSubTitle.text = voucherErrorData.description
                    noResultTvSubTitle.isVisible =
                        voucherErrorData.description.isNotNullAndNotEmpty()

                    btTryAgain.apply {
                        cancelCountDownTimer()
                        text = voucherErrorData.cta
                        isVisible = voucherErrorData.cta.isNotNullAndNotEmpty()
                        setOnClickListener {
                            when (source) {
                                DnrVoucherSource.BETTER_LUCK_NEXT_TIME.type -> {
                                    mayNavigate {
                                        navController.navigateUp()
                                    }
                                }
                                else -> {
                                    viewModel.redeemVoucher(voucherId, source)
                                }
                            }
                        }
                    }
                }
            }
            is VoucherState.Loading -> {
                val voucherLoadingData = state.data
                voucherStateLayoutVisibility = voucherLoadingData.voucherStateLayoutVisibility
                if (voucherLoadingData.isLoading) {
                    totalTimerDuration = voucherLoadingData.duration
                    dnrVoucherLoadingBinding.apply {
                        progressIndicator.max = totalTimerDuration.toInt()
                        setUpTimer(totalTimerDuration.minus(counter), voucherLoadingData)
                        val animationFile = voucherLoadingData.animationFileName
                        if (animationFile.isEmpty().not()) {
                            dnrLoadingAnimation.setAnimation(animationFile)
                            dnrLoadingAnimation.playAnimation()
                        }
                        tvMessage.text = voucherLoadingData.description
                    }
                } else {
                    cancelCountDownTimer()
                    dnrVoucherLoadingBinding.dnrLoadingAnimation.pauseAnimation()
                }
            }
            is VoucherState.Locked -> {
                val voucherLockedData = state.data
                voucherStateLayoutVisibility = voucherLockedData.voucherStateLayoutVisibility
                dnrVoucherDetailBinding.apply {
                    val voucherInfoItemAdapter = DnrVoucherInfoItemAdapter()
                    rvVoucherInfo.adapter = voucherInfoItemAdapter
                    rvVoucherInfo.layoutManager = LinearLayoutManager(requireContext())
                    voucherInfoItemAdapter.updateItems(voucherLockedData.voucherInfoItems.orEmpty())

                    tvTitle.text = voucherLockedData.title
                    tvTitle.isVisible = voucherLockedData.title.isNotNullAndNotEmpty()

                    tvOfferTitle.text = voucherLockedData.offerTitle
                    tvOfferTitle.isVisible = voucherLockedData.offerTitle.isNotNullAndNotEmpty()

                    tvOfferDescription.text = voucherLockedData.offerDescription
                    tvOfferDescription.isVisible =
                        voucherLockedData.offerDescription.isNotNullAndNotEmpty()

                    btRedeemNow.apply {
                        text = voucherLockedData.cta
                        setOnClickListener {
                            if (voucherLockedData.deeplink.isNotNullAndNotEmpty()) {
                                deeplinkAction.performAction(context, voucherLockedData.deeplink)
                            } else {
                                viewModel.redeemVoucher(voucherId, source)
                            }
                        }
                    }
                }
            }
            is VoucherState.NotEnoughDnr -> {
                val voucherNotEnoughDnrData = state.warningData
                voucherStateLayoutVisibility = voucherNotEnoughDnrData.voucherStateLayoutVisibility
                dnrVoucherDetailBinding.apply {

                    tvWarningMessage.apply {
                        show()
                        text = voucherNotEnoughDnrData.description
                        setTextColor(
                            Utils.parseColor(
                                voucherNotEnoughDnrData.descriptionColor,
                                Color.BLACK
                            )
                        )
                        setBackgroundColor(
                            Utils.parseColor(
                                voucherNotEnoughDnrData.backgroundColor,
                                Color.RED
                            )
                        )
                    }
                }
            }
            is VoucherState.Unlocked -> {
                val voucherUnlockedData = state.data
                voucherStateLayoutVisibility = voucherUnlockedData.voucherStateLayoutVisibility
                dnrVoucherDetailBinding.apply {
                    couponContainer.show()
                    tvCouponTitle.text = voucherUnlockedData.title
                    tvCouponTitle.isVisible = voucherUnlockedData.title.isNotNullAndNotEmpty()

                    tvCouponExpire.text = voucherUnlockedData.expireOn
                    tvCouponExpire.isVisible = voucherUnlockedData.expireOn.isNotNullAndNotEmpty()

                    tvCouponCode.text = voucherUnlockedData.voucherCode
                    tvCouponCode.isVisible = voucherUnlockedData.voucherCode.isNotNullAndNotEmpty()

                    tvCopyCode.apply {
                        text = voucherUnlockedData.copyCodeText
                        setOnClickListener {
                            context?.copy(
                                text = voucherUnlockedData.voucherCode,
                                label = "dnr_redeem_voucher_code",
                                toastMessage = getString(R.string.coupon_code_copied)
                            )
                        }
                    }
                    tvCouponCode.isVisible = voucherUnlockedData.copyCodeText.isNotNullAndNotEmpty()

                    btRedeemNow.apply {
                        text = voucherUnlockedData.cta
                        setOnClickListener {
                            viewModel.sendEvent(
                                EventConstants.DNR_REDEEM_NOW_CLICKED,
                                hashMapOf(
                                    EventConstants.URL to voucherUnlockedData.deeplink.orEmpty()
                                ),
                                ignoreSnowplow = true
                            )
                            val i = Intent(Intent.ACTION_VIEW)
                            i.data = Uri.parse(voucherUnlockedData.deeplink)
                            try {
                                startActivity(i)
                            } catch (exception: ActivityNotFoundException) {
                                mayNavigate {
                                    navController.navigateUp()
                                }
                            }
                        }
                    }
                }
            }
        }

        voucherStateLayoutVisibility?.apply {
            dnrVoucherDetailBinding.root.isVisible = isDetailLayoutVisible
            dnrVoucherErrorBinding.root.isVisible = isErrorLayoutVisible
            dnrVoucherLoadingBinding.root.isVisible = isLoadingLayoutVisible
        }
    }

    private fun setUpTimer(timerDuration: Long, data: VoucherLoadingData) {
        countDownTimer = object : CountDownTimer(timerDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                counter = counter.plus(1000L)
                binding.dnrVoucheLoadingLayout.apply {
                    progressIndicator.progress = counter.toInt()
                }
            }

            override fun onFinish() {
                binding.dnrVoucheLoadingLayout.apply {
                    btGoBack.apply {
                        show()
                        text = data.cta
                        setOnClickListener {
                            mayNavigate {
                                navController.navigateUp()
                            }
                        }
                    }
                }
            }
        }
        countDownTimer.start()
    }

    override fun onDetach() {
        cancelCountDownTimer()
        super.onDetach()
    }

    private fun cancelCountDownTimer() {
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}
