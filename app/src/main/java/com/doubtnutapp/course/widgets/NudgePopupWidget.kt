package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.OnNudgeClicked
import com.doubtnutapp.databinding.WidgetNudgePopupBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class NudgePopupWidget(context: Context) :
    BaseBindingWidget<NudgePopupWidget.NudgePopupWidgetHolder, NudgePopupWidget.NudgePopupWidgetModel, WidgetNudgePopupBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bindWidget(holder: NudgePopupWidgetHolder, model: NudgePopupWidgetModel): NudgePopupWidgetHolder {
        super.bindWidget(holder, model)
        val data: NudgeWidgetData = model.data
        val binding = holder.binding
        if (data.isClosed == true) {
            binding.root.setVisibleState(false)
            return holder
        }

        if (data.isBanner == true) {
            binding.button.setVisibleState(false)
            binding.tvTitle.setVisibleState(false)
            binding.tvSubtitle.setVisibleState(false)
            binding.ivBell.setVisibleState(false)
            val params = binding.bgImage.layoutParams as ConstraintLayout.LayoutParams
            if (data.ratio.isNullOrBlank()) {
                params.dimensionRatio = "6:1"
            } else {
                params.dimensionRatio = data.ratio
            }
            binding.bgImage.loadImageEtx(data.bgImageUrl.orEmpty())
        } else {
            binding.tvTitle.text = data.title.orEmpty()
            binding.tvTitle.setTextColor(Utils.parseColor(data.titleColor.orEmpty()))
            binding.tvTitle.textSize = data.titleSize?.toFloat() ?: 22f

            binding.tvSubtitle.text = data.subtitle.orEmpty()
            binding.tvSubtitle.setTextColor(Utils.parseColor(data.subtitleColor.orEmpty()))
            binding.tvSubtitle.textSize = data.subtitleSize?.toFloat() ?: 14f

            binding.button.text = data.ctaText.orEmpty()
            binding.button.setTextColor(Utils.parseColor(data.ctaTextColor.orEmpty()))
            binding.button.textSize = data.ctaTextSize?.toFloat() ?: 16f
            binding.ivBell.loadImageEtx(data.imageUrl.orEmpty())
            binding.button.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_NUDGE_CLICK,
                        hashMapOf<String, Any>(
                            EventConstants.NUDGE_ID to data.widgetId.orEmpty(),
                            EventConstants.NUDGE_TYPE to data.nudgeType.orEmpty(),
                        )
                    )
                )
                actionPerformer?.performAction(OnNudgeClicked())
                deeplinkAction.performAction(context, data.deeplink)
            }
            binding.button.background = Utils.getShape(
                "#ffffff",
                "#ea532c",
                8f,
                2
            )
            if (!data.bgColor.isNullOrEmpty()) {
                holder.itemView.background = (Utils.getShape(data.bgColor, data.bgColor, 12f))
            } else {
                binding.bgImage.loadImageEtx(data.bgImageUrl.orEmpty())
            }
        }
        holder.itemView.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.EVENT_NUDGE_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.NUDGE_ID to data.widgetId.orEmpty(),
                        EventConstants.NUDGE_TYPE to data.nudgeType.orEmpty(),
                    )
                )
            )
            actionPerformer?.performAction(OnNudgeClicked())
            deeplinkAction.performAction(context, data.deeplink)
        }
        binding.ivCross.setOnClickListener {
            data.isClosed = true
            holder.itemView.setVisibleState(false)
        }

        return holder
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = NudgePopupWidgetHolder(getViewBinding(), this)
    }

    class NudgePopupWidgetHolder(
        binding: WidgetNudgePopupBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetNudgePopupBinding>(binding, widget)

    class NudgePopupWidgetModel : WidgetEntityModel<NudgeWidgetData, WidgetAction>()

    @Keep
    data class NudgeWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("title_size") val titleSize: Int?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_color") val subtitleColor: String?,
        @SerializedName("subtitle_size") val subtitleSize: Int?,
        @SerializedName("cta_text") val ctaText: String?,
        @SerializedName("cta_text_color") val ctaTextColor: String?,
        @SerializedName("cta_text_size") val ctaTextSize: Int?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("is_banner") val isBanner: Boolean?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("bg_image_url") val bgImageUrl: String?,
        @SerializedName("bg_color") val bgColor: String?,
        @SerializedName("ratio") val ratio: String?,
        @SerializedName("widget_id") val widgetId: String?,
        @SerializedName("nudge_type") val nudgeType: String?,
        var isClosed: Boolean?,
    ) : WidgetData()

    override fun getViewBinding(): WidgetNudgePopupBinding {
        return WidgetNudgePopupBinding.inflate(LayoutInflater.from(context), this, true)
    }
}
