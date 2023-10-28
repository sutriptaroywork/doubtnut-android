package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.SubscribeChannel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.WidgetTeacherChannel2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.loadImage
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TeacherChannelWidget2(
    context: Context
) : BaseBindingWidget<TeacherChannelWidget2.WidgetHolder,
        TeacherChannelWidget2.Model,
        WidgetTeacherChannel2Binding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = ""

    override fun getViewBinding(): WidgetTeacherChannel2Binding {
        return WidgetTeacherChannel2Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: Model
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = WidgetLayoutConfig(0, 0, 0, 16)
        })
        val binding = holder.binding
        holder.itemView.layoutParams.width = 150f.dpToPx().toInt()
        val data = model.data

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.TEACHER_CAROUSEL_VIEWED,
                model.extraParams ?: hashMapOf()
            )
        )

        binding.root.setOnClickListener {
            deeplinkAction.performAction(
                binding.root.context,
                data.deeplink,
                EventConstants.WIDGET_SUGGESTED_TEACHER_CHANNEL
            )
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.TEACHER_CAROUSEL_CLICKED,
                    model.extraParams ?: hashMapOf()
                )
            )
        }

        binding.apply {

            if (!data.backgroundColor.isNullOrEmpty()) {
                cvTeacherImage.setBackgroundColor(Color.parseColor(data.backgroundColor))
            }

            if (data.tag.isNullOrEmpty()) {
                cvChannelTag.visibility = View.INVISIBLE
            } else {
                cvChannelTag.visibility = View.VISIBLE
                tvChannelTag.text = data.tag
            }

            ivTeacher.loadImage(data.teacherImageUrl, R.drawable.ic_dummy_profile_channel)
            tvTeacherName.isVisible = !data.teacherName.isNullOrEmpty()
            tvTeacherName.text = data.teacherName.orEmpty()

            tvSubjects.isVisible = !data.subjects.isNullOrEmpty()
            tvSubjects.text = data.subjects.orEmpty()

            tvSubscriberCount.isVisible = !data.subscriber.isNullOrEmpty()
            val subscriberText = "${data.subscriber.orEmpty()} Subscribers"
            tvSubscriberCount.text = subscriberText

            tvExperience.isVisible = !data.experience.isNullOrEmpty()
            val experienceText = "${data.experience.orEmpty()} Experience"
            tvExperience.text = experienceText

            if (!data.buttonText.isNullOrEmpty()) {
                btnSubscribe.text = data.buttonText
            }
            binding.btnSubscribe.isSelected = data.isSubscribed != true

            btnSubscribe.setOnClickListener {
                val subscribeState = if (data.isSubscribed == true) 0 else 1
                actionPerformer?.performAction(
                    SubscribeChannel(
                        data.id,
                        subscribeState
                    )
                )
                DataHandler.INSTANCE.teacherChannelRepository.subscribeChannel(
                    data.id.toInt(),
                    subscribeState
                ).applyIoToMainSchedulerOnCompletable()
                    .subscribeToCompletable({})

                data.isSubscribed = subscribeState == 1
                binding.btnSubscribe.isSelected = data.isSubscribed != true
                val msg = if (subscribeState == 1) "Subscribed" else "Unsubscribed"
                Toast.makeText(binding.root.context, msg, Toast.LENGTH_SHORT).show()

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.TEACHER_CAROUSEL_CTA_TAPPED,
                        model.extraParams ?: hashMapOf()
                    )
                )
            }
        }

        return holder
    }

    class WidgetHolder(
        binding: WidgetTeacherChannel2Binding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetTeacherChannel2Binding>(binding, widget)

    class Model :
        WidgetEntityModel<TeacherChannelWidgetData, WidgetAction>()


    @Keep
    data class TeacherChannelWidgetData(
        @SerializedName("id") val id: String,
        @SerializedName("name") val teacherName: String?,
        @SerializedName("image_url") val teacherImageUrl: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("subscriber") val subscriber: String?,
        @SerializedName("hours_taught") val hoursTaught: String?,
        @SerializedName("experience") val experience: String?,
        @SerializedName("button_text") val buttonText: String?,
        @SerializedName("button_deeplink") val buttonDeeplink: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("is_subscribed") var isSubscribed: Boolean?,
        @SerializedName("tag") val tag: String?,
        @SerializedName("subjects") val subjects: String?
    ) : WidgetData()

}