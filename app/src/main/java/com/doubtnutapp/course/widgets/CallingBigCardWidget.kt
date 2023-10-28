package com.doubtnutapp.course.widgets

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetCallingBigCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.UserUtil
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CallingBigCardWidget @JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<CallingBigCardWidget.WidgetHolder, CallingBigCardWidgetModel,
    WidgetCallingBigCardBinding>(context) {
    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetCallingBigCardBinding {
        return WidgetCallingBigCardBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: CallingBigCardWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)

        val binding = holder.binding

        binding.ivMain.loadImage(model.data.imageUrl)
        binding.ivMain.isVisible = model.data.imageUrl.isNullOrEmpty().not()

        binding.tvTitle.text = model.data.title
        binding.tvTitle.isVisible = model.data.title.isNullOrEmpty().not()

        binding.btnMain.text = model.data.ctaText
        binding.btnMain.isVisible = model.data.ctaText.isNullOrEmpty().not()
        binding.btnMain.setOnClickListener {
            if (model.data.deeplink.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + model.data.mobile))
                context.startActivity(intent)
            } else {
                deeplinkAction.performAction(context, model.data.deeplink)
            }
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET_TITLE to model.data.title.orEmpty(),
                        EventConstants.WIDGET to TAG,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.CTA_TEXT to model.data.ctaText.orEmpty(),
                        EventConstants.SOURCE to source.orEmpty(),
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        return holder
    }

    class WidgetHolder(binding: WidgetCallingBigCardBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCallingBigCardBinding>(binding, widget)

    companion object {
        const val TAG = "CallingBigCardWidget"
        const val EVENT_TAG = "calling_big_card_widget"
    }
}

@Keep
class CallingBigCardWidgetModel :
    WidgetEntityModel<CallingBigCardWidgetData, WidgetAction>()

@Keep
data class CallingBigCardWidgetData(
    @SerializedName("title")
    val title: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("cta_text")
    val ctaText: String?,
    @SerializedName("deeplink")
    val deeplink: String?,
    @SerializedName("mobile")
    val mobile: String?,
) : WidgetData()
