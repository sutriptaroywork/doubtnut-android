package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.ShowTeacherProfile
import com.doubtnutapp.base.SubscribeChannel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.LayoutTeacherChannelHeaderBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TeacherHeaderWidget(
    context: Context
) : BaseBindingWidget<TeacherHeaderWidget.WidgetHolder,
        TeacherHeaderWidget.Model,
        LayoutTeacherChannelHeaderBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = ""

    override fun getViewBinding(): LayoutTeacherChannelHeaderBinding {
        return LayoutTeacherChannelHeaderBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: Model
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.tvVideoTitle.text = model.data.title.orEmpty()
        binding.tvSubscriberCount.text = model.data.description.orEmpty()

        if (!model.data.buttonText.isNullOrEmpty()) {
            binding.btnSubscribe.text = model.data.buttonText
        }
        binding.btnSubscribe.isSelected = model.data.isSubscribed != true
        binding.teacherProfileImage.setOnClickListener {
            actionPerformer?.performAction(
                ShowTeacherProfile(
                    (model.data.teacherProfileId ?: "0"), model.data.profileHeaderTitle ?: model.data.title
                )
            )
        }
        binding.btnSubscribe.setOnClickListener {
            var subscribeState = if (model.data.isSubscribed == true) 0 else 1
            actionPerformer?.performAction(
                SubscribeChannel(
                    (model.data.teacherProfileId ?: "0"),
                    subscribeState
                )
            )
            DataHandler.INSTANCE.teacherChannelRepository.subscribeChannel(
                (model.data.teacherProfileId ?: "0").toInt(),
                subscribeState
            ).applyIoToMainSchedulerOnCompletable()
                .subscribeToCompletable({})

            model.data.isSubscribed = subscribeState == 1
            binding.btnSubscribe.isSelected = subscribeState != 1

            binding.btnSubscribe.text = model.data.buttonToggleText
            val text = model.data.buttonToggleText
            model.data.buttonToggleText = model.data.buttonText
            model.data.buttonText = text

            val msg = if (subscribeState == 1) "Subscribed" else "Unsubscribed"
            Toast.makeText(binding.root.context, msg, Toast.LENGTH_SHORT).show()

            val eventParams = hashMapOf<String, Any>()
            eventParams[Constants.TEACHER_ID] = model.data.teacherProfileId.orEmpty()
            eventParams[EventConstants.IS_SUBSCRIBED] = subscribeState == 1
            analyticsPublisher?.publishEvent(
                AnalyticsEvent(
                    EventConstants.TEACHER_PAGE_SUBCRIBED_CLICKED,
                    eventParams
                )
            )
        }

        if(model.data.type.isNotNullAndNotEmpty()){
            if(model.data.type.equals(Constants.INTERNAL_TEACHER)){
                binding.imageViewDoubtnutTeacher.visibility = View.VISIBLE
            }
            else{
                binding.imageViewDoubtnutTeacher.visibility = View.GONE
            }
        }
        else{
            binding.imageViewDoubtnutTeacher.visibility = View.GONE
        }

        binding.teacherProfileImage.loadImage(model.data.imageUrl, R.drawable.bg_circle_white)
        return holder
    }

    class WidgetHolder(
        binding: LayoutTeacherChannelHeaderBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<LayoutTeacherChannelHeaderBinding>(binding, widget)

    class Model : WidgetEntityModel<TeacherHeaderWidgetData, WidgetAction>()


    @Keep
    data class TeacherHeaderWidgetData(
        @SerializedName("profile_id") var teacherProfileId: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("description") val description: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("button_text") var buttonText: String?,
        @SerializedName("button_toggle_text") var buttonToggleText: String?,
        @SerializedName("is_subscribed") var isSubscribed: Boolean?,
        @SerializedName("button_deeplink") val deeplink: String?,
        @SerializedName("profile_header_title") val profileHeaderTitle: String?,
        @SerializedName("type") val type:String?
    ) : WidgetData()
}