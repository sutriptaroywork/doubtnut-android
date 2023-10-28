package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.databinding.WidgetPlanDescriptionBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 28/09/20.
 */
class PlanDescriptionInfoWidget(context: Context) :
    BaseBindingWidget<PlanDescriptionInfoWidget.WidgetHolder,
        PlanDescriptionInfoWidgetModel, WidgetPlanDescriptionBinding>(context) {

    companion object {
        const val TAG = "PlanDescriptionInfoWidget"
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: PlanDescriptionInfoWidgetModel): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val data: PlanDescriptionInfoWidgetData = model.data
        widgetViewHolder.binding.textViewTitle.text = data.title.orEmpty()
        widgetViewHolder.binding.textViewDescription.text = data.description.orEmpty()
        return holder
    }

    class WidgetHolder(
        binding: WidgetPlanDescriptionBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetPlanDescriptionBinding>(binding, widget)

    override fun getViewBinding(): WidgetPlanDescriptionBinding {
        return WidgetPlanDescriptionBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class PlanDescriptionInfoWidgetModel : WidgetEntityModel<PlanDescriptionInfoWidgetData, WidgetAction>()

@Keep
data class PlanDescriptionInfoWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?
) : WidgetData()
