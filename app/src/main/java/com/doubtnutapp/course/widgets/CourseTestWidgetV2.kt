package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetCourseTestWidgetV2Binding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.ui.mockTest.MockTestSubscriptionActivity
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import javax.inject.Inject

class CourseTestWidgetV2(context: Context) : BaseBindingWidget<CourseTestWidgetV2.WidgetHolder,
    CourseTestWidgetModelV2, WidgetCourseTestWidgetV2Binding>(context) {

    companion object {
        const val TAG = "CourseTestWidgetV2"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetCourseTestWidgetV2Binding {
        return WidgetCourseTestWidgetV2Binding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CourseTestWidgetModelV2): WidgetHolder {
        super.bindWidget(holder, model)
        val data: CourseTestWidgetData = model.data
        val binding = holder.binding

        binding.textViewTitle.text = data.title.orEmpty()
        binding.textViewTitle.setTextColor(
            Utils.parseColor(
                data.color,
                Color.BLACK
            )
        )
        binding.textViewSubtitle.text = data.subtitle.orEmpty()
        binding.tvContinue.text = data.buttonText.orEmpty()
        binding.tvDuration.isVisible = !model.data.duration.isNullOrBlank()
        binding.tvDuration.text = model.data.duration
        binding.tvContinue.setOnClickListener {
            context.startActivity(
                MockTestSubscriptionActivity.getStartIntent(
                    holder.itemView.context,
                    data.id.orDefaultValue().toInt(), false
                )
            )
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.LC_COURSE_TEST_ITEM_CLICK,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.COMPLETED, data.isCompleted ?: false)
                        put(EventConstants.TEST_ID, data.id.orDefaultValue())
                        putAll(model.extraParams ?: hashMapOf())
                    }
                )
            )
        }
        binding.tvDate.text = model.data.submitDate.orEmpty()
        binding.imageViewTest.loadImageEtx(model.data.imageUrl.orEmpty())
        if (data.margin == true) {
            holder.itemView.setMargins(
                Utils.convertDpToPixel(12f).toInt(),
                Utils.convertDpToPixel(10f).toInt(),
                Utils.convertDpToPixel(12f).toInt(),
                0
            )
        } else {
            holder.itemView.setMargins(
                0,
                0,
                0,
                0
            )
        }
        return holder
    }

    class WidgetHolder(binding: WidgetCourseTestWidgetV2Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseTestWidgetV2Binding>(binding, widget)
}

class CourseTestWidgetModelV2 : WidgetEntityModel<CourseTestWidgetData, WidgetAction>()
