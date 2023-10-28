package com.doubtnutapp.revisioncorner.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.remote.models.revisioncorner.UnavailableStats
import com.doubtnutapp.databinding.FragmentRcUnavailableStatsBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class RcUnavailableStatsFragment : Fragment(R.layout.fragment_rc_unavailable_stats) {

    companion object {
        private const val UNAVAILABLE_STATS = "unavailableStats"

        fun newInstance(data: UnavailableStats): RcUnavailableStatsFragment =
            RcUnavailableStatsFragment().apply {
                arguments = bundleOf(UNAVAILABLE_STATS to data)
            }
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val binding by viewBinding(FragmentRcUnavailableStatsBinding::bind)

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val data = arguments?.getParcelable<UnavailableStats>(UNAVAILABLE_STATS)!!
        with(binding) {

            tvTitle.text = data.title.orEmpty()
            tvTitle.isVisible = data.title.isNotNullAndNotEmpty()

            tvDescription.text = data.description.orEmpty()
            tvDescription.isVisible = data.description.isNotNullAndNotEmpty()

            ivSadFace.loadImage(data.image)

            if (data.ctaText.isNullOrEmpty().not()) {
                btStartTest.apply {
                    text = data.ctaText
                    show()
                    setOnClickListener { deeplinkAction.performAction(context, data.deeplink) }
                }
            }
        }
    }

    private fun sendEvent(eventName: String, type: String){
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                hashMapOf(
                    EventConstants.TYPE to type,
                ))
        )
    }

}