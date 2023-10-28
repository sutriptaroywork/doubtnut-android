package com.doubtnutapp.quiztfs.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemMyRewardsPointsBinding
import com.doubtnutapp.databinding.WidgetMyRewardsPointsBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.loadImage
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 07-09-2021
 */
class MyRewardsPointsWidget
constructor(
    context: Context
) : BaseBindingWidget<MyRewardsPointsWidget.WidgetHolder, MyRewardsPointsWidgetModel, WidgetMyRewardsPointsBinding>(
    context
) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetMyRewardsPointsBinding {
        return WidgetMyRewardsPointsBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.forceUnWrap()?.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: MyRewardsPointsWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig.DEFAULT
        })
        val data = model.data
        holder.binding.title.text = data.title

        val adapter = Adapter(
            data.items,
            model,
            model.extraParams ?: HashMap()
        )

        holder.binding.recyclerview.adapter = adapter

        return holder
    }

    inner class Adapter(
        val items: List<Points>,
        val model: MyRewardsPointsWidgetModel,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemMyRewardsPointsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            holder.bind(data)
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(private val binding: ItemMyRewardsPointsBinding) :
            RecyclerView.ViewHolder(binding.root) {

            private val points: TextView = binding.points
            private val scratchCard: ShapeableImageView = binding.ivScratchCard

            fun bind(item: Points) {
                points.text = item.points
                scratchCard.loadImage(item.imageUrl)
            }
        }
    }

    class WidgetHolder(
        binding: WidgetMyRewardsPointsBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetMyRewardsPointsBinding>(binding, widget)
}

@Keep
class MyRewardsPointsWidgetModel :
    WidgetEntityModel<MyRewardsPointsWidgetData, WidgetAction>()

@Keep
data class MyRewardsPointsWidgetData(
    @SerializedName("page_title") val pageTitle: String,
    @SerializedName("title") val title: String,
    @SerializedName("items") val items: List<Points>
) : WidgetData()

@Keep
data class Points(
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("display_text") val points: String
)