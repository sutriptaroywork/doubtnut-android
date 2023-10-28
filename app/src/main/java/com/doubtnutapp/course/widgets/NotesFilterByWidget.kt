package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.NotesFilterBySelectAction
import com.doubtnutapp.data.remote.models.CourseFilterTypeData
import com.doubtnutapp.databinding.WidgetNotesFilterByBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.libraryhome.coursev3.ui.CourseFilterDropDownAdapter
import com.doubtnutapp.libraryhome.coursev3.ui.CourseFilterDropDownMenu
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class NotesFilterByWidget(context: Context) :
    BaseBindingWidget<NotesFilterByWidget.WidgetHolder,
        NotesFilterByWidgetModel, WidgetNotesFilterByBinding>(context) {

    companion object {
        const val TAG = "NotesFilterByWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: NotesFilterByWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data: NotesFilterByWidgetData = model.data
        val binding = holder.binding
        binding.tvFilterBy.text = data.title.orEmpty()
        binding.tvFilterByItem.text = data.subTitle.orEmpty()
        binding.tvFilterByItem.setOnClickListener {
            val menu = CourseFilterDropDownMenu(context, data.items.orEmpty())
            menu.height = WindowManager.LayoutParams.WRAP_CONTENT
            menu.width = Utils.convertDpToPixel(200f).toInt()
            menu.isOutsideTouchable = true
            menu.isFocusable = true
            menu.showAsDropDown(binding.tvFilterByItem)
            menu.setCategorySelectedListener(object : CourseFilterDropDownAdapter.FilterSelectedListener {
                override fun onFilterSelected(position: Int, data: CourseFilterTypeData) {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.COURSE_NOTES_FILTER_SELECT,
                            hashMapOf<String, Any>(
                                EventConstants.DISPLAY_NAME to data.display
                            ).apply {
                                putAll(model.extraParams ?: hashMapOf())
                            }
                        )
                    )
                    menu.dismiss()
                    binding.tvFilterByItem.text = data.display
                    actionPerformer?.performAction(NotesFilterBySelectAction(data))
                }
            })
        }
        return holder
    }

    class WidgetHolder(
        binding: WidgetNotesFilterByBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetNotesFilterByBinding>(binding, widget)

    override fun getViewBinding(): WidgetNotesFilterByBinding {
        return WidgetNotesFilterByBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class NotesFilterByWidgetModel : WidgetEntityModel<NotesFilterByWidgetData, WidgetAction>()

@Keep
data class NotesFilterByWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("sub_title") val subTitle: String?,
    @SerializedName("items") val items: List<CourseFilterTypeData>?,
) : WidgetData()
