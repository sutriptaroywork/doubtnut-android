package com.doubtnutapp.course.widgets

/**
Created by Sachin Saxena on 04/02/22.
 */

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetSubjectCourseCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SubjectCourseCardWidget(context: Context) :
    BaseBindingWidget<SubjectCourseCardWidget.WidgetHolder,
            SubjectCourseCardWidget.Model, WidgetSubjectCourseCardBinding>(context) {

    companion object {
        const val TAG = "SubjectCourseCardWidget"
        const val EVENT_TAG = "subject_course_widget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun getViewBinding(): WidgetSubjectCourseCardBinding {
        return WidgetSubjectCourseCardBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(16, 2, 4, 4)
        })
        val data = model.data
        val binding = holder.binding

        binding.cardContainer.setBackgroundColor(
            Utils.parseColor(
                data.cardBgColor.orDefaultValue("#4ca4e3"),
                Color.BLUE
            )
        )
        binding.imageViewFaculty.loadImageEtx(data.facultyImageUrl.orEmpty())

        binding.tvMediumInfo.isVisible = !data.mediumText.isNullOrBlank()
        binding.tvMediumInfo.applyTextColor(data.mediumTextColor)
        binding.tvMediumInfo.background = Utils.getShape(
            data.mediumTextBgColor.orDefaultValue("#1e6ba3"),
            data.mediumTextBgColor.orDefaultValue("#1e6ba3")
        )
        binding.tvMediumInfo.text = data.mediumText.orEmpty()
        binding.textViewTitleInfo.text = data.title.orEmpty()
        binding.textViewSubject.text = data.subject.orEmpty()
        binding.tvPrice.text = data.price.orEmpty()

        binding.tvBuyNow.text = data.buttonText.orEmpty()
        binding.tvBuyNow.setTextColor(Utils.parseColor(data.buttonTextColor.orDefaultValue("#eb532c")))
        binding.tvBuyNow.background = Utils.getShape(
            data.buttonColor.orDefaultValue("#ffffff"),
            data.buttonTextColor.orDefaultValue("#eb532c")
        )

        binding.root.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = "${EVENT_TAG}_${EventConstants.WIDGET_ITEM_CLICK}",
                    params = hashMapOf<String, Any>().apply {
                        EventConstants.WIDGET to TAG
                    }.apply {
                        putAll(model.extraParams.orEmpty())
                    }, ignoreBranch = false
                )
            )
            MoEngageUtils.setUserAttribute(context, "dn_bnb_clicked",true)

            deeplinkAction.performAction(context, data.deeplink)
        }

        return holder
    }

    class WidgetHolder(binding: WidgetSubjectCourseCardBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetSubjectCourseCardBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("medium_text") val mediumText: String?,
        @SerializedName("medium_text_color") val mediumTextColor: String?,
        @SerializedName("medium_text_bg_color") val mediumTextBgColor: String?,
        @SerializedName("card_bg_color") val cardBgColor: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("subject") val subject: String?,
        @SerializedName("price") val price: String?,
        @SerializedName("button_text") val buttonText: String?,
        @SerializedName("button_color") val buttonColor: String?,
        @SerializedName("button_text_color") val buttonTextColor: String?,
        @SerializedName("faculty_image_url") val facultyImageUrl: String?,
        @SerializedName("deeplink") val deeplink: String?
    ) : WidgetData()

}