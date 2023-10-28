package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemHorizontalListWidgetBinding
import com.doubtnutapp.databinding.WidgetHorizontalListBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class HorizontalListWidget(
    context: Context
) : BaseBindingWidget<HorizontalListWidget.HorizontalListWidgetHolder,
        HorizontalListWidget.HorizontalListWidgetModel, WidgetHorizontalListBinding>(context) {

    @Inject
    lateinit var whatsAppSharing: WhatsAppSharing

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = HorizontalListWidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: HorizontalListWidgetHolder,
        model: HorizontalListWidgetModel
    ): HorizontalListWidgetHolder {
        super.bindWidget(holder, model)

        val data: HorizontalListWidgetData = model.data
        val binding = holder.binding as WidgetHorizontalListBinding

        binding.tvWidgetTitle.text = data.title.orEmpty()
        binding.rvItems.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        if(model.data.items!=null) {
            binding.rvItems.adapter = HorizontalListAdapter(
                model, analyticsPublisher,
                whatsAppSharing, deeplinkAction,
                model.data.items!!
            )
        }

        if (data.showViewAll.toBoolean()) {
            binding.tvViewAll.show()
            binding.tvViewAll.setOnClickListener {
                deeplinkAction.performAction(it.context, data.deeplink, model.type)
            }
        } else {
            binding.tvViewAll.hide()
        }

        if (data.showViewAll.toBoolean() || !data.title.isNullOrBlank()) {
            binding.layoutTitle.show()
        } else {
            binding.layoutTitle.hide()
        }

        trackingViewId = data.id
        return holder
    }

    class HorizontalListAdapter(
        val widgetModel: HorizontalListWidgetModel,
        val analyticsPublisher: AnalyticsPublisher,
        val whatsAppSharing: WhatsAppSharing,
        val deeplinkAction: DeeplinkAction,
        private val items:List<HorizontalListItemData>
    ) : RecyclerView.Adapter<HorizontalListAdapter.ViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemHorizontalListWidgetBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data: HorizontalListItemData = items[position]
            val binding = holder.binding

            val width = Utils.getWidthFromScrollSize(holder.itemView.context, data.cardWidth)
            val ratio = data.cardRatio ?: Utils.getRatioFromScrollSize(data.cardWidth)

            binding.rootContainer.layoutParams.width = width
            (binding.ivImage.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                ratio

            binding.ivImage.loadImage(data.imageUrl)
            binding.tvTitle.toggleVisibilityAndSetText(data.title)
            binding.tvSubtitle.toggleVisibilityAndSetText(data.subtitle)
            binding.ivVideoPlay.setVisibleState(data.showVideo)
            binding.ivShareWhatsapp.setVisibleState(data.showWhatsapp)

            binding.tvOne.toggleVisibilityAndSetText(data.text1)
            binding.tvTwo.toggleVisibilityAndSetText(data.text2)

            binding.rootContainer.setOnClickListener {
                if (data.isPremium == true && data.isVip != true) {
                    ToastUtils.makeText(
                        it.context,
                        data.blockContentMessage.orEmpty(),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    deeplinkAction.performAction(it.context, data.deeplink, widgetModel.type)

                }
            }
            binding.ivShareWhatsapp.setOnClickListener {
                if (data.deeplink != null) {
                    whatsAppSharing.shareOnWhatsAppFromDeeplink(
                        data.deeplink,
                        data.imageUrl,
                        widgetModel.data.sharingMessage ?: "",
                        data.id
                    )
                    whatsAppSharing.startShare(it.context)
                }
            }

        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemHorizontalListWidgetBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class HorizontalListWidgetHolder(binding: ViewBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<ViewBinding>(binding, widget)

    class HorizontalListWidgetModel : WidgetEntityModel<HorizontalListWidgetData, WidgetAction>()

    @Keep
    data class HorizontalListWidgetData(
        @SerializedName("_id") val id: String,
        @SerializedName("title") val title: String?,
        @SerializedName("items") val items: List<HorizontalListItemData>?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("sharing_message") val sharingMessage: String?,
        @SerializedName("show_view_all") val showViewAll: Int
    ) : WidgetData()

    @Keep
    data class HorizontalListItemData(
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("show_whatsapp") val showWhatsapp: Boolean,
        @SerializedName("show_video") val showVideo: Boolean,
        @SerializedName("image_url") val imageUrl: String,
        @SerializedName("card_width") val cardWidth: String,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("text1") val text1: String?,
        @SerializedName("text2") val text2: String?,
        @SerializedName("block_content_message") val blockContentMessage: String?,
        @SerializedName("is_premium") val isPremium: Boolean?,
        @SerializedName("is_vip") val isVip: Boolean?
    )

    override fun getViewBinding(): WidgetHorizontalListBinding {
        return WidgetHorizontalListBinding.inflate(LayoutInflater.from(context), this, true)
    }
}