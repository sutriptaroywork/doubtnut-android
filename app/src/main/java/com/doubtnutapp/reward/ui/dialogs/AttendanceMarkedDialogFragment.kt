package com.doubtnutapp.reward.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.reward.RewardPopupModel
import com.doubtnutapp.databinding.DialogAttendanceMarkedBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.load
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.utils.LifecycleAwareCountdownTimer
import com.doubtnut.core.utils.viewModelProvider

class AttendanceMarkedDialogFragment :
    BaseBindingDialogFragment<DummyViewModel, DialogAttendanceMarkedBinding>() {

    companion object {
        const val TAG = "AttendanceMarkedDialog"

        const val PARAM_KEY_POPUP_DATA = "popup_data"

        fun newInstance(rewardPopupModel: RewardPopupModel): AttendanceMarkedDialogFragment =
            AttendanceMarkedDialogFragment().apply {
                arguments = bundleOf(PARAM_KEY_POPUP_DATA to rewardPopupModel)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val popupData = arguments?.getParcelable<RewardPopupModel>(PARAM_KEY_POPUP_DATA) ?: return

        binding.tvTitle.text = popupData.popupHeading
        binding.tvDescription.text = popupData.popupDescription

        binding.imageView.load(
            when {
                popupData.isStreakBreak -> R.drawable.ic_attendance_missed
                popupData.isRewardPresent -> R.drawable.ic_reward_homepage
                else -> R.drawable.ic_attendance_marked
            }
        )

        binding.cardContainer.setOnClickListener {
            //intercept touch event
        }
        binding.rootLayout.setOnClickListener {
            dismiss()
        }
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        val dismissTimer = object : LifecycleAwareCountdownTimer(5000, 1000) {
            override fun onTimerFinish() {
                try {
                    dismissAllowingStateLoss()
                } catch (e: Exception) {
                    // https://console.firebase.google.com/u/0/project/doubtnut-e000a/crashlytics/app/android:com.doubtnutapp/issues/b3b58b8b434074584c2064ac763c0e8c?time=last-seven-days&sessionEventKey=614AB6C3024100017DB3D04E58DE91FA_1589062661402792075
                }
            }
        }
        viewLifecycleOwner.lifecycle.addObserver(dismissTimer)
        dismissTimer.start()
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogAttendanceMarkedBinding {
        return DialogAttendanceMarkedBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }

}