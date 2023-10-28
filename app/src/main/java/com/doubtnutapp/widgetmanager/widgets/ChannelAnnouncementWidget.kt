package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.ItemChannelAnnouncementBinding
import com.doubtnutapp.databinding.WidgetChannelContentBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ChannelAnnouncementWidget(
    context: Context
) : BaseBindingWidget<ChannelAnnouncementWidget.WidgetHolder,
        ChannelAnnouncementWidget.Model,
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

        binding.tvTitle.isVisible = !model.data.title.isNullOrEmpty()
        binding.tvTitle.text = model.data.title

        binding.rvChannelContent.setLayoutOrientation(
            holder.itemView.context,
            model.data.listOrientation ?: Constants.ORIENTATION_TYPE_HORIZONTAL_LIST
        )
        if (model.data.listOrientation != Constants.ORIENTATION_TYPE_VERTICAL_LIST) {
            val padding16Dp = 16.dpToPx()
            binding.root.setPadding(padding16Dp, 0, padding16Dp, 8.dpToPx())
        }
        binding.rvChannelContent.adapter =
            Adapter(
                model.data.announcements,
                analyticsPublisher,
                deeplinkAction,
                source,
                model.data.listOrientation
            )
        return holder
    }

    class WidgetHolder(
        binding: WidgetChannelContentBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetChannelContentBinding>(binding, widget)

    class Model :
        WidgetEntityModel<ChannelAnnoucementWidgetData, WidgetAction>()


    @Keep
    data class ChannelAnnoucementWidgetData(
        @SerializedName("title") val title: String,
        @SerializedName("items") val announcements: ArrayList<Announcement>,
        @SerializedName("list_orientation") val listOrientation: Int?
    ) : WidgetData()

    @Keep
    data class Announcement(
        @SerializedName("teacher_id") val teacherId: String?,
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String,
        @SerializedName("small_text") val smallText: String,
        @SerializedName("url") val url: String,
        @SerializedName("thumbnail_url") val thumbnailUrl: String,
        @SerializedName("background_url") val backgroundUrl: String,
        @SerializedName("background_color") val backgroundColor: String,
        @SerializedName("deeplink") val deeplink: String,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("card_ratio") val cardRatio: String?
    )

    class Adapter(
        private var items: List<Announcement>,
        private val analyticsPublisher: AnalyticsPublisher,
        private val deeplinkAction: DeeplinkAction,
        val source: String?,
        val listOrientation: Int?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemChannelAnnouncementBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), analyticsPublisher, deeplinkAction, source, listOrientation
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]

            val widgetLayoutConfig = WidgetLayoutConfig(0, 0, 0, 12)
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

            holder.itemView.setWidthFromScrollSize(data.cardWidth ?: "1.25")

            holder.binding.layoutContent.updateLayoutParams<ConstraintLayout.LayoutParams> {
                dimensionRatio = "16:5"
            }

            holder.bind(data)
        }

        override fun getItemCount(): Int = items.size

        fun updateItems(items: List<Announcement>) {
            this.items = items
            notifyDataSetChanged()
        }

        class ViewHolder(
            val binding: ItemChannelAnnouncementBinding,
            private val analyticsPublisher: AnalyticsPublisher,
            val deeplinkAction: DeeplinkAction,
            val source: String?,
            val listOrientation: Int?
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(data: Announcement) {
                if (listOrientation == Constants.ORIENTATION_TYPE_HORIZONTAL_LIST) {
                    val padding16Dp = 16.dpToPx()
                    binding.root.setPadding(0, 0, padding16Dp, 0)

                }
                binding.root.setOnClickListener {
                    deeplinkAction.performAction(binding.root.context, data.deeplink)
                    if (data.deeplink.contains(".pdf")) {
                        markNotesRead(data.id, 1)
                    }
                    val eventParams = hashMapOf<String, Any>()
                    eventParams[Constants.SOURCE] = source.orEmpty()
                    eventParams[EventConstants.RESOURCE_ID] = data.id.orEmpty()
                    eventParams[Constants.RESOURCE_TYPE] =
                        if (data.deeplink.contains(".pdf")) Constants.PDF else Constants.ANNOUNCEMENT
                    eventParams[Constants.TEACHER_ID] = data.teacherId.orEmpty()
                    analyticsPublisher?.publishEvent(
                        AnalyticsEvent(
                            EventConstants.TEACHER_PAGE_RESOURCE_CLICKED,
                            eventParams
                        )
                    )

                }

                if (!data.thumbnailUrl.isNullOrEmpty()) {

                    binding.title.hide()
                    binding.tvSmallText.hide()

                    binding.ivBackground.loadImage(
                        data.thumbnailUrl,
                        R.color.color_a8b3ba,
                        R.drawable.ic_group_1823
                    )
                } else {
                    binding.title.show()
                    binding.tvSmallText.show()
                    binding.title.text = data.title
                    binding.tvSmallText.text = data.smallText

                    if (!data.backgroundUrl.isNullOrEmpty()) {
                        binding.ivBackground.loadImage(
                            data.backgroundUrl,
                            R.color.color_a8b3ba,
                            R.drawable.ic_group_1823
                        )
                    } else {
                        val bgcolor =
                            if (!data.backgroundColor.isNullOrEmpty()) data.backgroundColor else "#EFC455"
                        binding.root.setBackgroundColor(Color.parseColor(bgcolor))
                    }
                }
            }

            private fun markNotesRead(resourceId: String?, isDownloaded: Int) {
                DataHandler.INSTANCE.courseRepository.markNotesRead(
                    resourceId.orEmpty(),
                    isDownloaded
                )
                    .applyIoToMainSchedulerOnCompletable()
                    .subscribeToCompletable({})
            }
        }
    }

}