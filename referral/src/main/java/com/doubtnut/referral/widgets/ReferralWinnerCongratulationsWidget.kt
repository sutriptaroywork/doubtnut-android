package com.doubtnut.referral.widgets

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.referral.databinding.ReferralWinnerCongratulationsWidgetBinding
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Raghav Aggarwal on 07/03/22.
 */
class ReferralWinnerCongratulationsWidget constructor(context: Context) :
    CoreBindingWidget<ReferralWinnerCongratulationsWidget.WidgetHolder, ReferralWinnerCongratulationsWidget.WidgetModel, ReferralWinnerCongratulationsWidgetBinding>(
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
        const val TAG = "ReferralWinnerCongratulationsWidget"
        const val EVENT_TAG = "referral_winner_congratulations_widget"
    }

    override fun getViewBinding(): ReferralWinnerCongratulationsWidgetBinding {
        return ReferralWinnerCongratulationsWidgetBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
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

        binding.apply {
            ivBackground2.layoutParams?.apply {
                width = data.imageWidth?.toInt()?.dpToPx() ?: 140.dpToPx()
                height = data.imageWidth?.toInt()?.dpToPx() ?: 140.dpToPx()
            }
            ivBackground2.requestLayout()

            ivImage.layoutParams?.apply {
                width = data.imageWidth?.toInt()?.dpToPx() ?: 126.dpToPx()
                height = data.imageHeight?.toInt()?.dpToPx() ?: ViewGroup.LayoutParams.WRAP_CONTENT
            }
            ivImage.loadImage2(data.imageUrl)
            ivImage.requestLayout()

            tvTitle1.text = data.title1.orEmpty()
            tvTitle1.applyTextSize(data.title1Size)
            tvTitle1.applyTextColor(data.title1Color)

            tvTitle2.text = data.title2.orEmpty()
            tvTitle2.applyTextSize(data.title2Size)
            tvTitle2.applyTextColor(data.title2Color)

            ivBackground.background = GradientUtils.getGradientBackground(
                data.bgColorOne,
                data.bgColorTwo,
                GradientDrawable.Orientation.TOP_BOTTOM
            )

            ivForeground.loadImage2(
                data.foregroundImageUrl
            )

            root.setOnClickListener {
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
        }
        return holder
    }

    class WidgetHolder(
        binding: ReferralWinnerCongratulationsWidgetBinding,
        widget: CoreWidget<*, *>
    ) :
        WidgetBindingVH<ReferralWinnerCongratulationsWidgetBinding>(binding, widget)

    @Keep
    class WidgetModel :
        WidgetEntityModel<ImageTextWidgetData, WidgetAction>()

    @Keep
    data class ImageTextWidgetData(
        @SerializedName("title1")
        val title1: String?,
        @SerializedName("title1_color")
        val title1Color: String?,
        @SerializedName("title1_size")
        val title1Size: String?,

        @SerializedName("title2")
        val title2: String?,
        @SerializedName("title2_color")
        val title2Color: String?,
        @SerializedName("title2_size")
        val title2Size: String?,

        @SerializedName("image_url")
        val imageUrl: String?,
        @SerializedName("image_height")
        val imageHeight: String?,
        @SerializedName("image_width")
        val imageWidth: String?,

        @SerializedName("bg_image_url")
        val bgImageUrl: String?,

        @SerializedName("foreground_image_url")
        val foregroundImageUrl: String?,

        @SerializedName("start_color")
        val bgColorOne: String?,
        @SerializedName("end_colour")
        val bgColorTwo: String?,

        @SerializedName("deeplink")
        val deeplink: String?,

        @SerializedName("extra_params")
        var extraParams: HashMap<String, Any>?
    ) : WidgetData()

}

