package com.doubtnutapp.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetLibararyCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class LibraryCardWidget(context: Context) : BaseBindingWidget<LibraryCardWidget.WidgetHolder,
        LibraryCardWidget.Model, WidgetLibararyCardBinding>(context) {

    companion object {
        private const val TAG = "LibraryCardWidget"
        private const val LIBRARY_CARD_WIDGET_CLICK = "library_card_widget_click"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = null

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model.apply {
            this.layoutConfig = WidgetLayoutConfig(
                marginTop = 0,
                marginBottom = 14,
                marginLeft = 0,
                marginRight = 8
            )
        })

        val data = model.data
        val binding = holder.binding

        Utils.setWidthBasedOnPercentage(
            context = holder.itemView.context,
            view = holder.itemView,
            size = data.cardWidth,
            spacing = R.dimen.spacing_4
        )
        data.cardRatio?.let {
            binding.imageCardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                dimensionRatio = data.cardRatio
            }
        }
        requestLayout()

        holder.itemView.apply {
            if (data.ocrText.isNotNullAndNotEmpty()) {
                binding.ivThumbnail.hide()
                binding.mathView.apply {
                    show()
                    text = data.ocrText
                    if (data.backgroundColor.isValidColorCode()) {
                        setBackgroundColor(Color.parseColor(data.backgroundColor))
                    }
                }
            } else {
                binding.mathView.hide()
                binding.ivThumbnail.apply {
                    show()
                    setScaleType(data.imageScaleType)
                    loadImage(data.imageUrl)
                }
            }

            setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        LIBRARY_CARD_WIDGET_CLICK,
                        hashMapOf<String, Any>(
                            EventConstants.CLICKED_ITEM_ID to data.id,
                            EventConstants.WIDGET to TAG,
                            EventConstants.SOURCE to source.orEmpty(),
                            EventConstants.WIDGET_TITLE to data.title.orEmpty(),
                            EventConstants.PAGE to data.page.orEmpty(),
                            EventConstants.ITEM_POSITION to widgetViewHolder.absoluteAdapterPosition
                        ).apply {
                            putAll(model.extraParams ?: HashMap())
                        }, ignoreMoengage = false
                    )
                )
                deeplinkAction.performAction(context, data.deeplink)
            }

            binding.tvTitle.apply {
                isVisible = data.title.isNotNullAndNotEmpty()
                text = data.title
            }
        }

        trackingViewId = data.id

        return holder
    }

    class WidgetHolder(
        binding: WidgetLibararyCardBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetLibararyCardBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String,
        @SerializedName("page") val page: String?,
        @SerializedName("image_url", alternate = ["thumbnail"]) val imageUrl: String?,
        @SerializedName("image_scale_type") val imageScaleType: String?,
        @SerializedName("card_width") val cardWidth: String,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("ocr_text") val ocrText: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("deeplink") val deeplink: String?,
    ) : WidgetData()

    override fun getViewBinding(): WidgetLibararyCardBinding {
        return WidgetLibararyCardBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

