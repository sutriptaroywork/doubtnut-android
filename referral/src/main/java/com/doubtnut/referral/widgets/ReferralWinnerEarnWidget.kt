package com.doubtnut.referral.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
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
import com.doubtnut.referral.R
import com.doubtnut.referral.databinding.ReferralWinnerEarnWidgetBinding
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Raghav Aggarwal on 07/03/22.
 */
class ReferralWinnerEarnWidget constructor(context: Context) :
    CoreBindingWidget<ReferralWinnerEarnWidget.WidgetHolder, ReferralWinnerEarnWidget.WidgetModel, ReferralWinnerEarnWidgetBinding>(
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
        const val TAG = "ReferralWinnerEarnWidget"
        const val EVENT_TAG = "referral_winner_earn_widget"
    }

    override fun getViewBinding(): ReferralWinnerEarnWidgetBinding {
        return ReferralWinnerEarnWidgetBinding.inflate(LayoutInflater.from(context), this, true)
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
        val data = model.data

        binding.apply {

            if (model.data.deeplink.isNullOrEmpty()) {
                binding.root.applyRippleColor("#00000000")
            } else {
                binding.root.rippleColor = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.grey_light)
                )
            }

            ivBackground.loadImage2(
                data.backgroundImageUrl,
                R.color.color_fff3fb
            )

            ivImage.loadImage2(data.imageUrl)

            tvTitle1.text = data.title1.orEmpty()
            tvTitle1.applyTextSize(data.title1Size)
            tvTitle1.applyTextColor(data.title1Color)

            TextViewUtils.setTextFromHtml(tvTitle2, data.title2.orEmpty())

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

    class WidgetHolder(binding: ReferralWinnerEarnWidgetBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<ReferralWinnerEarnWidgetBinding>(binding, widget)

    @Keep
    class WidgetModel :
        WidgetEntityModel<ReferralWinnerEarnWidgetData, WidgetAction>()

    @Keep
    data class ReferralWinnerEarnWidgetData(
        @SerializedName("title1")
        val title1: String?,
        @SerializedName("title1_color")
        val title1Color: String?,
        @SerializedName("title1_size")
        val title1Size: String?,
        @SerializedName("title2")
        val title2: String?,
        @SerializedName("image_url")
        val imageUrl: String?,
        @SerializedName("bg_image_url")
        val backgroundImageUrl: String?,
        @SerializedName("deeplink")
        val deeplink: String?,
        @SerializedName("extra_params")
        var extraParams: HashMap<String, Any>?
    ) : WidgetData()

}

