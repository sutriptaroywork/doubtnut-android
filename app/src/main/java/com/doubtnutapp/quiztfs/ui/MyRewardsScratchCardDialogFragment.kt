package com.doubtnutapp.quiztfs.ui

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.DialogMyRewardsScratchCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.quiztfs.ScratchCardState.STATE_EXPIRED
import com.doubtnutapp.quiztfs.ScratchCardState.STATE_LOCKED
import com.doubtnutapp.quiztfs.ScratchCardState.STATE_REDEEMED
import com.doubtnutapp.quiztfs.ScratchCardState.STATE_SCRATCHED
import com.doubtnutapp.quiztfs.ScratchCardState.STATE_UNSCRATCHED
import com.doubtnutapp.quiztfs.viewmodel.MyRewardsViewModel
import com.doubtnutapp.quiztfs.widgets.DialogData
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgets.scratchview.ScratchImageView
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 09-09-2021
 */

class MyRewardsScratchCardDialogFragment : BaseBindingDialogFragment<MyRewardsViewModel, DialogMyRewardsScratchCardBinding>() {

    companion object {
        const val TAG = "MyRewardsScratchCardDialogFragment"

        private const val DIALOG_DATA = "dialog_data"
        private var couponCode: Int? = null
        private var scratchListener: ScratchListener? = null

        fun newInstance(
            dialogData: DialogData,
            scratchListener: ScratchListener
        ): MyRewardsScratchCardDialogFragment {
            this.scratchListener = scratchListener
            return MyRewardsScratchCardDialogFragment().apply {
                arguments = bundleOf(DIALOG_DATA to dialogData)
            }
        }
    }

    @Inject
    lateinit var deepLinkAction: DeeplinkAction

    private var isScratched = false

    interface ScratchListener {
        fun onScratched(couponCode: Int?)
        fun onApiSuccess()
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogMyRewardsScratchCardBinding =
        DialogMyRewardsScratchCardBinding.inflate(layoutInflater)

    override fun provideViewModel(): MyRewardsViewModel {
        val myRewardsViewModel: MyRewardsViewModel by viewModels(
            ownerProducer = { requireActivity() }
        ) { viewModelFactory }
        return myRewardsViewModel
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val reward = arguments?.getParcelable<DialogData>(DIALOG_DATA)
        couponCode = reward?.value

        updateDialogUI(reward)

        binding.ivScratchView.setRevealListener(object : ScratchImageView.IRevealListener {
            override fun onRevealed(iv: ScratchImageView?) {

            }

            override fun onRevealPercentChangedListener(siv: ScratchImageView?, percent: Float) {
                if (percent >= 0.50) {
                    if (!isScratched) {
                        isScratched = true
                        siv?.reveal()
                        scratchListener?.onScratched(couponCode)
                    }
                }
            }
        })

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.rootLayout.setOnClickListener {
            dismiss()
        }

        binding.rootCardView.setOnClickListener {
            // intercept touch and do not dismiss dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity(), theme) {
            override fun onBackPressed() {
                this@MyRewardsScratchCardDialogFragment.dismiss()
            }
        }
    }

    override fun setupObservers() {
        viewModel.submittedRewardData.observeK(
            requireActivity(),
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgress
        )
    }

    private fun updateDialogUI(reward: DialogData?) {

        reward?.let { dialogData ->

            when (dialogData.state) {
                STATE_LOCKED -> {
                    hideLockedViews()
                }

                STATE_UNSCRATCHED -> {
                    hideLockedViews()
                    showScratchView(dialogData)
                }

                STATE_SCRATCHED -> {
                    showUnlockedViewsAndHandleDeepLink(dialogData)
                }

                STATE_REDEEMED -> {
                    showUnlockedViewsAndHandleDeepLink(dialogData)
                }

                STATE_EXPIRED -> {
                    showUnlockedViewsAndHandleDeepLink(dialogData)
                }

                else -> Unit
            }
        }
    }

    private fun showScratchView(dialogData: DialogData) {
        binding.ivScratchView.isVisible = true
        binding.ivScratchView.isScratchEnabled = true
        binding.ivScratched.loadImage(dialogData.scratchedImageLink)
        binding.ivScratchView.setScratchImage(R.drawable.scratch_card_unopen, false)
        binding.tvShortDescription.text = dialogData.description
    }

    private fun hideLockedViews() {
        binding.layoutCouponCode.setVisibleState(false)
        binding.button1.setVisibleState(false)
        binding.button2.setVisibleState(false)
    }

    private fun showUnlockedViewsAndHandleDeepLink(dialogData: DialogData) {
        binding.layoutCouponCode.setVisibleState(true)
        binding.button1.setVisibleState(true)
        binding.button2.setVisibleState(true)

        binding.ivScratchView.isVisible = false
        binding.ivScratchView.isScratchEnabled = false
        binding.ivScratched.loadImage(dialogData.scratchedImageLink)
        binding.tvShortDescription.text = dialogData.description

        binding.tvCouponCode.text = dialogData.couponCode.orEmpty()
        binding.button1.text = dialogData.topButtonText.orEmpty()
        binding.button2.text = dialogData.bottomButtonText.orEmpty()

        binding.tvButtonCopy.setOnClickListener {
            val context = DoubtnutApp.INSTANCE.applicationContext
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clip =
                ClipData.newPlainText("coupon code", dialogData.couponCode.orEmpty())
            clipboard?.setPrimaryClip(clip)
            ToastUtils.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        binding.button1.setOnClickListener {
            deepLinkAction.performAction(
                requireContext(),
                "doubtnutapp://course_explore"
            )
        }

        binding.button2.setOnClickListener {
            deepLinkAction.performAction(
                requireContext(),
                dialogData.deepLink
            )
        }
    }

    private fun onSuccess(dialogData: DialogData) {
        scratchListener?.onApiSuccess()
        updateDialogUI(dialogData)
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireActivity().supportFragmentManager, "BadRequestDialog")
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(DoubtnutApp.INSTANCE.applicationContext).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgress(state: Boolean) {
        binding.progress.setVisibleState(state)
    }
}