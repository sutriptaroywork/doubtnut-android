package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnCourseSelected
import com.doubtnutapp.databinding.ItemPackageDetailV2Binding
import com.doubtnutapp.databinding.WidgetPackageDetailBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class PurchasedCourseListWidget(context: Context) :
    BaseBindingWidget<PurchasedCourseListWidget.WidgetHolder,
        CourseListWidgetModel, WidgetPackageDetailBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "PurchasedCourseListWidget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetPackageDetailBinding {
        return WidgetPackageDetailBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CourseListWidgetModel): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val binding = holder.binding
        val data: CourseListWidgetData = model.data

        binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL, false
        )

        binding.recyclerView.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            deeplinkAction,
            model.extraParams ?: HashMap()
        )
        return holder
    }

    class Adapter(
        val items: List<CourseListWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>,
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemPackageDetailV2Binding
                    .inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            val item = items[position]
            binding.textViewTitleOne.text = item.title.orEmpty()
            binding.textViewTitleOne.textSize = item.titleSize?.toFloat() ?: 18f
            binding.textViewTitleOne.setTextColor(
                Utils.parseColor(
                    item.titleColor,
                    Color.BLACK
                )
            )
            binding.textViewTitleTwo.text = item.subtitle.orEmpty()
            binding.textViewTitleTwo.textSize = item.subtitleSize?.toFloat() ?: 14f
            binding.textViewTitleTwo.setTextColor(
                Utils.parseColor(
                    item.subtitleColor,
                    Color.BLACK
                )
            )
            binding.tvBestSeller.text = item.bestsellerText.orEmpty()
            binding.tvBestSeller.textSize = item.bestsellerSize?.toFloat() ?: 13f
            binding.tvBestSeller.setTextColor(
                Utils.parseColor(
                    item.bestsellerColor,
                    Color.BLACK
                )
            )
            binding.tvBestSeller.isVisible = item.bestseller ?: false
            binding.tvOldPrice.text = item.oldPrice.orEmpty()
            binding.tvOldPrice.textSize = item.oldPriceSize?.toFloat() ?: 13f
            binding.tvOldPrice.setTextColor(
                Utils.parseColor(
                    item.oldPriceColor,
                    Color.BLACK
                )
            )
            binding.tvNewPrice.text = item.newPrice.orEmpty()
            binding.tvNewPrice.textSize = item.newPriceSize?.toFloat() ?: 18f
            binding.tvNewPrice.setTextColor(
                Utils.parseColor(
                    item.newPriceColor,
                    Color.BLACK
                )
            )
            binding.viewBestSeller.isVisible = item.bestseller ?: false
            binding.tvOldPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

            binding.root.setOnClickListener {
                if (item.deeplink.isNullOrEmpty()) {
                    actionPerformer?.performAction(
                        OnCourseSelected(
                            item.assortmentId.orEmpty(),
                            item.categoryId.orEmpty()
                        )
                    )
                } else {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.COURSE_BOTTOM_SHEET_CLICK,
                            extraParams.apply {
                                put(EventConstants.SOURCE, EventConstants.TOP_ICON)
                            },
                            ignoreSnowplow = true
                        )
                    )
                    actionPerformer?.performAction(
                        OnCourseSelected(
                            item.assortmentId.orEmpty(),
                            item.categoryId.orEmpty()
                        )
                    )
                    deeplinkAction.performAction(binding.root.context, item.deeplink)
                }
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemPackageDetailV2Binding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetPackageDetailBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetPackageDetailBinding>(binding, widget)
}

class CourseListWidgetModel : WidgetEntityModel<CourseListWidgetData, WidgetAction>()

@Keep
data class CourseListWidgetData(
    @SerializedName("items") val items: List<CourseListWidgetItem>?,
) : WidgetData()

@Keep
data class CourseListWidgetItem(
    @SerializedName("duration") val title: String?,
    @SerializedName("duration_color") val titleColor: String?,
    @SerializedName("title_size") val titleSize: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("subtitle_color") val subtitleColor: String?,
    @SerializedName("subtitle_size") val subtitleSize: String?,
    @SerializedName("bestseller_text") val bestsellerText: String?,
    @SerializedName("bestseller") val bestseller: Boolean?,
    @SerializedName("bestseller_text_color") val bestsellerColor: String?,
    @SerializedName("bestseller_size") val bestsellerSize: Int?,
    @SerializedName("amount_strike_through") val oldPrice: String?,
    @SerializedName("amount_strike_through_color") val oldPriceColor: String?,
    @SerializedName("amount_strike_through_size") val oldPriceSize: String?,
    @SerializedName("amount_to_pay") val newPrice: String?,
    @SerializedName("amount_to_pay_color") val newPriceColor: String?,
    @SerializedName("amount_to_pay_size") val newPriceSize: String?,
    @SerializedName("deeplink") var deeplink: String?,
    @SerializedName("source") val source: String?,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("category_id") val categoryId: String?,
)
