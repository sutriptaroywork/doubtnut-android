package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnCourseCarouselChildWidgetItmeClicked
import com.doubtnutapp.databinding.ItemCourseContentTwBinding
import com.doubtnutapp.databinding.WidgetCourseBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseWidget(context: Context) : BaseBindingWidget<CourseWidget.WidgetHolder,
        CourseWidget.CourseWidgetModel, WidgetCourseBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "CourseWidget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun getViewBinding(): WidgetCourseBinding {
        return WidgetCourseBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CourseWidgetModel): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(
                    model.layoutConfig?.marginTop ?: 0,
                    model.layoutConfig?.marginBottom ?: 0, model.layoutConfig?.marginLeft ?: 0,
                    model.layoutConfig?.marginRight ?: 0
                )
            }
        )
        val item: CourseWidgetData = model.data
        val binding = holder.binding
        if (item.setWidth == true) {
            Utils.setWidthBasedOnPercentage(
                binding.root.context,
                holder.itemView,
                "1.3",
                R.dimen.spacing
            )
        }

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.POPULAR_COURSE_VIEW,
                hashMapOf<String, Any>(
                    EventConstants.ASSORTMENT_ID to item.id.orEmpty(),
                    EventConstants.WIDGET to TAG
                ).apply {
                    putAll(model.extraParams ?: HashMap())
                }
            )
        )

        if (item.iconUrl.isNullOrBlank()) {
            binding.imageViewIcon.hide()
        } else {
            binding.imageViewIcon.show()
            binding.imageViewIcon.loadImageEtx(item.iconUrl.orEmpty())
        }

        binding.cardContainer.loadBackgroundImage(item.imageBg.orEmpty(), R.color.blue)
        binding.textViewTitle.setTextColor(
            Utils.parseColor(
                item.titleTopColor,
                R.color.black
            )
        )
        binding.textViewTitleOne.setTextColor(
            Utils.parseColor(
                item.titleOneColor,
                R.color.white
            )
        )
        binding.textViewTitle.text = item.titleTop.orEmpty()
        binding.textViewTitleOne.text = item.title.orEmpty()

        when (item.lockState) {
            1 -> {
                binding.imageViewLock.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_tag_light_locked
                    )
                )
                binding.tvContinueStudying.visibility = View.GONE
            }
            2 -> {
                binding.imageViewLock.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.itemView.context,
                        R.drawable.ic_tag_light_unlocked
                    )
                )
                binding.tvContinueStudying.visibility = View.VISIBLE
                binding.tvContinueStudying.text = item.continueStudyingText
                binding.tvContinueStudying.setTextColor(Utils.parseColor(item.continueStudyingColor))
                binding.tvContinueStudying.textSize = item.continueStudyingSize?.toFloat() ?: 12f
            }
            else -> {
                binding.imageViewLock.setImageDrawable(null)
                binding.tvContinueStudying.visibility = View.GONE
            }
        }

        if (item.buttonState == "payment") {
            binding.layoutPaymentInfo.show()
            if (item.isPremium == true) {
                binding.layoutPaymentInfo.setBackgroundColor(Utils.parseColor(item.bottomLeftBgColor))
            } else {
                binding.layoutPaymentInfo.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorTransparent
                    )
                )
            }
            binding.textViewBottom.hide()
            binding.textViewAmountToPay.text = item.amountToPay.orEmpty()
            binding.textViewAmountToPay.textSize = item.bottomLeftTextSizeTwo?.toFloat()
                ?: 16f
            binding.textViewAmountToPay.setTextColor(Utils.parseColor(item.bottomLeftTextColorTwo))
            binding.textViewAmountStrikeThrough.text = item.amountStrikeThrough.orEmpty()
            binding.textViewAmountStrikeThrough.setTextColor(Utils.parseColor(item.strikethroughTextColor))
            binding.textViewAmountStrikeThrough.textSize =
                item.strikethroughTextSize?.toFloat()
                    ?: 10f
            if (item.isPremium == true) {
                binding.textViewBuy.setBackgroundColor(Utils.parseColor(item.bottomRightBgColor))
            } else {
                binding.textViewBuy.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorTransparent
                    )
                )
            }
            binding.textViewBuy.setTextColor(Utils.parseColor(item.bottomRightTextColor))
            binding.textViewBuy.textSize = item.bottomRightTextSize?.toFloat() ?: 16f
            binding.textViewDiscount.text = item.discount.orEmpty()
            binding.textViewDiscount.setTextColor(Utils.parseColor(item.discountTextColor))
            binding.textViewDiscount.textSize = item.discountTextSize?.toFloat() ?: 11f
            binding.textViewBuy.text = item.buyText.orEmpty()
            binding.textViewBuy.setOnClickListener {
                val event = AnalyticsEvent(
                    EventConstants.POPULAR_COURSE_BUTTON_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.ASSORTMENT_ID to item.id.orEmpty(),
                        EventConstants.WIDGET to TAG,
                        EventConstants.CLICKED_BUTTON_NAME to item.buyText.toString()
                    ).apply {
                        putAll(model.extraParams ?: HashMap())
                    }, ignoreMoengage = false
                )
                analyticsPublisher.publishEvent(event)
                val countToSendEvent: Int = Utils.getCountToSend(
                    RemoteConfigUtils.getEventInfo(),
                    EventConstants.POPULAR_COURSE_BUTTON_CLICK
                )

                MoEngageUtils.setUserAttribute(context, "dn_bnb_clicked",true)

                val eventCopy = event.copy()
                repeat((0 until countToSendEvent).count()) {
                    analyticsPublisher.publishBranchIoEvent(eventCopy)
                }
                deeplinkAction.performAction(context, item.buyDeeplink)
            }
            binding.textViewBottom.text = ""
            if (item.discount.isNullOrEmpty() && item.amountStrikeThrough.isNullOrEmpty()) {
                binding.tvStartingAt.show()
                binding.tvStartingAt.text = item.startingAtText
                binding.tvStartingAt.textSize = item.bottomLeftTextSizeOne?.toFloat() ?: 11f
                binding.tvStartingAt.setTextColor(Utils.parseColor(item.bottomLeftTextColorOne))
            } else {
                val containerParams =
                    binding.textViewAmountToPay.layoutParams as ConstraintLayout.LayoutParams
                containerParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                binding.textViewAmountToPay.layoutParams = containerParams
                binding.textViewAmountToPay.setMargins(6, 10, 2, 0)
                binding.tvStartingAt.hide()
            }
        } else {
            binding.layoutPaymentInfo.hide()
            binding.textViewBottom.show()
            binding.textViewAmountToPay.text = ""
            binding.textViewAmountStrikeThrough.text = ""
            binding.textViewDiscount.text = ""
            binding.textViewBuy.text = ""
            binding.textViewBottom.text = ""
        }

        binding.textViewAmountStrikeThrough.paintFlags =
            binding.textViewAmountStrikeThrough.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        binding.root.setOnClickListener {
            actionPerformer?.performAction(
                OnCourseCarouselChildWidgetItmeClicked(
                    item.title.orEmpty(),
                    item.id.orEmpty(),
                    -1,
                    source.orEmpty()
                )
            )
            deeplinkAction.performAction(binding.root.context, item.deeplink, source.orEmpty())
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.POPULAR_COURSE_BANNER_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.ASSORTMENT_ID to item.id.orEmpty(),
                        EventConstants.WIDGET to TAG
                    ).apply {
                        putAll(model.extraParams ?: HashMap())
                    }
                )
            )
        }

        binding.bestSellerIv.loadImageEtx(item.tagUrl.orEmpty())
        if (item.rating.isNullOrEmpty()) {
            binding.ratingBar.visibility = View.GONE
            binding.ratingTv.visibility = View.GONE
        } else {
            binding.ratingBar.visibility = View.VISIBLE
            // binding.ratingBar.rating = item.rating.toFloat()
            // binding.ratingTv.text = item.rating
        }
        binding.rvResources.adapter = ResourceAdapter(item.resources, analyticsPublisher)
        binding.rvResources.layoutManager = LinearLayoutManager(
            binding.root.context, RecyclerView.HORIZONTAL,
            false
        )
        binding.rvResources.isLayoutFrozen = true
        return holder
    }

    class ResourceAdapter(
        val items: List<CourseResources?>,
        val analyticsPublisher: AnalyticsPublisher
    ) : RecyclerView.Adapter<ResourceAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemCourseContentTwBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]

            holder.binding.imageCourseContent.loadImageEtx(data?.iconUrl.orEmpty())
            holder.binding.tvCourseContent.text = data?.text.orEmpty()
            holder.binding.tvCourseContent.setTextColor(Color.parseColor(data?.textColor))
        }

        class ViewHolder(val binding: ItemCourseContentTwBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun getItemCount(): Int = items.size
    }

    class WidgetHolder(binding: WidgetCourseBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseBinding>(binding, widget)

    class CourseWidgetModel : WidgetEntityModel<CourseWidgetData, WidgetAction>()

    @Keep
    data class CourseWidgetData(
        @SerializedName("id") val id: String?,
        @SerializedName("image_bg") val imageBg: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("icon_url") val iconUrl: String?,
        @SerializedName("title_top") val titleTop: String?,
        @SerializedName("title_top_color") val titleTopColor: String?,
        @SerializedName("title_one_color") val titleOneColor: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("buy_deeplink") val buyDeeplink: String?,
        @SerializedName("button_state") val buttonState: String?,
        @SerializedName("multiple_package") val multiplePackage: Boolean?,
        @SerializedName("amount_to_pay") val amountToPay: String?,
        @SerializedName("amount_strike_through") val amountStrikeThrough: String?,
        @SerializedName("discount") val discount: String?,
        @SerializedName("buy_text") val buyText: String?,
        @SerializedName("rating") val rating: String?,
        @SerializedName("tag") val tagUrl: String?,
        @SerializedName("set_width") val setWidth: Boolean?,
        @SerializedName("lock_state") val lockState: Int?,
        @SerializedName("resources") val resources: List<CourseResources>,
        @SerializedName("bottom_right_bg_color") val bottomRightBgColor: String?,
        @SerializedName("bottom_right_text_color") val bottomRightTextColor: String?,
        @SerializedName("bottom_right_text_size") val bottomRightTextSize: Int?,
        @SerializedName("bottom_left_bg_color") val bottomLeftBgColor: String?,
        @SerializedName("bottom_left_text1_color") val bottomLeftTextColorOne: String?,
        @SerializedName("bottom_left_text1_size") val bottomLeftTextSizeOne: Int?,
        @SerializedName("bottom_left_text2_color") val bottomLeftTextColorTwo: String?,
        @SerializedName("bottom_left_text2_size") val bottomLeftTextSizeTwo: Int?,
        @SerializedName("strikethrough_text_color") val strikethroughTextColor: String?,
        @SerializedName("strikethrough_text_size") val strikethroughTextSize: Int?,
        @SerializedName("discount_color") val discountTextColor: String?,
        @SerializedName("discount_text_size") val discountTextSize: Int?,
        @SerializedName("starting_at_text") val startingAtText: String?,
        @SerializedName("is_premium") val isPremium: Boolean?,
        @SerializedName("bottom_payment_text") val continueStudyingText: String?,
        @SerializedName("bottom_payment_text_color") val continueStudyingColor: String?,
        @SerializedName("bottom_payment_text_size") val continueStudyingSize: Int?
    ) : WidgetData()

    @Keep
    data class CourseResources(
        @SerializedName("count") val count: Int?,
        @SerializedName("icon_url") val iconUrl: String?,
        @SerializedName("text") val text: String?,
        @SerializedName("text_color") val textColor: String?
    )
}
