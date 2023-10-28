package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetPaidCourseBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.videoPage.widgets.LiveClassCarouselCard2Widget
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Sachin Saxena on 04/01/21.
 */
class PaidCourseWidget(context: Context) : BaseBindingWidget<PaidCourseWidget.WidgetHolder,
        PaidCourseWidget.PaidCourseChildWidgetModel, WidgetPaidCourseBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    companion object {
        private const val TAG = "PaidCourseWidget"
    }

    private val mWidgetName: String = this::class.simpleName.orEmpty()

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: PaidCourseChildWidgetModel): WidgetHolder {

        super.bindWidget(holder, model)
        setMargins(WidgetLayoutConfig(0, 9, 0, 0))

        val data: PaidCourseChildWidgetData = model.data
        val binding = holder.binding

        Utils.setWidthBasedOnPercentage(
            holder.itemView.context,
            holder.itemView,
            data.cardWidth,
            R.dimen.spacing_5
        )

        binding.cardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            dimensionRatio = data.cardRatio ?: "16:9"
        }
        requestLayout()

        model.extraParams?.put(EventConstants.SOURCE, source.orEmpty())

        binding.root.apply {
            binding.textViewTitleInfo.apply {
                text = data.title1.orEmpty()
                setTextColor(Utils.parseColor(data.textColorTitle))
            }

            binding.textViewSubject.apply {
                text = data.subject
                background = Utils.getShape(
                    data.color.orEmpty(),
                    data.color.orEmpty(),
                    4f
                )
            }

            binding.textViewFacultyInfo.apply {
                text = data.title2
                setTextColor(Utils.parseColor(data.textColorPrimary))
            }

            binding.textViewTimeInfo.apply {
                text = data.bottomTitle.orEmpty()
                setTextColor(Utils.parseColor(data.textColorSecondary))
            }

            binding.imageViewFaculty.apply {
                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    verticalBias = data.imageVerticalBias ?: 0.5f
                }
                loadImage(data.imageUrl.orEmpty())
            }

            binding.imageViewBackground.loadImage(data.imageBgCard.orEmpty())

            binding.tvVip.apply {
                isVisible = data.isVip == true
                text = data.vipText.orEmpty()
            }

            binding.tvVipTitle.apply {
                isVisible = data.isVip == true
                text = data.vipText.orEmpty()
            }

            binding.textViewTitleMain.apply {
                text = data.title.orEmpty()
            }

            binding.textViewSubTitleMain.apply {
                text = data.subTitle.orEmpty()
                isVisible = !data.subTitle.isNullOrEmpty()
            }

            binding.tvPrice.apply {
                text = data.bottomLayout?.title1.orEmpty()
                isVisible = !data.bottomLayout?.title1.isNullOrEmpty()
                setTextColor(Utils.parseColor(data.bottomLayout?.title1Color))
            }

            binding.tvActualPrice.apply {
                text = data.bottomLayout?.title2.orEmpty()
                isVisible = !data.bottomLayout?.title2.isNullOrEmpty()
                setTextColor(Utils.parseColor(data.bottomLayout?.title2Color))
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }

            binding.tvDiscount.apply {
                text = data.bottomLayout?.title3.orEmpty()
                isVisible = !data.bottomLayout?.title3.isNullOrEmpty()
                setTextColor(Utils.parseColor(data.bottomLayout?.title3Color))
            }
            binding.btnBuyNow.apply {
                text = data.bottomLayout?.button?.text.orEmpty()
                setOnClickListener {
                    model.extraParams?.put(EventConstants.CLICKED_BUTTON_NAME, text)
                    DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams))
                    deeplinkAction.performAction(
                        holder.itemView.context,
                        data.bottomLayout?.button?.action?.deeplink
                    )
                    val event = AnalyticsEvent(
                        EventConstants.POPULAR_COURSE_BUTTON_CLICK,
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to TAG,
                            EventConstants.ASSORTMENT_ID to data.assortmentId.toString()
                        )
                            .apply {
                                putAll(model.extraParams ?: hashMapOf())
                            }, ignoreMoengage = false
                    )
                    analyticsPublisher.publishEvent(event)
                    val countToSendEvent: Int = Utils.getCountToSend(
                        RemoteConfigUtils.getEventInfo(),
                        EventConstants.POPULAR_COURSE_BUTTON_CLICK
                    )
                    val eventCopy = event.copy()
                    repeat((0 until countToSendEvent).count()) {
                        analyticsPublisher.publishBranchIoEvent(eventCopy)
                    }
                }
            }

            setOnClickListener {
                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams))
                deeplinkAction.performAction(holder.itemView.context, data.paymentDeeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.POPULAR_COURSE_BANNER_CLICK,
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to TAG,
                            EventConstants.ASSORTMENT_ID to data.assortmentId.toString()
                        )
                            .apply {
                                putAll(model.extraParams ?: hashMapOf())
                            }
                    )
                )
            }
        }

        return holder
    }

    class WidgetHolder(
        binding: WidgetPaidCourseBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetPaidCourseBinding>(binding, widget)

    class PaidCourseChildWidgetModel : WidgetEntityModel<PaidCourseChildWidgetData, WidgetAction>()

    @Keep
    data class PaidCourseChildWidgetData(
        @SerializedName("id") val id: String?,
        @SerializedName("top_title") val topTitle: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("subject") val subject: String?,
        @SerializedName("state") val state: Int,
        @SerializedName("live_text") val liveText: String?,
        @SerializedName("title1") val title1: String?,
        @SerializedName("title2") val title2: String?,
        @SerializedName("students") val students: String?,
        @SerializedName("color") val color: String?,
        @SerializedName("show_reminder") val showReminder: Boolean?,
        @SerializedName("is_reminder_set") val isReminderSet: Int?,
        @SerializedName("button") val button: LiveClassCarouselCard2Widget.ButtonData?,
        @SerializedName("page") val page: String?,
        @SerializedName("start_gd") val startGd: String?,
        @SerializedName("mid_gd") val midGd: String?,
        @SerializedName("end_gd") val endGd: String?,
        @SerializedName("image_bg_card") val imageBgCard: String?,
        @SerializedName("is_premium") val isPremium: Boolean?,
        @SerializedName("is_vip") val isVip: Boolean?,
        @SerializedName("board") val board: String?,
        @SerializedName("interested") val interested: String?,
        @SerializedName("bottom_title") val bottomTitle: String?,
        @SerializedName("duration") val duration: String?,
        @SerializedName("remaining") val remaining: String?,
        @SerializedName("reminder_message") val reminderMessage: String?,
        @SerializedName("live_at") val liveAt: String?,
        @SerializedName("is_last_resource") val isLastResource: Boolean,
        @SerializedName("payment_deeplink") val paymentDeeplink: String?,
        @SerializedName("lock_state") val lockState: Int?,
        @SerializedName("assortment_id") val assortmentId: String?,
        @SerializedName("card_width") val cardWidth: String,
        @SerializedName("text_color_primary") val textColorPrimary: String?,
        @SerializedName("text_color_secondary") val textColorSecondary: String?,
        @SerializedName("text_color_title") val textColorTitle: String?,
        @SerializedName("image_vertical_bias") val imageVerticalBias: Float?,
        @SerializedName("vip_text") val vipText: String?,
        @SerializedName("is_live") val isLive: Boolean?,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("set_width") val setWidth: Boolean?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("bottom_layout") val bottomLayout: BottomLayout?,
        @SerializedName("title") val title: String?,
        @SerializedName("title_text_color") val titleTextColor: String?,
        @SerializedName("is_title_bold") val isTitleBold: Boolean?,
        @SerializedName("sub_title") val subTitle: String?,
        @SerializedName("sub_title_text_size") val subTitleTextSize: Float?,
        @SerializedName("is_sub_title_bold") val isSubTitleBold: Boolean?

    ) : WidgetData()

    @Keep
    data class BottomLayout(
        @SerializedName("title1") val title1: String?,
        @SerializedName("title1_color") val title1Color: String?,
        @SerializedName("title2") val title2: String?,
        @SerializedName("title2_color") val title2Color: String?,
        @SerializedName("title3") val title3: String?,
        @SerializedName("title3_color") val title3Color: String?,
        @SerializedName("button") val button: ButtonData?
    )

    @Keep
    data class ButtonData(
        @SerializedName("text") val text: String?,
        @SerializedName("text_color") val textColor: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("border_color") val borderColor: String?,
        @SerializedName("action") val action: WidgetAction?
    )

    override fun getViewBinding(): WidgetPaidCourseBinding {
        return WidgetPaidCourseBinding.inflate(LayoutInflater.from(context), this, true)
    }
}
