package com.doubtnutapp.leaderboard.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.applyRippleColor
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.utils.setTextFromHtml
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetWinnersCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.UserUtil
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.color.MaterialColors
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class WinnersCardWidget @JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<WinnersCardWidget.WidgetHolder, WinnersCardWidget.Model,
        WidgetWinnersCardBinding>(context) {
    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetWinnersCardBinding {
        return WidgetWinnersCardBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: Model
    ): WidgetHolder {
        super.bindWidget(holder, model)

        val binding = holder.binding
        binding.tvTitle1.isVisible = model.data.titleOne.isNullOrEmpty().not()
        binding.tvTitle1.text = model.data.titleOne
        binding.tvTitle1.applyTextColor(model.data.titleOneTextColor)
        binding.tvTitle1.applyTextSize(model.data.titleOneTextSize)

        binding.tvTitle2.isVisible = model.data.titleTwo.isNullOrEmpty().not()
        binding.tvTitle2.text = model.data.titleTwo
        binding.tvTitle2.applyTextColor(model.data.titleTwoTextColor)
        binding.tvTitle2.applyTextSize(model.data.titleTwoTextSize)

        binding.tvTitle3.isVisible = model.data.titleThree.isNullOrEmpty().not()
        binding.tvTitle3.text = model.data.titleThree
        binding.tvTitle3.applyTextColor(model.data.titleThreeTextColor)
        binding.tvTitle3.applyTextSize(model.data.titleThreeTextSize)

        binding.tvTitle4.isVisible = model.data.titleFour.isNullOrEmpty().not()
        binding.tvTitle4.setTextFromHtml(model.data.titleFour.orEmpty())
        binding.tvTitle4.applyTextColor(model.data.titleFourTextColor)
        binding.tvTitle4.applyTextSize(model.data.titleFourTextSize)

        if (model.data.rank == 1) {
            binding.ivImage1Bg.background =
                ContextCompat.getDrawable(context, R.drawable.gradient_champion)
            binding.ivBadge.setImageDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        context,
                        R.color.redTomato
                    )
                )
            )
        } else {
            binding.ivImage1Bg.background =
                ContextCompat.getDrawable(context, R.drawable.gradient_runner_up)
            binding.ivBadge.setImageDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        context,
                        R.color.yellow_fdca46
                    )
                )
            )
        }
        binding.ivImage1.loadImage(model.data.imageUrl1)
        binding.ivImage2.loadImage(model.data.imageUrl2)
        binding.ivImage3.loadImage(model.data.imageUrl3)

        if (model.data.deeplink.isNullOrEmpty()) {
            binding.root.applyRippleColor("#00000000")
        } else {
            binding.root.rippleColor = ColorStateList.valueOf(
                MaterialColors.getColor(binding.root, R.attr.colorControlHighlight)
            )
        }

        binding.root.setOnClickListener {
            deeplinkAction.performAction(context, model.data.deeplink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CARD_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        binding.ivImage1.setOnClickListener {
            deeplinkAction.performAction(context, model.data.deeplink1)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.PROFILE_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }


        return holder
    }

    class WidgetHolder(binding: WidgetWinnersCardBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetWinnersCardBinding>(binding, widget)

    companion object {
        const val TAG = "WinnersCardWidget"
        const val EVENT_TAG = "winners_card_widget"
    }

    @Keep
    class Model :
        WidgetEntityModel<Data, WidgetAction>()


    @Keep
    data class Data(
        @SerializedName("rank")
        val rank: Int?,

        @SerializedName("title_one", alternate = ["title1"])
        val titleOne: String?,
        @SerializedName("title_one_text_size", alternate = ["title1_text_size"])
        val titleOneTextSize: String?,
        @SerializedName("title_one_text_color", alternate = ["title1_text_color"])
        val titleOneTextColor: String?,

        @SerializedName("title_two", alternate = ["title2"])
        val titleTwo: String?,
        @SerializedName("title_two_text_size", alternate = ["title2_text_size"])
        val titleTwoTextSize: String?,
        @SerializedName("title_two_text_color", alternate = ["title2_text_color"])
        val titleTwoTextColor: String?,

        @SerializedName("title_three", alternate = ["title3"])
        val titleThree: String?,
        @SerializedName("title_three_text_size", alternate = ["title3_text_size"])
        val titleThreeTextSize: String?,
        @SerializedName("title_three_text_color", alternate = ["title3_text_color"])
        val titleThreeTextColor: String?,

        @SerializedName("title_four", alternate = ["title4"])
        val titleFour: String?,
        @SerializedName("title_four_text_size", alternate = ["title4_text_size"])
        val titleFourTextSize: String?,
        @SerializedName("title_four_text_color", alternate = ["title4_text_color"])
        val titleFourTextColor: String?,

        @SerializedName("image_url1")
        val imageUrl1: String?,
        @SerializedName("image_url2")
        val imageUrl2: String?,
        @SerializedName("image_url3")
        val imageUrl3: String?,

        @SerializedName("deeplink")
        val deeplink: String?,
        @SerializedName("deeplink1")
        val deeplink1: String?,

        ) : WidgetData()

}


