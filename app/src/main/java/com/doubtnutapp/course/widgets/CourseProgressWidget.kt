package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetCourseProgressBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.setVisibleState
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseProgressWidget(context: Context) : BaseBindingWidget<CourseProgressWidget.WidgetHolder,
    CourseProgressWidgetModel, WidgetCourseProgressBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        private const val TAG = "CourseProgressWidget"
    }

    override fun getViewBinding(): WidgetCourseProgressBinding {
        return WidgetCourseProgressBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: CourseProgressWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val data = model.data
        val binding = holder.binding
        binding.titleTv.setVisibleState(!data.title.isNullOrEmpty())
        binding.cardTitle.setVisibleState(!data.cardTitle.isNullOrEmpty())
        binding.cardSubtitle.setVisibleState(!data.subtitle.isNullOrEmpty())
        binding.titleTv.text = data.title.orEmpty()
        binding.cardTitle.text = data.cardTitle.orEmpty()
        binding.cardSubtitle.text = data.subtitle.orEmpty()
        binding.courseProgress.progress = data.myProgress ?: 0
        binding.othersProgress.progress = data.othersProgress ?: 0
        binding.myScoreTv.text = data.myProgress.toString() + "%"
        binding.othersScoreTv.text = data.othersProgress.toString() + "%"
        return holder
    }

    class WidgetHolder(binding: WidgetCourseProgressBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseProgressBinding>(binding, widget)
}

class CourseProgressWidgetModel : WidgetEntityModel<CourseProgressWidgetData, WidgetAction>()

@Keep
data class CourseProgressWidgetData(
    @SerializedName("title1") val title: String?,
    @SerializedName("title") val cardTitle: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("me") val myProgress: Int?,
    @SerializedName("others") val othersProgress: Int?
) : WidgetData()
