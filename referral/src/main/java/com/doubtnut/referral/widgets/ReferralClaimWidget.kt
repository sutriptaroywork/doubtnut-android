package com.doubtnut.referral.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.referral.R
import com.doubtnut.referral.databinding.ReferralClaimWidgetBinding
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Raghav Aggarwal on 11/03/22.
 */
class ReferralClaimWidget constructor(context: Context) :
    CoreBindingWidget<ReferralClaimWidget.WidgetHolder, ReferralClaimWidget.WidgetModel, ReferralClaimWidgetBinding>(
        context
    ) {
    class WidgetHolder(binding: ReferralClaimWidgetBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<ReferralClaimWidgetBinding>(binding, widget)

    @Keep
    class WidgetModel :
        WidgetEntityModel<ReferralClaimWidgetData, WidgetAction>()

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    var source: String? = null

    init {
        CoreApplication.INSTANCE.androidInjector().inject(this)
    }

    companion object {
        const val TAG = "ReferralClaimWidget"
        const val EVENT_TAG = "referral_claim_widget"
    }

    override fun getViewBinding(): ReferralClaimWidgetBinding {
        return ReferralClaimWidgetBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: WidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(
                marginTop = 0,
                marginBottom = 0,
                marginLeft = 16,
                marginRight = 16
            )
        }
        val binding = holder.binding
        val data: ReferralClaimWidgetData = model.data
        trackingViewId = data.id

        binding.apply {

            cvOuter.applyBackgroundColor(data.bgColor1)
            cvInner.apply {
                applyBackgroundColor(data.bgColor2)
                if (data.removeInnerLayoutMargin == true) {
                    setMargins(left = 0, top = 0, right = 0, bottom = 0)
                }
            }

            ivBackground.loadImage2(data.bgImageUrl)
            ivImage.apply {
                isVisible = data.imageUrl1.isNotNullAndNotEmpty2()
                loadImage2(data.imageUrl1)
            }
            ivRight.apply {
                layoutParams.height = (data.image2Height ?: 24).dpToPx()
                layoutParams.width = (data.image2Width ?: 24).dpToPx()
                requestLayout()
                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    verticalBias = data.image2VerticalBias ?: 0.5f
                }
                loadImage2(data.imageUrl2)
            }

            tvText1.text = data.title1.orEmpty()
            tvText1.applyTextColor(data.title1Color)
            tvText1.applyTextSize(data.title1Size)
            tvText1.isVisible = data.title1.isNotNullAndNotEmpty2()

            tvText2.applyTextColor(data.title2Color)
            tvText2.applyTextSize(data.title2Size)
            tvText2.setTextFromHtml(data.title2.orEmpty())
            tvText2.isVisible = data.title2.isNotNullAndNotEmpty2()

            tvText3.text = data.title3.orEmpty()
            tvText3.applyTextColor(data.title3Color)
            tvText3.applyTextSize(data.title3Size)
            tvText3.isVisible = data.title3.isNotNullAndNotEmpty2()

            tvText4.text = data.title4.orEmpty()
            tvText4.applyTextColor(data.title4Color)
            tvText4.applyTextSize(data.title4Size)
            cvAction.isVisible = data.title4.isNotNullAndNotEmpty2()
            tvText4.isVisible = data.title4.isNotNullAndNotEmpty2()
            cvAction.background = Utils.getShape(
                colorString = data.bgColor3,
                strokeColor = data.bgColor4,
                cornerRadius = 3.dpToPxFloat(),
                strokeWidth = 1.dpToPx()
            )

            if (model.data.deeplink.isNullOrEmpty()) {
                root.applyRippleColor("#00000000")
            } else {
                root.rippleColor = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.grey_light)
                )
            }

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

    @Keep
    data class ReferralClaimWidgetData(
        @SerializedName("id")
        val id: String?,
        @SerializedName("remove_inner_layout_margin")
        val removeInnerLayoutMargin: Boolean?,
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
        @SerializedName("title3")
        val title3: String?,
        @SerializedName("title3_color")
        val title3Color: String?,
        @SerializedName("title3_size")
        val title3Size: String?,
        @SerializedName("title4")
        val title4: String?,
        @SerializedName("title4_color")
        val title4Color: String?,
        @SerializedName("title4_size")
        val title4Size: String?,

        @SerializedName("bg_color1")
        val bgColor1: String?,
        @SerializedName("bg_color2")
        val bgColor2: String?,
        @SerializedName("bg_color3")
        val bgColor3: String?,
        @SerializedName("bg_color4")
        val bgColor4: String?,

        @SerializedName("bg_image_url")
        val bgImageUrl: String?,
        @SerializedName("image_url1")
        val imageUrl1: String?,
        @SerializedName("image_url2")
        val imageUrl2: String?,
        @SerializedName("image2_vertical_bias")
        val image2VerticalBias: Float?,
        @SerializedName("image2_height")
        val image2Height: Int?,
        @SerializedName("image2_width")
        val image2Width: Int?,
        @SerializedName("deeplink")
        val deeplink: String?,
        @SerializedName("extra_params")
        var extraParams: HashMap<String, Any>?
    ) : WidgetData()

}