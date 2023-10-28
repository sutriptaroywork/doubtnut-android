package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.*
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TestResultWidget(context: Context) : BaseBindingWidget<TestResultWidget.WidgetViewHolder,
    TestResultWidget.TestResultWidgetModel, WidgetTestResultBinding>(context) {

    companion object {
        const val TAG = "TestResultWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getView(): View {
        return View.inflate(context, R.layout.widget_test_result, this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetTestResultBinding {
        return WidgetTestResultBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(holder: WidgetViewHolder, model: TestResultWidgetModel): WidgetViewHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data = model.data
        binding.tvDescription.text = data.description.orEmpty()
        binding.tvAccuracy.text = data.accuracyText.orEmpty()
        binding.tvAccuracyText.text = data.accuracyTitle.orEmpty()
        binding.tvSpeed.text = data.speedText.orEmpty()
        binding.tvSpeedText.text = data.speedTitle.orEmpty()
        if (!data.deeplink.isNullOrEmpty()) {
            binding.btnLearning.show()
            binding.btnLearning.text = data.btnText.orEmpty()
            binding.btnLearning.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.TEST_ANALYSIS_BUTTON_CLICK,
                        hashMapOf<String, Any>().apply {
                            putAll(model.extraParams ?: hashMapOf())
                        },
                        ignoreSnowplow = true
                    )
                )
                deeplinkAction.performAction(holder.itemView.context, data.deeplink.orEmpty())
            }
        } else {
            binding.btnLearning.hide()
        }
        binding.accuracyProgress.progress = data.accuracy?.toInt() ?: 0
        binding.tvSpeedDescription.text = data.speedDescription.orEmpty()
        return holder
    }

    class WidgetViewHolder(binding: WidgetTestResultBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTestResultBinding>(binding, widget)

    class TestResultWidgetModel : WidgetEntityModel<TestResultWidgetData, WidgetAction>()
}

@Keep
data class TestResultWidgetData(
    @SerializedName("description") val description: String?,
    @SerializedName("accuracy_text") val accuracyText: String?,
    @SerializedName("accuracy") val accuracy: String?,
    @SerializedName("accuracy_title") val accuracyTitle: String?,
    @SerializedName("accuracy_graph_color") val accuracyGraphColor: String?,
    @SerializedName("speed_text") val speedText: String?,
    @SerializedName("speed_title") val speedTitle: String?,
    @SerializedName("speed_graph_color") val speedGraphColor: String?,
    @SerializedName("speed_description") val speedDescription: String?,
    @SerializedName("btn_text") val btnText: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("is_above_average") val isAboveAverage: Boolean?
) : WidgetData()
