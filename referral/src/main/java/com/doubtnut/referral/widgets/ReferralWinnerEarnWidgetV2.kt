package com.doubtnut.referral.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.referral.databinding.ItemReferralWinnerEarnWidgetV2Binding
import com.doubtnut.referral.databinding.ReferralWinnerEarnWidgetV2Binding
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Raghav Aggarwal on 07/03/22.
 */
class ReferralWinnerEarnWidgetV2 constructor(context: Context) :
    CoreBindingWidget<ReferralWinnerEarnWidgetV2.WidgetHolder, ReferralWinnerEarnWidgetV2.WidgetModel, ReferralWinnerEarnWidgetV2Binding>(
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
        const val TAG = "ReferralWinnerEarnWidgetV2"
        const val EVENT_TAG = "referral_winner_earn_widget_V2"
    }

    override fun getViewBinding(): ReferralWinnerEarnWidgetV2Binding {
        return ReferralWinnerEarnWidgetV2Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: WidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 16, 16)
        }
        val binding = holder.binding
        val data: ReferralWinnerEarnWidgetV2Data = model.data

        binding.apply {
            ivImage.loadImage2(data.imageUrl)

            tvTitle1.text = data.title1.orEmpty()
            tvTitle1.applyTextSize(data.title1Size)
            tvTitle1.applyTextColor(data.title1Color)

            tvTitle2.text = data.title2.orEmpty()
            tvTitle2.applyTextSize(data.title2Size)
            tvTitle2.applyTextColor(data.title2Color)

            rvMain.layoutManager = LinearLayoutManager(context)
            rvMain.adapter = Adapter(data.items.orEmpty())

            root.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        "${EVENT_TAG}_${CoreEventConstants.CLICKED}",
                        hashMapOf<String, Any>(
                            CoreEventConstants.WIDGET to TAG,
                            CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                            CoreEventConstants.SOURCE to source.orEmpty()
                        ).apply {
                            putAll(data.extraParams.orEmpty())
                        }
                    )
                )
                deeplinkAction.performAction(context, data.deeplink)
            }
        }

        return holder
    }

    inner class Adapter(
        private val items: List<String>,
    ) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val binding = ItemReferralWinnerEarnWidgetV2Binding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(private val binding: ItemReferralWinnerEarnWidgetV2Binding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(item: String) {
                binding.apply {
                    TextViewUtils.setTextFromHtml(tvTitle, item)
                }
            }
        }

    }

    class WidgetHolder(binding: ReferralWinnerEarnWidgetV2Binding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<ReferralWinnerEarnWidgetV2Binding>(binding, widget)

    @Keep
    class WidgetModel :
        WidgetEntityModel<ReferralWinnerEarnWidgetV2Data, WidgetAction>()

    @Keep
    data class ReferralWinnerEarnWidgetV2Data(
        @SerializedName("title1")
        val title1: String?,
        @SerializedName("title1_color")
        val title1Color: String?,
        @SerializedName("title1_size")

        val title1Size: String?,
        @SerializedName("title2")
        val title2: String?,
        @SerializedName("title2_color")
        val title2Color: String?,
        @SerializedName("title2_size")
        val title2Size: String?,

        @SerializedName("image_url")
        val imageUrl: String?,

        @SerializedName("title3")
        val items: List<String>?,

        @SerializedName("deeplink")
        val deeplink: String?,

        @SerializedName("extra_params")
        var extraParams: HashMap<String, Any>?
    ) : WidgetData()

}

