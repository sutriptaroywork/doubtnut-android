package com.doubtnut.referral.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.utils.CoreUserUtils
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.loadImage2
import com.doubtnut.core.utils.setTextFromHtml
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.referral.databinding.ImageTextWidgetBinding
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Raghav Aggarwal on 07/03/22.
 */
class ImageTextWidget constructor(context: Context) :
    CoreBindingWidget<ImageTextWidget.WidgetHolder, ImageTextWidget.WidgetModel, ImageTextWidgetBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    var source: String? = null

    init {
        CoreApplication.INSTANCE.androidInjector().inject(this)
    }

    companion object {
        const val TAG = "ImageTextWidget"
        const val EVENT_TAG = "image_text_widget"
    }

    override fun getViewBinding(): ImageTextWidgetBinding {
        return ImageTextWidgetBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: WidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: ImageTextWidgetData = model.data

        binding.tvTitle.setTextFromHtml(data.title.orEmpty())

        binding.ivImage.loadImage2(data.imageUrl)
        binding.ivImage.layoutParams?.apply {
            width = data.imageWidth?.toInt()?.dpToPx() ?: 128.dpToPx()
            height = data.imageHeight?.toInt()?.dpToPx() ?: ViewGroup.LayoutParams.WRAP_CONTENT
        }
        binding.ivImage.requestLayout()

        binding.root.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${CoreEventConstants.CLICKED}",
                    hashMapOf<String, Any>(
                        CoreEventConstants.WIDGET to TAG,
                        CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                        CoreEventConstants.SOURCE to source.orEmpty()
                    ).apply {
                        putAll(data.extraParams.orEmpty())
                    }
                )
            )
            deeplinkAction.performAction(context, data.deeplink)
        }

        return holder
    }

    class WidgetHolder(binding: ImageTextWidgetBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<ImageTextWidgetBinding>(binding, widget)

    @Keep
    class WidgetModel :
        WidgetEntityModel<ImageTextWidgetData, WidgetAction>()

    @Keep
    data class ImageTextWidgetData(
        @SerializedName("title")
        val title: String?,
        @SerializedName("image_url")
        val imageUrl: String?,
        @SerializedName("image_height")
        val imageHeight: String?,
        @SerializedName("image_width")
        val imageWidth: String?,
        @SerializedName("deeplink")
        val deeplink: String?,

        @SerializedName("extra_params")
        var extraParams: HashMap<String, Any>?
    ) : WidgetData()

}

