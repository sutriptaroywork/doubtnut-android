package com.doubtnutapp.dnr.ui.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.os.bundleOf
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.FragmentDnrPopupDialogBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.dnr.model.DnrPopupDialogData
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 27/10/21
 */

class DnrPopupDialogFragment :
    BaseBindingDialogFragment<DummyViewModel, FragmentDnrPopupDialogBinding>() {

    companion object {
        const val TAG = "DnrPopupDialogFragment"
        const val DNR_POPUP_DATA = "dnr_popup_data"
        fun newInstance(data: DnrPopupDialogData): DnrPopupDialogFragment {
            return DnrPopupDialogFragment().apply {
                arguments = bundleOf(DNR_POPUP_DATA to data)
            }
        }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val data by lazy { arguments?.getParcelable<DnrPopupDialogData>(DNR_POPUP_DATA) }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDnrPopupDialogBinding =
        FragmentDnrPopupDialogBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel = viewModelProvider(viewModelFactory)

    override fun getTheme(): Int = R.style.dialog_theme

    override fun setupView(view: View, savedInstanceState: Bundle?) {

        // set preference to avoid showing this dialog again
        defaultPrefs().edit {
            putBoolean(Constants.DNR_TOOLTIP_POPUP, true)
        }
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.logo.loadImage(data?.imageUrl)
        binding.title.text = data?.title
        binding.content.text = data?.description
        binding.button.text = data?.ctaText
        binding.button.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.DNR_INTRO_POPUP_CTA_CLICK, ignoreSnowplow = true))
            val deeplink = data?.deeplink ?: return@setOnClickListener
            deeplinkAction.performAction(requireContext(), deeplink)
            dismiss()
        }
        binding.ivClose.setOnClickListener {
            dismiss()
        }
        binding.parent.setOnClickListener {
            dismiss()
        }
    }
}
