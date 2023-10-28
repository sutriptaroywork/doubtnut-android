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
import androidx.core.content.ContextCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.applyBackgroundColor
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetCourseTimetableV2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * This widget uses 2 separate tables for showing time table,
 * such that the first column(table-1) is fixed and the other columns(table-2) are scrollable */

class CourseTimeTableWidgetV2 constructor(
    context: Context,
) : BaseBindingWidget<CourseTimeTableWidgetV2.WidgetHolder,
    CourseTimetableWidgetV2Model,
    WidgetCourseTimetableV2Binding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetCourseTimetableV2Binding {
        return WidgetCourseTimetableV2Binding
            .inflate(LayoutInflater.from(context), this, true)
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    private var i0 = 1
    private var i1 = 1

    override fun bindWidget(
        holder: WidgetHolder,
        model: CourseTimetableWidgetV2Model
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.tableTimeDays.removeAllViews()
        binding.tableSubjectsTime.removeAllViews()

        binding.title.text = model.data.title
        binding.title.applyTextSize(model.data.titleSize)
        binding.title.applyTextColor(model.data.titleColor)
        binding.title.setTypeface(
            binding.title.typeface,
            Typeface.BOLD
        )

        if (model.data.isExpanded == true) {
            binding.tableTimeDays.visibility = VISIBLE
            binding.tableSubjectsTime.visibility = VISIBLE
            binding.ivExpand.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_expand_less
                )
            )
        } else {
            binding.tableTimeDays.visibility = GONE
            binding.tableSubjectsTime.visibility = GONE
            binding.ivExpand.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_expand_more
                )
            )
        }

        binding.viewHeaderClickHandler.setOnClickListener {
            if (model.data.isExpanded == true) {
                binding.ivExpand.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_expand_more
                    )
                )
                binding.tableTimeDays.visibility = GONE
                binding.tableSubjectsTime.visibility = GONE
                model.data.isExpanded = false
            } else {
                binding.ivExpand.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_expand_less
                    )
                )
                binding.tableTimeDays.visibility = VISIBLE
                binding.tableSubjectsTime.visibility = VISIBLE
                model.data.isExpanded = true
            }
        }

        model.data.timetableList?.forEach { it ->
            val rowTimeDays = TableRow(context)
            val rowSubjectsTime = TableRow(context)
            it.cellList?.forEach { cellData ->
                val view = View.inflate(context, R.layout.item_time_table_v2_row, null)
                val tvSubject = view.findViewById<TextView>(R.id.tvSubject)
                tvSubject.text = cellData.text
                if (cellData.isBold == true) {
                    tvSubject.setTypeface(
                        null,
                        Typeface.BOLD
                    )
                }
                tvSubject.setTextColor(Utils.parseColor(cellData.textColor))
                tvSubject.applyBackgroundColor("#f6f6f6")
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
                                EventConstants.WIDGET to "CourseTimeTableWidgetV2"
                            )
                        )
                    )
                }
                if (i1 == 1 && i0 >= i1) {
                    rowTimeDays.addView(view)
                } else {
                    rowSubjectsTime.addView(view)
                }

                if (i1 == it.cellList.size) {
                    i1 = 1
                    i0++
                } else {
                    i1++
                }
            }
            binding.tableTimeDays.addView(rowTimeDays)
            binding.tableSubjectsTime.addView(rowSubjectsTime)
        }
        return holder
    }

    class WidgetHolder(binding: WidgetCourseTimetableV2Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseTimetableV2Binding>(binding, widget)
}

@Keep
class CourseTimetableWidgetV2Model :
    WidgetEntityModel<CourseTimeTableV2Data, WidgetAction>()

@Keep
data class CourseTimeTableV2Data(
    @SerializedName("timetable_data")
    val timetableList: List<TimetableRowV2Data>?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("title_color")
    val titleColor: String?,
    @SerializedName("title_size")
    val titleSize: String?,
    @SerializedName("is_expanded")
    var isExpanded: Boolean?
) : WidgetData()

@Keep
data class TimetableRowV2Data(
    @SerializedName("cell_list")
    val cellList: List<TimetableCellV2Data>?
)

@Keep
data class TimetableCellV2Data(
    @SerializedName("text")
    val text: String?,
    @SerializedName("text_color")
    val textColor: String?,
    @SerializedName("is_bold")
    val isBold: Boolean?,
    @SerializedName("reminder_link")
    val url: String?
)
