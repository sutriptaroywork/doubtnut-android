package com.doubtnutapp.sales

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetPrePurchaseCallingCard2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.sales.data.remote.PrePurchaseCallingCardRepository
import com.doubtnutapp.sales.event.PrePurchaseCallingCardDismiss
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import javax.inject.Inject

class PrePurchaseCallingCard2
@JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<PrePurchaseCallingCard2.WidgetHolder, PrePurchaseCallingCardModel2,
        WidgetPrePurchaseCallingCard2Binding>(context, attrs, defStyle) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var repository: PrePurchaseCallingCardRepository

    class WidgetHolder(binding: WidgetPrePurchaseCallingCard2Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetPrePurchaseCallingCard2Binding>(binding, widget)

    companion object {
        @Suppress("unused")
        private const val TAG = "PrePurchaseCallingCard2"

        private const val VIEW_CLOSE = "close"
        private const val VIEW_CARD = "card"
        private const val VIEW_CALL = "call"

        var isShownOnCheckAllCourses = false

        fun titleTextSize() = defaultPrefs().getString(
            Constants.CALLING_CARD_TITLE_TEXT_SIZE,
            ""
        )

        fun titleTextColor() = defaultPrefs().getString(
            Constants.CALLING_CARD_TITLE_TEXT_COLOR,
            ""
        )

        fun subtitleTextSize() = defaultPrefs().getString(
            Constants.CALLING_CARD_SUBTITLE_TEXT_SIZE,
            ""
        )

        fun subtitleTextColor() = defaultPrefs().getString(
            Constants.CALLING_CARD_SUBTITLE_TEXT_COLOR,
            ""
        )

        fun action() = defaultPrefs().getString(
            Constants.CALLING_CARD_ACTION,
            ""
        )

        fun actionTextSize() = defaultPrefs().getString(
            Constants.CALLING_CARD_ACTION_TEXT_SIZE,
            ""
        )

        fun actionTextColor() = defaultPrefs().getString(
            Constants.CALLING_CARD_ACTION_TEXT_COLOR,
            ""
        )

        fun actionImageUrl() = defaultPrefs().getString(
            Constants.CALLING_CARD_ACTION_IMAGE_URL,
            ""
        )

        fun imageUrl() = defaultPrefs().getString(
            Constants.CALLING_CARD_IMAGE_URL,
            ""
        )
    }

    override fun getViewBinding(): WidgetPrePurchaseCallingCard2Binding {
        return WidgetPrePurchaseCallingCard2Binding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: PrePurchaseCallingCardModel2
    ): WidgetHolder {
        if (model.layoutConfig == null) {
            model.layoutConfig = WidgetLayoutConfig(
                marginTop = 16,
                marginBottom = 0,
                marginLeft = 16,
                marginRight = 16
            )
        }
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.root.applyBackgroundColor(model.data.backgroundColor)
        if (model.data.source == PrePurchaseCallingCardConstant.SOURCE_COURSE_CATEGORY_BOTTOM_SHEET) {
            binding.root.cardElevation = 0f
            binding.root.useCompatPadding = false
        } else {
            binding.root.cardElevation = 4.dpToPxFloat()
            binding.root.useCompatPadding = true
        }

        binding.tvTitle.text = model.data.title
        binding.tvTitle.isVisible = model.data.title.isNotNullAndNotEmpty()
        binding.tvTitle.applyTextColor(model.data.titleTextColor)
        binding.tvTitle.applyAutoSizeTextTypeUniformWithConfiguration(model.data.titleTextSize)

        binding.tvSubtitle.isVisible = model.data.subtitle.isNotNullAndNotEmpty()
        binding.tvSubtitle.text = model.data.subtitle
        binding.tvSubtitle.applyTextColor(model.data.subtitleTextColor)
        binding.tvSubtitle.applyAutoSizeTextTypeUniformWithConfiguration(model.data.subtitleTextSize)

        if (model.data.imageUrl.isNullOrEmpty()) {
            binding.ivImage.hide()
        } else {
            binding.ivImage.show()
            binding.ivImage.loadImage(model.data.imageUrl)
        }

        binding.ivClose.isVisible = model.data.source != Constants.SOURCE_PAYMENT_FAILURE
                && model.data.source != PrePurchaseCallingCardConstant.SOURCE_COURSE_CATEGORY_BOTTOM_SHEET

        binding.ivClose.setOnClickListener {
            DoubtnutApp.INSTANCE.bus()?.send(PrePurchaseCallingCardDismiss())
            dismissPrePurchaseCallingCard(model.data, VIEW_CLOSE)
        }

        holder.itemView.setOnClickListener {
            if (model.data.deeplink.isNullOrEmpty().not()) {
                deeplinkAction.performAction(context, model.data.deeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        name = EventConstants.CALLING_CARD_CLICK,
                        ignoreFacebook = true,
                        params = hashMapOf(
                            EventConstants.SOURCE to model.data.source.orEmpty(),
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                        ),
                        ignoreSnowplow = true
                    )
                )
            }
            dismissPrePurchaseCallingCard(model.data, VIEW_CARD)
        }

        binding.clAction.isVisible = model.data.actionText.isNotNullAndNotEmpty()
        binding.tvAction.text = model.data.actionText
        binding.tvAction.applyTextColor(model.data.actionTextColor)
        binding.tvAction.applyTextSize(model.data.actionTextSize)

        binding.clAction.applyBackgroundTint(model.data.actionTextColor)

        if (model.data.actionImageUrl.isNullOrEmpty()) {
            binding.ivAction.hide()
        } else {
            binding.ivAction.show()
            binding.ivAction.loadImage(model.data.actionImageUrl)
        }

        binding.clAction.setOnClickListener {
            deeplinkAction.performAction(context, model.data.actionDeepLink)
            dismissPrePurchaseCallingCard(model.data, VIEW_CALL)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CARD_CALL_CLICK,
                    hashMapOf(
                        EventConstants.SOURCE to model.data.source.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                    ), ignoreBranch = false
                )
            )
            DoubtnutApp.INSTANCE.bus()?.send(PrePurchaseCallingCardDismiss())
        }
        return holder
    }

    private fun dismissPrePurchaseCallingCard(data: PrePurchaseCallingCardData2, view: String) {
        CoreApplication.INSTANCE.launch {
            repository.dismissPrePurchaseCallingCard(
                assortmentId = data.assortmentId,
                view = view,
                source = data.source
            )
                .collectSafely { }
        }
    }
}

