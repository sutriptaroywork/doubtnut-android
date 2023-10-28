package com.doubtnutapp.login.ui.fragment

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.SingleEvent
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentLoginPinBinding
import com.doubtnutapp.getScreenWidth
import com.doubtnutapp.login.viewmodel.LoginViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.utils.KeyboardUtils

/**
 * Created by Sachin Saxena on 2020-11-04.
 */

class LoginPinDialogFragment : BaseBindingDialogFragment<LoginViewModel, FragmentLoginPinBinding>() {

    companion object {
        const val TAG = "LoginPinDialogFragment"

        fun newInstance() = LoginPinDialogFragment()
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLoginPinBinding =
        FragmentLoginPinBinding.inflate(layoutInflater)

    override fun provideViewModel(): LoginViewModel {
        val viewModel: LoginViewModel by viewModels(
            ownerProducer = { requireActivity() }
        ) { viewModelFactory }
        return viewModel
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        view.apply {
            binding.loginPinView.itemWidth = requireActivity().getScreenWidth() / 9
            binding.loginPinView.requestFocus()

            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

            binding.ivClose.setOnClickListener {
                KeyboardUtils.hideKeyboard(binding.loginPinView)
                viewModel.startTimerWithBuffer.postValue(SingleEvent(Pair(true, null)))
                dismiss()
            }

            binding.btVerifyPin.setOnClickListener {
                KeyboardUtils.hideKeyboard(binding.loginPinView)
                viewModel.verifyOtp(pinInserted = true, pin = binding.loginPinView.text.toString())
                dismiss()
            }

            binding.loginPinView.doAfterTextChanged {

                if (context == null || it == null) return@doAfterTextChanged

                if (it.toString().length > 3) {
                    binding.loginPinView.setLineColor(ContextCompat.getColor(requireContext(), R.color.tomato))
                    binding.btVerifyPin.isEnabled = true
                } else {
                    binding.loginPinView.setLineColor(ContextCompat.getColor(requireContext(), R.color.disable_otp_view_cursor))
                    binding.btVerifyPin.isEnabled = false
                }
            }
        }

        viewModel.publishEventWith(EventConstants.VERIFY_PIN_DIALOG_OPEN, ignoreSnowplow = true)
    }

    override fun onStart() {
        super.onStart()
        val params = view?.layoutParams
        params?.width = (requireActivity().getScreenWidth() / 1.2).toInt()
        view?.layoutParams = params
    }

    override fun onDismiss(dialog1: DialogInterface) {
        super.onDismiss(dialog1)
        viewModel.startTimerWithBuffer.postValue(SingleEvent(Pair(true, null)))
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }
}