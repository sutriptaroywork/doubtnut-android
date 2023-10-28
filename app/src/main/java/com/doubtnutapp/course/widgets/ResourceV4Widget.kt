package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetResourceV4Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ResourceV4Widget(context: Context) :
    BaseBindingWidget<ResourceV4Widget.WidgetHolder,
            ResourceV4WidgetModel, WidgetResourceV4Binding>(context) {

    companion object {
        const val TAG = "ResourceV4Widget"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetResourceV4Binding {
        return WidgetResourceV4Binding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: ResourceV4WidgetModel): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
        })
        val data: ResourceV4WidgetData = model.data
        val binding = holder.binding
        binding.tvTitle.text = data.title.orEmpty()
        binding.tvFaculty.text = data.faculty.orEmpty()
        binding.tvSubject.text = data.subjectTitle.orEmpty()
            binding.tvSubject.setTextColor(
            Utils.parseColor(
                data.subjectColor,
                Color.BLACK
            )
        )
        binding.cardContainer.setBackgroundColor(
            Utils.parseColor(
                data.bgColor.orDefaultValue("#ffffff"),
                Color.WHITE
            )
        )
        binding.tvBottomTitle.text = data.bottomTitle.orEmpty()
        binding.ivImage.loadImageEtx(data.facultyImage.orEmpty())
        binding.ivImage.setBackgroundColor(
            Utils.parseColor(
                data.facultyImageBgColor.orDefaultValue("#ffffff"),
                Color.WHITE
            )
        )

        binding.root.setOnClickListener {
            deeplinkAction.performAction(context, data.deeplink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    model.type + EventConstants.CLICKED,
                    hashMapOf<String, Any>(
                        Constants.TITLE to data.title.toString()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }
        return holder
    }

    class WidgetHolder(binding: WidgetResourceV4Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetResourceV4Binding>(binding, widget)

}

class ResourceV4WidgetModel : WidgetEntityModel<ResourceV4WidgetData, WidgetAction>()

@Keep
data class ResourceV4WidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("subject_title") val subjectTitle: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("subject_color") val subjectColor: String?,
    @SerializedName("faculty") val faculty: String?,
    @SerializedName("bottom_title") val bottomTitle: String?,
    @SerializedName("bg_color") val bgColor: String?,
    @SerializedName("faculty_image") val facultyImage: String?,
    @SerializedName("faculty_image_bg_color") val facultyImageBgColor: String?
) : WidgetData()
