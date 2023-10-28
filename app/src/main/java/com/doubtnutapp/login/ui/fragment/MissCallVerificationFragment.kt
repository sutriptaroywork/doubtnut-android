package com.doubtnutapp.login.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.data.LottieAnimDataStore
import com.doubtnut.core.utils.LottieAnimationViewUtils.applyAnimationFromUrl
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentMissCallVerificationBinding
import com.doubtnutapp.hide
import com.doubtnutapp.login.viewmodel.LoginViewModel
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.utils.KeyboardUtils
import com.truecaller.android.sdk.clients.VerificationCallback
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Created by Sachin Saxena on 2020-05-12.
 */
class MissCallVerificationFragment :
    BaseBindingDialogFragment<LoginViewModel, FragmentMissCallVerificationBinding>() {

    companion object {
        const val TAG = "MissCallVerificationFragment"
        fun newInstance() = MissCallVerificationFragment()
    }

    @Inject
    lateinit var lottieAnimDataStore: LottieAnimDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMissCallVerificationBinding =
        FragmentMissCallVerificationBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): LoginViewModel = activityViewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpView()
        setUpClickListeners()
    }

    private fun setUpView() {
        onMissedCallInitiated()
        mBinding?.nameEntryLayout?.hide()
        mBinding?.name?.let {
            KeyboardUtils.hideKeyboard(it)
        }
    }

    private fun setUpClickListeners() {

        mBinding?.next?.setOnClickListener {

            if (context == null) return@setOnClickListener

            val nameEntry = mBinding?.name?.text?.toString()?.trim()
            if (nameEntry.isNullOrEmpty()) {
                ToastUtils.makeText(
                    requireContext(),
                    "Please enter first and last name",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            viewModel.publishEventWith(EventConstants.EVENT_NAME_TRUECALLER_NAME_SUBMITTED)

            val names = nameEntry.split(" ")
            val firstName = names[0]
            val lastName = if (names.size > 1) {
                names[1]
            } else {
                names[0]
            }
            dismiss()
            viewModel.verifyNameOnTrueCaller(firstName, lastName)
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.missedCallState.observe(viewLifecycleOwner) {
            when (it) {
                VerificationCallback.TYPE_MISSED_CALL_INITIATED -> {
                    onMissedCallInitiated()
                }

                VerificationCallback.TYPE_MISSED_CALL_RECEIVED -> {
                    onSuccessfulVerification()
                    showNameEntryLayout()
                    mBinding?.message?.text = getString(R.string.trucaller_verification_success)
                    mBinding?.message?.textSize = 14f
                    mBinding?.message?.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.color_completed
                        )
                    )
                }
            }
        }
    }

    private fun onMissedCallInitiated() {
        lifecycleScope.launchWhenResumed {
            val missedCallAnimationUrl = lottieAnimDataStore.missedCallAnimationUrl.firstOrNull()
            mBinding?.callAnimation?.applyAnimationFromUrl(missedCallAnimationUrl)
        }
    }

    private fun onSuccessfulVerification() {
        lifecycleScope.launchWhenResumed {
            val chekBoxAnimationUrl = lottieAnimDataStore.missedCallSuccessAnimationUrl.firstOrNull()
            mBinding?.callAnimation?.applyAnimationFromUrl(chekBoxAnimationUrl)
        }
    }

    private fun showNameEntryLayout() {
        viewModel.publishEventWith(EventConstants.EVENT_NAME_TRUECALLER_NAME_POPUP_DISPLAYED)
        mBinding?.nameEntryLayout?.show()
        mBinding?.name?.requestFocus()
        mBinding?.name?.let {
            KeyboardUtils.showKeyboard(it)
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.publishEventWith(
            EventConstants.EVENT_NAME_TRUECALLER_NAME_ENTERED,
            hashMapOf<String, Any>().apply {
                put(EventConstants.NAME, mBinding?.name.toString())
            }
        )
    }
}