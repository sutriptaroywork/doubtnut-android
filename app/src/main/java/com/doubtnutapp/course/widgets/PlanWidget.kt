package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemPlanWidgetBinding
import com.doubtnutapp.databinding.WidgetCoursePlanBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class PlanWidget(context: Context) : BaseBindingWidget<PlanWidget.WidgetHolder,
        PlanWidgetModel, WidgetCoursePlanBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "PlanWidget"
        const val EVENT_TAG = "plan_widget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetCoursePlanBinding {
        return WidgetCoursePlanBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: PlanWidgetModel): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val binding = holder.binding
        val data: PlanWidgetData = model.data

        binding.containerRoot.background = GradientUtils.getGradientBackground(
            model.data.bgColorOne,
            model.data.bgColorTwo,
            GradientDrawable.Orientation.LEFT_RIGHT
        )

        binding.tvTitle.isVisible = model.data.title.isNotNullAndNotEmpty()
        TextViewUtils.setTextFromHtml(binding.tvTitle, model.data.title.orEmpty())
        binding.tvTitle.applyTextSize(model.data.titleSize)
        binding.tvTitle.applyTextColor(model.data.titleColor)

        binding.ivTimerImage.isVisible = model.data.imageUrlOne.isNotNullAndNotEmpty()
        binding.ivTimerImage.loadImage(model.data.imageUrlOne)

        binding.ivImage1.isVisible = model.data.imageUrlTwo.isNotNullAndNotEmpty()
        binding.ivImage1.loadImage(model.data.imageUrlTwo)

        val timeReceived = model.data.time
        if (timeReceived == null || timeReceived <= 0L) {
            binding.ivTimerImage.visibility = GONE
            binding.tvTimer.visibility = GONE
        } else {
            binding.tvTimer.visibility = VISIBLE
            binding.tvTimer.applyTextColor(model.data.timeTextColor)
            binding.tvTimer.applyTextSize(model.data.timeTextSize)

            val actualTimeLeft =
                ((timeReceived.or(0)).minus(System.currentTimeMillis()))

            if (actualTimeLeft > 0) {
                val timer = object : CountDownTimer(
                    actualTimeLeft,
                    1000
                ) {
                    override fun onTick(millisUntilFinished: Long) {
                        binding.tvTimer.text =
                            DateTimeUtils.formatMilliSecondsToTime(millisUntilFinished)
                    }

                    override fun onFinish() {
                        binding.containerRoot.background = GradientUtils.getGradientBackground(
                            model.data.bgColorOneExpired,
                            model.data.bgColorTwoExpired,
                            GradientDrawable.Orientation.LEFT_RIGHT
                        )

                        model.data.bgColorOne = model.data.bgColorOneExpired
                        model.data.bgColorTwo = model.data.bgColorTwoExpired
                    }
                }
                timer.start()
            } else {
                binding.tvTimer.text = DateTimeUtils.formatMilliSecondsToTime(0)

                binding.containerRoot.background = GradientUtils.getGradientBackground(
                    model.data.bgColorOneExpired,
                    model.data.bgColorTwoExpired,
                    GradientDrawable.Orientation.LEFT_RIGHT
                )

                model.data.bgColorOne = model.data.bgColorOneExpired
                model.data.bgColorTwo = model.data.bgColorTwoExpired
            }
        }

        if (model.data.bottomText.isNullOrEmpty()) {
            binding.viewBottomDash.visibility = GONE
            binding.tvBottom.visibility = GONE
        } else {
            binding.viewBottomDash.visibility = VISIBLE
            binding.tvBottom.visibility = VISIBLE

            binding.tvBottom.text = model.data.bottomText
            binding.tvBottom.applyTextColor(model.data.bottomTextColor)
            binding.tvBottom.applyTextSize(model.data.bottomTextSize)
        }

        binding.tvBottom.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.CTA_TEXT to model.data.bottomText.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
            deeplinkAction.performAction(binding.root.context, model.data.bottomDeeplink)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL, false
        )

        binding.recyclerView.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            deeplinkAction,
            model.extraParams ?: HashMap()
        )
        return holder
    }

    class Adapter(
        val items: List<PlanWidgetItems>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>,
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemPlanWidgetBinding
                    .inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            Utils.setWidthBasedOnPercentage(
                binding.root.context,
                holder.itemView,
                "2.60",
                R.dimen.spacing
            )
            val item = items[position]
            binding.tvDuration.text = item.title.orEmpty()
            binding.tvDuration.textSize = item.titleSize?.toFloat() ?: 16f
            binding.tvDuration.setTextColor(Utils.parseColor(item.titleColor))
            binding.tvOldPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            binding.tvOldPrice.text = item.oldPrice.orEmpty()
            binding.tvOldPrice.textSize = item.oldPriceSize?.toFloat() ?: 10f
            binding.tvOldPrice.setTextColor(Utils.parseColor(item.oldPriceColor))
            binding.tvNewPrice.text = item.newPrice.orEmpty()
            binding.tvNewPrice.textSize = item.newPriceSize?.toFloat() ?: 24f
            binding.tvNewPrice.setTextColor(Utils.parseColor(item.newPriceColor))
            binding.tvMonthPrice.text = item.monthlyPrice.orEmpty()
            binding.tvMonthPrice.textSize = item.monthlyPriceSize?.toFloat() ?: 11f
            binding.tvMonthPrice.setTextColor(Utils.parseColor(item.monthlyPriceColor))
            binding.tvEmi.text = item.emi.orEmpty()
            binding.tvEmi.textSize = item.emiSize?.toFloat() ?: 10f
            binding.tvEmi.setTextColor(Utils.parseColor(item.emiColor))
            binding.tvDiscount.text = item.discountText.orEmpty()
            binding.tvDiscount.textSize = item.discountSize?.toFloat() ?: 13f
            binding.tvDiscount.setTextColor(Utils.parseColor(item.discountColor))

            binding.ivBestSeller.loadImage(item.bestsellerUrl.orEmpty())

            binding.root.setOnClickListener {
                val event = AnalyticsEvent(
                    EventConstants.NCP_CHOOSE_PLAN_ITEM_CLICKED,
                    extraParams,
                    ignoreMoengage = false
                )
                analyticsPublisher.publishEvent(event)
                val countToSendEvent: Int = Utils.getCountToSend(
                    RemoteConfigUtils.getEventInfo(),
                    EventConstants.NCP_CHOOSE_PLAN_ITEM_CLICKED
                )
                MoEngageUtils.setUserAttribute(binding.root.context, "dn_bnb_clicked",true)

                val eventCopy = event.copy()
                repeat((0 until countToSendEvent).count()) {
                    analyticsPublisher.publishBranchIoEvent(eventCopy)
                }
                deeplinkAction.performAction(binding.root.context, item.deeplink)
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemPlanWidgetBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetCoursePlanBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCoursePlanBinding>(binding, widget)
}

class PlanWidgetModel : WidgetEntityModel<PlanWidgetData, WidgetAction>()

@Keep
data class PlanWidgetData(
    @SerializedName("title")
    val title: String?,
    @SerializedName("title_color")
    val titleColor: String?,
    @SerializedName("title_size")
    val titleSize: String?,
    @SerializedName("image_url_one")
    val imageUrlOne: String?,
    @SerializedName("image_url_two")
    val imageUrlTwo: String?,
    @SerializedName("image_url_three")
    val imageUrlThree: String?,
    @SerializedName("time")
    val time: Long?,
    @SerializedName("time_text_size")
    val timeTextSize: String?,
    @SerializedName("time_text_color")
    val timeTextColor: String?,
    @SerializedName("bottom_text")
    var bottomText: String?,
    @SerializedName("bottom_text_color")
    val bottomTextColor: String?,
    @SerializedName("bottom_text_size")
    val bottomTextSize: String?,
    @SerializedName("bg_color_one")
    var bgColorOne: String?,
    @SerializedName("bg_color_two")
    var bgColorTwo: String?,
    @SerializedName("bg_color_one_expired")
    val bgColorOneExpired: String?,
    @SerializedName("bg_color_two_expired")
    val bgColorTwoExpired: String?,
    @SerializedName("bottom_deeplink")
    val bottomDeeplink: String?,
    @SerializedName("items")
    val items: List<PlanWidgetItems>?,

    ) : WidgetData()

@Keep
data class PlanWidgetItems(
    @SerializedName("duration") val title: String?,
    @SerializedName("duration_color") val titleColor: String?,
    @SerializedName("duration_size") val titleSize: String?,
    @SerializedName("monthly_price") val monthlyPrice: String?,
    @SerializedName("monthly_price_color") val monthlyPriceColor: String?,
    @SerializedName("monthly_price_size") val monthlyPriceSize: String?,
    @SerializedName("emi") val emi: String?,
    @SerializedName("emi_color") val emiColor: String?,
    @SerializedName("emi_size") val emiSize: String?,
    @SerializedName("image_url") val bestsellerUrl: String?,
    @SerializedName("amount_strike_through") val oldPrice: String?,
    @SerializedName("amount_strike_through_color") val oldPriceColor: String?,
    @SerializedName("amount_strike_through_size") val oldPriceSize: String?,
    @SerializedName("amount_to_pay") val newPrice: String?,
    @SerializedName("amount_to_pay_color") val newPriceColor: String?,
    @SerializedName("amount_to_pay_size") val newPriceSize: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("source") val source: String?,
    @SerializedName("discount") val discountText: String?,
    @SerializedName("discount_size") val discountSize: String?,
    @SerializedName("discount_color") val discountColor: String?
)
