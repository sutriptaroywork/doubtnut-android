package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetCourseTestBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.ui.mockTest.MockTestSubscriptionActivity
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseTestWidget(context: Context) : BaseBindingWidget<CourseTestWidget.WidgetHolder,
    CourseTestWidgetModel, WidgetCourseTestBinding>(context) {

    companion object {
        const val TAG = "CourseTestWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkActiion: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetCourseTestBinding {
        return WidgetCourseTestBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CourseTestWidgetModel): WidgetHolder {
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
        binding.root.background =
            Utils.getShape("#ffffff", data.color.orDefaultValue("#000000"))
        binding.textViewSubtitle.text = data.subtitle.orEmpty()
        binding.tvContinue.text = data.buttonText.orEmpty()
        binding.tvDuration.isVisible = !model.data.duration.isNullOrBlank()
        binding.tvDuration.text = model.data.duration
        binding.imageViewTest.loadImageEtx(data.imageUrl.orEmpty())
        binding.root.setOnClickListener {
            if (!data.deeplink.isNullOrBlank()) {
                deeplinkActiion.performAction(context, data.deeplink)
            } else {
                context.startActivity(
                    MockTestSubscriptionActivity.getStartIntent(
                        holder.itemView.context,
                        data.id.orDefaultValue().toInt(), false
                    )
                )
            }
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

    class WidgetHolder(binding: WidgetCourseTestBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseTestBinding>(binding, widget)
}

class CourseTestWidgetModel : WidgetEntityModel<CourseTestWidgetData, WidgetAction>()

@Keep
data class CourseTestWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("questions_count") val subtitle: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("action_text") val buttonText: String?,
    @SerializedName("is_completed") val isCompleted: Boolean?,
    @SerializedName("id") val id: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("margin") val margin: Boolean?,
    @SerializedName("submit_date") val submitDate: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("deeplink") val deeplink: String?
) : WidgetData()
