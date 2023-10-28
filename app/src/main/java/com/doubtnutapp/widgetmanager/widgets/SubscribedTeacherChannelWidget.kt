package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemChannelSubscribedBinding
import com.doubtnutapp.databinding.WidgetTeacherChannelBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SubscribedTeacherChannelWidget(
    context: Context
) : BaseBindingWidget<SubscribedTeacherChannelWidget.WidgetHolder,
        SubscribedTeacherChannelWidget.Model,
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
            binding.root.setPadding(padding16Dp, padding16Dp, padding16Dp, 0)
        }
        binding.recyclerView.adapter =
            Adapter(
                model.data.announcements,
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
        WidgetEntityModel<SubscribedTeacherChannelWidgetData, WidgetAction>()


    @Keep
    data class SubscribedTeacherChannelWidgetData(
        @SerializedName("title") val title: String,
        @SerializedName("items") val announcements: ArrayList<SubscribedTeacherChannel>,
        @SerializedName("list_orientation") val listOrientation: Int?
    ) : WidgetData()

    @Keep
    data class SubscribedTeacherChannel(
        @SerializedName("id") val id: String,
        @SerializedName("name") val teacherName: String?,
        @SerializedName("image_url") val teacherImageUrl: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("channel_image") val channelImageUrl: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("tag") val tag: String?,
        @SerializedName("teacher_info") val teacherInfo: String?,
        @SerializedName("subscriber") val subscriber: String?,
        @SerializedName("subjects") val subjects: String?,
        @SerializedName("experience") val experience: String?,
        @SerializedName("views_count") val viewsCount: String?,
        @SerializedName("circle_background_color") val circleBackgroundColor: String?,
        @SerializedName("button_text") val buttonText: String?,
        @SerializedName("button_deeplink") val buttonDeeplink: String?,
        @SerializedName("new_videos") val newVideos: String?,
        @SerializedName("friend_names") val friendNames: List<String>?,
        @SerializedName("friend_image") val friendsImages: List<String>?,
        @SerializedName("type") val type: String?

    )

    class Adapter(
        private var items: List<SubscribedTeacherChannel>,
        private var title: String?,
        private val actionPerformer: ActionPerformer?,
        private val analyticsPublisher: AnalyticsPublisher,
        private val deeplinkAction: DeeplinkAction
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemChannelSubscribedBinding.inflate(
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

            if(items.size > 1) {
                holder.itemView.setWidthFromScrollSize("1.1")
            }

            holder.bind(data, eventParams)
        }

        override fun getItemCount(): Int = items.size

        fun updateItems(items: List<SubscribedTeacherChannel>) {
            this.items = items
            notifyDataSetChanged()
        }

        class ViewHolder(
            val binding: ItemChannelSubscribedBinding,
            private val actionPerformer: ActionPerformer?,
            private val analyticsPublisher: AnalyticsPublisher,
            val deeplinkAction: DeeplinkAction
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(data: SubscribedTeacherChannel, eventParams: HashMap<String, Any>) {
                binding.root.setOnClickListener {
                    deeplinkAction.performAction(
                        binding.root.context,
                        data.deeplink,
                        EventConstants.WIDGET_SUBSCRIBED_TEACHER_CHANNEL
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
                        contentLayout.setCardBackgroundColor(
                            Color.parseColor(
                                data.backgroundColor
                            )
                        )
                    }
                    if (!data.circleBackgroundColor.isNullOrEmpty()) {
                        ivCircle.setImageDrawable(ColorDrawable(Color.parseColor(data.circleBackgroundColor)))
                    }

                    if (data.tag.isNullOrEmpty()) {
                        cvChannelTag.visibility = View.INVISIBLE
                    } else {
                        cvChannelTag.visibility = View.VISIBLE
                        tvChannelTag.text = data.tag
                    }
                    tvChannelName.isVisible = !data.teacherName.isNullOrEmpty()
                    tvChannelName.text = data.teacherName.orEmpty()+"\n\n"

                    tvSubscriberCount.isVisible = !data.teacherInfo.isNullOrEmpty()
                    tvSubscriberCount.text = data.teacherInfo.orEmpty()

                    tvExperience.isVisible = !data.experience.isNullOrEmpty()
                    tvExperience.text = data.experience.orEmpty()

                    tvSubjects.isVisible = !data.subjects.isNullOrEmpty()
                    if (!data.subjects.isNullOrEmpty()) {
                        val arr = data.subjects.split(",")
                        var subjects = ""
                        for (str in arr) {
                            subjects += "${str?.trim()}, "
                        }

                        tvSubjects.text = subjects.trim().substring(0, subjects.length - 2)+"\n"
                    }

                    tvChannelViewCount.isVisible = !data.viewsCount.isNullOrEmpty()
                    tvChannelViewCount.text = data.viewsCount.orEmpty()
                    if (!data.newVideos.isNullOrEmpty()) {
                        val viewCountText: Spannable =
                            SpannableString("${data.viewsCount.orEmpty()} | ${data.newVideos}")

                        var colorTomato = binding.root.context.resources.getColor(R.color.tomato)
                        viewCountText.setSpan(
                            ForegroundColorSpan(colorTomato),
                            viewCountText.indexOf("|")+1,
                            viewCountText.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        tvChannelViewCount.text = viewCountText
                    }

                    if (!data.buttonText.isNullOrEmpty()) {
                        btnViewChannel.text = data.buttonText
                    }
                    btnViewChannel.setOnClickListener {
                        deeplinkAction.performAction(
                            binding.root.context,
                            data.buttonDeeplink ?: data.deeplink,
                            EventConstants.WIDGET_SUBSCRIBED_TEACHER_CHANNEL
                        )
                        eventParams.put(EventConstants.CTA_TEXT, btnViewChannel.text)
                        analyticsPublisher?.publishEvent(
                            AnalyticsEvent(
                                EventConstants.SUBSCRIBED_TEACHER_CAROUSEL_CTA_CLICKED,
                                eventParams
                            )
                        )
                    }
                    ivTeacher.loadImage(
                        data.teacherImageUrl,
                        R.drawable.ic_dummy_profile_channel
                    )

                    if (data.friendNames.isNullOrEmpty()) {
                        msgLayout.hide()
                    } else {

                        if (!data.friendsImages.isNullOrEmpty()) {
                            listOf(ivStudent1, ivStudent2, ivStudent3).zip(data.friendsImages)
                                .forEach { (imageView, imageItem) ->
                                    imageView.show()
                                    imageView.loadImage(imageItem)
                                }
                        }

                        var message = ""
                        if (!data.friendNames.isNullOrEmpty()) {
                            if (data.friendNames.size > 1) {
                                message =
                                    "${data.friendNames!![0]} and ${data.friendNames.size - 1} others viewed this channel"
                            } else {
                                message = "${data.friendNames!![0]} viewed this channel"
                            }
                        }
                        msgToStudent.isVisible = !message.isNullOrEmpty()
                        msgToStudent.text = message

                    }

                    if (data.type.isNotNullAndNotEmpty()) {
                        if (data.type.equals(Constants.INTERNAL_TEACHER)) {
                            binding.imageDoubtnutTeacher.visibility = View.VISIBLE
                        } else {
                            binding.imageDoubtnutTeacher.visibility = View.GONE
                        }
                    } else {
                        binding.imageDoubtnutTeacher.visibility = View.GONE
                    }
                }

            }
        }
    }
}

