package com.doubtnutapp.reward.ui

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.data.remote.models.reward.Reward
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.reward.viewmodel.RewardViewModel
import com.doubtnutapp.widgets.scratchview.ScratchImageView
import dagger.android.support.DaggerDialogFragment
import kotlinx.android.synthetic.main.dialog_scratch_card.*
import javax.inject.Inject

/**
 * Created by devansh on 24/3/21.
 */

class ScratchCardDialogFragment : DaggerDialogFragment() {

    companion object {
        const val TAG = "ScratchCardDialog"

        const val PARAM_KEY_LEVEL = "level"

        fun newInstance(level: Int): ScratchCardDialogFragment = ScratchCardDialogFragment().apply {
            arguments = bundleOf(PARAM_KEY_LEVEL to level)
        }
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val viewModel: RewardViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.dialog_scratch_card, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity(), theme) {
            override fun onBackPressed() {
                this@ScratchCardDialogFragment.dismiss()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val reward = viewModel.getRewardByLevel(arguments?.getInt(PARAM_KEY_LEVEL) ?: 0) ?: return

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

            override fun onRevealPercentChangedListener(siv: ScratchImageView?, percent: Float) {
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

    override fun dismiss() {
        if (ivScratchView.isVisible && ivScratchView.isRevealed.not() && ivScratchView.revealPercent > 0f) {
            markScratched()
            updateScratchedUi()
        } else {
            super.dismiss()
        }
    }

    private fun updateScratchedUi() {
        val reward = viewModel.getRewardByLevel(arguments?.getInt(PARAM_KEY_LEVEL) ?: 0) ?: return

        ivScratchView.hide()
        tvShortDescription.text = reward.scratchDescription

        if (reward.ctaText.isNullOrEmpty().not() && reward.deeplink.isNullOrEmpty().not()) {
            button1.apply {
                show()
                text = reward.ctaText
                setOnClickListener {
                    deeplinkAction.performAction(requireContext(), reward.deeplink)
                    viewModel.sendEvent(EventConstants.REWARD_PAGE_SCRATCH_CARD_CTA_CLICKED, hashMapOf(
                            Constants.REWARD_LEVEL to reward.level,
                            Constants.REWARD_TYPE to reward.rewardType.orEmpty(),
                    ), ignoreSnowplow = true)
                }
            }
        }

        if (reward.isShareEnabled && reward.secondaryCtaText.isNullOrEmpty().not()) {
            button2.apply {
                show()
                text = reward.secondaryCtaText
                setOnClickListener {
                    performShareAction()
                    viewModel.sendEvent(EventConstants.REWARD_PAGE_SCRATCH_CARD_SHARE_CLICKED, hashMapOf(
                            Constants.REWARD_LEVEL to reward.level,
                            Constants.REWARD_TYPE to reward.rewardType.orEmpty(),
                    ), ignoreSnowplow = true)
                }
            }
        }

        when (reward.rewardType) {
            Reward.COUPON -> {
                layoutCouponCode.show()
                tvCouponCode.text = reward.couponCode
                layoutCouponCode.setOnClickListener {
                    val clipboard = requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("doubtnut_coupon_code", reward.couponCode.orEmpty())
                    clipboard.setPrimaryClip(clip)
                    toast(R.string.coupon_code_copied)
                    viewModel.sendEvent(EventConstants.REWARD_PAGE_CODE_COPIED, ignoreSnowplow = true)
                }
            }
        }
    }

    private fun performShareAction() {
        viewModel.getShareableViewImageUri(containerScratchedCard).observe(viewLifecycleOwner) {
            Intent(Intent.ACTION_SEND).apply {
                val shareText = viewModel.shareText ?: getString(R.string.reward_share_text)

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
        val level = arguments?.getInt(PARAM_KEY_LEVEL) ?: 0
        val reward = viewModel.getRewardByLevel(arguments?.getInt(PARAM_KEY_LEVEL) ?: 0) ?: return
        viewModel.markRewardScratched(level)
        viewModel.sendEvent(EventConstants.REWARD_PAGE_SCRATCH_CARD_SCRATCHED, hashMapOf(
                Constants.REWARD_LEVEL to level,
                Constants.REWARD_TYPE to reward.rewardType.orEmpty(),
        ), ignoreSnowplow = true)
    }

    private fun dismissScratchCardNotification() {
        val mNotificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(NotificationConstants.NOTIFICATION_ID_REWARD)
    }
}