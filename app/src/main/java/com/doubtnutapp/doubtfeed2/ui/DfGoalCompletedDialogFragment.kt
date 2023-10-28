package com.doubtnutapp.doubtfeed2.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.FragmentDfGoalCompletedDialogBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import java.util.*
import javax.inject.Inject

class DfGoalCompletedDialogFragment : DialogFragment(R.layout.fragment_df_goal_completed_dialog) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val binding by viewBinding(FragmentDfGoalCompletedDialogBinding::bind)
    private val args by navArgs<DfGoalCompletedDialogFragmentArgs>()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                dismiss()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val data = args.popupData

        sendEvent(EventConstants.DG_GOAL_COMPLETED)
        with(binding) {
            tvTitle.text = data.title
            tvDescription.text = data.description
            buttonCheckRank.text = data.mainCta
            buttonCheckRank.setOnClickListener {
                deeplinkAction.performAction(it.context, data.mainDeeplink)
                sendEvent(
                    EventConstants.DG_LEADERBOARD_VISITED,
                    hashMapOf(Constants.SOURCE to Constants.DG_CTA_AFTER_GOAL_COMPLETE)
                )
                dismiss()
            }

            ivCancel.setOnClickListener {
                dismiss()
            }

            rootContainer.setOnClickListener {
                dismiss()
            }
        }
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf()) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }
}
