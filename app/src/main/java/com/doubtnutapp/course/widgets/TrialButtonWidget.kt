package com.doubtnutapp.course.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.core.utils.applyBackgroundColor
import com.doubtnut.core.utils.applyStrike
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetTrialButtonBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TrialButtonWidget @JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<TrialButtonWidget.WidgetHolder, TrialButtonWidgetModel,
    WidgetTrialButtonBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetTrialButtonBinding {
        return WidgetTrialButtonBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: TrialButtonWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.cvMain.applyBackgroundColor(model.data.bgColor)

        binding.tvTextOne.text = model.data.textOne
        binding.tvTextTwo.text = model.data.textTwo
        binding.tvTextThree.text = model.data.textThree

        binding.tvTextOne.applyTextSize(model.data.textOneSize)
        binding.tvTextTwo.applyTextSize(model.data.textTwoSize)
        binding.tvTextThree.applyTextSize(model.data.textThreeSize)

        binding.tvTextOne.applyTextColor(model.data.textOneColor)
        binding.tvTextTwo.applyTextColor(model.data.textTwoColor)
        binding.tvTextThree.applyTextColor(model.data.textThreeColor)

        binding.tvTextOne.isVisible = model.data.textOne.isNullOrEmpty().not()
        binding.tvTextTwo.isVisible = model.data.textTwo.isNullOrEmpty().not()
        binding.tvTextThree.isVisible = model.data.textThree.isNullOrEmpty().not()

        binding.tvTextOne.applyStrike(model.data.textOneStrikeThrough)
        binding.tvTextTwo.applyStrike(model.data.textTwoStrikeThrough)
        binding.tvTextThree.applyStrike(model.data.textThreeStrikeThrough)

        binding.cvMain.setOnClickListener {
            deeplinkAction.performAction(context, model.data.deepLink)
        }
        return holder
    }

    class WidgetHolder(binding: WidgetTrialButtonBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTrialButtonBinding>(binding, widget)
}

@Keep
class TrialButtonWidgetModel :
    WidgetEntityModel<TrialButtonWidgetData, WidgetAction>()

@Keep
data class TrialButtonWidgetData(
    @SerializedName("bg_color")
    val bgColor: String?,
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

    @SerializedName("deeplink", alternate = ["deep_link"])
    val deepLink: String?
) : WidgetData()
