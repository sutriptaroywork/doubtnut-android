package com.doubtnutapp.course.widgets

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.Keep
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.AutoPlayVideoCompleted
import com.doubtnutapp.base.MuteAutoPlayVideo
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.WidgetLiveClassCarouselCardAutoplayBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.videoPage.entities.ImaAdTagResource
import com.doubtnutapp.rvexoplayer.RvExoPlayerView
import com.doubtnutapp.rvexoplayer.RvMuteStatus
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_BLOB
import com.doubtnutapp.utils.DateUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.material.snackbar.Snackbar
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.widget_live_class_carousel_card_autoplay.view.*
import javax.inject.Inject

/**
 * Created by Sachin Saxena on 04/01/21.
 *
 * Auto play widget used to preview Live classes on home page. It might be used inside widget_autoplay
 * see [ParentAutoplayWidget]
 */
class AutoPlayChildWidget(context: Context) : BaseBindingWidget<AutoPlayChildWidget.WidgetHolder,
        AutoPlayChildWidget.AutoplayChildWidgetModel,
        WidgetLiveClassCarouselCardAutoplayBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "AutoPlayChildWidget"
        const val FUTURE = 0
        const val LIVE = 1
        const val PAST = 2

        const val EVENT_TAG = "auto_play_child_widget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    private val mWidgetName: String = this::class.simpleName.orEmpty()
    var source: String? = null

    override fun getViewBinding(): WidgetLiveClassCarouselCardAutoplayBinding {
        return WidgetLiveClassCarouselCardAutoplayBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: AutoplayChildWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        setMargins(model.layoutConfig ?: WidgetLayoutConfig(0, 9, 0, 0))

        val data: AutoplayChildWidgetData = model.data

        val itemCount: Int = model.extraParams?.get(Constants.COUNT) as? Int ?: -1
        if (itemCount > 1) {
            if (!data.cardWidth.isNullOrEmpty()) {
                Utils.setWidthBasedOnPercentage(
                    holder.binding.root.context,
                    holder.itemView,
                    data.cardWidth,
                    R.dimen.spacing_5
                )
            }
        }

        holder.binding.cardView.clipToPadding = model.data.clipToPadding == true
        holder.binding.cardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            dimensionRatio = data.cardRatio ?: "16:9"
        }

        holder.binding.rvPlayer.updateLayoutParams<ConstraintLayout.LayoutParams> {
            dimensionRatio = data.cardRatio ?: "16:9"
        }

        requestLayout()

        val parentId = (model.extraParams?.get(Constants.PARENT_ID) as? String).orDefaultValue()

        holder.binding.apply {

            tvTargetExam.isVisible = data.targetExam.isNullOrEmpty().not()
            tvTargetExam.text = data.targetExam.orEmpty()
            val examTagBg = data.bgExamTag.orDefaultValue(data.color.orEmpty())
            tvTargetExam.background = Utils.getShape(
                examTagBg,
                examTagBg,
                11f.dpToPx()
            )
            tvTargetExam.applyTextColor(data.textColorExamTag ?: "#FFFFFF")

            imageViewBackground.loadImage(data.imageBgCard)

            cardContainer.background = Utils.getGradientView(
                data.startGd.orDefaultValue("#e34c4c"),
                data.midGd.orDefaultValue("#e34c4c"),
                data.endGd.orDefaultValue("#e34c4c")
            )

            val endIdEndSidePair =
                if (model.data.textHorizontalBias == null || model.data.textHorizontalBias == 0.0f) {
                    Pair(guidelineFacultyImage.id, ConstraintSet.START)
                } else {
                    Pair(cardContainer.id, ConstraintSet.END)
                }

            val constraintSet = ConstraintSet()
            constraintSet.clone(cardContainer)
            listOf(
                textViewSubject,
                textViewTitleInfo,
                tvFacultyInfo,
                tvTitle3,
                tvTargetExam,
                tvBottomInfo
            ).forEach {
                it.applyTextGravity(model.data.textHorizontalBias)
                it.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    horizontalBias = model.data.textHorizontalBias ?: 0f
                }
                constraintSet.connect(
                    it.id,
                    ConstraintSet.END,
                    endIdEndSidePair.first,
                    endIdEndSidePair.second
                )
            }
            constraintSet.applyTo(cardContainer)

            textViewSubject.updateLayoutParams<ConstraintLayout.LayoutParams> {
                verticalBias = model.data.textVerticalBias ?: 0f
            }
            textViewSubject.isVisible = data.subject.isNullOrEmpty().not()
            textViewSubject.text = data.subject.orEmpty()
            textViewSubject.applyTextSize(data.subjectTextSize)
            textViewSubject.background = Utils.getShape(
                data.color.orEmpty(),
                data.color.orEmpty(),
                4f
            )

            tvTopTitle.apply {
                isVisible = data.topTitle.isNullOrEmpty().not()
                text = data.topTitle.orEmpty()
                applyTextSize(data.subjectTextSize)
                background = Utils.getShape(
                    data.color.orEmpty(),
                    data.color.orEmpty(),
                    4f
                )
            }

            textViewTitleInfo.isVisible = data.title1.isNullOrEmpty().not()
            textViewTitleInfo.text = data.title1.orEmpty()
            textViewTitleInfo.applyTextSize(data.title1TextSize)
            textViewTitleInfo.applyTextColor(data.title1TextColor)
            textViewTitleInfo.applyTypeface(data.title1IsBold)

            tvFacultyInfo.isVisible = data.title2.isNullOrEmpty().not()
            tvFacultyInfo.text = data.title2.orEmpty()
            tvFacultyInfo.applyTextSize(data.title2TextSize)
            tvFacultyInfo.applyTextColor(data.title2TextColor)
            tvFacultyInfo.applyTypeface(data.title2IsBold)

            tvTitle3.isVisible = data.title3.isNullOrEmpty().not()
            tvTitle3.text = data.title3.orEmpty()
            tvTitle3.applyTextSize(data.title3TextSize)
            tvTitle3.applyTextColor(data.title3TextColor)
            tvTitle3.applyTypeface(data.title3IsBold)

            tvBottomInfo.isVisible = data.title4.isNullOrEmpty().not()
            tvBottomInfo.text = data.title4.orEmpty()
            tvBottomInfo.applyTextSize(data.title4TextSize)
            tvBottomInfo.applyTextColor(data.title4TextColor)
            tvBottomInfo.applyTypeface(data.title4IsBold)

            imageViewFaculty.isVisible = data.imageUrl.isNullOrEmpty().not()
            imageViewFaculty.apply {
                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    verticalBias = data.imageVerticalBias ?: 0.5f
                }
                loadImage(data.imageUrl.orEmpty())
            }

            facultyImage.isVisible =  data.facultyImage.isNullOrEmpty().not()
            facultyImage.apply {
                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    dimensionRatio = data.facultyImageRatio ?: "2:3"
                }
                loadImage(data.facultyImage.orEmpty())
            }

            tvBottomLayoutTitle.isVisible = data.bottomLayout?.title.isNullOrEmpty().not()
            tvBottomLayoutTitle.text = data.bottomLayout?.title.orEmpty()
            tvBottomLayoutTitle.setTextColor(
                Color.parseColor(
                    data.bottomLayout?.titleColor.orDefaultValue(
                        "#000000"
                    )
                )
            )

            tvBottomLayoutTitle2.isVisible = data.bottomLayout?.subTitle.isNullOrEmpty().not()
            tvBottomLayoutTitle2.text = data.bottomLayout?.subTitle.orEmpty()
            tvBottomLayoutTitle2.applyTextColor(data.bottomLayout?.subTitleColor)
            ivBottomTitle2.isVisible = data.bottomLayout?.subTitle.isNullOrEmpty()
                .not() && data.bottomLayout?.iconSubtitle.isNullOrEmpty().not()
            ivBottomTitle2.loadImage(data.bottomLayout?.iconSubtitle)

            button.apply {
                setTextColor(Utils.parseColor(data.button?.textColor ?: "#ea532c"))
                background =
                    Utils.getShape(data.button?.backgroundColor ?: "#ffffff", "#ea532c", 4f)
                isVisible = data.button?.text.isNullOrBlank().not()
                text = data.button?.text.orEmpty()
                setOnClickListener {
                    deeplinkAction.performAction(holder.itemView.context, data.button?.deepLink)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                            hashMapOf<String, Any>(
                                EventConstants.WIDGET_TITLE to model.data.title1.orEmpty(),
                                EventConstants.WIDGET to TAG,
                                EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                                EventConstants.CTA_TEXT to data.button?.text.orEmpty(),
                                EventConstants.SOURCE to source.orEmpty(),
                            ).apply {
                                putAll(model.extraParams.orEmpty())
                            }
                        )
                    )
                }
            }

            btBottomLayoutText.text = data.bottomLayout?.button?.text.orEmpty()
            btBottomLayoutText.isAllCaps = data.bottomLayout?.button?.textAllCaps == null ||
                    data.bottomLayout.button.textAllCaps == true

            btBottomLayoutText.setTextColor(
                Color.parseColor(
                    data.bottomLayout?.button?.textColor.orDefaultValue(
                        "#ea532c"
                    )
                )
            )
            btBottomLayoutText.setBackgroundColor(
                Color.parseColor(
                    data.bottomLayout?.button?.backgroundColor.orDefaultValue(
                        "#ffffff"
                    )
                )
            )

            // if bottom text is empty hide
            if (data.bottomLayout?.title.isNullOrEmpty()
                && data.bottomLayout?.subTitle.isNullOrEmpty()
                && data.popularTag.isNullOrEmpty()
                && data.button?.text.isNullOrEmpty()
            ) {
                buttonsContainer.visibility = View.GONE
            } else {
                buttonsContainer.visibility = View.VISIBLE
            }

            ivVolume.isVisible =
                data.bottomLayout?.button?.showVolume == null || data.bottomLayout.button.showVolume == true
            if (data.videoResource?.resource == null) {
                // Special case when we don't want to play video in the card.
                rvPlayer.uniqueViewHolderId = data.id
                rvPlayer.url = null
            } else {
                rvPlayer.uniqueViewHolderId = data.id
                rvPlayer.url = data.videoResource.resource
                rvPlayer.drmScheme = data.videoResource.drmScheme
                rvPlayer.drmLicenseUrl = data.videoResource.drmLicenseUrl
                rvPlayer.mediaType = data.videoResource.mediaType.orDefaultValue(MEDIA_TYPE_BLOB)
                rvPlayer.isMute = data.defaultMute ?: true

                ivVolume.load(if (rvPlayer.isMute) R.drawable.ic_small_volume_mute else R.drawable.ic_small_volume_unmute)
                ivVolume.setOnClickListener {
                    performAction(MuteAutoPlayVideo(rvPlayer.isMute.not()))
                }

                // IMA ad tag
                val imaAdTagData = data.imaAdTagResource.orEmpty()
                val imaAdTag = if (imaAdTagData.isNotEmpty()) {
                    imaAdTagData[0].adTag.orEmpty()
                } else {
                    ""
                }
                val imaAdTimeout = if (imaAdTagData.isNotEmpty()) {
                    imaAdTagData[0].adTimeout ?: 0
                } else {
                    0
                }
                if (imaAdTag.isNotEmpty()) {
                    rvPlayer.imaAdTagUrl = imaAdTag
                }
                if (imaAdTimeout > 0) {
                    rvPlayer.imaAdMediaLoadTimeOut = imaAdTimeout
                }

                rvPlayer.listener = object : RvExoPlayerView.Listener {

                    override fun onBuffering(isBuffering: Boolean) {
                        super.onBuffering(isBuffering)
                        holder.binding.progressBar.setVisibleState(isBuffering)
                    }

                    override fun onPlayerReady() {
                        super.onPlayerReady()
                        holder.binding.progressBar.show()
                    }

                    override fun onStart() {
                        super.onStart()
                        togglePlayerUi(true, holder)
                        holder.binding.progressBar.hide()
                    }

                    override fun onError(error: ExoPlaybackException?) {
                        holder.binding.rvPlayer.removePlayer()
                        super.onError(error)
                    }

                    override fun onProgress(positionMs: Long) {
                        super.onProgress(positionMs)
                        data.videoResource.autoPlayDuration?.let {
                            if (positionMs >= it) {
                                holder.binding.rvPlayer.canResumePlayer = false
                                holder.binding.rvPlayer.removePlayer()
                            }
                        }
                    }

                    override fun onPause() {
                        super.onPause()
                        holder.binding.progressBar.hide()
                        togglePlayerUi(false, holder)
                    }

                    override fun onStop() {
                        super.onStop()
                        holder.binding.progressBar.hide()
                        togglePlayerUi(false, holder)

                        performAction(
                            AutoPlayVideoCompleted(
                                adapterPosition = holder.bindingAdapterPosition,
                                delayToMoveToNext = 1000L
                            )
                        )
                    }

                    override fun setVideoEngagementStatusListener(videoEngagementStats: ExoPlayerHelper.VideoEngagementStats) {
                        super.setVideoEngagementStatusListener(videoEngagementStats)
                        if (data.videoResource.resource.isNotBlank() && videoEngagementStats.engagementTime > 0L) {
                            analyticsPublisher.publishEvent(
                                AnalyticsEvent(
                                    EventConstants.LIVE_CLASS_AUTO_PLAYED,
                                    hashMapOf(
                                        EventConstants.EVENT_NAME_ID to data.id,
                                        EventConstants.ANSWER_VIDEO to data.videoResource.resource,
                                        EventConstants.SOURCE to data.page.orEmpty(),
                                        EventConstants.ENGAGEMENT_TIME to videoEngagementStats.engagementTime
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }

        holder.binding.btnReminder.apply {
            isVisible = data.showReminder == true

            isSelected = data.isReminderSet == 1

            setOnClickListener {
                checkInternetConnection(holder.itemView.context) {
                    isSelected = !isSelected
                    data.isReminderSet = if (isSelected) 1 else 0

                    markInterested(
                        id = data.id,
                        isReminder = true,
                        assortmentId = data.assortmentId.orEmpty(),
                        liveAt = data.liveAt.toString(),
                        reminderSet = if (isSelected) 1 else 0
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
            }
            DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams, model.type))
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.REMINDER_CARD_CLICK,
                    hashMapOf(
                        EventConstants.WIDGET to mWidgetName,
                        EventConstants.SUBJECT to data.subject.orEmpty(),
                        EventConstants.TEACHER_NAME to data.title2.orEmpty(),
                        EventConstants.BOARD to data.board.orEmpty()
                    )
                )
            )
        }

        holder.binding.root.setOnClickListener {
            holder.binding.rvPlayer.stopPlayer()

            DoubtnutApp.INSTANCE.bus()?.send(
                WidgetClickedEvent(
                    model.extraParams?.apply {
                        put(Constants.ID, data.id)
                    }
                )
            )

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.LIVE_CLASS_VIDEO_PLAYED,
                    hashMapOf<String, Any>(
                        EventConstants.CLICKED_ITEM_ID to data.id,
                        EventConstants.SOURCE to source.orEmpty(),
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
                deeplinkAction.performAction(holder.itemView.context, data.paymentDeeplink)
            } else {
                val currentContext = holder.itemView.context
                if (data.state == LIVE ||
                    DateUtils.isBeforeCurrentTime(data.liveAt) ||
                    data.state == PAST
                ) {
                    // allow to watch video
                    openVideoPage(
                        context = currentContext,
                        id = data.id,
                        page = data.page,
                        parentId = parentId
                    )
                } else {
                    // for future state
                    markInterested(
                        id = data.id,
                        isReminder = false,
                        assortmentId = data.assortmentId.orEmpty(),
                        liveAt = data.liveAt?.toString().orEmpty(),
                        reminderSet = 0
                    )
                    if (!data.isLastResource) {
                        deeplinkAction.performAction(holder.itemView.context, data.deeplink)
                    } else {
                        showToast(currentContext, currentContext.getString(R.string.coming_soon))
                    }
                }
            }

            performAction(
                AutoPlayVideoCompleted(
                    adapterPosition = holder.bindingAdapterPosition,
                    delayToMoveToNext = 3000L
                )
            )
        }

        if (model.data.viewsCount.isNotNullAndNotEmpty()) {
            layoutUsersWatching.visibility = View.VISIBLE
            holder.binding.textViewUsersWatched.visibility = View.VISIBLE
            holder.binding.textViewUsersWatched.text = model.data.viewsCount
        } else {
            layoutUsersWatching.visibility = View.GONE
        }
        if (model.data.popularTag.isNotNullAndNotEmpty()) {
            holder.binding.apply {
                layoutPopularity.visibility = View.VISIBLE
                textViewPopularityTag.visibility = View.VISIBLE
                textViewPopularityTag.text = model.data.popularTag
            }
        } else {
            holder.binding.apply {
                layoutPopularity.visibility = View.GONE
                textViewPopularityTag.visibility = View.GONE
            }
        }

        trackingViewId = data.id

        return holder
    }

    @Suppress("SameParameterValue")
    private fun markInterested(
        id: String,
        isReminder: Boolean,
        assortmentId: String,
        liveAt: String?,
        reminderSet: Int?
    ) {
        DataHandler.INSTANCE.courseRepository.markInterested(
            resourceId = id,
            isReminder = isReminder,
            assortmentId = assortmentId,
            liveAt = liveAt,
            reminderSet = reminderSet
        )
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({})
    }

    private fun openVideoPage(
        context: Context,
        id: String?,
        page: String?,
        parentId: String = ""
    ) {
        context.startActivity(
            VideoPageActivity.startActivity(
                context = context,
                questionId = id.orEmpty(),
                page = page.orEmpty(),
                parentId = parentId
            )
        )
    }

    private fun togglePlayerUi(isPlaying: Boolean, holder: WidgetHolder) {
        if (isPlaying) {
            holder.binding.rvPlayer.show()
            holder.binding.imageViewPlay.hide()
        } else {
            holder.binding.rvPlayer.hide()
            holder.binding.imageViewPlay.show()
        }
    }

    class WidgetHolder(
        binding: WidgetLiveClassCarouselCardAutoplayBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetLiveClassCarouselCardAutoplayBinding>(binding, widget) {

        override fun bindItemPayload(payload: Any) {
            if (payload is RvMuteStatus) {
                binding.rvPlayer.isMute = payload.isMute
                binding.ivVolume.load(if (payload.isMute) R.drawable.ic_small_volume_mute else R.drawable.ic_small_volume_unmute)
            }
        }
    }

    class AutoplayChildWidgetModel : WidgetEntityModel<AutoplayChildWidgetData, WidgetAction>()

    @Keep
    data class AutoplayChildWidgetData(
        @SerializedName("id") val id: String,
        @SerializedName("assortment_id") val assortmentId: String?,
        @SerializedName("page") val page: String?,
        @SerializedName("top_title") val topTitle: String?,
        @SerializedName("text_vertical_bias") val textVerticalBias: Float?,
        @SerializedName("text_horizontal_bias") val textHorizontalBias: Float?,
        @SerializedName("title1") val title1: String?,
        @SerializedName("title1_text_size") val title1TextSize: String?,
        @SerializedName("title1_text_color") val title1TextColor: String?,
        @SerializedName("title1_is_bold") val title1IsBold: Boolean?,
        @SerializedName("title2") val title2: String?,
        @SerializedName("title2_text_size") val title2TextSize: String?,
        @SerializedName("title2_text_color") val title2TextColor: String?,
        @SerializedName("title2_is_bold") val title2IsBold: Boolean?,
        @SerializedName("title3") val title3: String?,
        @SerializedName("title3_text_size") val title3TextSize: String?,
        @SerializedName("title3_text_color") val title3TextColor: String?,
        @SerializedName("title3_is_bold") val title3IsBold: Boolean?,
        @SerializedName("title4") val title4: String?,
        @SerializedName("title4_text_size") val title4TextSize: String?,
        @SerializedName("title4_text_color") val title4TextColor: String?,
        @SerializedName("title4_is_bold") val title4IsBold: Boolean?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("faculty_image") val facultyImage: String?,
        @SerializedName("faculty_image_ratio") val facultyImageRatio: String?,
        @SerializedName("is_live") val isLive: Boolean?,
        @SerializedName("subject") val subject: String?,
        @SerializedName("subject_text_size") val subjectTextSize: String?,
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
        @SerializedName("button2") val button: ButtonData?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("bottom_layout") val bottomLayout: BottomLayout?,
        @SerializedName("video_resource") val videoResource: ApiVideoResource?,
        @SerializedName("default_mute") var defaultMute: Boolean?,
        @SerializedName("start_gd") val startGd: String?,
        @SerializedName("mid_gd") val midGd: String?,
        @SerializedName("end_gd") val endGd: String?,
        @SerializedName("board") val board: String?,
        @SerializedName("remaining") val remaining: String?,
        @SerializedName("is_last_resource") val isLastResource: Boolean,
        @SerializedName("image_vertical_bias") val imageVerticalBias: Float?,
        @SerializedName("target_exam") val targetExam: String?,
        @SerializedName("bg_exam_tag") val bgExamTag: String?,
        @SerializedName("text_color_exam_tag") val textColorExamTag: String?,
        @SerializedName("clip_to_padding") var clipToPadding: Boolean?,
        @SerializedName("popular_tag") val popularTag: String?,
        @SerializedName("views") val viewsCount: String?,
        @SerializedName("adTagResource") val imaAdTagResource: List<ImaAdTagResource>?,
        var flagrId: String?,
        var variantId: String?
    ) : WidgetData()

    @Keep
    data class ApiVideoResource(
        @SerializedName("resource") val resource: String?,
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
        @SerializedName("icon_subtitle") val iconSubtitle: String?,
        @SerializedName("button") val button: ButtonData?
    )

    @Keep
    data class ButtonData(
        @SerializedName("text") val text: String?,
        @SerializedName("text_color") val textColor: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("border_color") val borderColor: String?,
        @SerializedName("action") val action: WidgetAction?,
        @SerializedName("text_all_caps") val textAllCaps: Boolean?,
        @SerializedName("show_volume") val showVolume: Boolean?,
        @SerializedName("deep_link") val deepLink: String?
    )
}
