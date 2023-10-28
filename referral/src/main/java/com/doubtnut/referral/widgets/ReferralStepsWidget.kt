package com.doubtnut.referral.widgets

import android.content.Context
import android.content.res.ColorStateList
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
import com.doubtnut.referral.databinding.ItemReferralStepsWidgetBinding
import com.doubtnut.referral.databinding.ReferralStepsWidgetBinding
import com.doubtnut.referral.ui.ReferralHomeFragment
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Raghav Aggarwal on 02/03/22.
 */
class ReferralStepsWidget constructor(context: Context) :
    CoreBindingWidget<ReferralStepsWidget.WidgetHolder, ReferralStepsWidget.ReferralStepsWidgetModel, ReferralStepsWidgetBinding>(
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
        const val TAG = "ReferralStepsWidget"
        const val EVENT_TAG = "referral_steps_widget"
    }

    override fun getViewBinding(): ReferralStepsWidgetBinding {
        return ReferralStepsWidgetBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ReferralStepsWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: ReferralStepsWidgetData = model.data

        TextViewUtils.setTextFromHtml(
            binding.tvTitle,
            data.title.orEmpty()
        )
        binding.tvTitle.applyTextSize(data.titleSize)
        binding.tvTitle.applyTextColor(data.titleColor)

        val adapter = Adapter(
            data.items.orEmpty(),
            model.extraParams ?: HashMap()
        )
        holder.binding.rvWidgets.adapter = adapter
        return holder
    }

    class WidgetHolder(binding: ReferralStepsWidgetBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<ReferralStepsWidgetBinding>(binding, widget)

    inner class Adapter(
        val items: List<StepsData>,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemReferralStepsWidgetBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            holder.bind(data)
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(private val binding: ItemReferralStepsWidgetBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(item: StepsData) {
                binding.apply {
                    view.applyBackgroundColor(item.highlightColor)

                    if (item.imageUrl.isNullOrEmpty()) {
                        ivImage.visibility = GONE
                    } else {
                        ivImage.visibility = VISIBLE
                        ivImage.loadImage2(item.imageUrl)
                    }

                    tvNumber.text = item.sNo
                    tvNumber.applyTextSize(item.sNoSize)
                    tvNumber.applyTextColor(item.highlightColor)

                    TextViewUtils.setTextFromHtml(tvContent, item.title.orEmpty())
                    tvContent.applyTextSize(item.titleSize)

                    if (item.deeplink.isNullOrEmpty()) {
                        root.applyRippleColor("#00000000")
                    } else {
                        root.rippleColor = ColorStateList.valueOf(
                            ContextCompat.getColor(context, R.color.grey_light)
                        )
                    }

                    root.setOnClickListener {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                "${EVENT_TAG}_${CoreEventConstants.CLICKED}",
                                hashMapOf<String, Any>(
                                    CoreEventConstants.WIDGET to TAG,
                                    CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                                    CoreEventConstants.SOURCE to source.orEmpty()
                                ).apply {
                                    putAll(item.extraParams.orEmpty())
                                }
                            )
                        )
                        if (item.deeplink.isNotNullAndNotEmpty2()) {
                            deeplinkAction.performAction(context, item.deeplink)
                        } else if (bindingAdapterPosition == 0) {
                            actionPerformer?.performAction(ItemClick(ReferralHomeFragment.ACTION_SHAKE_CTA))
                        }
                    }
                }
            }
        }
    }

    @Keep
    class ReferralStepsWidgetModel :
        WidgetEntityModel<ReferralStepsWidgetData, WidgetAction>()

    @Keep
    data class ReferralStepsWidgetData(
        @SerializedName("title", alternate = ["heading"])
        val title: String?,
        @SerializedName("title_color", alternate = ["heading_color"])
        val titleColor: String?,
        @SerializedName("title_size", alternate = ["heading_size"])
        val titleSize: String?,
        @SerializedName("items", alternate = ["steps"])
        val items: List<StepsData>?,
    ) : WidgetData()

    @Keep
    data class StepsData(
        @SerializedName("s_no")
        val sNo: String?,
        @SerializedName("s_no_size")
        val sNoSize: String?,
        @SerializedName("title")
        val title: String?,
        @SerializedName("title_size")
        val titleSize: String?,
        @SerializedName("color", alternate = ["highlight_color"])
        val highlightColor: String?,
        @SerializedName("image_url")
        val imageUrl: String?,
        @SerializedName("deeplink")
        val deeplink: String?,
        @SerializedName("extra_params")
        var extraParams: HashMap<String, Any>?
    )
}

