package com.doubtnutapp.course.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.content.res.ResourcesCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetCourseTimetableBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseTimeTableWidget constructor(
    context: Context,
) : BaseBindingWidget<CourseTimeTableWidget.WidgetHolder,
    CourseTimetableWidgetModel,
    WidgetCourseTimetableBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetCourseTimetableBinding {
        return WidgetCourseTimetableBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: CourseTimetableWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.tableLayout.removeAllViews()
        model.data.timetableList?.forEach { it ->
            val row = TableRow(context)
            it.cellList?.forEach { cellData ->
                val view = View.inflate(context, R.layout.item_time_table_row, null)
                val tvSubject = view.findViewById<TextView>(R.id.tvSubject)
                tvSubject.text = cellData.text
                if (cellData.isBold == true) {
                    tvSubject.setTypeface(
                        ResourcesCompat.getFont(context, R.font.lato_bold),
                        Typeface.BOLD
                    )
                }
                tvSubject.setTextColor(Utils.parseColor(cellData.textColor))
                view.setOnClickListener {
                    val url = cellData.url
                    if (url.isNullOrBlank()) return@setOnClickListener
                    val uri: Uri = Uri.parse(url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    holder.itemView.context.startActivity(intent)

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.REMINDER_CARD_CLICK,
                            hashMapOf(
                                EventConstants.WIDGET to "CourseTimeTableWidget"
                            ),
                            ignoreSnowplow = true
                        )
                    )
                }
                row.addView(view)
            }
            binding.tableLayout.addView(row)
        }
        return holder
    }

    class WidgetHolder(binding: WidgetCourseTimetableBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseTimetableBinding>(binding, widget)
}

@Keep
class CourseTimetableWidgetModel :
    WidgetEntityModel<CourseTimeTableData, WidgetAction>()

@Keep
data class CourseTimeTableData(
    @SerializedName("timetable_data")
    val timetableList: List<TimetableRowData>?,
) : WidgetData()

@Keep
data class TimetableRowData(
    @SerializedName("cell_list")
    val cellList: List<TimetableCellData>?
)

@Keep
data class TimetableCellData(
    @SerializedName("text")
    val text: String?,
    @SerializedName("text_color")
    val textColor: String?,
    @SerializedName("is_bold")
    val isBold: Boolean?,
    @SerializedName("reminder_link")
    val url: String?
)
