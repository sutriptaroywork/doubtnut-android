package com.doubtnutapp.doubtfeed2.reward.ui

import android.app.Dialog
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.base.extension.setNavigationResult
import com.doubtnutapp.data.remote.models.reward.Reward
import com.doubtnutapp.databinding.FragmentDfRewardScratchCardDialogBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.doubtfeed2.reward.viewmodel.ScratchCardViewModel
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.widgets.scratchview.ScratchImageView
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Created by devansh on 14/7/21.
 */

class ScratchCardDialogFragment : DialogFragment(R.layout.fragment_df_reward_scratch_card_dialog) {

    companion object {
        const val CARD_SCRATCHED = "card_scratched"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val binding by viewBinding(FragmentDfRewardScratchCardDialogBinding::bind)
    private val args by navArgs<ScratchCardDialogFragmentArgs>()
    private val viewModel by viewModels<ScratchCardViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity(), theme) {
            override fun onBackPressed() {
                this@ScratchCardDialogFragment.dismiss()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setupObservers()

        val reward = args.reward

        with(binding) {
            ivScratchView.isVisible = reward.isScratched.not()
            ivScratchView.isScratchEnabled = reward.isScratched.not() && reward.isUnlocked
            ivScratchView.setScratchImage(R.drawable.scratch_card_unopen, false)

            if (reward.isUnlocked) {
                lockView.hide()
                tvSubtitle.hide()
                tvLongDescription.hide()

                ivScratched.loadImage(reward.scratchedImageLink)

                tvShortDescription.text = if (reward.isScratched) {
                    reward.scratchDescription
                } else {
                    reward.shortDescription
                }

                when (reward.rewardType) {
                    Reward.WALLET -> {
                        tvRupeesIcon.show()
                        tvRewardAmount.show()
                        tvRewardAmount.text = reward.walletAmount.toString()
                    }
                }

                if (reward.isScratched) {
                    updateScratchedUi()
                }
            } else {
                lockView.show()

                tvLevel.text = getString(R.string.level, reward.level)

                tvShortDescription.setTypeface(tvShortDescription.typeface, Typeface.BOLD)
                tvShortDescription.text = reward.lockedShortDescription

                if (reward.lockedSubtitle.isNullOrEmpty().not()) {
                    tvSubtitle.show()
                    tvSubtitle.text = reward.lockedSubtitle
                }

                if (reward.lockedLongDescription.isNullOrEmpty().not()) {
                    tvLongDescription.show()
                    tvLongDescription.text = reward.lockedLongDescription
                }
            }

            ivScratchView.setRevealListener(object : ScratchImageView.IRevealListener {
                override fun onRevealed(iv: ScratchImageView?) {
                    markScratched()
                }

                override fun onRevealPercentChangedListener(
                    siv: ScratchImageView?,
                    percent: Float
                ) {
                    if (percent >= 0.50) {
                        siv?.reveal()
                        updateScratchedUi()
                    }
                }
            })

            ivClose.setOnClickListener {
                dismiss()
            }

            rootLayout.setOnClickListener {
                dismiss()
            }

            rootCardView.setOnClickListener {
                // intercept touch and do not dismiss dialog
            }
        }
    }

    override fun dismiss() {
        with(binding) {
            if (ivScratchView.isVisible && ivScratchView.isRevealed.not() && ivScratchView.revealPercent > 0f) {
                markScratched()
                updateScratchedUi()
            } else {
                super.dismiss()
            }
        }
    }

    private fun setupObservers() {
        viewModel.cardScratchedLiveData.observe(viewLifecycleOwner) {
            if (it) {
                setNavigationResult(true, CARD_SCRATCHED)
            }
        }
    }

    private fun updateScratchedUi() {
        with(binding) {
            val reward = args.reward

            ivScratchView.hide()
            tvShortDescription.text = reward.scratchDescription

            if (reward.ctaText.isNullOrEmpty().not() && reward.deeplink.isNullOrEmpty().not()) {
                button1.apply {
                    show()
                    text = reward.ctaText
                    setOnClickListener {
                        deeplinkAction.performAction(requireContext(), reward.deeplink)
                        viewModel.sendEvent(
                            EventConstants.DG_SCRATCHCARD_CTA_CLICK,
                            hashMapOf(
                                Constants.REWARD_LEVEL to reward.level
                            )
                        )
                    }
                }
            }

            if (reward.isShareEnabled && reward.secondaryCtaText.isNullOrEmpty().not()) {
                button2.apply {
                    show()
                    text = reward.secondaryCtaText
                    setOnClickListener {
                        performShareAction()
                        viewModel.sendEvent(
                            EventConstants.DG_SCRATCHCARD_SHARE_CLICK,
                            hashMapOf(
                                Constants.REWARD_LEVEL to reward.level
                            )
                        )
                    }
                }
            }

            when (reward.rewardType) {
                Reward.COUPON -> {
                    layoutCouponCode.show()
                    tvCouponCode.text = reward.couponCode
                    layoutCouponCode.setOnClickListener {
                        val clipboard =
                            requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(
                            "doubtnut_coupon_code",
                            reward.couponCode.orEmpty()
                        )
                        clipboard.setPrimaryClip(clip)
                        toast(R.string.coupon_code_copied)
//                    viewModel.sendEvent(EventConstants.REWARD_PAGE_CODE_COPIED)
                    }
                }
            }
        }
    }

    private fun performShareAction() {
        viewModel.getShareableViewImageUri(binding.containerScratchedCard)
            .observe(viewLifecycleOwner) {
                Intent(Intent.ACTION_SEND).apply {
                    val shareText = args.shareText ?: getString(R.string.reward_share_text)

                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(it))
                    startActivity(Intent.createChooser(this, shareText))
                }
            }
    }

    private fun markScratched() {
        dismissScratchCardNotification()
        val level = args.reward.level
        viewModel.markRewardScratched(level)
        viewModel.sendEvent(
            EventConstants.DG_SCRATCHCARD_SCRATCHED,
            hashMapOf(
                Constants.REWARD_LEVEL to level
            )
        )
    }

    private fun dismissScratchCardNotification() {
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(NotificationConstants.NOTIFICATION_ID_REWARD)
    }
}
