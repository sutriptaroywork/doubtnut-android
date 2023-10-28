package com.doubtnutapp.reward.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.R
import com.doubtnutapp.reward.viewmodel.RewardViewModel
import com.doubtnut.core.utils.toast
import kotlinx.android.synthetic.main.fragment_reward_backpress_dialog.view.*

/**
 * Created by Pankaj on 2021-04-24.
 */

class RewardBackpressDialogFragment : DialogFragment() {

    private val viewModel: RewardViewModel by activityViewModels()

    companion object {
        const val TAG = "RewardBackpressDialogFragment"

        private const val IS_REMINDER_SET = "is_reminder_set"

        fun newInstance(isReminderSet: Boolean): RewardBackpressDialogFragment =
            RewardBackpressDialogFragment().apply {
                arguments = bundleOf(IS_REMINDER_SET to isReminderSet)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                viewModel.sendEvent(EventConstants.REWARD_PAGE_BACKPRESS_POPUP_CLOSE_BACKPRESS, ignoreSnowplow = true)
                activity?.finish()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.fragment_reward_backpress_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.apply {
            textViewReminderMessage.text = viewModel.backpressPopupdata?.backpressPopupText
            buttonRemindMe.text = viewModel.backpressPopupdata?.backpressPopupCtaText

            buttonRemindMe.setOnClickListener {
                viewModel.sendEvent(EventConstants.REWARD_PAGE_BACKPRESS_POPUP_REMIDER_CLICK, ignoreSnowplow = true)
                setReminder()
                dismiss()
                activity?.finish()
            }

            ivRewardReminderCancel.setOnClickListener {
                viewModel.sendEvent(EventConstants.REWARD_PAGE_BACKPRESS_POPUP_CROSS_CLICK, ignoreSnowplow = true)
                dismiss()
                activity?.finish()
            }

            backpressDialogRootContainer.setOnClickListener {
                viewModel.sendEvent(EventConstants.REWARD_PAGE_BACKPRESS_POPUP_DISMISSED, ignoreSnowplow = true)
                dismiss()
            }

            backpressCardContainer.setOnClickListener {
                /* no-op */
            }

        }
    }

    private fun setReminder() {
        if (arguments?.getBoolean(IS_REMINDER_SET) == false) {
            viewModel.subscribeRewardNotification(true)
        } else {
            toast(getString(R.string.we_will_remind_you))
        }
    }
}