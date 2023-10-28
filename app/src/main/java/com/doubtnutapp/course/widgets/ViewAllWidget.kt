package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetViewAllBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ViewAllWidget(context: Context) : BaseBindingWidget<ViewAllWidget.WidgetHolder,
    ViewAllWidget.ViewAllWigetModel, WidgetViewAllBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "ViewAllWidget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetViewAllBinding {
        return WidgetViewAllBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: ViewAllWigetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        Utils.setWidthBasedOnPercentage(
            holder.itemView.context, holder.itemView,
            model.data.cardWidth
                ?: "2.8",
            R.dimen.spacing_5
        )

        binding.tvViewAll.text = model.data.title.orEmpty()
        binding.parentLayout.setOnClickListener {
            deeplinkAction.performAction(context, model.data.deeplink)
        }
        return holder
    }

    class WidgetHolder(binding: WidgetViewAllBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetViewAllBinding>(binding, widget)

    class ViewAllWigetModel : WidgetEntityModel<TestWidgetData, WidgetAction>()

    @Keep
    data class TestWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("card_width") val cardWidth: String?
    ) : WidgetData()
}
