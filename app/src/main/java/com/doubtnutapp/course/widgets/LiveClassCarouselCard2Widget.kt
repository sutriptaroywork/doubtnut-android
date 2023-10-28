package com.doubtnutapp.videoPage.widgets

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.*
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.WidgetLiveClassCarouselCard2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.liveclass.ui.LiveClassActivity
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.similarVideo.ui.NcertSimilarFragment
import com.doubtnutapp.similarVideo.ui.SimilarVideoFragment
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
 * Created by devansh on 1/2/21.
 */

class LiveClassCarouselCard2Widget(context: Context) :
    BaseBindingWidget<LiveClassCarouselCard2Widget.WidgetHolder,
        LiveClassCarouselCard2Widget.Model, WidgetLiveClassCarouselCard2Binding>(context) {

    companion object {
        const val FUTURE = 0
        const val LIVE = 1
        const val PAST = 2
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val mWidgetName: String = "LiveClassCarouselCard2Widget"

    var source: String? = ""

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Supporting only horizontal cards list UI. XML layout has been designed for vertical orientation
        super.bindWidget(
            holder,
            model.apply {
                this.layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(
                    marginTop = 0,
                    marginBottom = 9,
                    marginLeft = 0,
                    marginRight = 0
                )
            }
        )

        val data = model.data
        val binding = holder.binding
        val parentId = (model.extraParams?.get(Constants.PARENT_ID) as? String).orDefaultValue()

        val width = Utils.getWidthFromScrollSize(holder.itemView.context, data.cardWidth) -
            (binding.cardView.marginStart + binding.cardView.marginEnd)

        binding.cardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            this.width = width
            dimensionRatio = data.cardRatio ?: "16:9"
        }
        requestLayout()

        binding.textViewTitleInfo.apply {
            text = data.title1.orEmpty()
            setTextColor(Utils.parseColor(data.textColorTitle))
            if (data.title1Size != null) {
                textSize = data.title1Size
            }
        }

        if (!data.subject.isNullOrEmpty()) {
            binding.textViewSubject.apply {
                visibility = View.VISIBLE
                text = data.subject
                setTextColor(Utils.parseColor(data.textColorPrimary))
            }
        } else {
            binding.textViewSubject.visibility = View.GONE
        }

        if (!data.tagText.isNullOrEmpty())
            binding.tvTag.apply {
                visibility = View.VISIBLE
                text = data.tagText
                setTextColor(Utils.parseColor(data.tagTextColor ?: "#000000"))
                background =
                    Utils.getShape(data.tagBgColor.orEmpty(), data.tagBgColor.orEmpty(), 4f)
            } else {
            binding.tvTag.visibility = View.GONE
        }

        if (!data.tagTwoText.isNullOrEmpty())
            binding.tvTagTwo.apply {
                visibility = View.VISIBLE
                text = data.tagTwoText
                setTextColor(Utils.parseColor(data.tagTwoTextColor ?: "#000000"))
                background =
                    Utils.getShape(data.tagTwoBgColor.orEmpty(), data.tagTwoBgColor.orEmpty(), 4f)
            } else {
            binding.tvTagTwo.visibility = View.GONE
        }

        val timeText = if (!data.remaining.isNullOrBlank()) {
            data.remaining.orEmpty()
        } else {
            data.topTitle.orEmpty()
        }

        binding.textViewTimeInfo.apply {
            if (timeText.isNotEmpty()) {
                visibility = View.VISIBLE
                text = timeText
                background = if (data.color != null) {
                    setTextColor(Utils.parseColor(data.textColorSecondary))
                    updatePadding(left = 10.dpToPx(), top = 3.dpToPx())
                    Utils.getMaterialShapeDrawable(data.color, 11f.dpToPx())
                } else {
                    setTextColor(Utils.parseColor(data.textColorPrimary))
                    updatePadding(left = 0, top = 0)
                    null
                }
            } else {
                visibility = View.GONE
            }
        }

        binding.textViewFacultyInfo.apply {
            text = data.title2
            setTextColor(Utils.parseColor(data.textColorPrimary))
        }

        binding.imageViewFaculty.apply {
            updateLayoutParams<ConstraintLayout.LayoutParams> {
                verticalBias = data.imageVerticalBias ?: 0.5f
            }
            isVisible = data.imageUrl.isNotNullAndNotEmpty()
            loadImage(data.imageUrl.orEmpty())
        }
        binding.imageViewBackground.apply {
            setScaleType(data.imageBgScaleType, ImageView.ScaleType.CENTER_CROP)
            loadImage(data.imageBgCard.orEmpty())
        }

        binding.root.setOnClickListener {
            DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams, model.type))
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.LIVE_CLASS_VIDEO_PLAYED,
                    hashMapOf<String, Any>(
                        EventConstants.CLICKED_ITEM_ID to data.id.orEmpty(),
                        EventConstants.SOURCE to source.orEmpty(),
                        EventConstants.ITEM_POSITION to widgetViewHolder.absoluteAdapterPosition,
                        EventConstants.WIDGET to mWidgetName,
                        EventConstants.SUBJECT to data.subject.orEmpty(),
                        EventConstants.TEACHER_NAME to data.title2.orEmpty(),
                        EventConstants.BOARD to data.board.orEmpty()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
            if (data.isPremium == true && data.isVip != true) {
                deeplinkAction.performAction(
                    holder.itemView.context,
                    data.paymentDeeplink,
                    source.orEmpty()
                )
            } else {
                val currentContext = holder.itemView.context
                if (data.state == LIVE ||
                    DateUtils.isBeforeCurrentTime(data.liveAt?.toLongOrNull()) ||
                    data.state == PAST
                ) {
                    // allow to watch video
                    if (currentContext is LiveClassActivity) {
                        currentContext.finish()
                    } else if (currentContext is VideoPageActivity &&
                        source != SimilarVideoFragment.TAG &&
                        source != NcertSimilarFragment.SOURCE_NCERT_PAGE
                    ) {
                        currentContext.finish()
                    }

                    openVideoPage(
                        context = currentContext,
                        id = data.id,
                        page = data.page,
                        parentId = parentId,
                    )
                } else {
                    // for future state
                    markInterested(
                        data.id.orEmpty(), false,
                        data.assortmentId.orEmpty(), data.liveAt, 0
                    )
                    if (!data.isLastResource) {
                        deeplinkAction.performAction(holder.itemView.context, data.deeplink)
                    } else {
                        showToast(currentContext, currentContext.getString(R.string.coming_soon))
                    }
                }
            }
        }

        if (!data.targetExam.isNullOrEmpty()) {
            binding.tvTargetExam.visibility = View.VISIBLE
            binding.tvTargetExam.text = data.targetExam
        } else {
            binding.tvTargetExam.visibility = View.GONE
        }

        val examTagBg = data.bgExamTag.orDefaultValue(data.color.orEmpty())
        binding.tvTargetExam.background = Utils.getShape(
            examTagBg,
            examTagBg,
            11f.dpToPx()
        )
        binding.tvTargetExam.setTextColor(Utils.parseColor(data.textColorExamTag ?: "#FFFFFF"))

        binding.recommendedLayout.apply {
            isVisible = data.recommendedClass != null
            binding.tvChapterName.apply {
                isVisible = data.recommendedClass?.chapterName?.isNotNullAndNotEmpty() == true
                text = data.recommendedClass?.chapterName
            }

            binding.tvTeacherName.apply {
                isVisible = data.recommendedClass?.teacherName?.isNotNullAndNotEmpty() == true
                text = data.recommendedClass?.teacherName
            }

            binding.imageViewPlay.apply {
                layoutParams.height = 24.0F.dpToPx().toInt()
                layoutParams.width = 24.0F.dpToPx().toInt()
                requestLayout()
            }
        }

        binding.buttonReminder.apply {
            isVisible = data.showReminder == true

            isSelected = data.isReminderSet == 1

            setOnClickListener {
                checkInternetConnection(holder.itemView.context) {
                    isSelected = !isSelected
                    data.isReminderSet = if (isSelected) 1 else 0

                    markInterested(
                        data.id.orEmpty(), true,
                        data.assortmentId.orEmpty(), data.liveAt, if (isSelected) 1 else 0
                    )
                    var message = data.reminderMessage.orDefaultValue("Your reminder has been set")
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
                                AppCompatResources.getDrawable(
                                    context,
                                    R.drawable.bg_capsule_black_90
                                )
                            setActionTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.redTomato
                                )
                            )
                            val textView =
                                this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                            textView.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.white
                                )
                            )
                            show()
                        }
                    }
                }

                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams, model.type))
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.REMINDER_CARD_CLICK,
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to mWidgetName,
                            EventConstants.SUBJECT to data.subject.orEmpty(),
                            EventConstants.TEACHER_NAME to data.title2.orEmpty(),
                            EventConstants.BOARD to data.board.orEmpty()
                        ),
                        ignoreSnowplow = true
                    )
                )

            }

        }

        when (data.buttonState) {
            "payment" -> {
                binding.buttonsContainer.isVisible = false
                binding.buyNowContainer.isVisible = true
                binding.tvOldPrice.text = data.oldPrice.orEmpty()
                binding.tvNewPrice.text = data.newPrice.orEmpty()
                binding.tvBuyNow.text = data.buyNowText.orEmpty()
                binding.tvDiscount.text = data.discountText.orEmpty()
                holder.itemView.background = Utils.getShape("", "#ea532c", 4f, 1)
                binding.buyNowContainer.setOnClickListener {
                    deeplinkAction.performAction(holder.itemView.context, data.deeplink.orEmpty())
                }
            }
            "multiple" -> {
                setButtonData(holder, model)
            }
            "continue_buying" -> {
                setButtonData(holder, model)
                binding.buttonsContainer.setPadding(
                    ViewUtils.dpToPx(8f, holder.itemView.context).toInt(),
                    ViewUtils.dpToPx(8f, holder.itemView.context).toInt(),
                    ViewUtils.dpToPx(8f, holder.itemView.context).toInt(),
                    ViewUtils.dpToPx(8f, holder.itemView.context).toInt()
                )
                binding.buttonsContainer.setMargins(0, 0, 0, 0)
                binding.buttonsContainer.background =
                    ContextCompat.getDrawable(holder.itemView.context, R.drawable.buy_now_button_bg)

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.CONTINUE_BUYING_CTA_CLICKED,
                        hashMapOf<String, Any>(
                            EventConstants.CTA_TITLE to data.button?.text.orEmpty(),
                        ).apply {
                            putAll(model.extraParams ?: hashMapOf())
                        }, ignoreBranch = false
                    )
                )
                MoEngageUtils.setUserAttribute(context, "dn_bnb_clicked",true)
            }

            "join_now" -> {
                binding.apply {
                    binding.buttonsContainer.visibility = INVISIBLE
                    classDetailContainer.isVisible = true
                    tvJoinNow.isVisible = data.joinNowCta.isNotNullAndNotEmpty()
                    tvJoinNow.text = data.joinNowCta
                    tvTopTitle.apply {
                        isVisible = data.topHeadingText.isNotNullAndNotEmpty()
                        text = data.topHeadingText
                        if (data.topBackgroundTextColor.isValidColorCode()) {
                            setBackgroundColor(Utils.parseColor(data.topBackgroundTextColor))
                        }
                    }
                    tvTopic.apply {
                        isVisible = data.classTopic.isNotNullAndNotEmpty()
                        text = data.classTopic
                        textSize = data.classTopicSize?.toFloat() ?: 14f
                    }
                    tvVideoDuration.apply {
                        isVisible = data.videoDurationText.isNotNullAndNotEmpty()
                        text = data.videoDurationText
                        textSize = data.videoDurationTextSize?.toFloat() ?: 10f
                    }
                    classDetailContainer.setOnClickListener {
                        deeplinkAction.performAction(holder.itemView.context, data.joinNowDeeplink.orEmpty())
                    }
                }
            }
            else -> {
                setButtonData(holder, model)
            }
        }

        binding.extraBottomInfo.apply {
            isVisible = data.extraBottomText.isNullOrEmpty().not()
            text = data.extraBottomText
        }

        data.ocrText?.let {
            binding.mathView.apply {
                show()
                text = data.ocrText
                // Set background color only if OCR text is shown to look better
                if (data.backgroundColor.isValidColorCode()) {
                    binding.imageViewBackground.setBackgroundColor(Color.parseColor(data.backgroundColor))
                }
            }
        }
        trackingViewId = data.id
        return holder
    }

    private fun setButtonData(holder: WidgetHolder, model: Model) {
        val data = model.data
        val binding = holder.binding
        binding.buttonsContainer.isVisible = true
        binding.buyNowContainer.isVisible = false
        binding.button.apply {
            setTextColor(Utils.parseColor(data.buttonTextColor ?: "#ea532c"))
            background = Utils.getShape(data.buttonBgColor ?: "#ffffff", "#ea532c", 4f)
            isVisible = data.button?.text.isNullOrBlank().not()
            text = data.button?.text.orEmpty()
            setOnClickListener {
                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams, model.type))
                deeplinkAction.performAction(holder.itemView.context, data.button?.deeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.LIVE_CLASS_GO_TO_COURSE_CLICK,
                        hashMapOf<String, Any>().apply {
                            putAll(model.extraParams ?: hashMapOf())
                        }
                    )
                )
            }
        }
    }

    private fun markInterested(
        id: String,
        isReminder: Boolean,
        assortmentId: String,
        liveAt: String?,
        reminderSet: Int?
    ) {
        DataHandler.INSTANCE.courseRepository.markInterested(
            id,
            isReminder,
            assortmentId,
            liveAt,
            reminderSet
        )
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({})
    }

    private fun openVideoPage(context: Context, id: String?, page: String?, parentId: String = "") {
        context.startActivity(
            VideoPageActivity.startActivity(
                context = context,
                questionId = id.orEmpty(),
                page = page.orEmpty(),
                parentId = parentId,
                createNewInstance = source == NcertSimilarFragment.SOURCE_NCERT_PAGE
            )
        )
    }

    class WidgetHolder(
        binding: WidgetLiveClassCarouselCard2Binding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetLiveClassCarouselCard2Binding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String?,
        @SerializedName("top_title") val topTitle: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("subject") val subject: String?,
        @SerializedName("state") val state: Int,
        @SerializedName("live_text") val liveText: String?,
        @SerializedName("title1") val title1: String?,
        @SerializedName("title1_size") val title1Size: Float?,
        @SerializedName("title2") val title2: String?,
        @SerializedName("students") val students: String?,
        @SerializedName("color") val color: String?,
        @SerializedName("show_reminder") val showReminder: Boolean?,
        @SerializedName("is_reminder_set") var isReminderSet: Int?,
        @SerializedName("button") val button: ButtonData?,
        @SerializedName("page") val page: String?,
        @SerializedName("start_gd") val startGd: String?,
        @SerializedName("mid_gd") val midGd: String?,
        @SerializedName("end_gd") val endGd: String?,
        @SerializedName("image_bg_card") val imageBgCard: String?,
        @SerializedName("image_bg_scale_type") val imageBgScaleType: String?,
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
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("text_color_primary") val textColorPrimary: String?,
        @SerializedName("text_color_secondary") val textColorSecondary: String?,
        @SerializedName("text_color_title") val textColorTitle: String?,
        @SerializedName("image_vertical_bias") val imageVerticalBias: Float?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("button_state") val buttonState: String?,
        @SerializedName("old_price") val oldPrice: String?,
        @SerializedName("new_price") val newPrice: String?,
        @SerializedName("discount_text") val discountText: String?,
        @SerializedName("buy_now_text") val buyNowText: String?,
        @SerializedName("button_text_color") val buttonTextColor: String?,
        @SerializedName("button_bg_color") val buttonBgColor: String?,
        @SerializedName("tag_text") val tagText: String?,
        @SerializedName("tag_text_color") val tagTextColor: String?,
        @SerializedName("tag_bg_color") val tagBgColor: String?,
        @SerializedName("tag_two_text") val tagTwoText: String?,
        @SerializedName("target_exam") val targetExam: String?,
        @SerializedName("bg_exam_tag") val bgExamTag: String?,
        @SerializedName("text_color_exam_tag") val textColorExamTag: String?,
        @SerializedName("tag_two_text_color") val tagTwoTextColor: String?,
        @SerializedName("tag_two_bg_color") val tagTwoBgColor: String?,
        @SerializedName("class_topic") val classTopic: String?,
        @SerializedName("join_now_cta") val joinNowCta: String?,
        @SerializedName("join_now_deeplink") val joinNowDeeplink: String?,
        @SerializedName("video_duration_text") val videoDurationText: String?,
        @SerializedName("class_topic_size") val classTopicSize: String?,
        @SerializedName("video_duration_text_size") val videoDurationTextSize: String?,
        @SerializedName("top_heading_text") val topHeadingText: String?,
        @SerializedName("top_background_text_color") val topBackgroundTextColor: String?,
        @SerializedName("guideline_constraint_begin") val guidelineConstraintBegin: Float?,
        @SerializedName("recommended_class") val recommendedClass: RecommendedClassData?,
        @SerializedName("extra_bottom_text") val extraBottomText: String?,
        @SerializedName("ocr_text") val ocrText: String?,
        @SerializedName("background_color") val backgroundColor: String?,
    ) : WidgetData()

    @Keep
    data class ButtonData(
        @SerializedName("text") val text: String,
        @SerializedName("action") val action: WidgetAction?,
        @SerializedName("deeplink") val deeplink: String?
    )

    @Keep
    data class RecommendedClassData(
        @SerializedName("chapter_name") val chapterName: String,
        @SerializedName("teacher_name") val teacherName: String
    )

    override fun getViewBinding(): WidgetLiveClassCarouselCard2Binding {
        return WidgetLiveClassCarouselCard2Binding.inflate(LayoutInflater.from(context), this, true)
    }
}
