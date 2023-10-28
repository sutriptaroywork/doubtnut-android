package com.doubtnutapp.liveclass.ui.practice_english

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.TextViewUtils
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.FragmentEndSessionBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.loadImage
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.showToast
import com.doubtnut.core.utils.viewModelProvider
import javax.inject.Inject

class EndSessionFragment :
    BaseBindingFragment<PracticeEnglishViewModel, FragmentEndSessionBinding>() {


    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var data: PracticeEndScreenData? = null
    private var isReminderClicked = false

    companion object {
        private const val TAG = "EndSessionFragment"
        private const val DATA = "data"
        fun newInstance(data: PracticeEndScreenData) =
            EndSessionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DATA, data)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            data = it.getParcelable(DATA)
        }
    }


    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentEndSessionBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): PracticeEnglishViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            ivCheers.loadImage(data?.image_url)
            TextViewUtils.setTextFromHtml(tvTitle, data?.display_title_text.orEmpty())
            tvSubtitle.text = data?.display_subtitle_text
            tvReminder.setVisibleState(data?.reminder_text.isNullOrEmpty().not())
            tvReminder.text = data?.reminder_text
            tvReminder.setOnClickListener {
                if (isReminderClicked) {
                    showToast(
                        requireContext(),
                        "Apka reminder notification set ho chuka hai.. Chill karo!"
                    )
                    return@setOnClickListener
                }
                showToast(
                    requireContext(),
                    "OK! Hum kal aapko notification bhej kar yaad dila denge.."
                )
                viewModel.setReminder()
                isReminderClicked = true
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_BUTTON_CLICK,
                        hashMapOf(
                            EventConstants.TYPE to EventConstants.REMIND,
                            EventConstants.SOURCE to EventConstants.SESSION_END
                        )
                    )
                )
            }
            btnTryAgain.setVisibleState(data?.try_again_button_text.isNullOrEmpty().not())
            btnTryAgain.text = data?.try_again_button_text
            btnTryAgain.setOnClickListener {
                deeplinkAction.performAction(
                    requireContext(),
                    data?.deeplink
                )
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_BUTTON_CLICK,
                        hashMapOf(
                            EventConstants.TYPE to EventConstants.SESSION_REPEAT,
                            EventConstants.SOURCE to EventConstants.SESSION_END
                        )
                    )
                )
                requireActivity().finish()
            }
            btnPracticeMore.setVisibleState(data?.practice_more_button_text.isNullOrEmpty().not())
            btnPracticeMore.text = data?.practice_more_button_text
            btnPracticeMore.setOnClickListener {
                deeplinkAction.performAction(
                    requireContext(),
                    data?.deeplink
                )
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_BUTTON_CLICK,
                        hashMapOf(
                            EventConstants.TYPE to EventConstants.PRACTICE_MORE,
                            EventConstants.SOURCE to EventConstants.SESSION_END
                        )
                    )
                )
                requireActivity().finish()
            }
        }
    }

}