package com.doubtnutapp.leaderboard.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.applyBackgroundTint
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetPreviousWinnersBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.UserUtil
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class PreviousWinnersWidget @JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<PreviousWinnersWidget.WidgetHolder, PreviousWinnersWidget.Model,
        WidgetPreviousWinnersBinding>(context) {
    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetPreviousWinnersBinding {
        return WidgetPreviousWinnersBinding
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
        binding.root.applyBackgroundTint(model.data.strokeColor)

        binding.tvTitle.isVisible = model.data.titleOne.isNullOrEmpty().not()
        binding.tvTitle.text = model.data.titleOne
        binding.tvTitle.applyTextColor(model.data.titleOneTextColor)
        binding.tvTitle.applyTextSize(model.data.titleOneTextSize)

        binding.ivImage1.loadImage(model.data.imageUrl1)
        binding.ivImage2.loadImage(model.data.imageUrl2)
        binding.ivImage3.loadImage(model.data.imageUrl3)
        binding.ivImage4.isVisible = model.data.imageUrl4.isNullOrEmpty().not()
        binding.ivImage4.loadImage(model.data.imageUrl4)

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
                        putAll(model.data.extraParams.orEmpty())
                    }
                )
            )
        }

        return holder
    }

    class WidgetHolder(binding: WidgetPreviousWinnersBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetPreviousWinnersBinding>(binding, widget)

    companion object {
        const val TAG = "PreviousWinnersWidget"
        const val EVENT_TAG = "previous_winners_widget"
    }

    @Keep
    class Model :
        WidgetEntityModel<Data, WidgetAction>()


    @Keep
    data class Data(
        @SerializedName("title_one", alternate = ["title1"])
        val titleOne: String?,
        @SerializedName("title_one_text_size", alternate = ["title1_text_size"])
        val titleOneTextSize: String?,
        @SerializedName("title_one_text_color", alternate = ["title1_text_color"])
        val titleOneTextColor: String?,

        @SerializedName("image_url1")
        val imageUrl1: String?,
        @SerializedName("image_url2")
        val imageUrl2: String?,
        @SerializedName("image_url3")
        val imageUrl3: String?,
        @SerializedName("image_url4")
        val imageUrl4: String?,

        @SerializedName("stroke_color")
        val strokeColor: String?,
        @SerializedName("deeplink")
        val deeplink: String?,

        @SerializedName("extra_params")
        var extraParams: HashMap<String, Any>? = null,
    ) : WidgetData()

}


