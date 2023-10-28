package com.doubtnutapp.widgettest.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.databinding.WidgetDummyBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName

/**
 * Created by Mehul Bisht on 18/11/21
 */

class DummyWidget
constructor(
    context: Context
) : BaseBindingWidget<DummyWidget.WidgetHolder, DummyWidgetModel, WidgetDummyBinding>(
    context
) {

    override fun getViewBinding(): WidgetDummyBinding {
        return WidgetDummyBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.forceUnWrap()?.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: DummyWidgetModel): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(8,8,16,16)
        })
        val data = model.data

        holder.binding.title.apply {
            text = data.title
            setTextColor(Utils.parseColor(data.titleColor))
        }

        holder.binding.background.setBackgroundColor(Utils.parseColor(data.backgroundColor))

        return holder
    }

    class WidgetHolder(
        binding: WidgetDummyBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetDummyBinding>(binding, widget)
}

@Keep
class DummyWidgetModel :
    WidgetEntityModel<DummyWidgetData, WidgetAction>()

@Keep
data class DummyWidgetData(
    @SerializedName("title") val title: String,
    @SerializedName("title_color") val titleColor: String,
    @SerializedName("background_color") val backgroundColor: String
) : WidgetData()