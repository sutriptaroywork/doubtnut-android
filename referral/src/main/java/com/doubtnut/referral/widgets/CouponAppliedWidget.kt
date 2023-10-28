package com.doubtnut.referral.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.core.CoreApplication
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
import com.doubtnut.referral.databinding.WidgetCouponAppliedBinding
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CouponAppliedWidget(context: Context) :
    CoreBindingWidget<CouponAppliedWidget.WidgetViewHolder,
            CouponAppliedWidget.Model, WidgetCouponAppliedBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "CouponAppliedWidget"
        const val EVENT_TAG = "coupon_applied_widget"
    }

    init {
        CoreApplication.INSTANCE.androidInjector().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetCouponAppliedBinding {
        return WidgetCouponAppliedBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: Model
    ): WidgetViewHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.ivImage.isVisible = model.data.imageUrl.isNullOrEmpty().not()
        binding.ivImage.loadImage2(model.data.imageUrl.ifEmptyThenNull2())

        binding.tvTitle.isVisible = model.data.title.isNullOrEmpty().not()
        binding.tvTitle.text = model.data.title
        binding.tvTitle.applyTextSize(model.data.titleTextSize)
        binding.tvTitle.applyTextColor(model.data.titleTextColor)

        binding.flexAction.isVisible = model.data.action.isNullOrEmpty().not()
        binding.tvAction.text = model.data.action
        binding.tvAction.applyTextSize(model.data.actionTextSize)
        binding.tvAction.applyTextColor(model.data.actionTextColor)
        binding.ivAction.isVisible = model.data.actionImageUrl.isNullOrEmpty().not()
        binding.ivAction.loadImage2(model.data.actionImageUrl.ifEmptyThenNull2())

        binding.flexAction.setOnClickListener {
            deeplinkAction.performAction(context, model.data.actionDeepLink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${CoreEventConstants.CTA_CLICKED}",
                    hashMapOf<String, Any>(
                        CoreEventConstants.WIDGET to TAG,
                        CoreEventConstants.CTA_TEXT to model.data.action.orEmpty(),
                        CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        return holder
    }

    class WidgetViewHolder(binding: WidgetCouponAppliedBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetCouponAppliedBinding>(binding, widget)

    class Model :
        WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("title_text_size") val titleTextSize: String?,
        @SerializedName("title_text_color") val titleTextColor: String?,

        @SerializedName("action") val action: String?,
        @SerializedName("action_text_size") val actionTextSize: String?,
        @SerializedName("action_text_color") val actionTextColor: String?,
        @SerializedName("action_image_url") val actionImageUrl: String?,
        @SerializedName("action_deeplink") val actionDeepLink: String?,

        @SerializedName("image_url") val imageUrl: String?,

        ) : WidgetData()
}