@Parcelize
@Keep
class PrePurchaseCallingCardModel2 :
    WidgetEntityModel<PrePurchaseCallingCardData2, WidgetAction>(), Parcelable

@Parcelize
@Keep
data class PrePurchaseCallingCardData2(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("title_text_size")
    val titleTextSize: String? = null,
    @SerializedName("title_text_color")
    val titleTextColor: String? = null,

    @SerializedName("subtitle")
    val subtitle: String? = null,
    @SerializedName("subtitle_text_size")
    val subtitleTextSize: String? = null,
    @SerializedName("subtitle_text_color")
    val subtitleTextColor: String? = null,

    @SerializedName("action")
    val actionText: String? = null,
    @SerializedName("action_text_size")
    val actionTextSize: String? = null,
    @SerializedName("action_text_color")
    val actionTextColor: String? = null,
    @SerializedName("action_image_url")
    val actionImageUrl: String? = null,
    @SerializedName("action_deep_link", alternate = ["action_deeplink"])
    val actionDeepLink: String? = null,

    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("background_color")
    val backgroundColor: String? = null,
    @SerializedName("deeplink")
    val deeplink: String? = null,
    @SerializedName("assortment_id")
    val assortmentId: String? = null,

    var source: String? = null,
) : WidgetData(), Parcelable
