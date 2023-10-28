package com.doubtnutapp.course.widgets

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.OnCourseCarouselChildWidgetItmeClicked
import com.doubtnutapp.base.RefreshUI
import com.doubtnutapp.databinding.WidgetCourseCarouselChildBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.utils.DateUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.snackbar.Snackbar
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 02/10/20.
 */
class CourseCarouselChildWidget(context: Context) :
    BaseBindingWidget<CourseCarouselChildWidget.WidgetHolder,
        CourseCarouselChildWidget.CourseCarouselChildWidgetModel,
        WidgetCourseCarouselChildBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "CourseCarouselChildWidget"
        const val FUTURE = 0
        const val LIVE = 1
        const val PAST = 2
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetCourseCarouselChildBinding {
        return WidgetCourseCarouselChildBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CourseCarouselChildWidgetModel): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        if (model.data.setWidth == true) {
            Utils.setWidthBasedOnPercentage(
                holder.binding.root.context,
                holder.itemView,
                "1.3",
                R.dimen.spacing
            )
        }

        val item: CourseCarouselChildWidgetData = model.data
        holder.binding.textViewSubject.background = Utils.getShape(
            item.color.orEmpty(),
            item.color.orEmpty(),
            4f
        )

        holder.binding.button.background = Utils.getShape(
            "#ffffff",
            "#000000",
            8f,
            2
        )

        holder.binding.buttonReminder.background = Utils.getShape(
            "#ffffff",
            "#ea532c",
            8f,
            1
        )

        holder.binding.textViewTitleInfo.text = item.title1.orEmpty()
        holder.binding.textViewSubject.text = item.subject

        val timeText = if (!item.remaining.isNullOrBlank()) {
            item.remaining.orEmpty()
        } else {
            item.topTitle.orEmpty()
        }

        holder.binding.textViewTimeInfo.text = timeText
        holder.binding.textViewDuration.toggleVisibilityAndSetText(item.duration)

        if (item.state == LIVE) {
            holder.binding.textViewTimeInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_live_icon_small, 0, 0, 0)
            holder.binding.textViewTimeInfo.setTextColor(Utils.parseColor("#ffffff"))
        } else {
            holder.binding.textViewTimeInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calendar, 0, 0, 0)
            holder.binding.textViewTimeInfo.setTextColor(Utils.parseColor("#ffc700"))
        }

        holder.binding.textViewFacultyInfo.text = item.title2

        holder.binding.imageViewFaculty.loadImageEtx(item.imageUrl.orEmpty())
        holder.binding.imageViewBackground.loadImageEtx(item.imageBgCard.orEmpty())

        if (item.showReminder == true) {
            holder.binding.buttonReminder.visibility = View.VISIBLE
        } else {
            holder.binding.buttonReminder.visibility = View.GONE
        }

        if (item.button?.text.isNullOrBlank()) {
            holder.binding.button.visibility = View.INVISIBLE
        } else {
            holder.binding.button.visibility = View.VISIBLE
        }

        when (item.lockState) {
            1 -> {
                holder.binding.imageViewLock.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.binding.root.context,
                        R.drawable.ic_tag_light_locked
                    )
                )
            }
            2 -> {
                holder.binding.imageViewLock.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.binding.root.context,
                        R.drawable.ic_tag_light_unlocked
                    )
                )
            }
            else -> {
                holder.binding.imageViewLock.setImageDrawable(null)
            }
        }

        holder.binding.buttonReminder.setOnClickListener {
            checkInternetConnection(holder.itemView.context) {
                (holder.binding.root.context as? Activity)?.let { context ->
                    Snackbar.make(
                        context.findViewById(android.R.id.content),
                        item.reminderMessage.orDefaultValue("Your reminder has been set"),
                        Snackbar.LENGTH_LONG
                    ).apply {
                        this.view.background = context.getDrawable(R.drawable.bg_capsule_black_90)
                        setActionTextColor(ContextCompat.getColor(context, R.color.redTomato))
                        val textView = this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                        textView.setTextColor(ContextCompat.getColor(context, R.color.white))
                        show()
                    }
                }
            }
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.REMINDER_CARD_CLICK,
                    hashMapOf(
                        EventConstants.WIDGET to TAG,
                        EventConstants.SUBJECT to item.subject.orEmpty(),
                        EventConstants.TEACHER_NAME to item.title2.orEmpty(),
                        EventConstants.BOARD to item.board.orEmpty()
                    ),
                    ignoreSnowplow = true
                )
            )
        }

        holder.binding.cardContainer.background = Utils.getGradientView(
            item.startGd.orDefaultValue("#232a4f"),
            item.midGd.orDefaultValue("#232a4f"),
            item.endGd.orDefaultValue("#232a4f")
        )

        holder.binding.root.setOnClickListener {
            actionPerformer?.performAction(OnCourseCarouselChildWidgetItmeClicked(item.title1.orEmpty(), item.id.orEmpty(), -1, source.orEmpty()))
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.EXPLORE_CAROUSEL +
                        "_" + EventConstants.WIDGET_ITEM_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.SOURCE to item.id.orEmpty(),
                        EventConstants.WIDGET to TAG,
                        EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty(),
                        EventConstants.SUBJECT to item.subject.orEmpty(),
                        EventConstants.TEACHER_NAME to item.title2.orEmpty(),
                        EventConstants.BOARD to item.board.orEmpty()
                    ).apply {
                        putAll(model.extraParams ?: HashMap())
                    }
                )
            )
            DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams))
            if (item.isPremium == true && item.isVip != true) {
                deeplinkAction.performAction(holder.binding.root.context, item.paymentDeeplink)
            } else if (item.showEMIDialog == true) {
                val dialog = EMIReminderDialog.newInstance(Integer.parseInt(item.assortmentId.orEmpty()))
                dialog.show((holder.binding.root.context as AppCompatActivity).supportFragmentManager, EMIReminderDialog.TAG)
                (holder.binding.root.context as AppCompatActivity).supportFragmentManager.executePendingTransactions()
                dialog.dialog?.setOnDismissListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.EMI_REMINDER_CLOSE,
                            hashMapOf<String, Any>(
                                EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty()
                            )
                        )
                    )
                    dialog.dismiss()
                    actionPerformer?.performAction(RefreshUI())
                    playVideo(holder, item)
                }
            } else {
                playVideo(holder, item)
            }
        }

        holder.binding.button.setOnClickListener {
            deeplinkAction.performAction(holder.itemView.context, item.button?.deeplink, source.orEmpty())
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.EXPLORE_GO_TO_COURSE,
                    hashMapOf<String, Any>(
                        EventConstants.SOURCE to item.id.orEmpty(),
                        EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty(),
                        EventConstants.WIDGET to TAG,
                        EventConstants.SUBJECT to item.subject.orEmpty(),
                        EventConstants.TEACHER_NAME to item.title2.orEmpty(),
                        EventConstants.BOARD to item.board.orEmpty()
                    ).apply {
                        putAll(model.extraParams ?: HashMap())
                    }
                )
            )
        }

        if (item.buttonState == "payment") {
            holder.binding.layoutPaymentInfo.show()
            holder.binding.layoutBottomDefault.hide()

            holder.binding.textViewAmountToPay.text = item.amountToPay.orEmpty()
            holder.binding.textViewAmountStrikeThrough.text = item.amountStrikeThrough.orEmpty()
            holder.binding.textViewDiscount.text = item.discount.orEmpty()
            holder.binding.textViewBuy.text = item.buyText.orEmpty()
            holder.binding.button.text = ""
        } else {
            holder.binding.layoutPaymentInfo.hide()
            holder.binding.layoutBottomDefault.show()

            holder.binding.textViewAmountToPay.text = ""
            holder.binding.textViewAmountStrikeThrough.text = ""
            holder.binding.textViewDiscount.text = ""
            holder.binding.textViewBuy.text = ""

            holder.binding.button.text = item.button?.text.orEmpty()
        }
        holder.binding.textViewAmountStrikeThrough.paintFlags =
            holder.binding.textViewAmountStrikeThrough.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        return holder
    }

    private fun playVideo(holder: WidgetHolder, item: CourseCarouselChildWidgetData) {
        val currentContext = holder.binding.root.context
        if (item.state == LIVE ||
            DateUtils.isBeforeCurrentTime(item.liveAt?.toLongOrNull()) ||
            item.state == PAST
        ) {
            // allow to watch video
            var pageSoure = item.page
            if (source == Constants.PAGE_SEARCH_SRP) {
                pageSoure = Constants.PAGE_SEARCH_SRP
            }
            openVideoPage(currentContext, item.id, pageSoure)
        } else {
            // for future state
            if (!item.isLastResource) {
                deeplinkAction.performAction(holder.binding.root.context, item.deeplink, source.orEmpty())
            } else {
                showToast(currentContext, currentContext.getString(R.string.coming_soon))
            }
        }
    }

    private fun openVideoPage(context: Context, id: String?, page: String?) {
        context.startActivity(
            VideoPageActivity.startActivity(
                context = context,
                questionId = id.orEmpty(),
                page = page.orEmpty()
            )
        )
    }

    class WidgetHolder(binding: WidgetCourseCarouselChildBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseCarouselChildBinding>(binding, widget)

    class CourseCarouselChildWidgetModel : WidgetEntityModel<CourseCarouselChildWidgetData, WidgetAction>()

    @Keep
    data class CourseCarouselChildWidgetData(
        @SerializedName("id") val id: String?,
        @SerializedName("assortment_id") val assortmentId: String?,
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
        @SerializedName("button") val button: ButtonData?,
        @SerializedName("page") val page: String?,
        @SerializedName("start_gd") val startGd: String?,
        @SerializedName("mid_gd") val midGd: String?,
        @SerializedName("end_gd") val endGd: String?,
        @SerializedName("image_bg_card") val imageBgCard: String?,
        @SerializedName("is_premium") val isPremium: Boolean?,
        @SerializedName("is_vip") val isVip: Boolean?,
        @SerializedName("board") val board: String?,
        @SerializedName("duration") val duration: String?,
        @SerializedName("remaining") val remaining: String?,
        @SerializedName("reminder_message") val reminderMessage: String?,
        @SerializedName("live_at") val liveAt: String?,
        @SerializedName("is_last_resource") val isLastResource: Boolean,
        @SerializedName("set_width") val setWidth: Boolean?,
        @SerializedName("payment_deeplink") val paymentDeeplink: String?,
        @SerializedName("button_state") val buttonState: String?,
        @SerializedName("amount_to_pay") val amountToPay: String?,
        @SerializedName("amount_strike_through") val amountStrikeThrough: String?,
        @SerializedName("discount") val discount: String?,
        @SerializedName("buy_text") val buyText: String?,
        @SerializedName("lock_state") val lockState: Int?,
        @SerializedName("show_emi_dialog") val showEMIDialog: Boolean?,
        @SerializedName("deeplink") val deeplink: String?
    ) : WidgetData() {

        @Keep
        data class ButtonData(
            @SerializedName("text") val text: String,
            @SerializedName("action") val action: WidgetAction?,
            @SerializedName("deeplink") val deeplink: String?
        )
    }
}
