package com.doubtnutapp.login.ui.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.data.LottieAnimDataStore
import com.doubtnut.core.entitiy.SingleEvent
import com.doubtnut.core.observer.SingleEventObserver
import com.doubtnut.core.utils.LottieAnimationViewUtils.applyAnimationFromUrl
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.gone
import com.doubtnut.core.utils.visible
import com.doubtnutapp.databinding.FragmentOtpOverCallBinding
import com.doubtnutapp.getScreenWidth
import com.doubtnutapp.login.viewmodel.LoginViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject


/**
 * Created by Sachin Saxena on 2020-11-04.
 */

class OtpOverCallDialogFragment :
    BaseBindingDialogFragment<LoginViewModel, FragmentOtpOverCallBinding>(), View.OnClickListener {

    companion object {
        const val TAG = "OtpOverCallDialogFragment"
        const val CALLING_BUFFER_TIME = 30
        fun newInstance() = OtpOverCallDialogFragment()
    }

    @Inject
    lateinit var lottieAnimDataStore: LottieAnimDataStore

    private var callButtonClicked = false

    override fun onStart() {
        super.onStart()
        val params = view?.layoutParams
        params?.width = (requireActivity().getScreenWidth() / 1.2).toInt()
        view?.layoutParams = params
    }

    override fun onDismiss(dialog1: DialogInterface) {
        super.onDismiss(dialog1)
        viewModel.startTimerWithBuffer.postValue(
            SingleEvent(
                Pair(
                    true,
                    if (callButtonClicked) CALLING_BUFFER_TIME else null
                )
            )
        )
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.otpOverCall.observe(viewLifecycleOwner, SingleEventObserver {
            showIncomingCallLayout(it.message)
        })
    }

    private fun showIncomingCallLayout(msg: String) {
        mBinding?.apply {
            callAnimation.cancelAnimation()
            messageAnimation.cancelAnimation()
            callLayout.gone()
            messageLayout.gone()
            tvTitle.gone()
            incomingCallLayout.visible()

            lifecycleScope.launchWhenResumed {
                val incomingCallAnimationUrl = lottieAnimDataStore.incomingCallAnimationUrl.firstOrNull()
                incomingCallAnimation.apply {
                    visible()
                    applyAnimationFromUrl(incomingCallAnimationUrl)
                }
            }

            message.text = msg
            lifecycleScope.launchWhenStarted {
                delay(5000L)
                dismiss()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding?.ivClose -> {
                if (context == null) return
                viewModel.publishEventWith(EventConstants.OTP_OVER_CLOSE_BUTTON_CLICKED)
                viewModel.startTimerWithBuffer.postValue(SingleEvent(Pair(true, null)))
                dismiss()
            }

            mBinding?.messageLayout, mBinding?.btMessage -> {
                if (context == null) return
                viewModel.publishEventWith(EventConstants.OTP_OVER_MESSAGE_BUTTON_CLICKED)
                viewModel.resendOtp()
                viewModel.startTimerWithBuffer.postValue(SingleEvent(Pair(true, null)))
                dismiss()
            }

            mBinding?.callLayout, mBinding?.btCall -> {
                if (context == null) return
                callButtonClicked = true
                viewModel.publishEventWith(EventConstants.OTP_OVER_CALL_BUTTON_CLICKED)
                viewModel.otpOverCall()
            }
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOtpOverCallBinding =
        FragmentOtpOverCallBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): LoginViewModel = activityViewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBinding?.apply {
            lifecycleScope.launchWhenResumed {
                val callAnimationUrl = lottieAnimDataStore.callAnimationUrl.firstOrNull()
                callAnimation.applyAnimationFromUrl(callAnimationUrl)

                val messageAnimationUrl = lottieAnimDataStore.messageAnimationUrl.firstOrNull()
                messageAnimation.applyAnimationFromUrl(messageAnimationUrl)
            }

            ivClose.setOnClickListener(this@OtpOverCallDialogFragment)

            messageLayout.setOnClickListener(this@OtpOverCallDialogFragment)
            btMessage.setOnClickListener(this@OtpOverCallDialogFragment)

            callLayout.setOnClickListener(this@OtpOverCallDialogFragment)
            btCall.setOnClickListener(this@OtpOverCallDialogFragment)

            viewModel.publishEventWith(EventConstants.OTP_OVER_CALL_DIALOG_OPEN)
        }
    }
}