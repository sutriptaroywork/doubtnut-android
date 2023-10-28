package com.doubtnutapp.course.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.doubtnut.core.utils.*
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetBuyCompleteCourseBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class BuyCompleteCourseWidget @JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<BuyCompleteCourseWidget.WidgetHolder, BuyCompleteCourseWidgetModel,
    WidgetBuyCompleteCourseBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetBuyCompleteCourseBinding {
        return WidgetBuyCompleteCourseBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: BuyCompleteCourseWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        if (!model.data.bgStrokeColor.isNullOrEmpty()) {
            binding.cvMain.applyStrokeColor(model.data.bgStrokeColor.orEmpty())
        } else {
            binding.cvMain.strokeWidth = 0
            binding.cvMain.background =
                Utils.getShape(model.data.bgColor.orEmpty(), model.data.bgColor.orEmpty())
        }
        binding.tvTextOne.text = model.data.textOne
        binding.tvTextTwo.text = model.data.textTwo
        binding.tvTextThree.text = model.data.textThree
        binding.tvTextFour.text = model.data.textFour
        if (!model.data.textFive.isNullOrEmpty()) {
            binding.tvTextFive.show()
            binding.tvTextFive.text = model.data.textFive
        } else {
            binding.tvTextFive.hide()
        }
        binding.tvTextOne.applyTextSize(model.data.textOneSize)
        binding.tvTextTwo.applyTextSize(model.data.textTwoSize)
        binding.tvTextThree.applyTextSize(model.data.textThreeSize)
        binding.tvTextFour.applyTextSize(model.data.textFourSize)
        binding.tvTextFive.applyTextSize(model.data.textFiveSize)

        binding.tvTextOne.applyTextColor(model.data.textOneColor)
        binding.tvTextTwo.applyTextColor(model.data.textTwoColor)
        binding.tvTextThree.applyTextColor(model.data.textThreeColor)
        binding.tvTextFour.applyTextColor(model.data.textFourColor)
        binding.tvTextFive.applyTextColor(model.data.textFiveColor)

        binding.tvTextOne.isVisible = model.data.textOne.isNullOrEmpty().not()
        binding.tvTextTwo.isVisible = model.data.textTwo.isNullOrEmpty().not()
        binding.tvTextThree.isVisible = model.data.textThree.isNullOrEmpty().not()
        binding.tvTextFour.isVisible = model.data.textFour.isNullOrEmpty().not()
        binding.tvTextFive.isVisible = model.data.textFive.isNullOrEmpty().not()

        binding.tvTextOne.applyStrike(model.data.textOneStrikeThrough)
        binding.tvTextTwo.applyStrike(model.data.textTwoStrikeThrough)
        binding.tvTextThree.applyStrike(model.data.textThreeStrikeThrough)
        binding.tvTextFour.applyStrike(model.data.textFourStrikeThrough)
        binding.tvTextFive.applyStrike(model.data.textFiveStrikeThrough)

        val radiusArray = floatArrayOf(
            0f,
            0f,
            8f,
            8f,
            8f,
            8f,
            0f,
            0f,
        )
        binding.viewBgEnd.background = Utils.getShape(
            model.data.bgEndColor.orEmpty(),
            model.data.bgEndColor.orEmpty(),
            radiusArray = radiusArray
        )
        binding.viewBgEnd.isVisible =
            model.data.textFour.isNullOrEmpty().not() or model.data.textFive.isNullOrEmpty().not()
        binding.cvMain.setOnClickListener {
            deeplinkAction.performAction(context, model.data.deepLink)
        }
        if (model.data.isDrawable == true) {
            binding.clFirst.background =
                ContextCompat.getDrawable(context, R.drawable.bg_buy_now_button)
        }
        return holder
    }

    class WidgetHolder(binding: WidgetBuyCompleteCourseBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetBuyCompleteCourseBinding>(binding, widget)
}

@Keep
class BuyCompleteCourseWidgetModel :
    WidgetEntityModel<BuyCompleteCourseWidgetData, WidgetAction>()

@Keep
data class BuyCompleteCourseWidgetData(
    @SerializedName("bg_stroke_color")
    val bgStrokeColor: String?,

    @SerializedName("text_one")
    val textOne: String?,
    @SerializedName("text_one_size")
    val textOneSize: String?,
    @SerializedName("text_one_color")
    val textOneColor: String?,
    @SerializedName("text_one_strike_through")
    val textOneStrikeThrough: Boolean?,

    @SerializedName("text_two")
    val textTwo: String?,
    @SerializedName("text_two_size")
    val textTwoSize: String?,
    @SerializedName("text_two_color")
    val textTwoColor: String?,
    @SerializedName("text_two_strike_through")
    val textTwoStrikeThrough: Boolean?,

    @SerializedName("text_three")
    val textThree: String?,
    @SerializedName("text_three_size")
    val textThreeSize: String?,
    @SerializedName("text_three_color")
    val textThreeColor: String?,
    @SerializedName("text_three_strike_through")
    val textThreeStrikeThrough: Boolean?,

    @SerializedName("text_four")
    val textFour: String?,
    @SerializedName("text_four_size")
    val textFourSize: String?,
    @SerializedName("text_four_color")
    val textFourColor: String?,
    @SerializedName("text_four_strike_through")
    val textFourStrikeThrough: Boolean?,

    @SerializedName("text_five")
    val textFive: String?,
    @SerializedName("text_five_size")
    val textFiveSize: String?,
    @SerializedName("text_five_color")
    val textFiveColor: String?,
    @SerializedName("text_five_strike_through")
    val textFiveStrikeThrough: Boolean?,

    @SerializedName("bg_end_color")
    val bgEndColor: String?,

    @SerializedName("deeplink", alternate = ["deep_link"])
    val deepLink: String?,
    @SerializedName("bg_color")
    val bgColor: String?,
    @SerializedName("is_drawable")
    val isDrawable: Boolean?,
) : WidgetData()
