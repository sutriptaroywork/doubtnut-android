package com.doubtnutapp.freeclasses.widgets

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.GradientUtils
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetWatchAndWinBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.ifEmptyThenNull
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class WatchAndWinWidget(context: Context) :
    BaseBindingWidget<WatchAndWinWidget.WidgetViewHolder,
            WatchAndWinWidget.Model, WidgetWatchAndWinBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "WatchAndWinWidget"
        const val EVENT_TAG = "watch_and_win_widget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetWatchAndWinBinding {
        return WidgetWatchAndWinBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: Model
    ): WidgetViewHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.root.background = GradientUtils.getGradientBackground(
            startGradient = model.data.bgColor1,
            endGradient = model.data.bgColor2,
            orientation = GradientDrawable.Orientation.LEFT_RIGHT
        )

        binding.tvTitle1.isVisible = model.data.titleOne.isNullOrEmpty().not()
        binding.tvTitle1.text = model.data.titleOne
        binding.tvTitle1.applyTextSize(model.data.titleOneTextSize)
        binding.tvTitle1.applyTextColor(model.data.titleOneTextColor)

        binding.tvTitle2.isVisible = model.data.titleTwo.isNullOrEmpty().not()
        binding.tvTitle2.text = model.data.titleTwo
        binding.tvTitle2.applyTextSize(model.data.titleTwoTextSize)
        binding.tvTitle2.applyTextColor(model.data.titleTwoTextColor)

        binding.ivAction.isVisible = model.data.imageUrl1.isNullOrEmpty().not()
        binding.ivAction.loadImage(model.data.imageUrl1.ifEmptyThenNull())

        binding.tvAction.isVisible = model.data.titleThree.isNullOrEmpty().not()
        binding.tvAction.text = model.data.titleThree
        binding.tvAction.applyTextSize(model.data.titleThreeTextSize)
        binding.tvAction.applyTextColor(model.data.titleThreeTextColor)

        binding.ivIcon.isVisible = model.data.imageUrl2.isNullOrEmpty().not()
        binding.ivIcon.loadImage(model.data.imageUrl2.ifEmptyThenNull())

        binding.root.setOnClickListener {
            deeplinkAction.performAction(context, model.data.deeplink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.WIDGET_TITLE to model.data.titleOne.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        binding.clAction.setOnClickListener {
            deeplinkAction.performAction(context, model.data.ctaDeeplink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.CTA_TEXT to model.data.titleThree.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        return holder
    }

    class WidgetViewHolder(binding: WidgetWatchAndWinBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetWatchAndWinBinding>(binding, widget)

    class Model :
        WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title_one") val titleOne: String?,
        @SerializedName("title_one_text_size") val titleOneTextSize: String?,
        @SerializedName("title_one_text_color") val titleOneTextColor: String?,

        @SerializedName("title_two") val titleTwo: String?,
        @SerializedName("title_two_text_size") val titleTwoTextSize: String?,
        @SerializedName("title_two_text_color") val titleTwoTextColor: String?,

        @SerializedName("image_url1") val imageUrl1: String?,
        @SerializedName("title_three") val titleThree: String?,
        @SerializedName("title_three_text_size") val titleThreeTextSize: String?,
        @SerializedName("title_three_text_color") val titleThreeTextColor: String?,
        @SerializedName("cta_deeplink") val ctaDeeplink: String?,

        @SerializedName("image_url2") val imageUrl2: String?,

        @SerializedName("bg_color1") val bgColor1: String?,
        @SerializedName("bg_color2") val bgColor2: String?,

        @SerializedName("deeplink") val deeplink: String?,

        ) : WidgetData()
}
