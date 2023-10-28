package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetResouceUpcomingBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ResourcePageUpcomingWidget(context: Context) :
    BaseBindingWidget<ResourcePageUpcomingWidget.WidgetHolder,
        ResourcePageUpcomingWidgetModel, WidgetResouceUpcomingBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetResouceUpcomingBinding {
        return WidgetResouceUpcomingBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ResourcePageUpcomingWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val data = model.data
        val binding = holder.binding

        binding.tvLectureName.text = data.title1.orEmpty()
        binding.tvSubject.text = data.subject.orEmpty()
        binding.tvTeacherName.text = data.title2.orEmpty()
        binding.ivTeacher.loadImage(data.imageUrl)
        return holder
    }

    class WidgetHolder(binding: WidgetResouceUpcomingBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetResouceUpcomingBinding>(binding, widget)
}

class ResourcePageUpcomingWidgetModel :
    WidgetEntityModel<ResourcePageUpcomingWidgetData, WidgetAction>()

@Keep
data class ResourcePageUpcomingWidgetData(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("title1") val title1: String?,
    @SerializedName("title2") val title2: String?
) : WidgetData()
