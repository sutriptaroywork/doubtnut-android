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
import com.doubtnutapp.databinding.FragmentDfBackPressDialogBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.loadImage
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import java.util.HashMap
import javax.inject.Inject

class DfBackPressDialogFragment : DialogFragment(R.layout.fragment_df_back_press_dialog) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val binding by viewBinding(FragmentDfBackPressDialogBinding::bind)
    private val args by navArgs<DfBackPressDialogFragmentArgs>()

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

        with(binding) {
            ivEmoji.loadImage(data.imageUrl)
            tvPopupMessage.text = data.description
            buttonAskQuestion.text = data.mainCta
            tvSecondaryCta.text = data.secondaryCta

            buttonAskQuestion.setOnClickListener {
                deeplinkAction.performAction(view.context, data.mainDeeplink)
                sendEvent(EventConstants.DG_BACKPRESS_POPUP_ASK_CLICK)
                sendEvent(
                    EventConstants.DG_ASK_QUESTION_CLICK,
                    hashMapOf(
                        Constants.SOURCE to Constants.POPUP
                    )
                )
            }
            tvSecondaryCta.setOnClickListener {
                dismiss()
            }
            ivCancel.setOnClickListener { dismiss() }
            rootContainer.setOnClickListener { dismiss() }
            cardContainer.setOnClickListener { }
        }
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf()) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }
}
