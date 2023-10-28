package com.doubtnutapp.reward.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.reward.RewardPopupModel
import com.doubtnutapp.databinding.FragmentRewardStreakResetDialogBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class RewardStreakResetDialogFragment :
    DialogFragment(R.layout.fragment_reward_streak_reset_dialog) {

    companion object {
        const val TAG = "RewardStreakResetDialog"

        private const val PARAM_KEY_POPUP_DATA = "popup_data"

        fun newInstance(rewardPopupModel: RewardPopupModel): RewardStreakResetDialogFragment =
            RewardStreakResetDialogFragment().apply {
                arguments = bundleOf(PARAM_KEY_POPUP_DATA to rewardPopupModel)
            }
    }

    private val binding by viewBinding(FragmentRewardStreakResetDialogBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val data = arguments?.getParcelable<RewardPopupModel>(PARAM_KEY_POPUP_DATA) ?: return

        with(binding) {
            tvTitle.text = data.popupHeading
            tvSubtitle.text = data.popupSubHeading
            tvDescription.text = data.popupDescription

            cardContainer.setOnClickListener {
                //intercept touch event
            }
            rootLayout.setOnClickListener {
                dismiss()
            }
            ivClose.setOnClickListener {
                dismiss()
            }
        }
    }
}