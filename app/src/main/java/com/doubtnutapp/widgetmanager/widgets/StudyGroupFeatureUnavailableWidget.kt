package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetStudyGroupFeatureUnavailableBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 26/05/21.
 */

class StudyGroupFeatureUnavailableWidget(context: Context) :
    BaseBindingWidget<StudyGroupFeatureUnavailableWidget.WidgetHolder,
            StudyGroupFeatureUnavailableWidget.Model, WidgetStudyGroupFeatureUnavailableBinding>(
        context
    ) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetStudyGroupFeatureUnavailableBinding {
        return WidgetStudyGroupFeatureUnavailableBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        model.layoutConfig = WidgetLayoutConfig(
            marginTop = 5,
            marginBottom = 5,
        )
        super.bindWidget(holder, model)
        val data = model.data
        val binding = holder.binding
        binding.apply {
            val widgetDisplayName =
                (model.extraParams?.get(Constants.WIDGET_DISPLAY_NAME) as? String).orEmpty()
            textView.text = data.message
                ?: context.getString(
                    R.string.study_group_feature_unavailable_message,
                    widgetDisplayName
                )
            tvCta.text = data.ctaText ?: context.getString(R.string.update_now)
            tvCta.setOnClickListener {
                Utils.openPlayStore(context)
            }
        }
        return holder
    }

    class WidgetHolder(
        binding: WidgetStudyGroupFeatureUnavailableBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetStudyGroupFeatureUnavailableBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>(
        _type = WidgetTypes.TYPE_WIDGET_STUDY_GROUP_FEATURE_UNAVAILABLE,
        _data = Data()
    )

    @Keep
    data class Data(
        @SerializedName("unavailable_message") val message: String? = null,
        @SerializedName("cta_text") val ctaText: String? = null,
        @SerializedName("deeplink") val deeplink: String? = null,
    ) : WidgetData()

}