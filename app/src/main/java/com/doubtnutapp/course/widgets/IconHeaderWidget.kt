package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.databinding.WidgetIconHeaderBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.loadImageEtx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 28/09/20.
 */
class IconHeaderWidget(context: Context) : BaseBindingWidget<IconHeaderWidget.WidgetHolder,
    IconHeaderWidgetModel, WidgetIconHeaderBinding>(context) {

    companion object {
        const val TAG = "IconHeaderWidget"
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: IconHeaderWidgetModel): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val data: IconHeaderWidgetData = model.data
        holder.binding.textViewTitle.text = data.title.orEmpty()
        holder.binding.imageView.loadImageEtx(data.iconUrl.orEmpty())
        return holder
    }

    class WidgetHolder(binding: WidgetIconHeaderBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetIconHeaderBinding>(binding, widget)

    override fun getViewBinding(): WidgetIconHeaderBinding {
        return WidgetIconHeaderBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class IconHeaderWidgetModel : WidgetEntityModel<IconHeaderWidgetData, WidgetAction>()

@Keep
data class IconHeaderWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("icon_url") val iconUrl: String?
) : WidgetData()
