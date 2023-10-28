package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.course.widgets.ParentWidget
import com.doubtnutapp.databinding.*
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 16/2/21.
 */

class ImageCardGroupWidget(context: Context) :
    BaseBindingWidget<ImageCardGroupWidget.WidgetHolder, ImageCardGroupWidget.Model, WidgetImageCardGroupBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        val data = model.data
        val binding = holder.binding
        with(binding) {
            tvtTitle.text = data.title
            recyclerView.adapter =
                Adapter(data.cardGroups, model.extraParams, deeplinkAction, analyticsPublisher)
        }
        trackingViewId = data.id
        return holder
    }

    class Adapter(
        private val cardGroups: List<CardGroup>,
        private val extraParams: HashMap<String, Any>?,
        private val deeplinkAction: DeeplinkAction,
        private val analyticsPublisher: AnalyticsPublisher
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemImageCardGroupBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = cardGroups[position]
            val binding = holder.binding

            with(binding) {
                tvGroupTitle.text = data.groupTitle
                tvGroupViewAll.isVisible = data.deeplink.isNullOrBlank().not()
                tvGroupViewAll.setOnClickListener {
                    deeplinkAction.performAction(binding.root.context, data.deeplink)
                    analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                    EventConstants.EXPLORE_SEE_MORE
                                            + "_" + EventConstants.WIDGET_ITEM_CLICK,
                                    hashMapOf<String, Any>(
                                            EventConstants.PARENT_TITLE to data.groupTitle,
                                            EventConstants.WIDGET to ParentWidget.TAG
                                    ).apply {
                                        putAll(extraParams ?: HashMap())
                                    }
                            ))
                }

                listOf(imageView1, imageView2, imageView3).zip(data.items).forEach { (imageView, imageItem) ->
                    imageView.show()
                    imageView.loadImage(imageItem.imageUrl)
                    imageView.setOnClickListener {
                        extraParams?.put(EventConstants.PARENT_TITLE, data.groupTitle)
                        extraParams?.put(Constants.ID, imageItem.imageItemId.orEmpty())
                        DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(extraParams))

                        deeplinkAction.performAction(binding.root.context, imageItem.deeplink)
                    }
                }
            }
        }

        override fun getItemCount(): Int = cardGroups.size

        class ViewHolder(val binding: ItemImageCardGroupBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetImageCardGroupBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetImageCardGroupBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
            @SerializedName("id") val id: String?,
            @SerializedName("title") val title: String,
            @SerializedName("card_groups") val cardGroups: List<CardGroup>
    ) : WidgetData()

    @Keep
    data class CardGroup(
            @SerializedName("group_id") val groupId: String?,
            @SerializedName("group_title") val groupTitle: String,
            @SerializedName("deeplink") val deeplink: String?,
            @SerializedName("items") val items: List<ImageItem>
    )

    @Keep
    data class ImageItem(
            @SerializedName("id") val imageItemId: String?,
            @SerializedName("image_url") val imageUrl: String,
            @SerializedName("deeplink") val deeplink: String
    )

    override fun getViewBinding(): WidgetImageCardGroupBinding {
        return WidgetImageCardGroupBinding.inflate(LayoutInflater.from(context), this, true)
    }
}