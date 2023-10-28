package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetCourseEmiInfoBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImageEtx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseEmiInfoWidget(context: Context) : BaseBindingWidget<CourseEmiInfoWidget.WidgetHolder,
    CourseEmiInfoWidgetModel, WidgetCourseEmiInfoBinding>(context) {

    companion object {
        const val TAG = "CourseEmiInfoWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetCourseEmiInfoBinding {
        return WidgetCourseEmiInfoBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CourseEmiInfoWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data: CourseEmiInfoWidgetData = model.data
        val binding = holder.binding
        binding.textView.text = data.title.orEmpty()
        binding.imageView.loadImageEtx(data.icon.orEmpty())
        return holder
    }

    class WidgetHolder(binding: WidgetCourseEmiInfoBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseEmiInfoBinding>(binding, widget)
}

class CourseEmiInfoWidgetModel : WidgetEntityModel<CourseEmiInfoWidgetData, WidgetAction>()

@Keep
data class CourseEmiInfoWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("icon") val icon: String?
) : WidgetData()
