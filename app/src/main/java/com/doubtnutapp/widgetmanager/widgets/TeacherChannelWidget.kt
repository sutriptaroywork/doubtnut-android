package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.SubscribeChannel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.ItemChannelTeachersBinding
import com.doubtnutapp.databinding.WidgetTeacherChannelBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TeacherChannelWidget(
    context: Context
) : BaseBindingWidget<TeacherChannelWidget.WidgetHolder,
        TeacherChannelWidget.Model,
        WidgetTeacherChannelBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = ""

    override fun getViewBinding(): WidgetTeacherChannelBinding {
        return WidgetTeacherChannelBinding.inflate(LayoutInflater.from(context), this, true)
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

        binding.tvTitle.text = model.data.title
        binding.recyclerView.setLayoutOrientation(
            holder.itemView.context,
            model.data.listOrientation ?: Constants.ORIENTATION_TYPE_HORIZONTAL_LIST
        )
        if (model.data.listOrientation != Constants.ORIENTATION_TYPE_VERTICAL_LIST) {
            val padding16Dp = 16.dpToPx()
            binding.root.setPadding(padding16Dp, padding16Dp, padding16Dp, padding16Dp)
        }
        binding.recyclerView.adapter =
            Adapter(
                model.data.items,
                model.data.title,
                actionPerformer,
                analyticsPublisher,
                deeplinkAction
            )
        return holder
    }

    class WidgetHolder(
        binding: WidgetTeacherChannelBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetTeacherChannelBinding>(binding, widget)

    class Model :
        WidgetEntityModel<TeacherChannelWidgetData, WidgetAction>()


    @Keep
    data class TeacherChannelWidgetData(
        @SerializedName("title") val title: String,
        @SerializedName("items") val items: ArrayList<TeacherChannel>,
        @SerializedName("list_orientation") val listOrientation: Int?
    ) : WidgetData()

    @Keep
    data class TeacherChannel(
        @SerializedName("id") val id: String,
        @SerializedName("name") val teacherName: String,
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
        @SerializedName("subjects") val subjects: String?,
        @SerializedName("type") val type:String?
    )

    class Adapter (
        private var items: List<TeacherChannel>,
        private var title: String?,
        private var actionPerformer: ActionPerformer?,
        private val analyticsPublisher: AnalyticsPublisher,
        private val deeplinkAction: DeeplinkAction
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemChannelTeachersBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), actionPerformer, analyticsPublisher, deeplinkAction
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]

            val eventParams = hashMapOf<String, Any>()
            eventParams[Constants.TITLE] = title.orEmpty()
            eventParams[Constants.TEACHER_ID] = data.id
            analyticsPublisher?.publishEvent(
                AnalyticsEvent(
                    EventConstants.TEACHER_CAROUSEL_VIEWED,
                    eventParams
                )
            )

            val widgetLayoutConfig = WidgetLayoutConfig(0, 0, 0, 14)
            var marginTop = 10
            var marginLeft = 0
            var marginRight = 0
            var marginBottom: Int = 0
            if (widgetLayoutConfig != null) {
                if (widgetLayoutConfig.marginTop >= 0) marginTop = widgetLayoutConfig.marginTop
                if (widgetLayoutConfig.marginLeft >= 0) marginLeft = widgetLayoutConfig.marginLeft
                if (widgetLayoutConfig.marginRight >= 0) marginRight =
                    widgetLayoutConfig.marginRight
                if (widgetLayoutConfig.marginBottom >= 0) marginBottom =
                    widgetLayoutConfig.marginBottom
            }

            val params = holder.itemView.layoutParams as MarginLayoutParams
            params.topMargin = Utils.convertDpToPixel(marginTop.toFloat()).toInt()
            params.leftMargin = Utils.convertDpToPixel(marginLeft.toFloat()).toInt()
            params.rightMargin = Utils.convertDpToPixel(marginRight.toFloat()).toInt()
            params.bottomMargin = Utils.convertDpToPixel(marginBottom.toFloat()).toInt()


            holder.itemView.setWidthFromScrollSize("2.25")
            holder.binding.ivTeacher.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = if (data.cardRatio != null) 0 else height
                dimensionRatio = "16:17"
            }
            holder.bind(data, eventParams)
        }

        override fun getItemCount(): Int = items.size

        fun updateItems(items: List<TeacherChannel>) {
            this.items = items
            notifyDataSetChanged()
        }

        class ViewHolder(
            val binding: ItemChannelTeachersBinding,
            private var actionPerformer: ActionPerformer?,
            private val analyticsPublisher: AnalyticsPublisher,
            val deeplinkAction: DeeplinkAction
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(data: TeacherChannel, eventParams: HashMap<String, Any>) {
                binding.root.setOnClickListener {
                    deeplinkAction.performAction(
                        binding.root.context,
                        data.deeplink,
                        EventConstants.WIDGET_SUGGESTED_TEACHER_CHANNEL
                    )
                    analyticsPublisher?.publishEvent(
                        AnalyticsEvent(
                            EventConstants.TEACHER_CAROUSEL_CLICKED,
                            eventParams
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
                    tvSubscriberCount.text = "${data.subscriber.orEmpty()} Subscribers"

                    tvExperience.isVisible = !data.experience.isNullOrEmpty()
                    tvExperience.text = "${data.experience.orEmpty()} Experience"

                    if (!data.buttonText.isNullOrEmpty()) {
                        btnSubscribe.text = data.buttonText
                    }
                    binding.btnSubscribe.isSelected = data.isSubscribed != true

                    btnSubscribe.setOnClickListener {
                        var subscribeState = if (data.isSubscribed == true) 0 else 1
                        actionPerformer?.performAction(
                            SubscribeChannel(
                                (data.id ?: "0"),
                                subscribeState
                            )
                        )
                        DataHandler.INSTANCE.teacherChannelRepository.subscribeChannel(
                            (data.id ?: "0").toInt(),
                            subscribeState
                        ).applyIoToMainSchedulerOnCompletable()
                            .subscribeToCompletable({})

                        data.isSubscribed = subscribeState == 1
                        binding.btnSubscribe.isSelected = data.isSubscribed != true
                        val msg = if (subscribeState == 1) "Subscribed" else "Unsubscribed"
                        Toast.makeText(binding.root.context, msg, Toast.LENGTH_SHORT).show()

                        eventParams[EventConstants.IS_SUBSCRIBED] = subscribeState == 1
                        analyticsPublisher?.publishEvent(
                            AnalyticsEvent(
                                EventConstants.TEACHER_CAROUSEL_CTA_TAPPED,
                                eventParams
                            )
                        )
                    }

                    if(data.type.isNotNullAndNotEmpty()){
                        if(data.type.equals(Constants.INTERNAL_TEACHER)){
                            imageDoubtnutTeacher.visibility = View.VISIBLE
                        }
                        else{
                            imageDoubtnutTeacher.visibility = View.GONE
                        }
                    }
                    else{
                        imageDoubtnutTeacher.visibility = View.GONE
                    }
                }
            }

        }
    }

}