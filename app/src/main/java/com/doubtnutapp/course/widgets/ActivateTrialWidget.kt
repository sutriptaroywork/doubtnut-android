package com.doubtnutapp.course.widgets

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.IntentUtils
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.RemoteConfigUtils
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.ActivateVipTrial
import com.doubtnutapp.databinding.WidgetActivateTrialBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ActivateTrialWidget(context: Context) : BaseBindingWidget<ActivateTrialWidget.WidgetHolder,
        TrialWidgetModel, WidgetActivateTrialBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetActivateTrialBinding {
        return WidgetActivateTrialBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: TrialWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        if (model.data.state.equals("inactive")) {
            binding.layoutGetMyTrial.setVisibleState(model.data.trial != null)
            binding.layoutCall.setVisibleState(model.data.callData != null)
            binding.timerLayout.visibility = GONE
            binding.btnCall.text = model.data.callData?.text.orEmpty()
            binding.buttonGetMyTrial.text = model.data.trial?.text.orEmpty()
            if (model.data.trial != null) {
                binding.layoutGetMyTrial.background = Utils.getShape(
                    "#ffffff",
                    "#ea532c",
                    8f,
                    1
                )
            }
            if (model.data.callData != null) {
                binding.layoutCall.background = Utils.getShape(
                    "#ffffff",
                    "#ea532c",
                    8f,
                    1
                )
            }
        } else {
            binding.layoutGetMyTrial.visibility = GONE
            binding.layoutCall.visibility = GONE
            binding.timerLayout.visibility = VISIBLE
            binding.btnTimer.text = model.data.timerText.orEmpty()
        }

        binding.layoutCall.setOnClickListener {
            if (model.data.callData?.deeplink.isNullOrEmpty()) {
                try {
                    holder.itemView.context.startActivity(
                        Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse("tel:" + model.data.callData?.number)
                        )
                    )
                    val event = AnalyticsEvent(
                        EventConstants.COURSE_CALL_CLICK,
                        hashMapOf(
                            EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID to model.data.trial?.assortmentId.orEmpty(),
                            EventConstants.PHONE_NUMBER to model.data.callData?.number.orEmpty()
                        )
                    )
                    analyticsPublisher.publishEvent(event)
                    val countToSendEvent: Int = Utils.getCountToSend(
                        RemoteConfigUtils.getEventInfo(),
                        EventConstants.COURSE_CALL_CLICK
                    )
                    val eventCopy = event.copy()
                    repeat((0 until countToSendEvent).count()) {
                        analyticsPublisher.publishBranchIoEvent(eventCopy)
                    }
                } catch (e: Exception) {
                    // No Activity found to handle Intent { act=android.intent.action.DIAL dat=tel:xxxxxxxxxxx }
                    IntentUtils.showCallActionNotPerformToast(
                        context,
                        model.data.callData?.number.orEmpty()
                    )
                }
            } else {
                deeplinkAction.performAction(context, model.data.callData?.deeplink)
            }
        }
        binding.layoutGetMyTrial.setOnClickListener {
            if (model.data.trial?.deeplink.isNullOrEmpty()) {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.COURSE_TRIAL_CLICK,
                        hashMapOf(
                            EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID to model.data.trial?.assortmentId.orEmpty()
                        ),
                        ignoreBranch = false
                    )
                )
                actionPerformer?.performAction(ActivateVipTrial(model.data.trial?.assortmentId.orEmpty()))
            } else {
                val event = AnalyticsEvent(
                    EventConstants.BUY_NOW_CLICK,
                    ignoreMoengage = false
                    )
                val event2 = AnalyticsEvent(
                    EventConstants.BUY_NOW_CLICK + "_v2",
                    hashMapOf(
                        EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID to model.data.trial?.assortmentId.orEmpty()
                    )
                )
                analyticsPublisher.publishEvent(event)
                analyticsPublisher.publishMoEngageEvent(event)
                val countToSendEvent: Int = Utils.getCountToSend(
                    RemoteConfigUtils.getEventInfo(),
                    EventConstants.BUY_NOW_CLICK
                )
                val eventCopy = event.copy()
                val event2Copy = event2.copy()
                repeat((0 until countToSendEvent).count()) {
                    analyticsPublisher.publishBranchIoEvent(eventCopy)
                    analyticsPublisher.publishBranchIoEvent(event2Copy)
                }
                deeplinkAction.performAction(context, model.data.trial?.deeplink)
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetActivateTrialBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetActivateTrialBinding>(binding, widget)
}

class TrialWidgetModel : WidgetEntityModel<TrialWidgetData, WidgetAction>()

@Keep
data class TrialWidgetData(
    @SerializedName("call") val callData: CallData?,
    @SerializedName("state") val state: String?,
    @SerializedName("trial") val trial: TrialData?,
    @SerializedName("timer") val timerText: String?
) : WidgetData() {
    @Keep
    data class CallData(
        @SerializedName("text") val text: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("number") val number: String?
    )

    @Keep
    data class TrialData(
        @SerializedName("text") val text: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("assortment_id") val assortmentId: String?
    )
}
