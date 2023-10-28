package com.doubtnutapp.course.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.Keep
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.scholarship.event.RegisterTestEvent
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.ActivateVipTrial
import com.doubtnutapp.base.Dismiss
import com.doubtnutapp.databinding.WidgetButtonBorderBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.revisioncorner.ui.RcHomeFragment
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.doubtnutapp.widgets.GlideApp
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ButtonBorderWidget @JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<ButtonBorderWidget.WidgetHolder, ButtonBorderWidgetModel, WidgetButtonBorderBinding>(
    context
) {
    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetButtonBorderBinding {
        return WidgetButtonBorderBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ButtonBorderWidgetModel
    ): WidgetHolder {
        if (model.layoutConfig == null) {
            if (source == CourseBottomSheetFragment.TAG) {
                // removing bottom space
                model.layoutConfig = WidgetLayoutConfig(
                    marginTop = 10,
                    marginBottom = 0,
                    marginLeft = 20,
                    marginRight = 20,
                )
            } else {
                model.layoutConfig = WidgetLayoutConfig(
                    marginLeft = 20,
                    marginRight = 20,
                )
            }
        }

        super.bindWidget(holder, model)

        val binding = holder.binding
        if (model.data.wrapWidth == true) {
            binding.root.layoutParams =
                LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
        } else {
            binding.root.layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        gravity = model.data.gravity ?: Gravity.START

        holder.itemView.setWidthFromScrollSize(model.data.cardWidth)

        binding.cvMain.applyBackgroundColor(model.data.bgColor)
        binding.cvMain.applyStrokeColor(model.data.bgStrokeColor)

        binding.btnMain.text = model.data.textOne
        binding.btnMain.applyTextSize(model.data.textOneSize)
        binding.btnMain.applyTextColor(model.data.textOneColor)

        binding.cvMain.radius = model.data.cornerRadius?.toFloatOrNull()?.dpToPx() ?: 4f.dpToPx()
        binding.cvMain.elevation = model.data.elevation?.toFloatOrNull() ?: 0f
        binding.btnMain.minHeight = model.data.minHeight?.toIntOrNull()?.dpToPx() ?: 48.dpToPx()

        model.data.horizontalMargin?.dpToPx()?.let {
            binding.cvMain.updateMargins(start = it, end = it)
        }

        binding.btnMain.applyBackgroundColor(model.data.bgColor)
        binding.btnMain.applyStrokeColor(model.data.bgStrokeColor)
        if (model.data.icon.isNullOrEmpty()) {
            binding.btnMain.icon = null
        } else {
            GlideApp.with(context)
                .load(model.data.icon)
                .into(
                    object : CustomViewTarget<TextView, Drawable>(binding.btnMain) {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            binding.btnMain.icon = resource
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            binding.btnMain.icon = null
                        }

                        override fun onResourceCleared(placeholder: Drawable?) {
                            binding.btnMain.icon = null
                        }
                    }
                )
            binding.btnMain.iconSize = model.data.iconSize?.toIntOrNull()?.dpToPx() ?: 0
            binding.btnMain.iconGravity =
                model.data.iconGravity?.toIntOrNull() ?: MaterialButton.ICON_GRAVITY_START
            binding.btnMain.applyIconColor(model.data.iconColor)
        }

        if (model.data.rippleColor.isNullOrEmpty()) {
            binding.btnMain.rippleColor = ColorStateList.valueOf(
                MaterialColors.getColor(binding.root, R.attr.colorControlHighlight)
            )
        } else {
            binding.btnMain.applyRippleColor(model.data.rippleColor)
        }

        binding.btnMain.setOnClickListener {
            val eventName =
                if (source == CourseBottomSheetFragment.TAG && model.data.isTrialButton == true) {
                    if (CourseBottomSheetFragment.isLoadedViaExploreFragment) {
                        EventConstants.EXPLORE_PAGE_STRIP_PREVIEW_TRIAL_CLICKED
                    } else {
                        EventConstants.MPVP_COURSE_BOTTOMSHEET_TRIAL_CLICKED
                    }
                } else if (source == RcHomeFragment.TAG) {
                    EventConstants.RC_FULL_TEST_CLICK
                } else {
                    MoEngageUtils.setUserAttribute(context, "dn_bnb_clicked",true)
                    EventConstants.BORDER_BUTTON_CLICKED
                }

            val event = AnalyticsEvent(
                eventName,
                hashMapOf<String, Any>(
                    EventConstants.WIDGET to TAG,
                    EventConstants.CTA_TEXT to model.data.textOne.orEmpty(),
                    EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                    EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                    EventConstants.FLAG_ID to model.data.flagrId.orEmpty(),
                    EventConstants.VARIANT_ID to model.data.variantId.orEmpty(),
                    EventConstants.SOURCE to source.orEmpty(),
                ).apply {
                    putAll(model.extraParams.orEmpty())
                },
                ignoreSnowplow = eventName == EventConstants.RC_FULL_TEST_CLICK,
                ignoreBranch = eventName != EventConstants.BORDER_BUTTON_CLICKED,
                ignoreMoengage = false
            )

            analyticsPublisher.publishEvent(event)

            if (eventName == EventConstants.MPVP_COURSE_BOTTOMSHEET_TRIAL_CLICKED) {
                val countToSendEvent: Int = Utils.getCountToSend(
                    RemoteConfigUtils.getEventInfo(),
                    EventConstants.MPVP_COURSE_BOTTOMSHEET_TRIAL_CLICKED
                )
                val eventCopy = event.copy()
                repeat((0 until countToSendEvent).count()) {
                    analyticsPublisher.publishBranchIoEvent(eventCopy)
                }
            }


            if (!model.data.deepLink.isNullOrEmpty()) {
                deeplinkAction.performAction(context, model.data.deepLink)
            } else if (model.data.isRegisterTestBtn == true) {
                DoubtnutApp.INSTANCE.bus()?.send(RegisterTestEvent(model.data.testId.orEmpty()))
            } else {
                actionPerformer?.performAction(ActivateVipTrial(model.data.assortmentId.orEmpty()))
            }
            actionPerformer?.performAction(Dismiss())
        }
        return holder
    }

    class WidgetHolder(binding: WidgetButtonBorderBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetButtonBorderBinding>(binding, widget)

    companion object {
        const val TAG = "ButtonBorderWidget"
    }
}

@Keep
class ButtonBorderWidgetModel :
    WidgetEntityModel<ButtonBorderWidgetData, WidgetAction>()

@Keep
data class ButtonBorderWidgetData(
    @SerializedName("bg_color") val bgColor: String?,
    @SerializedName("bg_stroke_color") val bgStrokeColor: String?,
    @SerializedName("text_one") val textOne: String?,
    @SerializedName("text_one_size") val textOneSize: String?,
    @SerializedName("text_one_color") val textOneColor: String?,
    @SerializedName("deep_link", alternate = ["deeplink"]) val deepLink: String?,
    @SerializedName("is_trial_btn") val isTrialButton: Boolean?,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("card_width") val cardWidth: String?,
    @SerializedName("horizontal_margin") val horizontalMargin: Int?,
    @SerializedName("is_offer_btn") val isOfferBtn: Boolean?,
    @SerializedName("test_id") val testId: String?,
    @SerializedName("register_test_btn") val isRegisterTestBtn: Boolean?,
    @SerializedName("corner_radius") val cornerRadius: String?,
    @SerializedName("elevation")
    val elevation: String?,
    @SerializedName("min_height")
    val minHeight: String?,

    @SerializedName("ripple_color")
    val rippleColor: String?,

    @SerializedName("icon")
    val icon: String?,
    @SerializedName("icon_size")
    val iconSize: String?,
    @SerializedName("icon_gravity")
    val iconGravity: String?,
    @SerializedName("icon_color")
    val iconColor: String?,

    @SerializedName("wrap_width")
    val wrapWidth: Boolean?,
    @SerializedName("gravity")
    val gravity: Int?,
    var flagrId: String?,
    var variantId: String?
) : WidgetData()
