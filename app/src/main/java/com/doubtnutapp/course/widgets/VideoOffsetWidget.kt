package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnVideoOffsetClicked
import com.doubtnutapp.databinding.ItemVideoOffsetBinding
import com.doubtnutapp.databinding.WidgetVideoOffsetBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class VideoOffsetWidget(context: Context) :
    BaseBindingWidget<VideoOffsetWidget.VideoOffsetWidgetHolder,
        VideoOffsetWidget.VideoOffsetWidgetModel, WidgetVideoOffsetBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    val map = hashMapOf<String, String>()

    override fun setupViewHolder() {
        this.widgetViewHolder = VideoOffsetWidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: VideoOffsetWidgetHolder,
        model: VideoOffsetWidgetModel
    ): VideoOffsetWidgetHolder {
        super.bindWidget(holder, model)
        val data: VideoOffsetWidgetData = model.data
        holder.binding.recyclerView.adapter = VideoOffsetAdapter(
            data,
            context,
            actionPerformer,
            analyticsPublisher,
            model.extraParams ?: hashMapOf()
        )
        return holder
    }

    class VideoOffsetWidgetHolder(binding: WidgetVideoOffsetBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetVideoOffsetBinding>(binding, widget)

    class VideoOffsetWidgetModel : WidgetEntityModel<VideoOffsetWidgetData, WidgetAction>()

    @Keep
    data class VideoOffsetWidgetData(
        @SerializedName("items") val items: List<VideoOffsetItem>,
        var selectedSubject: String? = "",
    ) : WidgetData()

    @Keep
    data class VideoOffsetItem(
        @SerializedName("title") val title: String?,
        @SerializedName("offset") val offset: Long?,
        @SerializedName("offset_title") val offsetTitle: String?,
        @SerializedName("deeplink") val deeplink: String?,
        var isSelected: Boolean = false,
    )

    override fun getViewBinding(): WidgetVideoOffsetBinding {
        return WidgetVideoOffsetBinding.inflate(LayoutInflater.from(context), this, true)
    }

    class VideoOffsetAdapter(
        val data: VideoOffsetWidgetData,
        val context: Context,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val extraParams: HashMap<String, Any>,
    ) : RecyclerView.Adapter<VideoOffsetAdapter.VideoOffsetItemViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): VideoOffsetItemViewHolder {
            return VideoOffsetItemViewHolder(
                ItemVideoOffsetBinding
                    .inflate(LayoutInflater.from(context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: VideoOffsetItemViewHolder, position: Int) {
            holder.binding.titleTv.text = data.items[position].title.orEmpty()
            holder.binding.offsetTv.text = data.items[position].offsetTitle.orEmpty()
            holder.binding.mainLayout.setOnClickListener {
                data.items.forEachIndexed { index, item ->
                    item.isSelected = index == position
                }
                notifyDataSetChanged()
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.VIDEO_PAGE_VERTICAL_TIMESTAMP_CLICK,
                        hashMapOf<String, Any>().apply {
                            putAll(extraParams)
                        }
                    )
                )
                actionPerformer?.performAction(OnVideoOffsetClicked(data.items[position].offset))
            }
            if (data.items[position].isSelected) {
                holder.binding.titleTv.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.orange_eb532c
                    )
                )
                holder.binding.offsetTv.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.orange_eb532c
                    )
                )
            } else {
                holder.binding.titleTv.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.grey_504949
                    )
                )
                holder.binding.offsetTv.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.grey_808080
                    )
                )
            }
        }

        override fun getItemCount(): Int = data.items.size

        class VideoOffsetItemViewHolder(val binding: ItemVideoOffsetBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}
