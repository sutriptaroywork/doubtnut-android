package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetCourseRecommendationMessageBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseRecommendationMessageWidget(context: Context) :
    BaseBindingWidget<CourseRecommendationMessageWidget.WidgetHolder,
        CourseRecommendationMessageWidget.Model, WidgetCourseRecommendationMessageBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = null

    override fun getViewBinding(): WidgetCourseRecommendationMessageBinding {
        return WidgetCourseRecommendationMessageBinding
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
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(4, 4, 4, 4)
            }
        )

        val data = model.data

        val binding = holder.binding
        binding.tvTitle.text = data.title

        return holder
    }

    class WidgetHolder(
        binding: WidgetCourseRecommendationMessageBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetCourseRecommendationMessageBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?
    ) : WidgetData()
}
