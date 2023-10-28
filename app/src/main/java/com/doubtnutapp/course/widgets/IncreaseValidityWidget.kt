package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants.COURSE_VALIDITY_BUY_NOW_CLICK
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetIncreaseValidityBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class IncreaseValidityWidget(context: Context) :
    BaseBindingWidget<IncreaseValidityWidget.WidgetHolder,
            IncreaseValidityWidgetModel, WidgetIncreaseValidityBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: IncreaseValidityWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val data: IncreaseValidityWidgetData = model.data
        val binding = holder.binding
        binding.card.strokeColor = Utils.parseColor(model.data.borderColor.orEmpty())

        binding.titleTv.isVisible = model.data.title.isNotNullAndNotEmpty()
        binding.titleTv.text = model.data.title.orEmpty()
        binding.titleTv.setTextColor(Utils.parseColor(model.data.borderColor.orEmpty()))
        binding.titleTv.applyTextColor(model.data.titleColor)

        binding.subtitleTv.isVisible = model.data.subtitle.isNotNullAndNotEmpty()
        binding.subtitleTv.text = model.data.subtitle.orEmpty()
        binding.subtitleTv.applyTextColor(model.data.titleColor)

        binding.increaseValidityBtn.isVisible = model.data.buttonText.isNotNullAndNotEmpty()
        binding.increaseValidityBtn.text = model.data.buttonText.orEmpty()

        binding.increaseValidityBtn.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    COURSE_VALIDITY_BUY_NOW_CLICK,
                    hashMapOf<String, Any>().apply {
                        putAll(model.extraParams ?: hashMapOf())
                    },
                    ignoreSnowplow = true
                )
            )
            deeplinkAction.performAction(holder.itemView.context, data.deeplink)
        }
        return holder
    }

    override fun getViewBinding(): WidgetIncreaseValidityBinding {
        return WidgetIncreaseValidityBinding.inflate(LayoutInflater.from(context), this, true)
    }

    class WidgetHolder(binding: WidgetIncreaseValidityBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetIncreaseValidityBinding>(binding, widget)
}

class IncreaseValidityWidgetModel : WidgetEntityModel<IncreaseValidityWidgetData, WidgetAction>()

@Keep
data class IncreaseValidityWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("title_color") val titleColor: String?,

    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("subtitle_color") val subtitleColor: String?,

    @SerializedName("background_color") val backgroundColor: String?,
    @SerializedName("border_color") val borderColor: String?,

    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("deeplink") val deeplink: String?,
) : WidgetData()
