package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.AutoPlayVideoCompleted
import com.doubtnutapp.base.MuteAutoPlayVideo
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.WidgetCourseAutoPlayBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.libraryhome.coursev3.ui.CourseActivityV3
import com.doubtnutapp.liveclass.ui.LiveClassActivity
import com.doubtnutapp.resultpage.ui.ResultPageActivity
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
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseAutoPlayChildWidget(context: Context) :
    BaseBindingWidget<CourseAutoPlayChildWidget.WidgetHolder,
            CourseAutoPlayChildWidget.CourseAutoPlayChildWidgetModel,
            WidgetCourseAutoPlayBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "CourseAutoPlayWidget"
        const val FUTURE = 0
        const val LIVE = 1
        const val PAST = 2
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    private val mWidgetName: String = TAG
    var source: String? = null

    override fun getViewBinding(): WidgetCourseAutoPlayBinding {
        return WidgetCourseAutoPlayBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: CourseAutoPlayChildWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        setMargins(WidgetLayoutConfig(0, 9, 0, 0))

        val data: CourseAutoPlayChildWidgetData = model.data
        val binding = holder.binding
        if (model.data.clipToPadding != true) {
            Utils.setWidthBasedOnPercentage(
                binding.root.context,
                holder.itemView,
                data.cardWidth,
                R.dimen.spacing_5
            )
        }

        binding.cardView.clipToPadding = model.data.clipToPadding == true

        binding.cardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            dimensionRatio = data.cardRatio ?: "16:9"
        }

        binding.rvPlayer.updateLayoutParams<ConstraintLayout.LayoutParams> {
            dimensionRatio = data.cardRatio ?: "16:9"
        }

        requestLayout()

        val parentId = (model.extraParams?.get(Constants.PARENT_ID) as? String).orDefaultValue()

        binding.apply {

            imageViewBackground.loadImage(data.imageBgCard)

            textViewTopTitle.text = data.topTitle.orEmpty()
            if (data.topTitle.isNullOrBlank()) {
                textViewTopTitle.invisible()
            } else {
                textViewTopTitle.show()
            }

            textViewSubject.text = data.subject.orEmpty()
            textViewTitleInfo.text = data.title1.orEmpty()
            tvFacultyInfo.text = data.title2.orEmpty()

            cardContainer.background = Utils.getGradientView(
                data.startGd.orDefaultValue("#e34c4c"),
                data.midGd.orDefaultValue("#e34c4c"),
                data.endGd.orDefaultValue("#e34c4c")
            )

            textViewSubject.background = Utils.getShape(
                data.color.orEmpty(),
                data.color.orEmpty(),
                4f
            )

            imageViewFaculty.apply {
                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    verticalBias = data.imageVerticalBias ?: 0.5f
                }
                loadImage(data.imageUrl.ifEmptyThenNull())
            }

            rvPlayer.uniqueViewHolderId = data.id
            rvPlayer.url = data.videoResource.resource
            rvPlayer.drmScheme = data.videoResource.drmScheme
            rvPlayer.drmLicenseUrl = data.videoResource.drmLicenseUrl
            rvPlayer.mediaType = data.videoResource.mediaType.orDefaultValue(MEDIA_TYPE_BLOB)
            rvPlayer.isMute = data.defaultMute ?: true

            ivVolume.load(if (rvPlayer.isMute) R.drawable.ic_small_volume_mute else R.drawable.ic_small_volume_unmute)
            ivVolume.setOnClickListener {
                performAction(MuteAutoPlayVideo(rvPlayer.isMute.not()))
                if (source == ResultPageActivity.TAG) {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            CoreEventConstants.TESTIMONIAL_VIDEO_MUTE_CLICK,
                            hashMapOf(
                                CoreEventConstants.SOURCE to source.orEmpty(),
                                CoreEventConstants.QUESTION_ID to data.id,
                                CoreEventConstants.ITEM_POSITION to holder.bindingAdapterPosition
                            ), ignoreFacebook = true
                        )
                    )
                }
            }

            rvPlayer.listener = object : RvExoPlayerView.Listener {

                override fun onBuffering(isBuffering: Boolean) {
                    super.onBuffering(isBuffering)
                    binding.progressBar.setVisibleState(isBuffering)
                }

                override fun onPlayerReady() {
                    super.onPlayerReady()
                    binding.progressBar.show()
                }

                override fun onStart() {
                    super.onStart()
                    togglePlayerUi(true, holder, model)
                    binding.progressBar.hide()
                    if (source == ResultPageActivity.TAG) {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                CoreEventConstants.TESTIMONIAL_VIDEO_AUTOPLAYED,
                                hashMapOf(
                                    CoreEventConstants.SOURCE to source.orEmpty(),
                                    CoreEventConstants.QUESTION_ID to data.id,
                                    CoreEventConstants.ITEM_POSITION to holder.bindingAdapterPosition
                                ), ignoreFacebook = true
                            )
                        )
                    }
                }

                override fun onError(error: ExoPlaybackException?) {
                    binding.rvPlayer.removePlayer()
                    super.onError(error)
                }

                override fun onProgress(positionMs: Long) {
                    super.onProgress(positionMs)
                    data.videoResource.autoPlayDuration?.let {
                        if (positionMs >= it) {
                            binding.rvPlayer.canResumePlayer = false
                            binding.rvPlayer.removePlayer()
                        }
                    }
                }

                override fun onPause() {
                    super.onPause()
                    binding.progressBar.hide()
                    togglePlayerUi(false, holder, model)
                }

                override fun onStop() {
                    super.onStop()
                    binding.progressBar.hide()
                    togglePlayerUi(false, holder, model)

                    performAction(
                        AutoPlayVideoCompleted(
                            adapterPosition = holder.adapterPosition,
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

        binding.cardView.setOnClickListener {
            if (model.data.disableClick == true) {
                binding.rvPlayer.playerView?.player?.seekTo(0)
                binding.rvPlayer.playerView?.player?.prepare()
                return@setOnClickListener
            }

            if (source == CourseBottomSheetFragment.TAG) {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.MPVP_COURSE_BOTTOMSHEET_INTRO_VIDEO_CLICKED,
                        hashMapOf(
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                            EventConstants.FLAG_ID to model.data.flagrId.orEmpty(),
                            EventConstants.VARIANT_ID to model.data.variantId.orEmpty(),
                        )
                    )
                )
            }

            if (source == ResultPageActivity.TAG) {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        CoreEventConstants.TESTIMONIAL_VIDEO_CLICKED,
                        hashMapOf(
                            CoreEventConstants.SOURCE to source.orEmpty(),
                            CoreEventConstants.QUESTION_ID to data.id,
                            CoreEventConstants.ITEM_POSITION to holder.bindingAdapterPosition
                        ), ignoreFacebook = true
                    )
                )
            }

            binding.rvPlayer.stopPlayer()

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.LC_COURSE_CAROUSAL_ITEM_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.CLICKED_ITEM_ID to data.id,
                    ).apply {
                        putAll(model.extraParams ?: hashMapOf<String, String>())
                    }
                )
            )

            if (data.isPremium == true && data.isVip != true) {
                deeplinkAction.performAction(
                    binding.root.context,
                    data.paymentDeeplink,
                    source.orEmpty()
                )
            } else {
                val currentContext = holder.itemView.context
                if (data.state == LIVE ||
                    DateUtils.isBeforeCurrentTime(data.liveAt) ||
                    data.state == PAST
                ) {
                    // allow to watch video

                    if (currentContext is LiveClassActivity) {
                        currentContext.finish()
                    } else if (currentContext is VideoPageActivity) {
                        currentContext.finish()
                    }

                    var page = data.page
                    if (source == Constants.PAGE_SEARCH_SRP) {
                        page = Constants.PAGE_SEARCH_SRP
                    }
                    openVideoPage(
                        context = currentContext,
                        id = data.id,
                        page = page,
                        parentId = parentId
                    )
                } else {
                    // for future state
                    markInterested(
                        data.id, false,
                        data.assortmentId.orEmpty(),
                        data.liveAt?.toString().orEmpty(),
                        0
                    )
                    showToast(currentContext, currentContext.getString(R.string.coming_soon))
                }
            }

            performAction(
                AutoPlayVideoCompleted(
                    adapterPosition = holder.adapterPosition,
                    delayToMoveToNext = 3000L
                )
            )
            if (source == ResultPageActivity.TAG) {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        CoreEventConstants.TESTIMONIAL_VIDEO_CLICKED,
                        hashMapOf(
                            CoreEventConstants.SOURCE to source.orEmpty(),
                            CoreEventConstants.QUESTION_ID to data.id,
                            CoreEventConstants.ITEM_POSITION to holder.bindingAdapterPosition
                        )
                    )
                )
            }
        }

        return holder
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

    private fun openCoursePage(context: Context, id: String?) {
        CourseActivityV3.startActivity(context, true, id.orEmpty(), source.orEmpty())
    }

    private fun openVideoPage(
        context: Context,
        id: String?,
        page: String?,
        parentId: String = "",
    ) {
        context.startActivity(
            VideoPageActivity.startActivity(
                context = context,
                questionId = id.orEmpty(),
                page = page.orEmpty(),
                source = source.orEmpty(),
                parentId = parentId
            )
        )
    }

    private fun togglePlayerUi(
        isPlaying: Boolean,
        holder: WidgetHolder,
        model: CourseAutoPlayChildWidgetModel
    ) {
        if (isPlaying) {
            holder.binding.rvPlayer.show()
            holder.binding.ivVolume.show()
            holder.binding.imageViewPlay.hide()
        } else {
            holder.binding.rvPlayer.hide()
            holder.binding.ivVolume.hide()
            holder.binding.imageViewPlay.show()
        }
    }

    class WidgetHolder(binding: WidgetCourseAutoPlayBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseAutoPlayBinding>(binding, widget) {

        override fun bindItemPayload(payload: Any) {
            if (payload is RvMuteStatus) {
                binding.rvPlayer.isMute = payload.isMute
                binding.ivVolume.load(if (payload.isMute) R.drawable.ic_small_volume_mute else R.drawable.ic_small_volume_unmute)
            }
        }
    }

    class CourseAutoPlayChildWidgetModel :
        WidgetEntityModel<CourseAutoPlayChildWidgetData, WidgetAction>()

    @Keep
    data class CourseAutoPlayChildWidgetData(
        @SerializedName("question_id") val id: String,
        @SerializedName("assortment_id") val assortmentId: String?,
        @SerializedName("page") val page: String?,
        @SerializedName("title1") val title1: String?,
        @SerializedName("title2") val title2: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("subject") val subject: String?,
        @SerializedName("color") val color: String?,
        @SerializedName("player_type") val playerType: String?,
        @SerializedName("live_at") val liveAt: Long?,
        @SerializedName("image_bg_card") val imageBgCard: String?,
        @SerializedName("interested") val interested: Long?,
        @SerializedName("is_premium") val isPremium: Boolean?,
        @SerializedName("is_vip") val isVip: Boolean?,
        @SerializedName("state") val state: Int,
        @SerializedName("payment_deeplink") val paymentDeeplink: String?,
        @SerializedName("card_width") val cardWidth: String,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("video_resource") val videoResource: ApiVideoResource,
        @SerializedName("default_mute") var defaultMute: Boolean?,
        @SerializedName("start_gd") val startGd: String?,
        @SerializedName("mid_gd") val midGd: String?,
        @SerializedName("end_gd") val endGd: String?,
        @SerializedName("board") val board: String?,
        @SerializedName("image_vertical_bias") val imageVerticalBias: Float?,
        @SerializedName("top_title1") val topTitle: String?,
        @SerializedName("clip_to_padding") var clipToPadding: Boolean?,
        @SerializedName("disable_click") var disableClick: Boolean?,

        var flagrId: String?,
        var variantId: String?
    ) : WidgetData()

    @Keep
    data class ApiVideoResource(
        @SerializedName("resource") val resource: String,
        @SerializedName("drm_scheme") val drmScheme: String?,
        @SerializedName("drm_license_url") val drmLicenseUrl: String?,
        @SerializedName("auto_play_duration") val autoPlayDuration: Long?,
        @SerializedName("media_type") val mediaType: String?,
    )
}
