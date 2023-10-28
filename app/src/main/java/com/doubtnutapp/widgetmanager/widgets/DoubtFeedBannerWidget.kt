package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.WidgetClickedEvent

import com.doubtnutapp.databinding.WidgetDoubtFeedBannerBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.hide
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

/**
 * Created by devansh on 08/06/21.
 */

class DoubtFeedBannerWidget(context: Context) : BaseBindingWidget<DoubtFeedBannerWidget.WidgetHolder,
        DoubtFeedBannerWidget.Model, WidgetDoubtFeedBannerBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetDoubtFeedBannerBinding {
        return WidgetDoubtFeedBannerBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        val data = model.data

        val binding = holder.binding

        if (data.isHidden) {
            binding.root.hide()
            return holder
        }

        binding.apply {
            tvDoubtFeedTitle.text = data.title
            tvDoubtFeedSubtitle.isVisible = data.subtitle.isNullOrEmpty().not()
            tvDoubtFeedSubtitle.text = data.subtitle
            buttonDoubtFeed.text = data.ctaText

            setOnClickListener {
                publishEvent(EventConstants.DF_HOME_BANNER_CLICKED, hashMapOf(
                    Constants.STATE to data.type,
                    Constants.TOPIC to data.topic,
                ))
                publishEvent(EventConstants.DG_HOME_BANNER_CLICKED, hashMapOf(
                    Constants.STATE to data.type,
                    Constants.TOPIC to data.topic,
                ))
                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams?.apply {
                    put(Constants.ID, data.id)
                }))
                deeplinkAction.performAction(context, data.deeplink)
            }

            ivDoubtFeedClose.setOnClickListener {
                root.hide()
                data.isHidden = true
            }

            publishEvent(EventConstants.DF_HOME_BANNER_VIEWED, hashMapOf(
                Constants.STATE to data.type,
                Constants.TOPIC to data.topic,
            ), ignoreSnowplow = true)
            publishEvent(EventConstants.DG_HOME_BANNER_VIEWED, hashMapOf(
                Constants.STATE to data.type,
                Constants.TOPIC to data.topic,
            ))
        }
        trackingViewId = data.id
        return holder
    }

    class WidgetHolder(binding: WidgetDoubtFeedBannerBinding, widget: BaseWidget<*, *>) :
            WidgetBindingVH<WidgetDoubtFeedBannerBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    @Parcelize
    data class Data(
        @SerializedName("id") val id: String,
        @SerializedName("title") val title: String,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("cta_text") val ctaText: String,
        @SerializedName("deeplink") val deeplink: String,
        @SerializedName("topic") val topic: String,
        @SerializedName("type") val type: String,
        @SerializedName("is_hidden") var isHidden: Boolean = false,
    ) : WidgetData(), Parcelable {

        companion object {
            const val TYPE_NO_TASK_GENERATED = "no_task_generated"
        }
    }

}