package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetTestBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.ui.mockTest.MockTestSubscriptionActivity
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TestWidget(context: Context) : BaseBindingWidget<TestWidget.WidgetHolder,
    TestWidget.TestWidgetModel, WidgetTestBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "TestWidget"
        const val FUTURE = 0
        const val LIVE = 1
        const val PAST = 2
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetTestBinding {
        return WidgetTestBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: TestWidgetModel): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val binding = holder.binding

        Utils.setWidthBasedOnPercentage(
            holder.itemView.context, holder.itemView,
            model.data.cardWidth ?: "1.1",
            R.dimen.spacing_5
        )

        binding.cardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            dimensionRatio = model.data.cardRatio ?: "16:9"
        }

        requestLayout()

        val data: TestWidgetData = model.data
        binding.textViewTitle.text = data.title.orEmpty()
        binding.textViewTitle.setTextColor(Utils.parseColor(data.color, Color.BLACK))
        binding.textViewTitleInfo.text = data.title2.orEmpty()
        binding.textViewDate.text = data.date.orEmpty()
        binding.textViewDate.isVisible = data.date.isNullOrBlank().not()
        binding.tvDuration.text = data.duration.orEmpty()
        binding.tvDuration.isVisible = data.duration.isNullOrBlank().not()
        binding.textViewBottom.text = data.bottomTitle.orEmpty()

        holder.itemView.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.LC_COURSE_CAROUSAL_ITEM_CLICK,
                    hashMapOf<String, Any>().apply {
                        put(
                            EventConstants.CLICKED_ITEM_ID,
                            data.id?.toIntOrNull()
                                ?: 0
                        )
                        putAll(model.extraParams ?: hashMapOf())
                    }
                )
            )
            if (data.isPremium == true && data.isVip != true) {
                deeplinkAction.performAction(context, data.paymentDeeplink)
            } else {
                context.startActivity(
                    MockTestSubscriptionActivity.getStartIntent(
                        holder.itemView.context,
                        data.id?.toIntOrNull() ?: 0, false
                    )
                )
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetTestBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTestBinding>(binding, widget)

    class TestWidgetModel : WidgetEntityModel<TestWidgetData, WidgetAction>()

    @Keep
    data class TestWidgetData(
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("title2") val title2: String?,
        @SerializedName("date") val date: String?,
        @SerializedName("duration") val duration: String?,
        @SerializedName("bottom_title") val bottomTitle: String?,
        @SerializedName("is_premium") val isPremium: Boolean?,
        @SerializedName("is_vip") val isVip: Boolean?,
        @SerializedName("payment_deeplink") val paymentDeeplink: String?,
        @SerializedName("color") val color: String?,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("card_ratio") val cardRatio: String?,
    ) : WidgetData()
}
