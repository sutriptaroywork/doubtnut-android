package com.doubtnutapp.doubtfeed2.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.FragmentDfUnavailableBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.doubtfeed2.ui.adapter.DfInfoAdapter
import com.doubtnutapp.loadImage
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import java.util.*
import javax.inject.Inject

/**
 * Created by devansh on 12/7/21.
 */

class DfUnavailableFragment : Fragment(R.layout.fragment_df_unavailable) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val binding by viewBinding(FragmentDfUnavailableBinding::bind)
    private val args by navArgs<DfUnavailableFragmentArgs>()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            ivBack.setOnClickListener {
                activity?.onBackPressed()
            }

            tvDoubtFeedTitle.text = args.title

            // Top
            args.topPaneData?.let {
                ivNoDailyGoal.loadImage(it.headingImage)
                tvTitle.text = it.headingText
                tvSubtitle.text = it.description
                buttonAskQuestion.text = it.buttonText
                buttonAskQuestion.setOnClickListener { _ ->
                    sendEvent(
                        EventConstants.DG_ASK_QUESTION_CLICK,
                        hashMapOf(
                            Constants.SOURCE to Constants.DG_NEW_USER
                        )
                    )
                    deeplinkAction.performAction(requireContext(), it.buttonDeeplink)
                }
            }

            // Info
            args.infoData?.let {
                tvInfo.text = it.title
                val infoAdapter = DfInfoAdapter()
                rvInfo.adapter = infoAdapter
                infoAdapter.updateList(it.infoItems)
            }

            // Benefits
            args.benefitsData?.let {
                tvBenefits.text = it.title
                val benefitsAdapter = DfInfoAdapter()
                rvBenefits.adapter = benefitsAdapter
                benefitsAdapter.updateList(it.infoItems)
            }
        }
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf()) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }
}
