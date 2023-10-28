package com.doubtnut.referral.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.actions.OnReferNowCtaClicked
import com.doubtnut.core.analytics.IAnalyticsPublisher
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
import com.doubtnut.referral.R
import com.doubtnut.referral.databinding.WidgetReferralHeaderBinding
import com.doubtnut.referral.ui.ReferAndEarnHomeFragment
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ReferAndEarnHeaderWidget constructor(context: Context) :
    CoreBindingWidget<ReferAndEarnHeaderWidget.ReferAndEarnHeaderWidgetHolder,
            ReferAndEarnHeaderWidget.WidgetModel, WidgetReferralHeaderBinding>(context) {

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    companion object {
        const val EVENT_TAG_REFER_NOW_CLICKED = "ReferQA_refer_friend_click"
    }

    override fun getViewBinding(): WidgetReferralHeaderBinding {
        return WidgetReferralHeaderBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = ReferAndEarnHeaderWidgetHolder(getViewBinding(), this)
        CoreApplication.INSTANCE.androidInjector().inject(this)
    }

    override fun bindWidget(
        holder: ReferAndEarnHeaderWidgetHolder,
        model: WidgetModel
    ): ReferAndEarnHeaderWidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val binding = holder.binding
        val data = model.data

        binding.imageViewReferHeading.loadImage2(data.imageTopUrl)
        TextViewUtils.setTextFromHtml(binding.tvTitle, data.title.orEmpty())
        binding.tvTitle.applyTextGravity(data.titleTextGravity)
        binding.tvTitle.applyTextSize(data.titleTextSize)
        TextViewUtils.setTextFromHtml(binding.tvSubtitle, data.subtitle.orEmpty())
        binding.tvSubtitle.applyTextSize(data.subtitleTextSize)
        binding.tvSubtitle.applyTextGravity(data.subtitleTextGravity)
        if (data.textTermsAndConditions.isNotNullAndNotEmpty2()) {
            TextViewUtils.setTextFromHtml(
                binding.tvTermsAndConditions,
                data.textTermsAndConditions.orEmpty()
            )
        } else {
            binding.tvTermsAndConditions.visibility = View.GONE
        }
        binding.topContainer.applyBackgroundColor(data.bgColor)
        TextViewUtils.setTextFromHtml(
            binding.tvTermsAndConditions,
            data.textTermsAndConditions.orEmpty()
        )
        if (data.cta != null && data.cta.title.isNotNullAndNotEmpty2()) {
            binding.buttonCta.visibility = View.VISIBLE
            binding.tvTitleCta.text = data.cta.title.orEmpty()
            getBackgroundForCta(context, binding)

        } else {
            binding.buttonCta.visibility = View.GONE
        }

        binding.tvTermsAndConditions.setOnClickListener {
            deeplinkAction.performAction(context, data.termsAndConditionsDeeplink)
        }

        binding.buttonCta.setOnClickListener {
            actionPerformer?.performAction(
                OnReferNowCtaClicked()
            )

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EVENT_TAG_REFER_NOW_CLICKED,
                    hashMapOf("source" to "")
                )
            )
        }


        return holder

    }

    private fun getBackgroundForCta(context: Context, binding: WidgetReferralHeaderBinding) {
        val radius = resources.getDimension(R.dimen.dimen_150dp)
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, radius)
            .build()
        val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        materialShapeDrawable.fillColor =
            ContextCompat.getColorStateList(context, R.color.orange_light_faq)
        materialShapeDrawable.strokeWidth = 1.5f
        materialShapeDrawable.strokeColor =
            ContextCompat.getColorStateList(context, R.color.colorPrimary)
        binding.buttonCta.background = materialShapeDrawable
    }

    @Keep
    class WidgetModel : WidgetEntityModel<ReferAndEarnHeaderWidgetData, WidgetAction>()

    class ReferAndEarnHeaderWidgetHolder(
        binding: WidgetReferralHeaderBinding,
        widget: CoreWidget<*, *>
    ) :
        WidgetBindingVH<WidgetReferralHeaderBinding>(binding, widget)

    @Keep
    data class ReferAndEarnHeaderWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("title_text_size") val titleTextSize: String?,
        @SerializedName("title_text_gravity") val titleTextGravity: Float?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_text_size") val subtitleTextSize: String?,
        @SerializedName("subtitle_text_gravity") val subtitleTextGravity: Float?,
        @SerializedName("text_terms_and_conditions") val textTermsAndConditions: String?,
        @SerializedName("text_terms_and_conditions_deeplink") val termsAndConditionsDeeplink: String?,
        @SerializedName("bg_color") val bgColor: String?,
        @SerializedName("image_top") val imageTopUrl: String?,
        @SerializedName("cta") val cta: Cta?
    ) : WidgetData() {

        @Keep
        data class Cta(
            @SerializedName("title") val title: String?,
            @SerializedName("text_size") val textSize: String?
        )
    }

}