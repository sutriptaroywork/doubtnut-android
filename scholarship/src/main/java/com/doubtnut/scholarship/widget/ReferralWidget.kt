package com.doubtnut.scholarship.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.Keep
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.sharing.entities.BranchShareData
import com.doubtnut.core.sharing.entities.ShareOnApp
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.scholarship.databinding.WidgetReferralBinding
import com.google.android.material.button.MaterialButton
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ReferralWidget(
    context: Context
) : CoreBindingWidget<ReferralWidget.WidgetHolder, ReferralWidgetModel, WidgetReferralBinding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    override fun getViewBinding(): WidgetReferralBinding {
        return WidgetReferralBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        CoreApplication.INSTANCE.androidInjector().inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ReferralWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.root.applyBackgroundColor(model.data.backgroundColor)
        binding.ivMain.loadImage2(model.data.imageUrl)

        binding.tvTitle.text = model.data.title
        binding.tvTitle.applyTextSize(model.data.titleTextSize)
        binding.tvTitle.applyTextColor(model.data.titleTextColor)


        binding.btnInviteWhatsapp.text = model.data.actionOneText
        binding.btnInviteWhatsapp.applyTextSize(model.data.actionOneTextSize)
        binding.btnInviteWhatsapp.applyTextColor(model.data.actionOneTextColor)
        binding.btnInviteWhatsapp.applyBackgroundTint(model.data.actionOneBgColor)

        if (model.data.actionOneIcon.isNullOrEmpty()) {
            binding.btnInviteWhatsapp.icon = null
        } else {
            Glide.with(context)
                .load(model.data.actionOneIcon)
                .into(
                    object : CustomViewTarget<TextView, Drawable>(binding.btnInviteWhatsapp) {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            binding.btnInviteWhatsapp.icon = resource
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            binding.btnInviteWhatsapp.icon = null
                        }

                        override fun onResourceCleared(placeholder: Drawable?) {
                            binding.btnInviteWhatsapp.icon = null
                        }
                    }
                )
            binding.btnInviteWhatsapp.iconSize =
                model.data.actionOneIconSize?.toIntOrNull()?.dpToPx() ?: 0
            binding.btnInviteWhatsapp.iconGravity =
                model.data.actionOneIconGravity?.toIntOrNull() ?: MaterialButton.ICON_GRAVITY_START
            binding.btnInviteWhatsapp.applyIconColor(model.data.actionOneIconColor)
        }

        binding.btnInviteWhatsapp.setOnClickListener {
            if (model.data.actionOneShareData != null) {
                actionPerformer?.performAction(ShareOnApp(model.data.actionOneShareData))
            } else {
                deeplinkAction.performAction(context, model.data.actionOneDeeplink)
            }

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET_TITLE to model.data.title.orEmpty(),
                        EventConstants.WIDGET to TAG,
                        EventConstants.CTA_TEXT to model.data.actionOneText.orEmpty(),
                        EventConstants.PACKAGE_NAME to model.data.actionOneShareData?.packageName.orEmpty(),
                        EventConstants.APP_NAME to model.data.actionOneShareData?.appName.orEmpty(),
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }


        binding.btnInviteTelegram.text = model.data.actionTwoText
        binding.btnInviteTelegram.applyTextSize(model.data.actionTwoTextSize)
        binding.btnInviteTelegram.applyTextColor(model.data.actionTwoTextColor)
        binding.btnInviteTelegram.applyBackgroundTint(model.data.actionTwoBgColor)

        if (model.data.actionTwoIcon.isNullOrEmpty()) {
            binding.btnInviteTelegram.icon = null
        } else {
            Glide.with(context)
                .load(model.data.actionTwoIcon)
                .into(
                    object : CustomViewTarget<TextView, Drawable>(binding.btnInviteTelegram) {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            binding.btnInviteTelegram.icon = resource
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            binding.btnInviteTelegram.icon = null
                        }

                        override fun onResourceCleared(placeholder: Drawable?) {
                            binding.btnInviteTelegram.icon = null
                        }
                    }
                )
            binding.btnInviteTelegram.iconSize =
                model.data.actionTwoIconSize?.toIntOrNull()?.dpToPx() ?: 0
            binding.btnInviteTelegram.iconGravity =
                model.data.actionTwoIconGravity?.toIntOrNull() ?: MaterialButton.ICON_GRAVITY_START
            binding.btnInviteTelegram.applyIconColor(model.data.actionTwoIconColor)
        }

        binding.btnInviteTelegram.setOnClickListener {
            if (model.data.actionOneShareData != null) {
                actionPerformer?.performAction(ShareOnApp(model.data.actionTwoShareData))
            } else {
                deeplinkAction.performAction(context, model.data.actionTwoDeeplink)
            }

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET_TITLE to model.data.title.orEmpty(),
                        EventConstants.WIDGET to TAG,
                        EventConstants.CTA_TEXT to model.data.actionTwoText.orEmpty(),
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        return holder
    }

    class WidgetHolder(binding: WidgetReferralBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetReferralBinding>(binding, widget)

    companion object {
        const val TAG = "ReferralWidget"
        const val EVENT_TAG = "referral_widget"
    }
}

@Keep
class ReferralWidgetModel :
    WidgetEntityModel<ReferralWidgetData, WidgetAction>()

@Keep
data class ReferralWidgetData(
    @SerializedName("background_color", alternate = ["bg_color"])
    var backgroundColor: String?,
    @SerializedName("deeplink")
    val deepLink: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("title_text_size")
    val titleTextSize: String?,
    @SerializedName("title_text_color")
    val titleTextColor: String?,

    @SerializedName("action_one_text")
    val actionOneText: String?,
    @SerializedName("action_one_text_size")
    val actionOneTextSize: String?,
    @SerializedName("action_one_text_color")
    val actionOneTextColor: String?,
    @SerializedName("action_one_bg_color")
    val actionOneBgColor: String?,
    @SerializedName("action_one_icon")
    val actionOneIcon: String?,
    @SerializedName("action_one_icon_gravity")
    val actionOneIconGravity: String?,
    @SerializedName("action_one_icon_size")
    val actionOneIconSize: String?,
    @SerializedName("action_one_icon_color")
    val actionOneIconColor: String?,
    @SerializedName("action_one_deeplink")
    val actionOneDeeplink: String?,
    @SerializedName("action_one_share_data")
    val actionOneShareData: BranchShareData?,

    @SerializedName("action_two_text")
    val actionTwoText: String?,
    @SerializedName("action_two_text_size")
    val actionTwoTextSize: String?,
    @SerializedName("action_two_text_color")
    val actionTwoTextColor: String?,
    @SerializedName("action_two_bg_color")
    val actionTwoBgColor: String?,
    @SerializedName("action_two_icon")
    val actionTwoIcon: String?,
    @SerializedName("action_two_icon_gravity")
    val actionTwoIconGravity: String?,
    @SerializedName("action_two_icon_size")
    val actionTwoIconSize: String?,
    @SerializedName("action_two_icon_color")
    val actionTwoIconColor: String?,
    @SerializedName("action_two_deeplink")
    val actionTwoDeeplink: String?,
    @SerializedName("action_two_share_data")
    val actionTwoShareData: BranchShareData?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>? = null
) : WidgetData()

