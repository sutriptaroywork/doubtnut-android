package com.doubtnutapp.videoPage.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetSrpNudgeCourseBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.loadImage
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject


/**
 * Created by Sachin Saxena on 1/20/22.
 */

class SrpNudgeCourseWidget(context: Context) :
    BaseBindingWidget<SrpNudgeCourseWidget.WidgetHolder,
            SrpNudgeCourseWidget.Model, WidgetSrpNudgeCourseBinding>(context) {

    companion object {
        private const val TAG = "SrpNudgeCourseWidget"
        private const val EVENT_TAG = "nudge_course_widget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = ""

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model)

        val data = model.data
        val binding = holder.binding

        val width = Utils.getWidthFromScrollSize(
            context = holder.itemView.context,
            scrollSize = data.cardWidth
        ) - (binding.cardView.marginStart + binding.cardView.marginEnd)
        binding.cardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            this.width = width
            dimensionRatio = data.cardRatio ?: "19:10"
        }
        requestLayout()

        binding.apply {
            tvHeading1.apply {
                isVisible = data.heading1 != null
                data.heading1?.let { heading1 ->
                    text = heading1.title
                    setTextColor(Utils.parseColor(heading1.titleColor))
                    background = Utils.getShape(
                        colorString = heading1.backgroundColor.orEmpty(),
                        strokeColor = heading1.backgroundColor.orEmpty(),
                        cornerRadius = heading1.cornerRadius ?: 4F
                    )
                }
            }

            tvHeading2.apply {
                isVisible = data.heading2 != null
                data.heading2?.let { heading2 ->
                    text = heading2.title
                    setTextColor(Utils.parseColor(heading2.titleColor))
                    background = Utils.getShape(
                        colorString = heading2.backgroundColor.orEmpty(),
                        strokeColor = heading2.backgroundColor.orEmpty(),
                        cornerRadius = heading2.cornerRadius ?: 4F
                    )
                }
            }

            tvDescription.apply {
                isVisible = data.description.isNotNullAndNotEmpty()
                text = data.description
            }

            tvPrice.apply {
                isVisible = data.price.isNotNullAndNotEmpty()
                text = data.price
            }

            btCta.apply {
                isVisible = data.cta != null
                data.cta?.let { cta ->
                    text = cta.title
                    setTextColor(Utils.parseColor(cta.titleColor))
                    background = Utils.getShape(
                        colorString = cta.backgroundColor.orEmpty(),
                        strokeColor = cta.backgroundColor.orEmpty(),
                        cornerRadius = cta.cornerRadius ?: 4F
                    )
                }
            }

            ivBottomText.apply {
                isVisible = data.bottomImage.isNotNullAndNotEmpty()
                loadImage(data.bottomImage)
            }

            tvBottomText.apply {
                isVisible = data.bottomText.isNotNullAndNotEmpty()
                text = data.bottomText
            }

            rightHalfLayout.apply {
                data.rightHalf?.background?.let { backgroundImageUrl ->
                    Glide.with(context)
                        .load(backgroundImageUrl)
                        .into(object : CustomTarget<Drawable?>() {
                            override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                            override fun onResourceReady(
                                resource: Drawable, transition: Transition<in Drawable?>?
                            ) {
                                background = resource
                            }
                        })
                }
            }

            tvSubject.apply {
                isVisible = data.rightHalf?.title.isNotNullAndNotEmpty()
                text = data.rightHalf?.title
            }

            tvSubjectMedium.apply {
                isVisible = data.rightHalf?.subtitle.isNotNullAndNotEmpty()
                text = data.rightHalf?.subtitle
            }

            tvCourseId.apply {
                isVisible = data.rightHalf?.bottomText.isNotNullAndNotEmpty()
                text = data.rightHalf?.bottomText
            }

            setOnClickListener {
                val deeplink = data.deeplink ?: return@setOnClickListener
                deeplinkAction.performAction(context, deeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        "${EVENT_TAG}_${EventConstants.WIDGET_ITEM_CLICK}",
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to TAG,
                            EventConstants.ID to data.id.orEmpty(),
                            EventConstants.SOURCE to source.orEmpty(),
                        ).apply {
                            putAll(model.extraParams.orEmpty())
                        }, ignoreBranch = false
                    )
                )
                MoEngageUtils.setUserAttribute(context, "dn_bnb_clicked",true)

            }
        }

        trackingViewId = data.id
        return holder
    }

    class WidgetHolder(
        binding: WidgetSrpNudgeCourseBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetSrpNudgeCourseBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String?,
        @SerializedName("heading1") val heading1: Heading?,
        @SerializedName("heading2") val heading2: Heading?,
        @SerializedName("description") val description: String?,
        @SerializedName("price") val price: String?,
        @SerializedName("bottom_text") val bottomText: String?,
        @SerializedName("bottom_image") val bottomImage: String?,
        @SerializedName("right_half") val rightHalf: RightHalf?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("card_width") val cardWidth: String,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("cta") val cta: Cta?
    ) : WidgetData()

    @Keep
    data class Heading(
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("corner_radius") val cornerRadius: Float?
    )

    @Keep
    data class Cta(
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("corner_radius") val cornerRadius: Float?,
    )

    @Keep
    data class RightHalf(
        @SerializedName("title") val title: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("bottom_text") val bottomText: String?,
        @SerializedName("background") val background: String?,
    )

    override fun getViewBinding(): WidgetSrpNudgeCourseBinding {
        return WidgetSrpNudgeCourseBinding.inflate(LayoutInflater.from(context), this, true)
    }
}
