package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetCourseItemInfoBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.setVisibleState
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseItemInfoWidget(context: Context) : BaseBindingWidget<CourseItemInfoWidget.WidgetHolder,
    CourseItemInfoWidgetModel, WidgetCourseItemInfoBinding>(context) {

    companion object {
        const val TAG = "CourseItemInfoWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetCourseItemInfoBinding {
        return WidgetCourseItemInfoBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CourseItemInfoWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data: CourseItemInfoWidgetData = model.data
        val binding = holder.binding
        binding.textViewTitleMain.text = data.title.orEmpty()
        binding.textViewAmountStartingAt.text = data.startingAtText.orEmpty()
        binding.textViewAmountStartingAtHi.text = data.startingAtTextHi.orEmpty()
        binding.textViewAmountToPay.text = data.amountToPay.orEmpty()
        binding.textViewAmountStrikeThrough.text = data.amountStrikeThrough.orEmpty()
        binding.textViewAmountStrikeThrough.paintFlags =
            binding.textViewAmountStrikeThrough.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.tvDuration.text = data.duration.orEmpty()
        binding.tvTag.text = data.tag.orEmpty()
        binding.tvRating.text = data.rating.orEmpty()
        binding.ivRating.setVisibleState(!data.rating.isNullOrBlank())
        return holder
    }

    class WidgetHolder(binding: WidgetCourseItemInfoBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseItemInfoBinding>(binding, widget)
}

class CourseItemInfoWidgetModel : WidgetEntityModel<CourseItemInfoWidgetData, WidgetAction>()

@Keep
data class CourseItemInfoWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("starting_at_text") val startingAtText: String?,
    @SerializedName("starting_at_text_hi") val startingAtTextHi: String?,
    @SerializedName("amount_to_pay") val amountToPay: String?,
    @SerializedName("amount_strike_through") val amountStrikeThrough: String?,
    @SerializedName("tag") val tag: String?,
    @SerializedName("rating") val rating: String?
) : WidgetData()
