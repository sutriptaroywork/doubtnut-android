package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.doubtnut.core.utils.applyBackgroundTint
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.databinding.WidgetRevisionCornerBannerBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.toggleVisibilityAndSetText
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 09/08/21.
 */

class RevisionCornerBannerWidget(context: Context) :
    BaseBindingWidget<RevisionCornerBannerWidget.WidgetHolder,
            RevisionCornerBannerWidget.Model, WidgetRevisionCornerBannerBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetRevisionCornerBannerBinding {
        return WidgetRevisionCornerBannerBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        val data = model.data

        holder.binding.apply {
            cardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                dimensionRatio = data.cardRatio ?: "3.2:1"
            }

            tvHeading.toggleVisibilityAndSetText(data.heading)

            tvTitle.apply {
                text = data.title
                applyTextColor(data.titleTextColor)
                applyTextSize(data.titleTextSize)
            }

            tvSubtitle.apply {
                toggleVisibilityAndSetText(data.subtitle)
                applyTextColor(data.subtitleTextColor)
                applyTextSize(data.subtitleTextSize)
            }

            tvCompleted.apply {
                toggleVisibilityAndSetText(data.completedText)
                applyTextColor(data.completedTextColor)
                applyTextSize(data.completedTextSize)
                applyBackgroundTint(data.completedBackgroundColor)
            }

            tvInfo.toggleVisibilityAndSetText(data.infoText)

            ivBackground.loadImage(data.backgroundImage)
            ivIcon.loadImage(data.iconImage)

            cardView.setOnClickListener {
                val extraParams = if (model.extraParams == null) hashMapOf() else model.extraParams
                extraParams?.put(
                    Constants.WIDGET_CLICK_SOURCE,
                    Constants.WIDGET_REVISION_CORNER_BANNER
                )
                extraParams?.put(Constants.TYPE, data.eventName.orEmpty())
                DoubtnutApp.INSTANCE.bus()?.send(
                    WidgetClickedEvent(
                        extraParams = extraParams,
                        widgetType = WidgetTypes.TYPE_REVISION_CORNER_BANNER
                    )
                )

                deeplinkAction.performAction(it.context, data.deeplink)
            }
        }
        trackingViewId = data.id
        return holder
    }

    class WidgetHolder(binding: WidgetRevisionCornerBannerBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetRevisionCornerBannerBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String,
        @SerializedName("heading") val heading: String?,
        @SerializedName("title") val title: String,
        @SerializedName("title_text_color") val titleTextColor: String,
        @SerializedName("title_text_size") val titleTextSize: String,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_text_color") val subtitleTextColor: String,
        @SerializedName("subtitle_text_size") val subtitleTextSize: String,
        @SerializedName("deeplink") val deeplink: String,
        @SerializedName("background_image") val backgroundImage: String,
        @SerializedName("icon_image") val iconImage: String,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("event_name") val eventName: String?,
        @SerializedName("completed_text") val completedText: String?,
        @SerializedName("completed_text_color") val completedTextColor: String?,
        @SerializedName("completed_background_color") val completedBackgroundColor: String?,
        @SerializedName("completed_text_size") val completedTextSize: String?,
        @SerializedName("info_text") val infoText: String?,
    ) : WidgetData()
}