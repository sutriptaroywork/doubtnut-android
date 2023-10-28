package com.doubtnut.referral.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.actions.ItemClick
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.referral.R
import com.doubtnut.referral.databinding.ItemReferralLevelWidgetBinding
import com.doubtnut.referral.databinding.ReferralLevelWidgetBinding
import com.doubtnut.referral.ui.ReferralHomeFragment
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Raghav Aggarwal on 02/03/22.
 */
class ReferralLevelWidget constructor(context: Context) :
    CoreBindingWidget<ReferralLevelWidget.WidgetHolder, ReferralLevelWidget.WidgetModel, ReferralLevelWidgetBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    var source: String? = null

    init {
        CoreApplication.INSTANCE.androidInjector().inject(this)
    }

    companion object {
        const val TAG = "ReferralLevelWidget"
        const val EVENT_TAG = "referral_level_widget"

        val colors = arrayOf(
            "#ff715e",
            "#ffc331",
            "#34b02c",
            "#2a8fec",
            "#ffc331",
            "#bb65ff",
            "#56d952",
            "#2a8fec",
            "#ff715e",
            "#ffc331"
        )
    }

    override fun getViewBinding(): ReferralLevelWidgetBinding {
        return ReferralLevelWidgetBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: WidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: ReferralLevelWidgetData = model.data

        binding.tvTitle.applyTextColor(data.titleColor)
        binding.tvTitle.applyTextSize(data.titleSize)
        binding.tvTitle.setTextFromHtml(data.title.orEmpty())

        val adapter: Adapter
        if ((data.items?.size ?: 0) <= 4) {
            data.isExpanded = true
        }

        if (data.isExpanded == true) {
            adapter = Adapter(
                items = ArrayList<LevelData>().apply {
                    addAll(data.items.orEmpty())
                },
                extraParams = model.extraParams ?: HashMap()
            )

            binding.tvSeeAllReward.gone()
            binding.viewDivider.gone()
        } else {
            adapter = Adapter(
                ArrayList<LevelData>().apply {
                    addAll(data.items?.take(4).orEmpty())
                },
                model.extraParams ?: HashMap()
            )

            binding.tvSeeAllReward.visible()
            binding.viewDivider.visible()
        }

        holder.binding.rvWidgets.adapter = adapter

        binding.tvSeeAllReward.setOnClickListener {
            data.isExpanded = true

            binding.tvSeeAllReward.gone()
            binding.viewDivider.gone()

            data.items ?: return@setOnClickListener
            adapter.items.addAll(data.items.subList(4, data.items.size))
            adapter.notifyItemRangeInserted(4, data.items.size - 4)

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${CoreEventConstants.MORE_CLICKED}",
                    hashMapOf(
                        CoreEventConstants.WIDGET to TAG,
                        CoreEventConstants.SOURCE to source.orEmpty()
                    )
                )
            )
        }
        return holder
    }

    class WidgetHolder(binding: ReferralLevelWidgetBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<ReferralLevelWidgetBinding>(binding, widget)

    inner class Adapter(
        val items: ArrayList<LevelData>,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemReferralLevelWidgetBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            holder.bind(data)
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(private val binding: ItemReferralLevelWidgetBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(item: LevelData) {
                binding.apply {
                    ivBackground.applyBackgroundTint(
                        colors.getOrNull(bindingAdapterPosition) ?: colors.random()
                    )

                    if (item.isLocked.toBool()) {
                        ivBackground.loadImage2(null)
                        ivBlurBackground.visible()
                        ivLock.visible()
                    } else {
                        ivBackground.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.bg_celebration
                            )
                        )
                        ivBlurBackground.gone()
                        ivLock.gone()
                    }


                    if (item.imageUrl.isNullOrEmpty()) {
                        ivImage.gone()
                    } else {
                        ivImage.visible()
                        ivImage.loadImage2(item.imageUrl)
                    }

                    tvText2.text = item.text2 ?: ""
                    tvText2.applyTextColor(item.text2Color)
                    tvText2.applyTextSize(item.text2Size)

                    tvText.text = item.text ?: ""
                    tvText.applyTextColor(item.textColor)
                    tvText.applyTextSize(item.textSize)

                    root.setOnClickListener {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                "${EVENT_TAG}_${CoreEventConstants.CLICKED}",
                                hashMapOf(
                                    CoreEventConstants.WIDGET to TAG,
                                    CoreEventConstants.SOURCE to source.orEmpty(),
                                    CoreEventConstants.IS_LOCKED to item.isLocked.toBool(),
                                    CoreEventConstants.ITEM_POSITION to bindingAdapterPosition
                                ), ignoreMoengage = false
                            )
                        )
                        if (item.deeplink.isNotNullAndNotEmpty2()) {
                            deeplinkAction.performAction(context, item.deeplink)
                        } else if (item.isLocked.toBool()) {
                            actionPerformer?.performAction(ItemClick(ReferralHomeFragment.ACTION_SHAKE_CTA))
                        }
                    }
                }
            }
        }
    }

    @Keep
    class WidgetModel :
        WidgetEntityModel<ReferralLevelWidgetData, WidgetAction>()

    @Keep
    data class ReferralLevelWidgetData(
        @SerializedName("title")
        val title: String?,
        @SerializedName("title_color")
        val titleColor: String?,
        @SerializedName("title_size")
        val titleSize: String?,
        @SerializedName("items", alternate = ["levels"])
        val items: List<LevelData>?,
        @SerializedName("is_expanded")
        var isExpanded: Boolean?

    ) : WidgetData()

    @Keep
    data class LevelData(
        @SerializedName("text")
        val text: String?,
        @SerializedName("text_color")
        val textColor: String?,
        @SerializedName("text_size")
        val textSize: String?,

        @SerializedName("text2", alternate = ["amount"])
        val text2: String?,
        @SerializedName("text2_color", alternate = ["amount_color"])
        val text2Color: String?,
        @SerializedName("text2_size", alternate = ["amount_size"])
        val text2Size: String?,

        @SerializedName("is_locked")
        val isLocked: Int?,

        @SerializedName("image_url")
        val imageUrl: String?,

        @SerializedName("deeplink")
        val deeplink: String?
    )
}

