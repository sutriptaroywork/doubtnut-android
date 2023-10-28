package com.doubtnutapp.login.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.MainActivity
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.FragmentAnonymousLoginBlockerBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.ui.splash.SplashActivity
import com.doubtnut.core.utils.viewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject

class AnonymousLoginBlockerFragment : BaseBindingBottomSheetDialogFragment<DummyViewModel, FragmentAnonymousLoginBlockerBinding>() {

    companion object {
        const val TAG = "AnonymousLoginBlockerFragment"
        fun newInstance() = AnonymousLoginBlockerFragment()
    }

    private var mBehavior: BottomSheetBehavior<*>? = null

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun onAttach(context: Context) {
        super.onAttach(context)
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.ANONYMOUS_LOGIN_BLOCKER))
    }

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAnonymousLoginBlockerBinding =
        FragmentAnonymousLoginBlockerBinding.inflate(layoutInflater)

    override fun setupView(view: View, savedInstanceState: Bundle?) {

        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)

        mBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        if (mBehavior != null) {
            (mBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_EXPANDED
            (mBehavior as BottomSheetBehavior<*>).setBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(p0: View, p1: Int) {
                    (mBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_EXPANDED
                }

                override fun onSlide(p0: View, p1: Float) {
                    (mBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_EXPANDED
                }
            })
        }
        binding.countryCodePicker.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.ANONYMOUS_LOGIN_BLOCKER_LOGIN_CLICK))
            logoutAnonymousAndMoveToLogin()
        }

        binding.tvPhone.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.ANONYMOUS_LOGIN_BLOCKER_LOGIN_CLICK))
            logoutAnonymousAndMoveToLogin()
        }

        dialog?.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                if (activity is MainActivity) {
                    logoutAnonymousAndMoveToLogin()
                } else {
                    activity?.finish()
                }
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
    }

    private fun logoutAnonymousAndMoveToLogin() {
        userPreference.logOutUser()
        startActivity(
            Intent(requireContext(), SplashActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

}