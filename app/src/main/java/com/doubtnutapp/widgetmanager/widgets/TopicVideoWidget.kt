package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetTopicVideoBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 10/5/21.
 */

class TopicVideoWidget(context: Context) :
    BaseBindingWidget<TopicVideoWidget.WidgetHolder, TopicVideoWidget.Model, WidgetTopicVideoBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetTopicVideoBinding {
        return WidgetTopicVideoBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val data = model.data
        val binding = holder.binding
        binding.apply {
            rootLayout.updateLayoutParams {
                width = Utils.getWidthFromScrollSize(holder.itemView.context, data.cardWidth) -
                        (marginStart + marginEnd)
            }

            tvTitle.text = data.title
            tvSubtitle.text = data.subtitle

            if (data.ocrText.isNullOrBlank().not()) {
                mathView.apply {
                    show()
                    setFontSize(10)
                    setTextColor("black")
                    text = data.ocrText
                }
                ivThumbnail.hide()
            } else {
                mathView.hide()
                ivThumbnail.show()
                ivThumbnail.loadImage(data.thumbnailImage)
            }

            setOnClickListener {
                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams?.apply {
                    put(Constants.ID, data.id)
                    put(Constants.WIDGET_TYPE, WidgetTypes.TYPE_TOPIC_VIDEO)
                }))
                deeplinkAction.performAction(context, data.deeplink)
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetTopicVideoBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTopicVideoBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String,
        @SerializedName("subtitle") val subtitle: String,
        @SerializedName("card_width") val cardWidth: String,
        @SerializedName("deeplink") val deeplink: String,
        @SerializedName("ocr_text") val ocrText: String?,
        @SerializedName("thumbnail_image") val thumbnailImage: String?,
        @SerializedName("id") val id: String,
    ) : WidgetData()
}