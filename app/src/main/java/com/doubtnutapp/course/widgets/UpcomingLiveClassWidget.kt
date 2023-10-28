package com.doubtnutapp.course.widgets

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.WidgetUpcomingLiveClassCarouselCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.utils.DateUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.snackbar.Snackbar
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Sachin Saxena on 04/01/21.
 */
class UpcomingLiveClassWidget(context: Context) :
    BaseBindingWidget<UpcomingLiveClassWidget.WidgetHolder,
        UpcomingLiveClassWidget.UpcomingLiveClassWidgetModel, WidgetUpcomingLiveClassCarouselCardBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "UpcomingLiveClassWidget"

        const val FUTURE = 0
        const val LIVE = 1
        const val PAST = 2
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    private val mWidgetName: String = this::class.simpleName.orEmpty()

    override fun getViewBinding(): WidgetUpcomingLiveClassCarouselCardBinding {
        return WidgetUpcomingLiveClassCarouselCardBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: UpcomingLiveClassWidgetModel
    ): WidgetHolder {

        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 12, 0)
            }
        )

        val widgetData: UpcomingLiveClassWidgetData = model.data
        val binding = holder.binding
        widgetData.cardWidth?.let {
            Utils.setWidthBasedOnPercentage(
                holder.itemView.context,
                holder.itemView,
                it,
                R.dimen.spacing
            )
        }

        val parentId = (model.extraParams?.get(Constants.PARENT_ID) as? String).orDefaultValue()

        binding.apply {

            imageViewBackground.loadImage(widgetData.imageBgCard)

            imageViewBackground.hide()
            imageViewFaculty.loadImage(widgetData.imageUrl.orEmpty())
            textViewSubject.text = widgetData.subject.orEmpty()
            textViewTitleInfo.text = widgetData.title1.orEmpty()
            tvFacultyInfo.text = widgetData.title2.orEmpty()
            tvClassTiming.text = widgetData.topTitle.orEmpty()

            binding.cardContainer.background = Utils.getGradientView(
                widgetData.startGd.orDefaultValue("#232a4f"),
                widgetData.midGd.orDefaultValue("#232a4f"),
                widgetData.endGd.orDefaultValue("#232a4f")
            )

            binding.textViewSubject.background = Utils.getShape(
                widgetData.color.orEmpty(),
                widgetData.color.orEmpty(),
                4f
            )

            button.text = widgetData.bottomLayout?.button?.text.orEmpty()
            button.setTextColor(
                Color.parseColor(
                    widgetData.bottomLayout?.button?.textColor?.orDefaultValue(
                        "#ea532c"
                    )
                )
            )
            button.setBackgroundColor(
                Color.parseColor(
                    widgetData.bottomLayout?.button?.backgroundColor.orDefaultValue(
                        "#ffffff"
                    )
                )
            )

            button.text = widgetData.bottomLayout?.button?.text.orEmpty()
            button.setTextColor(
                Color.parseColor(
                    widgetData.bottomLayout?.button?.textColor?.orDefaultValue(
                        "#ea532c"
                    )
                )
            )
            button.setBackgroundColor(
                Color.parseColor(
                    widgetData.bottomLayout?.button?.backgroundColor.orDefaultValue(
                        "#ffffff"
                    )
                )
            )

            buttonReminder.isVisible = widgetData.showReminder == true
            buttonReminder.isSelected = widgetData.isReminderSet == 1

            if (!widgetData.targetExam.isNullOrEmpty()) {
                tvTargetExam.visibility = View.VISIBLE
                tvTargetExam.text = widgetData.targetExam
            } else {
                tvTargetExam.visibility = View.GONE
            }
            var examTagBg = widgetData.bgExamTag.orDefaultValue(widgetData.color.orEmpty())
            tvTargetExam.background = Utils.getShape(
                examTagBg,
                examTagBg,
                11f.dpToPx()
            )
            tvTargetExam.setTextColor(Utils.parseColor(widgetData.textColorExamTag ?: "#FFFFFF"))
        }

        holder.itemView.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.LIVE_CLASS_VIDEO_PLAYED,
                    hashMapOf(
                        EventConstants.CLICKED_ITEM_ID to widgetData.id,
                        EventConstants.SOURCE to source.orEmpty(),
                        EventConstants.WIDGET to mWidgetName,
                        EventConstants.SUBJECT to widgetData.subject.orEmpty(),
                        EventConstants.TEACHER_NAME to widgetData.title2.orEmpty(),
                        EventConstants.BOARD to widgetData.board.orEmpty()
                    )
                )
            )
            if (widgetData.isPremium == true && widgetData.isVip != true) {
                deeplinkAction.performAction(holder.itemView.context, widgetData.paymentDeeplink)
            } else {
                val currentContext = holder.itemView.context
                if (widgetData.state == LIVE ||
                    DateUtils.isBeforeCurrentTime(widgetData.liveAt) ||
                    widgetData.state == PAST
                ) {
                    // allow to watch video
                    openVideoPage(
                        context = currentContext,
                        id = widgetData.id,
                        page = widgetData.page,
                        parentId = parentId
                    )
                } else {
                    // for future state
                    markInterested(
                        widgetData.id, false,
                        widgetData.assortmentId.orEmpty(), widgetData.liveAt?.toString().orEmpty(), 0
                    )
                    if (!widgetData.isLastResource) {
                        deeplinkAction.performAction(holder.itemView.context, widgetData.deeplink)
                    } else {
                        showToast(currentContext, currentContext.getString(R.string.coming_soon))
                    }
                }
            }
        }

        binding.buttonReminder.setOnClickListener {
            checkInternetConnection(holder.itemView.context) {

                it.isSelected = !it.isSelected
                widgetData.isReminderSet = if (it.isSelected) 1 else 0

                var message = widgetData.reminderMessage.orDefaultValue("Your reminder has been set")
                if (!it.isSelected) {
                    message = "Reminder removed"
                }

                (holder.itemView.context as? Activity)?.let { context ->
                    Snackbar.make(
                        context.findViewById(android.R.id.content),
                        message,
                        Snackbar.LENGTH_LONG
                    ).apply {
                        this.view.background =
                            ContextCompat.getDrawable(context, R.drawable.bg_capsule_black_90)
                        setActionTextColor(ContextCompat.getColor(context, R.color.redTomato))
                        val textView =
                            this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                        textView.setTextColor(ContextCompat.getColor(context, R.color.white))
                        show()
                    }
                }
                markInterested(
                    widgetData.id, true,
                    widgetData.assortmentId.orEmpty(), widgetData.liveAt?.toString().orEmpty(), if (it.isSelected) 1 else 0
                )
            }
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.REMINDER_CARD_CLICK,
                    hashMapOf(
                        EventConstants.WIDGET to mWidgetName,
                        EventConstants.SUBJECT to widgetData.subject.orEmpty(),
                        EventConstants.TEACHER_NAME to widgetData.title2.orEmpty(),
                        EventConstants.BOARD to widgetData.board.orEmpty()
                    ),
                    ignoreSnowplow = true
                )
            )
        }

        return holder
    }

    private fun markInterested(id: String, isReminder: Boolean, assortmentId: String, liveAt: String?, reminderSet: Int?) {
        DataHandler.INSTANCE.courseRepository.markInterested(id, isReminder, assortmentId, liveAt, reminderSet)
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({})
    }

    private fun openVideoPage(
        context: Context,
        id: String?,
        page: String?,
        parentId: String = "",
        source: String = Constants.LIVE_CLASS_SIMILAR_VIDEO_PAGE
    ) {
        context.startActivity(
            VideoPageActivity.startActivity(
                context = context,
                questionId = id.orEmpty(),
                page = page.orEmpty(),
                source = source,
                parentId = parentId
            )
        )
    }

    class WidgetHolder(
        binding: WidgetUpcomingLiveClassCarouselCardBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetUpcomingLiveClassCarouselCardBinding>(binding, widget)

    class UpcomingLiveClassWidgetModel :
        WidgetEntityModel<UpcomingLiveClassWidgetData, WidgetAction>()

    @Keep
    data class UpcomingLiveClassWidgetData(
        @SerializedName("id") val id: String,
        @SerializedName("assortment_id") val assortmentId: String?,
        @SerializedName("page") val page: String?,
        @SerializedName("top_title") val topTitle: String?,
        @SerializedName("title1") val title1: String?,
        @SerializedName("title2") val title2: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("is_live") val isLive: Boolean?,
        @SerializedName("subject") val subject: String?,
        @SerializedName("color") val color: String?,
        @SerializedName("player_type") val playerType: String?,
        @SerializedName("live_at") val liveAt: Long?,
        @SerializedName("image_bg_card") val imageBgCard: String?,
        @SerializedName("lock_state") val lockState: Int?,
        @SerializedName("bottom_title") val bottomTitle: String?,
        @SerializedName("topic") val topic: String?,
        @SerializedName("students") val students: Long?,
        @SerializedName("interested") val interested: Long?,
        @SerializedName("is_premium") val isPremium: Boolean?,
        @SerializedName("is_vip") val isVip: Boolean?,
        @SerializedName("state") val state: Int,
        @SerializedName("live_text") val liveText: String?,
        @SerializedName("duration") val duration: String?,
        @SerializedName("show_reminder") val showReminder: Boolean?,
        @SerializedName("is_reminder_set") var isReminderSet: Int?,
        @SerializedName("reminder_message") val reminderMessage: String?,
        @SerializedName("payment_deeplink") val paymentDeeplink: String?,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("text_color_primary") val textColorPrimary: String?,
        @SerializedName("text_color_secondary") val textColorSecondary: String?,
        @SerializedName("text_color_title") val textColorTitle: String?,
        @SerializedName("set_width") val setWidth: Boolean?,
        @SerializedName("button_state") val buttonState: String?,
        @SerializedName("button") val button: ButtonData?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("bottom_layout") val bottomLayout: BottomLayout?,
        @SerializedName("video_resource") val videoResource: ApiVideoResource,
        @SerializedName("start_gd") val startGd: String?,
        @SerializedName("mid_gd") val midGd: String?,
        @SerializedName("end_gd") val endGd: String?,
        @SerializedName("board") val board: String?,
        @SerializedName("target_exam") val targetExam: String?,
        @SerializedName("bg_exam_tag") val bgExamTag: String?,
        @SerializedName("text_color_exam_tag") val textColorExamTag: String?,
        @SerializedName("remaining") val remaining: String?,
        @SerializedName("is_last_resource") val isLastResource: Boolean

    ) : WidgetData()

    @Keep
    data class ApiVideoResource(
        @SerializedName("resource") val resource: String,
        @SerializedName("drm_scheme") val drmScheme: String?,
        @SerializedName("drm_license_url") val drmLicenseUrl: String?,
        @SerializedName("auto_play_duration") val autoPlayDuration: Long?,
        @SerializedName("media_type") val mediaType: String?
    )

    @Keep
    data class BottomLayout(
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("sub_title") val subTitle: String?,
        @SerializedName("sub_title_color") val subTitleColor: String?,
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
}
