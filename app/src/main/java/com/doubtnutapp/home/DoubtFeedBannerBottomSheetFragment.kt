package com.doubtnutapp.home

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.FragmentDoubtFeedBannerBottomSheetBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.widgetmanager.widgets.DoubtFeedBannerWidget
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Created by devansh on 04/08/21.
 */

class DoubtFeedBannerBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "DoubtFeedBannerBottomSheetFragment"

        private const val PARAM_DATA = "data"

        fun newInstance(data: DoubtFeedBannerWidget.Data) =
            DoubtFeedBannerBottomSheetFragment().apply {
                arguments = bundleOf(PARAM_DATA to data)
            }
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val binding by viewBinding(FragmentDoubtFeedBannerBottomSheetBinding::bind)

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return FragmentDoubtFeedBannerBottomSheetBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        DoubtnutApp.INSTANCE.isInAppDialogShowing = true

        val data = arguments?.getParcelable<DoubtFeedBannerWidget.Data>(PARAM_DATA) ?: return

        with(binding) {
            tvDoubtFeedTitle.text = data.title

            buttonDoubtFeed.text = data.ctaText
            buttonDoubtFeed.setOnClickListener {
                deeplinkAction.performAction(requireContext(), data.deeplink)
                dismiss()
                sendEvent(EventConstants.DG_HOME_PAGE_BOTTOMSHEET_CLICKED)
                sendEvent(EventConstants.DG_ICON_CLICK, hashMapOf(
                    Constants.SOURCE to Constants.BOTTOM_SHEET
                ))
            }

            container.setOnClickListener {
                // intercept touch event
            }

            ivClose.setOnClickListener {
                dismiss()
            }

            binding.root.setOnClickListener {
                dismiss()
            }

            sendEvent(EventConstants.DG_HOME_PAGE_BOTTOMSHEET_SHOWN)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        DoubtnutApp.INSTANCE.isInAppDialogShowing = false
    }

    private fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf()) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }
}