package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.EventBus.WidgetShownEvent
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.databinding.WidgetFeedBannerBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 11/1/21.
 */

class FeedBannerWidget(context: Context)
    : BaseBindingWidget<FeedBannerWidget.WidgetHolder, FeedBannerWidget.Model,WidgetFeedBannerBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init{DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)}

    override fun getViewBinding(): WidgetFeedBannerBinding {
        return WidgetFeedBannerBinding.inflate(LayoutInflater.from(context),this,true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(),this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        val data = model.data
        holder.binding.apply {
            tvTitle.text = data.title
            tvBannerText.text = data.bannerText
            buttonJoin.text = data.buttonText

            banner.background = MaterialShapeDrawable(
                    ShapeAppearanceModel()
                            .toBuilder()
                            .setAllCornerSizes(4f.dpToPx())
                            .build()
            ).apply {
                setTint(ContextCompat.getColor(context, R.color.blue_223d4d))
            }

            setOnClickListener {
                deeplinkAction.performAction(context, data.deeplink)
                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams))
            }
        }
        DoubtnutApp.INSTANCE.bus()?.send(WidgetShownEvent(model.extraParams))

        return holder
    }

    class WidgetHolder(binding: WidgetFeedBannerBinding,widget: BaseWidget<*, *>) :
            WidgetBindingVH<WidgetFeedBannerBinding>(binding,widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
            @SerializedName("title") val title: String,
            @SerializedName("banner_text") val bannerText: String,
            @SerializedName("button_text") val buttonText: String,
            @SerializedName("deeplink") val deeplink: String
    ) : WidgetData()
}