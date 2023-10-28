package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.Dismiss
import com.doubtnutapp.databinding.WidgetSampleQuestionBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SampleQuestionWidget(context: Context) :
    BaseBindingWidget<SampleQuestionWidget.WidgetHolder,
            SampleQuestionWidget.Model, WidgetSampleQuestionBinding>(context) {

    @Inject
    lateinit var deepLinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = null

    override fun getViewBinding(): WidgetSampleQuestionBinding {
        return WidgetSampleQuestionBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(2, 2, 4, 4)
            }
        )
        val data = model.data
        val binding = holder.binding

        binding.apply {
            title.apply {
                text = data.title
                isVisible = data.title.isNotNullAndNotEmpty()
            }
            tryButton.setOnClickListener {
                tryQuestion(context, data.deepLink, data.imageUrl)
            }
            sampleQuestionImage.loadImageEtx(data.imageUrl)
            sampleQuestionLayout.setOnClickListener {
                tryQuestion(context, data.deepLink, data.imageUrl)
            }
            if (data.showOrView == true) {
                viewOrLeft.show()
                tvOr.show()
                viewOrRight.show()
            } else {
                viewOrLeft.hide()
                tvOr.hide()
                viewOrRight.hide()
            }
            tryButton.apply {
                text = data.cta
                isVisible = data.cta.isNotNullAndNotEmpty()
            }
        }

        return holder
    }

    private fun tryQuestion(context: Context, deeplink: String, imageUrl: String) {
        deepLinkAction.performAction(context, deeplink)
        actionPerformer?.performAction(Dismiss())
    }

    class WidgetHolder(binding: WidgetSampleQuestionBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetSampleQuestionBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("image_url") val imageUrl: String,
        @SerializedName("show_or_view") val showOrView: Boolean?,
        @SerializedName("deeplink") val deepLink: String,
        @SerializedName("cta") val cta: String?
    ) : WidgetData()
}
