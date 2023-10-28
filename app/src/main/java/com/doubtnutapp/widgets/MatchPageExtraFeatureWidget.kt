package com.doubtnutapp.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.MatchPageFeatureAction
import com.doubtnutapp.databinding.WidgetMatchPageExtraFeatureBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * this widget can be used throughout match page.
 * Currently Any CTA click in this widget is using extra data which is coming on match page.
 * that is why using an extra key 'action'.
 * Note - For any other screen use this widget with different deeplink for CTA.
 */
class MatchPageExtraFeatureWidget(context: Context) :
    BaseBindingWidget<MatchPageExtraFeatureWidget.WidgetHolder,
            MatchPageExtraFeatureWidget.Model, WidgetMatchPageExtraFeatureBinding>(context) {

    var source: String? = null

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model)
        val data = model.data
        holder.binding.apply {
            cardView.apply {
                applyStrokeColor(data.cardStrokeColor)
                applyCornerRadius(data.cardCornerRadius)
            }

            ivHeader.apply {
                if (data.headerImage.isNotNullAndNotEmpty()) {
                    visible()
                    loadImageEtx(data.headerImage)
                } else {
                    gone()
                }
            }

            tvTitle.apply {
                if (data.title.isNotNullAndNotEmpty()) {
                    visible()
                    text = data.title
                    applyTypeface(data.isTitleBold)
                    applyTextColor(data.titleColor)
                } else {
                    gone()
                }
            }

            tvSubTitle.apply {
                if (data.subtitle.isNotNullAndNotEmpty()) {
                    visible()
                    text = data.subtitle
                    applyTypeface(data.isSubtitleBold)
                    applyTextColor(data.subtitleColor)
                } else {
                    gone()
                }
            }

            btCta1.apply {
                data.cta1?.let { cta1 ->
                    visible()
                    text = cta1.title
                    applyTextColor(cta1.titleColor)
                    applyCornerRadius(cta1.cornerRadius)
                    applyStrokeWidth(cta1.strokeWidth)
                    applyBackgroundColor(cta1.backgroundColor)
                    applyStrokeColor(cta1.strokeColor)
                    setOnClickListener {
                        val deeplink = cta1.deeplink
                        val action = cta1.action
                        deeplinkAction.performAction(context, deeplink)
                        actionPerformer?.performAction(MatchPageFeatureAction(action))
                    }
                } ?: gone()
            }

            btCta2.apply {
                data.cta2?.let { cta2 ->
                    visible()
                    text = cta2.title
                    applyTextColor(cta2.titleColor)
                    applyCornerRadius(cta2.cornerRadius)
                    applyStrokeWidth(cta2.strokeWidth)
                    applyBackgroundColor(cta2.backgroundColor)
                    applyStrokeColor(cta2.strokeColor)
                    setOnClickListener {
                        val deeplink = cta2.deeplink
                        val action = cta2.action
                        deeplinkAction.performAction(context, deeplink)
                        actionPerformer?.performAction(MatchPageFeatureAction(action))
                    }
                } ?: gone()
            }
        }

        return holder
    }

    class WidgetHolder(
        binding: WidgetMatchPageExtraFeatureBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetMatchPageExtraFeatureBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String,
        @SerializedName("feature") val feature: String,
        @SerializedName("header_image") val headerImage: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("is_title_bold") val isTitleBold: Boolean?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_color") val subtitleColor: String?,
        @SerializedName("is_subtitle_bold") val isSubtitleBold: Boolean?,
        @SerializedName("cta1") val cta1: Cta?,
        @SerializedName("cta2") val cta2: Cta?,
        @SerializedName("card_stroke_color") val cardStrokeColor: String?,
        @SerializedName("card_corner_radius") val cardCornerRadius: Float?
    ) : WidgetData()

    @Keep
    data class Cta(
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("stroke_width") val strokeWidth: Int?,
        @SerializedName("stroke_color") val strokeColor: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("corner_radius") val cornerRadius: Int?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("action") val action: String?,
    )

    override fun getViewBinding(): WidgetMatchPageExtraFeatureBinding {
        return WidgetMatchPageExtraFeatureBinding.inflate(LayoutInflater.from(context), this, true)
    }
}