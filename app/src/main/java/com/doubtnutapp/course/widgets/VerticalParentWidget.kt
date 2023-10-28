package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetVerticalParentBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class VerticalParentWidget(context: Context) :
    BaseBindingWidget<VerticalParentWidget.WidgetViewHolder,
            VerticalParentWidget.VerticalParentWidgetModel, WidgetVerticalParentBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        private const val TAG = "VerticalParentWidget"
        const val EVENT_TAG = "vertical_parent_widget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetVerticalParentBinding {
        return WidgetVerticalParentBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: VerticalParentWidgetModel
    ): WidgetViewHolder {
        super.bindWidget(
            holder,
            model.apply { layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0) })
        val data = model.data
        val binding = holder.binding

        with(binding) {
            tvTitle.text = data.title.orEmpty()
            tvTitle.setVisibleState(data.title.isNotNullAndNotEmpty())
            rvWidgets.adapter = WidgetLayoutAdapter(context, actionPerformer, source)
            (rvWidgets.adapter as WidgetLayoutAdapter).setWidgets(data.items.orEmpty())
        }

        return holder
    }

    class WidgetViewHolder(binding: WidgetVerticalParentBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetVerticalParentBinding>(binding, widget)

    class VerticalParentWidgetModel : WidgetEntityModel<VerticalParentWidgetData, WidgetAction>()

    @Keep
    data class VerticalParentWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("items") val items: List<WidgetEntityModel<*, *>>?,
    ) : WidgetData()

}
