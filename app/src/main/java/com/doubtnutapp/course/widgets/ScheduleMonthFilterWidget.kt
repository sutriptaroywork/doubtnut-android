package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.remote.models.CourseFilterTypeData
import com.doubtnutapp.databinding.WidgetScheduleMonthFilterBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ScheduleMonthFilterWidget(context: Context) :
    BaseBindingWidget<ScheduleMonthFilterWidget.WidgetHolder,
        ScheduleMonthFilterWidgetModel, WidgetScheduleMonthFilterBinding>(context) {

    companion object {
        const val TAG = "ScheduleMonthFilterWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher
    var source: String? = ""

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetScheduleMonthFilterBinding {
        return WidgetScheduleMonthFilterBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ScheduleMonthFilterWidgetModel
    ): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(8, 8, 0, 0)
            }
        )
        val data: ScheduleMonthFilterWidgetData = model.data
        val bindng = holder.binding

        bindng.textViewTitle.text = data.title.orEmpty()
        bindng.textViewMonth.text = data.subTitle.orEmpty()
        // uncomment to use dropdown
//        widgetViewHolder.itemView.textViewMonth.setOnClickListener {
//            val menu = CourseFilterDropDownMenu(context, data.items.orEmpty())
//            menu.height = WindowManager.LayoutParams.WRAP_CONTENT
//            menu.width = Utils.convertDpToPixel(200f).toInt()
//            menu.isOutsideTouchable = true
//            menu.isFocusable = true
//            menu.showAsDropDown(widgetViewHolder.itemView.textViewMonth)
//            menu.setCategorySelectedListener(object : CourseFilterDropDownAdapter.FilterSelectedListener {
//                override fun onFilterSelected(position: Int, data: CourseFilterTypeData) {
//                    menu.dismiss()
//                    actionPerformer?.performAction(ScheduleMonthFilterSelectAction(data))
//                }
//            })
//        }
        return holder
    }

    class WidgetHolder(binding: WidgetScheduleMonthFilterBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetScheduleMonthFilterBinding>(binding, widget)
}

class ScheduleMonthFilterWidgetModel :
    WidgetEntityModel<ScheduleMonthFilterWidgetData, WidgetAction>()

@Keep
data class ScheduleMonthFilterWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("sub_title") var subTitle: String?,
    @SerializedName("items") val items: List<CourseFilterTypeData>?
) : WidgetData()
