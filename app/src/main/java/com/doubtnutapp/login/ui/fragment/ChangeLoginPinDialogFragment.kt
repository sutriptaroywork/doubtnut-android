package com.doubtnutapp.login.ui.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.widget.doAfterTextChanged
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.data.common.AESHelper
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.FragmentChangeLoginPinBinding
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.getScreenWidth
import com.doubtnutapp.login.ui.activity.StudentLoginActivity
import com.doubtnutapp.login.viewmodel.LoginViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.utils.KeyboardUtils
import javax.inject.Inject

/**
 * Created by Sachin Saxena on 2020-11-04.
 */
class ChangeLoginPinDialogFragment :
    BaseBindingDialogFragment<LoginViewModel, FragmentChangeLoginPinBinding>() {

    @Inject
    lateinit var userPreference: UserPreference

    private val action: String? by lazy { arguments?.getString(ACTION) }

    companion object {
        const val TAG = "ChangeLoginPinDialogFragment"

        private const val ACTION = "ACTION"

        fun newInstance(action: String? = null) = ChangeLoginPinDialogFragment().apply {
            val bundle = Bundle()
            bundle.putString(ACTION, action)
            arguments = bundle
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentChangeLoginPinBinding {
        return FragmentChangeLoginPinBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): LoginViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBinding?.loginPinView?.itemWidth = requireActivity().getScreenWidth() / 9
        mBinding?.loginPinView?.requestFocus()

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        if (defaultPrefs().getBoolean(Constants.IS_PIN_SET, false)) {
            val encryptedPin = defaultPrefs().getString(Constants.PIN, null)
            encryptedPin?.let {
                try {
                    val decryptedPin = AESHelper.decrypt(encryptedPin)
                    mBinding?.loginPinView?.setText(decryptedPin)
                    mBinding?.btSavePin?.isEnabled = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        mBinding?.ivClose?.setOnClickListener {
            KeyboardUtils.hideKeyboard(mBinding?.loginPinView ?: return@setOnClickListener)
            dismiss()
        }

        mBinding?.btSavePin?.setOnClickListener {
            KeyboardUtils.hideKeyboard(mBinding?.loginPinView ?: return@setOnClickListener)
            viewModel.storePin(
                mBinding?.loginPinView?.text?.toString() ?: return@setOnClickListener
            )
        }

        mBinding?.loginPinView?.doAfterTextChanged {

            if (context == null || it == null) return@doAfterTextChanged

            if (it.toString().length > 3) {
                mBinding?.loginPinView?.setLineColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.tomato
                    )
                )
                mBinding?.btSavePin?.isEnabled = true
            } else {
                mBinding?.loginPinView?.setLineColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.disable_otp_view_cursor
                    )
                )
                mBinding?.btSavePin?.isEnabled = false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val params = view?.layoutParams
        params?.width = (requireActivity().getScreenWidth() / 1.2).toInt()
        view?.layoutParams = params
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.hasPinChanged.observe(viewLifecycleOwner, { pair ->
            pair.second?.let { toast(it) }
            if (pair.first) {
                try {
                    val encryptedPin = AESHelper.encrypt(
                        mBinding?.loginPinView?.text?.toString() ?: return@observe
                    )
                    defaultPrefs().edit {
                        putString(Constants.PIN, encryptedPin)
                        putBoolean(Constants.IS_PIN_SET, true)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                KeyboardUtils.hideKeyboard(mBinding?.loginPinView ?: return@observe)
                if (action == Constants.NAVIGATE_LOGOUT) {
                    userPreference.logOutUser()
                    openLoginScreen()
                }
                dismiss()
            }
        })
    }

    private fun openLoginScreen() {
//        val languageCode = defaultPrefs().getString(Constants.STUDENT_LANGUAGE_CODE, null)
        val intent = StudentLoginActivity.getStartIntent(requireActivity())
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

}