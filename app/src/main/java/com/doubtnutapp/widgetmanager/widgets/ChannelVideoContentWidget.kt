package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnutapp.databinding.ItemChannelVideoBinding
import com.doubtnutapp.databinding.WidgetChannelContentBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH

import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ChannelVideoContentWidget(
    context: Context
) : BaseBindingWidget<ChannelVideoContentWidget.WidgetHolder,
        ChannelVideoContentWidget.Model,
        WidgetChannelContentBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = ""

    override fun getViewBinding(): WidgetChannelContentBinding {
        return WidgetChannelContentBinding.inflate(LayoutInflater.from(context), this, true)
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

        binding.rvChannelContent.setLayoutOrientation(
            holder.itemView.context,
            model.data.listOrientation ?: Constants.ORIENTATION_TYPE_VERTICAL_LIST
        )
        if (model.data.listOrientation != Constants.ORIENTATION_TYPE_VERTICAL_LIST) {
            val padding16Dp = 16.dpToPx()
            binding.root.setPadding(padding16Dp, padding16Dp, padding16Dp, padding16Dp)
        }
        binding.rvChannelContent.adapter =
            Adapter(
                model.data.items,
                analyticsPublisher,
                deeplinkAction,
                source.orEmpty(),
                model.data.listOrientation
            )

        binding.tvTitle.isVisible = !model.data.title.isNullOrEmpty()
        binding.tvTitle.text = model.data.title

        return holder
    }

    class WidgetHolder(
        binding: WidgetChannelContentBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetChannelContentBinding>(binding, widget)

    class Model : WidgetEntityModel<WidgetChannelVideoData, WidgetAction>()

    @Keep
    data class WidgetChannelVideoData(
        @SerializedName("title") val title: String,
        @SerializedName("items") val items: ArrayList<ChannelVideoContentData>,
        @SerializedName("list_orientation") val listOrientation: Int?
    ) :
        WidgetData()

    @Keep
    data class ChannelVideoContentData(

        @SerializedName("teacher_id") val teacherId: String?,
        @SerializedName("deeplink") val deeplink: String,
        @SerializedName("image_url") val imageUrl: String,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("course_resource_id") val courseResourceId: Int,
        @SerializedName("question_id") val questionId: String?,
        @SerializedName("image_text") val imageText: String?,
        @SerializedName("title1") val title1: String?,
        @SerializedName("title2") val title2: String?,
        @SerializedName("description") val description: String?,
        @SerializedName("teacher_image") val teacherImage: String?,
        @SerializedName("tag_text") val tagText: String?,
        @SerializedName("tag") val tag: String?,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("views_count") val viewsCount: String?,
        @SerializedName("friend_names") val friendNames: List<String>?,
        @SerializedName("friend_image") val friendsImages: List<String>?,
        @SerializedName("type")val type:String?
    )

    class Adapter(
        private var items: List<ChannelVideoContentData>,
        private val analyticsPublisher: AnalyticsPublisher,
        private val deeplinkAction: DeeplinkAction,
        private val source: String,
        val listOrientation: Int?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemChannelVideoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), analyticsPublisher, deeplinkAction, source, listOrientation
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            var widgetLayoutConfig: WidgetLayoutConfig? = null
            if (listOrientation != Constants.ORIENTATION_TYPE_VERTICAL_LIST) {
                widgetLayoutConfig = WidgetLayoutConfig(0, 0, 0, 12)
            } else {
                widgetLayoutConfig = WidgetLayoutConfig(0, 16, 0, 0)

            }
            if (widgetLayoutConfig != null) {
                var marginTop = 10
                var marginLeft = 0
                var marginRight = 0
                var marginBottom = 0
                if (widgetLayoutConfig != null) {
                    if (widgetLayoutConfig.marginTop >= 0) marginTop = widgetLayoutConfig.marginTop
                    if (widgetLayoutConfig.marginLeft >= 0) marginLeft = widgetLayoutConfig.marginLeft
                    if (widgetLayoutConfig.marginRight >= 0) marginRight = widgetLayoutConfig.marginRight
                    if (widgetLayoutConfig.marginBottom >= 0) marginBottom = widgetLayoutConfig.marginBottom
                }

                val params = holder.itemView.layoutParams as MarginLayoutParams
                params.topMargin = Utils.convertDpToPixel(marginTop.toFloat()).toInt()
                params.leftMargin = Utils.convertDpToPixel(marginLeft.toFloat()).toInt()
                params.rightMargin = Utils.convertDpToPixel(marginRight.toFloat()).toInt()
                params.bottomMargin = Utils.convertDpToPixel(marginBottom.toFloat()).toInt()
            }

            if (listOrientation == Constants.ORIENTATION_TYPE_HORIZONTAL_LIST) {
                holder.itemView.setWidthFromScrollSize("1.2")
                holder.binding.videoLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    dimensionRatio = "16:9"
                }

            } else {
                holder.itemView.setWidthFromScrollSize("match_parent")
                holder.binding.videoLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    dimensionRatio = "16:9"
                }
            }
            holder.bind(data)
        }

        override fun getItemCount(): Int = items.size

        fun updateItems(items: List<ChannelVideoContentData>) {
            this.items = items
            notifyDataSetChanged()
        }

        class ViewHolder(
            val binding: ItemChannelVideoBinding,
            val analyticsPublisher: AnalyticsPublisher,
            val deeplinkAction: DeeplinkAction,
            val source: String,
            val listOrientation: Int?
        ) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(data: ChannelVideoContentData) {
                Log.d("source: $source", "TCh::")
                binding.apply {
                    contentView.setOnClickListener {
                        deeplinkAction.performAction(
                            binding.root.context,
                            data.deeplink,
                            Bundle().apply {
                                putString(Constants.SOURCE, source)
                                putInt(Constants.COURSE_ID, data.courseResourceId)
                                putString(Constants.QUESTION_ID, data.questionId)
                            })

                        val eventParams = hashMapOf<String, Any>()
                        eventParams[Constants.SOURCE] = source
                        eventParams[EventConstants.RESOURCE_ID] = data.courseResourceId
                        eventParams[Constants.QUESTION_ID] = data.questionId.orEmpty()
                        eventParams[Constants.RESOURCE_TYPE] = Constants.VIDEO
                        eventParams[Constants.TEACHER_ID] = data.teacherId.orEmpty()
                        analyticsPublisher?.publishEvent(
                            AnalyticsEvent(
                                EventConstants.TEACHER_PAGE_RESOURCE_CLICKED,
                                eventParams
                            )
                        )
                    }
                    tvTag.isVisible = !data.tag.isNullOrEmpty()
                    tvTag.text = data.tag

                    tvImageText.isVisible = !data.imageText.isNullOrEmpty() || !data.title1.isNullOrEmpty()
                    if(!data.imageText.isNullOrEmpty()) {
                        tvImageText.text = data.imageText
                    }else{
                        tvImageText.text = data.title1
                    }

                    tvTitle.isVisible = !data.title1.isNullOrEmpty()
                    tvTitle.text = data.title1.orEmpty()

                    if (source == Constants.SOURCE_HOME) {
                        tvTeacherName.isVisible = !data.title2.isNullOrEmpty()
                        tvTeacherName.text = data.title2.orEmpty()

                        tvVideoSubTitle.hide()
                        tvDescription.hide()
                        msgLayout.show()
                    }else{
                        tvVideoSubTitle.isVisible = !data.title2.isNullOrEmpty()
                        tvVideoSubTitle.text = data.title2.orEmpty()

                        tvDescription.isVisible = !data.description.isNullOrEmpty()
                        tvDescription.text = data.description.orEmpty()

                        tvTeacherName.hide()
                        msgLayout.hide()
                    }

                    teacherProfileImage.loadImage(data.teacherImage, R.color.grey_light)

                    if (!data.backgroundColor.isNullOrEmpty()) {
                        videoLayout.setBackgroundColor(Color.parseColor(data.backgroundColor))
                    }
                    if (!data.imageUrl.isNullOrEmpty()) {
                        ivVideoImage.loadImage(data.imageUrl)
                    }
                    ivVideoTeacherImage.loadImage(data.teacherImage, R.color.grey_light)

                    if (!data.friendsImages.isNullOrEmpty()) {
                        listOf(ivStudent1, ivStudent2, ivStudent3).zip(data.friendsImages)
                            .forEach { (imageView, imageItem) ->
                                imageView.show()
                                imageView.loadImage(imageItem)
                            }
                    }

                    var message = ""
                    if(!data.viewsCount.isNullOrEmpty()){
                        if (!data.friendNames.isNullOrEmpty()) {
                            tvChannelViewCount.text = "${data.viewsCount} |"
                        }else {
                            tvChannelViewCount.text = "${data.viewsCount}"
                        }
                    }
                    if (!data.friendNames.isNullOrEmpty()) {
                        if (data.friendNames.size > 1) {
                            message += "${data.friendNames!![0]} & ${data.friendNames.size - 1} viewed this video"
                        } else {
                            message += "${data.friendNames!![0]} viewed this channel"
                        }
                    }
                    msgToStudent.isVisible = !message.isNullOrEmpty()
                    msgToStudent.text = message

                    if(data.type.isNotNullAndNotEmpty()){
                        if(data.type.equals(Constants.INTERNAL_TEACHER)){
                            binding.imageDoubtnutTeacher.visibility = View.VISIBLE
                        }
                        else{
                            binding.imageDoubtnutTeacher.visibility = View.GONE
                        }
                    }
                    else{
                        binding.imageDoubtnutTeacher.visibility = View.GONE
                    }
                }

            }
        }
    }

}
