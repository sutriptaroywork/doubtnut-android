package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetShortsVideosExhaustedBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ShortsVideosExhaustedWidget(context: Context) :
    BaseBindingWidget<ShortsVideosExhaustedWidget.WidgetHolder,
            ShortsVideosExhaustedWidgetModel, WidgetShortsVideosExhaustedBinding>(context) {

    companion object {
        const val TAG = "ShortsVideosExhaustedWidget"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent
            .forceUnWrap()
            .inject(this)

        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ShortsVideosExhaustedWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
        })
        val data: ShortsVideosExhaustedWidgetData = model.data
        val binding = holder.binding

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                model.type + EventConstants.UNDERSCORE + EventConstants.VIEWED,
                hashMapOf<String, Any>().apply {
                    putAll(model.extraParams ?: hashMapOf())
                }
            )
        )

        binding.tvTitle.text = data.title.orEmpty()
        binding.tvSubtitle.text = data.subtitle.orEmpty()
        if (data.button != null) {
            binding.btn.show()
            binding.btn.text = data.button.title.orEmpty()
            binding.btn.setOnClickListener {
                deeplinkAction.performAction(context, data.button.deeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        model.type + EventConstants.UNDERSCORE + EventConstants.BUTTON_CLICKED,
                        hashMapOf<String, Any>().apply {
                            putAll(model.extraParams ?: hashMapOf())
                        }
                    )
                )
            }
        } else {
            binding.btn.hide()
        }
        return holder
    }

    class WidgetHolder(
        binding: WidgetShortsVideosExhaustedBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetShortsVideosExhaustedBinding>(binding, widget)

    override fun getViewBinding(): WidgetShortsVideosExhaustedBinding {
        return WidgetShortsVideosExhaustedBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class ShortsVideosExhaustedWidgetModel :
    WidgetEntityModel<ShortsVideosExhaustedWidgetData, WidgetAction>()

@Keep
data class ShortsVideosExhaustedWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("button") val button: Button?
) : WidgetData()

@Keep
data class Button(
    @SerializedName("title") val title: String?,
    @SerializedName("deeplink") val deeplink: String?
)
