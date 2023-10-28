package com.doubtnutapp.mediumSwitch

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetMediumSwitchBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.libraryhome.coursev3.ui.CourseActivityV3
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class MediumSwitchWidget(context: Context) : BaseBindingWidget<MediumSwitchWidget.WidgetHolder,
        MediumSwitchWidgetModel, WidgetMediumSwitchBinding>(context) {

    companion object {
        const val TAG = "MediumSwitchWidget"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: MediumSwitchWidgetModel): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
        })
        val data: MediumSwitchWidgetData = model.data
        val binding = holder.binding
        binding.tvTitle.text = data.title.orEmpty()
        binding.tvCtaText.text = data.buttonCtaText.orEmpty()
        binding.tvCtaText.setOnClickListener {
            if (deeplinkAction.performAction(context, data.deeplink)) {
                (widgetViewHolder.itemView.context as? AppCompatActivity)?.let { activity ->
                    if (activity is CourseActivityV3) {
                        activity.finish()
                    }
                }
            }
        }

        binding.parentView.isVisible = data.isHidden != true
        binding.ivClose.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.WIDGET_MEDIUM_SWITCH_CLOSE))
            data.isHidden = true
            binding.parentView.isVisible = false
        }
        return holder
    }

    class WidgetHolder(
        binding: WidgetMediumSwitchBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetMediumSwitchBinding>(binding, widget)

    override fun getViewBinding(): WidgetMediumSwitchBinding {
        return WidgetMediumSwitchBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class MediumSwitchWidgetModel : WidgetEntityModel<MediumSwitchWidgetData, WidgetAction>()

@Keep
data class MediumSwitchWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("button_cta_text") val buttonCtaText: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @Transient var isHidden: Boolean? = null
) : WidgetData()
