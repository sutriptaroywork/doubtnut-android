package com.doubtnutapp.course.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.TextViewUtils
import com.doubtnut.core.utils.applyBackgroundColor
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.RemoteConfigUtils
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetViewPlanButtonBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.loadImage
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class WidgetViewPlanButton @JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<WidgetViewPlanButton.WidgetHolder, WidgetViewPlanButtonModel,
        WidgetViewPlanButtonBinding>(context, attrs, defStyle) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetViewPlanButtonBinding {
        return WidgetViewPlanButtonBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: WidgetViewPlanButtonModel
    ): WidgetHolder {
        super.bindWidget(holder, model)

        val binding = holder.binding

        binding.clButton.applyBackgroundColor(model.data.bgColor)
        binding.tvHeader.applyBackgroundColor(model.data.headerBackgroundColor)

        binding.tvHeader.text = model.data.headerTitle
        binding.tvTextOne.text = model.data.textOne
        binding.tvTextTwo.text = model.data.textTwo

        binding.tvHeader.applyTextSize(model.data.headerTitleTextSize)
        binding.tvTextOne.applyTextSize(model.data.textOneSize)
        binding.tvTextTwo.applyTextSize(model.data.textTwoSize)

        binding.tvHeader.applyTextColor(model.data.headerTitleTextColor)
        binding.tvTextOne.applyTextColor(model.data.textOneColor)
        binding.tvTextTwo.applyTextColor(model.data.textTwoColor)

        binding.tvHeader.isVisible = model.data.headerTitle.isNullOrEmpty().not()
        binding.tvTextOne.isVisible = model.data.textOne.isNullOrEmpty().not()
        binding.tvTextTwo.isVisible = model.data.textTwo.isNullOrEmpty().not()

        binding.ivIconEnd.isVisible = model.data.showIconEnd == true
        TextViewUtils.setTextFromHtml(binding.tvTextStrike, model.data.strikeThroughText ?: "")

        if (model.data.iconEnd.isNullOrEmpty().not()) {
            binding.ivIconEnd.loadImage(model.data.iconEnd)
        }

        binding.clButton.setOnClickListener {
            val event = AnalyticsEvent(
                EventConstants.NCP_VIEW_PLAN_TAPPED,
                model.extraParams ?: hashMapOf(),
                ignoreMoengage = false
            )
            analyticsPublisher.publishEvent(event)
            val countToSendEvent: Int = Utils.getCountToSend(
                RemoteConfigUtils.getEventInfo(),
                EventConstants.NCP_VIEW_PLAN_TAPPED
            )
            MoEngageUtils.setUserAttribute(context, "dn_bnb_clicked",true)

            val eventCopy = event.copy()
            repeat((0 until countToSendEvent).count()) {
                analyticsPublisher.publishBranchIoEvent(eventCopy)
            }
            deeplinkAction.performAction(context, model.data.deepLink)
        }

        return holder
    }

    class WidgetHolder(binding: WidgetViewPlanButtonBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetViewPlanButtonBinding>(binding, widget)
}

@Keep
class WidgetViewPlanButtonModel :
    WidgetEntityModel<WidgetViewPlanButtonData, WidgetAction>()

@Keep
data class WidgetViewPlanButtonData(
    @SerializedName("bg_color")
    val bgColor: String?,

    @SerializedName("header_title")
    val headerTitle: String?,
    @SerializedName("header_title_text_size")
    val headerTitleTextSize: String?,
    @SerializedName("header_title_text_color")
    val headerTitleTextColor: String?,
    @SerializedName("header_background_color")
    val headerBackgroundColor: String?,

    @SerializedName("text_one")
    val textOne: String?,
    @SerializedName("text_one_size")
    val textOneSize: String?,
    @SerializedName("text_one_color")
    val textOneColor: String?,

    @SerializedName("text_two")
    val textTwo: String?,
    @SerializedName("text_two_size")
    val textTwoSize: String?,
    @SerializedName("text_two_color")
    val textTwoColor: String?,

    @SerializedName("show_icon_end")
    val showIconEnd: Boolean?,
    @SerializedName("icon_end")
    val iconEnd: String?,
    @SerializedName("deep_link")
    val deepLink: String?,
    @SerializedName("strike_through_text")
    val strikeThroughText: String?,
    @SerializedName("extra_params")
    val extraParams: HashMap<String, Any>?
) : WidgetData()
