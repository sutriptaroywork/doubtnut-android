package com.doubtnutapp.freeclasses.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetTwoTextsHorizontalBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TwoTextsHorizontalWidget(context: Context) :
    BaseBindingWidget<TwoTextsHorizontalWidget.WidgetViewHolder,
            TwoTextsHorizontalWidget.Model, WidgetTwoTextsHorizontalBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "TwoTextsHorizontalWidget"
        const val EVENT_TAG = "two_texts_horizontal_widget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetTwoTextsHorizontalBinding {
        return WidgetTwoTextsHorizontalBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: Model
    ): WidgetViewHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.tvTitle1.isVisible = model.data.titleOne.isNullOrEmpty().not()
        binding.tvTitle1.text = model.data.titleOne
        binding.tvTitle1.applyTextSize(model.data.titleOneTextSize)
        binding.tvTitle1.applyTextColor(model.data.titleOneTextColor)

        binding.tvTitle2.isVisible = model.data.titleTwo.isNullOrEmpty().not()
        binding.tvTitle2.text = model.data.titleTwo
        binding.tvTitle2.applyTextSize(model.data.titleTwoTextSize)
        binding.tvTitle2.applyTextColor(model.data.titleTwoTextColor)

        return holder
    }

    class WidgetViewHolder(binding: WidgetTwoTextsHorizontalBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTwoTextsHorizontalBinding>(binding, widget)

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

        ) : WidgetData()
}
