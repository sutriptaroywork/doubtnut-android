package com.doubtnutapp.topicboostergame2.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.remote.models.topicboostergame2.UnavailableData
import com.doubtnutapp.databinding.FragmentTbgUnavailableBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class TbgUnavailableFragment : Fragment(R.layout.fragment_tbg_unavailable) {

    companion object {
        private const val UNAVAILABLE_DATA = "unavailableData"

        fun newInstance(id: UnavailableData): TbgUnavailableFragment =
            TbgUnavailableFragment().apply {
                arguments = bundleOf(UNAVAILABLE_DATA to id)
            }
    }

    object Type {
        const val  INVITER_NOT_JOINED = "inviter_not_joined"
        const val INVITEE_NOT_JOINED = "invitee_not_joined"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val binding by viewBinding(FragmentTbgUnavailableBinding::bind)
    private val args by navArgs<TbgUnavailableFragmentArgs>()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {

            val data = args.unavailableData as UnavailableData

            if (data.isInviter != null){
                val type = if (data.isInviter == true) Type.INVITER_NOT_JOINED else Type.INVITEE_NOT_JOINED
                sendEvent(EventConstants.TOPIC_BOOSTER_GAME_PLAY_WITH_OPPONENT_CLICK, type)
            }

            if (data.isIconVisible == false) {
                ivIcon.hide()
            } else {
                ivIcon.show()
            }

            if (!data.title.isNullOrEmpty()) {
                tvTitle.apply {
                    text = data.title
                    show()
                }
            }

            if (!data.subtitle.isNullOrEmpty()) {
                tvSubTitle.apply {
                    text = data.subtitle
                    data.subtitle
                    show()
                }
            }

            if (!data.ctaText.isNullOrEmpty()) {
                buttonCta.apply {
                    text = data.ctaText
                    show()
                    setOnClickListener {
                        deeplinkAction.performAction(context, data.deeplink)
                    }
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