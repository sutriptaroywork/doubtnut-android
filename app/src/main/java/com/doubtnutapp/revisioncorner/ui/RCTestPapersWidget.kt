package com.doubtnutapp.revisioncorner.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemRcTestBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 21/4/22.
 */

class RCTestPapersWidget(context: Context) :
    BaseBindingWidget<RCTestPapersWidget.WidgetHolder, RCTestPapersWidget.Model, ItemRcTestBinding>(
        context
    ) {

    companion object {
        private const val TAG = "RCTestPapersWidget"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
        })
        val data: Data = model.data
        val binding = holder.binding

        binding.apply {
            tvTitle1.text = data.heading
            tvTitle2.text = data.subheading
            tvTitle2.setTextColor(Color.parseColor(data.subheadingColor.orEmpty()))
            setOnClickListener { deeplinkAction.performAction(context, data.deeplink) }
        }
        return holder
    }


    class WidgetHolder(
        binding: ItemRcTestBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<ItemRcTestBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("heading") val heading: String,
        @SerializedName("subheading") val subheading: String,
        @SerializedName("subheading_color") val subheadingColor: String?,
        @SerializedName("deeplink") val deeplink: String,
    ) : WidgetData()

    override fun getViewBinding() =
        ItemRcTestBinding.inflate(LayoutInflater.from(context), this, true)

}
