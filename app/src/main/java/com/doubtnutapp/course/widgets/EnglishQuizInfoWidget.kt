package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetEnglishQuizInfoBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class EnglishQuizInfoWidget(context: Context) :
    BaseBindingWidget<EnglishQuizInfoWidget.EnglishQuizInfoWidgetHolder,
            EnglishQuizInfoWidget.EnglishQuizInfoWidgetModel, WidgetEnglishQuizInfoBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bindWidget(
        holder: EnglishQuizInfoWidgetHolder,
        model: EnglishQuizInfoWidgetModel
    ): EnglishQuizInfoWidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = WidgetLayoutConfig(
                model.layoutConfig?.marginTop ?: 0,
                model.layoutConfig?.marginBottom ?: 0,
                model.layoutConfig?.marginLeft ?: 0,
                model.layoutConfig?.marginRight ?: 0
            )
        })
        val data: EnglishQuizInfoWidgetData = model.data
        val binding = holder.binding
        binding.apply {
            tvTitle.text = data.title.toString()
            tvTitle.textSize = data.titleSize ?: 24f
            tvSubtitle.text = data.subtitle.toString()
            tvSubtitle.textSize = data.subtitleSize ?: 14f
            ivBottom.loadImageEtx(data.imageUrl.orEmpty())
            ivPlay.loadImageEtx(data.ctaImageUrl.orEmpty())
            ivPlay.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        model.type + EventConstants.WIDGET_ITEM_CLICK,
                        hashMapOf<String, Any>().apply {
                            putAll(model.extraParams ?: HashMap())
                        }
                    )
                )
                deeplinkAction.performAction(context, data.deeplink.orEmpty())
            }
            tvCta.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        model.type + EventConstants.WIDGET_ITEM_CLICK,
                        hashMapOf<String, Any>().apply {
                            putAll(model.extraParams ?: HashMap())
                        }
                    )
                )
                deeplinkAction.performAction(context, data.deeplink.orEmpty())
            }
            tvCta.text = data.ctaText.orEmpty()
            tvCta.textSize = data.ctaTextSize ?: 16f
        }
        return holder
    }


    override fun setupViewHolder() {
        this.widgetViewHolder = EnglishQuizInfoWidgetHolder(getViewBinding(), this)
    }

    class EnglishQuizInfoWidgetHolder(
        binding: WidgetEnglishQuizInfoBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetEnglishQuizInfoBinding>(binding, widget)

    class EnglishQuizInfoWidgetModel : WidgetEntityModel<EnglishQuizInfoWidgetData, WidgetAction>()

    @Keep
    data class EnglishQuizInfoWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("title_size") val titleSize: Float?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_size") val subtitleSize: Float?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("cta_image_url") val ctaImageUrl: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("cta_text") val ctaText: String?,
        @SerializedName("cta_text_size") val ctaTextSize: Float?,
    ) : WidgetData()

    override fun getViewBinding(): WidgetEnglishQuizInfoBinding {
        return WidgetEnglishQuizInfoBinding.inflate(LayoutInflater.from(context), this, true)
    }
}
