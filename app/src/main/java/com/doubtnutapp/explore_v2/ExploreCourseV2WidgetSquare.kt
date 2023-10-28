package com.doubtnutapp.explore_v2

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.ScrollToPosition
import com.doubtnutapp.databinding.ItemExploreCourseV2SquareBinding
import com.doubtnutapp.databinding.WidgetExploreCourseV2SquareBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 01-10-2021
 */

class ExploreCourseV2WidgetSquare constructor(
    context: Context
) : BaseBindingWidget<ExploreCourseV2WidgetSquare.WidgetHolder, ExploreCourseV2WidgetSquareModel, WidgetExploreCourseV2SquareBinding>(
    context
) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetExploreCourseV2SquareBinding {
        return WidgetExploreCourseV2SquareBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.forceUnWrap()?.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = ExploreCourseV2WidgetSquare.WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ExploreCourseV2WidgetSquareModel
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(8, 8, 12, 16)
        })
        val data = model.data
        holder.binding.title.text = data.title
        holder.binding.title.textSize = data.titleSize ?: 16f
        val adapter = Adapter(
            data.items,
            model,
            model.extraParams ?: HashMap()
        )
        holder.binding.rvWidgets.adapter = adapter

        return holder
    }

    inner class Adapter(
        val items: List<CourseItem>,
        val model: ExploreCourseV2WidgetSquareModel,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemExploreCourseV2SquareBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            if (items.isNotEmpty()) {
                if (items.size > 5) {
                    Utils.setWidthBasedOnPercentage(
                        holder.itemView.context, holder.itemView, "5.65", R.dimen.spacing
                    )
                } else {
                    Utils.setWidthBasedOnPercentage(
                        holder.itemView.context, holder.itemView, "5", R.dimen.spacing
                    )
                }
            }
            holder.bind(data, position)
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(private val binding: ItemExploreCourseV2SquareBinding) :
            RecyclerView.ViewHolder(binding.root) {

            private val image: ImageView = binding.image
            private val courseTitle: TextView = binding.courseTitle

            fun bind(item: CourseItem, itemPosition: Int) {
                courseTitle.text = item.title
                image.loadImage(item.imageUrl)

                binding.root.setOnClickListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            model.type
                                    + "_" + EventConstants.WIDGET_ITEM_CLICK,
                            hashMapOf<String, Any>().apply {
                                putAll(model.extraParams ?: hashMapOf())
                            }
                        )
                    )
                    if (item.deeplink.isNullOrEmpty()) {
                        actionPerformer?.performAction(
                            ScrollToPosition(item.position ?: 0, itemPosition)
                        )
                    } else {
                        deeplinkAction.performAction(context, item.deeplink)
                    }
                }
            }
        }
    }

    class WidgetHolder(
        binding: WidgetExploreCourseV2SquareBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetExploreCourseV2SquareBinding>(binding, widget)
}

@Keep
class ExploreCourseV2WidgetSquareModel :
    WidgetEntityModel<ExploreCourseV2WidgetSquareData, WidgetAction>()

@Keep
data class ExploreCourseV2WidgetSquareData(
    @SerializedName("title") val title: String?,
    @SerializedName("title_size") val titleSize: Float?,
    @SerializedName("items") val items: List<CourseItem>
) : WidgetData()

@Keep
data class CourseItem(
    @SerializedName("title") val title: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("position") val position: Int?,
)