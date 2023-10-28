package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetSgJoinNewgroupBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SgJoinNewGroupWidget(context: Context) : BaseBindingWidget<
        SgJoinNewGroupWidget.WidgetHolder,
        SgJoinNewGroupWidget.Model,
        WidgetSgJoinNewgroupBinding>(context) {

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE
            .daggerAppComponent
            .forceUnWrap()
            .inject(this)
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction
    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetSgJoinNewgroupBinding =
        WidgetSgJoinNewgroupBinding.inflate(LayoutInflater.from(context), this, true)

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        model.layoutConfig = WidgetLayoutConfig(
            marginTop = 0,
            marginBottom = 0,
        )
        super.bindWidget(holder, model)

        val data = model.data
        val binding = holder.binding

        with(binding) {
            tvTitle.text = data.title ?: binding.root.context.getString(R.string.sg_join_new_groups)
            setOnClickListener {
                deeplinkAction.performAction(binding.root.context, data.deeplink ?: "doubtnutapp://study_group?screen=public_groups")
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.SG_JOIN_GROUP_LIST_ITEM_CLICKED, ignoreSnowplow = true))
            }
        }

        return holder
    }

    class WidgetHolder(binding: WidgetSgJoinNewgroupBinding, baseWidget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetSgJoinNewgroupBinding>(binding, baseWidget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("deeplink") val deeplink: String?,
    ) : WidgetData()

}