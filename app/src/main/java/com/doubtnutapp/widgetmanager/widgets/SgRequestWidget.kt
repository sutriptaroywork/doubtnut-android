package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.core.utils.applyBackgroundTint
import com.doubtnut.core.utils.applyStrokeColor
import com.doubtnut.core.utils.applyTextColor
import com.doubtnutapp.*
import com.doubtnutapp.base.SgRequestPrimaryCtaClick
import com.doubtnutapp.base.SgRequestSecondaryCtaClick
import com.doubtnutapp.databinding.WidgetSgRequestBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SgRequestWidget(context: Context) : BaseBindingWidget<
        SgRequestWidget.WidgetHolder,
        SgRequestWidget.Model,
        WidgetSgRequestBinding>(context) {

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE
            .daggerAppComponent
            .forceUnWrap()
            .inject(this)
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetSgRequestBinding =
        WidgetSgRequestBinding.inflate(LayoutInflater.from(context), this, true)

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        model.layoutConfig = WidgetLayoutConfig(
            marginTop = 0,
            marginBottom = 0,
        )
        super.bindWidget(holder, model)

        val data = model.data
        val binding = holder.binding

        with(binding) {
            if (data.pendingRequestCount.isNotNullAndNotEmpty()) {
                tvRequestCount.text = data.pendingRequestCount
                tvRequestCount.show()
                ivImage.hide()
            } else {
                ivImage.loadImage(data.image, R.drawable.ic_default_group_chat)
                ivImage.show()
                tvRequestCount.hide()
            }

            tvTitle.apply {
                text = data.title
                applyTextColor(data.titleColor)
            }

            subtitle.apply {
                text = data.subtitle
                applyTextColor(data.subtitleColor)
            }

            tvTimeStrap.isVisible = data.timeStrap.isNotNullAndNotEmpty()
            tvTimeStrap.text = data.timeStrap.orEmpty()

            primaryCta.apply {
                isVisible = data.primaryCta.isNotNullAndNotEmpty()
                text = data.primaryCta
                applyTextColor(data.primaryCtaTextColor)
                applyBackgroundTint(data.primaryCtaBgColor)
                setOnClickListener {
                    actionPerformer?.performAction(
                        SgRequestPrimaryCtaClick(
                            holder.absoluteAdapterPosition,
                            data.primaryCtaDeeplink
                        )
                    )
                }
            }

            secondaryCta.apply {
                isVisible = data.secondaryCta.isNotNullAndNotEmpty()
                text = data.secondaryCta
                applyStrokeColor(data.secondaryCtaBgColor)
                applyTextColor(data.secondaryTextColor)
                setOnClickListener {
                    actionPerformer?.performAction(
                        SgRequestSecondaryCtaClick(
                            holder.absoluteAdapterPosition,
                            data.secondaryCtaDeeplink
                        )
                    )
                }
            }

            setOnClickListener {
                deeplinkAction.performAction(binding.root.context, data.deeplink)
            }
        }

        return holder
    }

    class WidgetHolder(binding: WidgetSgRequestBinding, baseWidget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetSgRequestBinding>(binding, baseWidget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("group_id") val groupId: String,
        @SerializedName("title") val title: String,
        @SerializedName("title_color") val titleColor: String,
        @SerializedName("subtitle") val subtitle: String,
        @SerializedName("subtitle_color") val subtitleColor: String,
        @SerializedName("image") val image: String?,
        @SerializedName("pending_request_count") val pendingRequestCount: String?,
        @SerializedName("inviter") val inviter: String?,
        @SerializedName("primary_cta") val primaryCta: String?,
        @SerializedName("primary_cta_bg_color") val primaryCtaBgColor: String?,
        @SerializedName("primary_cta_text_color") val primaryCtaTextColor: String?,
        @SerializedName("primary_cta_deeplink") val primaryCtaDeeplink: String?,
        @SerializedName("secondary_cta") val secondaryCta: String?,
        @SerializedName("secondary_cta_deeplink") val secondaryCtaDeeplink: String?,
        @SerializedName("secondary_cta_text_color") val secondaryTextColor: String?,
        @SerializedName("secondary_cta_bg_color") val secondaryCtaBgColor: String?,
        @SerializedName("time_strap") val timeStrap: String?,
        @SerializedName("deeplink") val deeplink: String?,
    ) : WidgetData()

}