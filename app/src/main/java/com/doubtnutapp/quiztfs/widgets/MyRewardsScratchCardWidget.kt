package com.doubtnutapp.quiztfs.widgets

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.ScratchCardClicked
import com.doubtnutapp.databinding.ItemMyRewardsScratchCardBinding
import com.doubtnutapp.databinding.WidgetMyRewardsScratchCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.quiztfs.ScratchCardState.STATE_LOCKED
import com.doubtnutapp.loadImage
import com.doubtnutapp.setVisibleState
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 07-09-2021
 */
class MyRewardsScratchCardWidget
constructor(
    context: Context,
) : BaseBindingWidget<MyRewardsScratchCardWidget.WidgetHolder, MyRewardsScratchCardWidgetModel, WidgetMyRewardsScratchCardBinding>(
    context
) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetMyRewardsScratchCardBinding {
        return WidgetMyRewardsScratchCardBinding
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
        model: MyRewardsScratchCardWidgetModel
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

        holder.binding.rvReward.adapter = adapter

        return holder
    }

    inner class Adapter(
        val items: List<ScratchItem>,
        val model: MyRewardsScratchCardWidgetModel,
        val extraParams: HashMap<String, Any>,
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemMyRewardsScratchCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            holder.bind(data)
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(private val binding: ItemMyRewardsScratchCardBinding) :
            RecyclerView.ViewHolder(binding.root) {

            private val level: TextView = binding.tvLevel
            private val description: TextView = binding.tvDescription
            private val reward: ImageView = binding.ivReward
            private val lock: LinearLayout = binding.lockView

            fun bind(item: ScratchItem) {
                description.text = item.text
                reward.loadImage(item.imageUrl)

                if (item.dialogData.state == STATE_LOCKED) {
                    level.text = "Level ${item.level}"
                    lock.setVisibleState(true)
                } else {
                    lock.setVisibleState(false)
                }

                binding.root.setOnClickListener {
                    actionPerformer?.performAction(ScratchCardClicked(item.dialogData))

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            model.type
                                    + "_" + EventConstants.WIDGET_ITEM_CLICK,
                            hashMapOf<String, Any>().apply {
                                putAll(model.extraParams ?: hashMapOf())
                            }
                        )
                    )
                }
            }
        }
    }

    class WidgetHolder(
        binding: WidgetMyRewardsScratchCardBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetMyRewardsScratchCardBinding>(binding, widget)
}

@Keep
class MyRewardsScratchCardWidgetModel :
    WidgetEntityModel<MyRewardsScratchCardWidgetData, WidgetAction>()

@Keep
data class MyRewardsScratchCardWidgetData(
    @SerializedName("title") val title: String,
    @SerializedName("items") val items: List<ScratchItem>
) : WidgetData()

@Keep
data class ScratchItem(
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("level") val level: String,
    @SerializedName("text") val text: String,
    @SerializedName("value") val value: Int?,
    @SerializedName("dialog_data") val dialogData: DialogData
)

@Keep
@Parcelize
data class DialogData(
    @SerializedName("description") val description: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("scratched_image_link") val scratchedImageLink: String,
    @SerializedName("share_deeplink") val deepLink: String?,
    @SerializedName("coupon_code") val couponCode: String?,
    @SerializedName("value") val value: Int?,
    @SerializedName("top_button_text") val topButtonText: String?,
    @SerializedName("bottom_button_text") val bottomButtonText: String?,
    @SerializedName("level") val level: String,
    @SerializedName("status") var state: String,
) : Parcelable