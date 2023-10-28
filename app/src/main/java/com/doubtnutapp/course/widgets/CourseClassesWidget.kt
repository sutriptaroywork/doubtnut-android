package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.OnNextVideoClicked
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.WidgetCourseClassBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.downloadedVideos.ExoDownloadTracker
import com.doubtnutapp.utils.BannerActionUtils.deeplinkAction
import com.doubtnutapp.utils.DateUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.doubtnutapp.widgetmanager.widgets.RelatedLecturesWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseClassesWidget(context: Context) : BaseBindingWidget<CourseClassesWidget.WidgetHolder,
    CourseClassesWidgetModel, WidgetCourseClassBinding>(context) {

    companion object {
        const val TAG = "CourseClassesWidget"
        const val FUTURE = 0
        const val LIVE = 1
        const val PAST = 2
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher
    var source: String? = ""

    var adapter: WidgetLayoutAdapter? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetCourseClassBinding {
        return WidgetCourseClassBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CourseClassesWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data: CourseClassesWidgetData = model.data
        holder.binding.textViewTitle.setTextColor(Utils.parseColor(data.color, Color.BLACK))
        holder.binding.root.background =
            Utils.getShape("#ffffff", data.color.orDefaultValue("#000000"))
        holder.binding.textViewTitle.text = data.title.orEmpty()
        holder.binding.textViewSubtitle.text = data.subtitle.orEmpty()
        holder.binding.imageViewDownload.isVisible = model.data.showDownload == true
        holder.binding.root.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.LC_COURSE_VIDEO_PLAY_ITEM,
                    hashMapOf<String, Any>(
                        EventConstants.EVENT_NAME_ID to data.id.toString()
                    ).apply {
                        putAll(model.extraParams ?: hashMapOf())
                    }
                )
            )
            playVideo(model.data, holder)
        }
        holder.binding.imageViewDownload.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.EVENT_OFFLINE_DOWNLOAD_START,
                    ignoreFirebase = false,
                    params = hashMapOf(Constants.QUESTION_ID to data.id.orEmpty()),
                    ignoreSnowplow = true
                )
            )
            downloadVideo(holder, data.id.orEmpty(), data.title.orEmpty())
        }
        when {
            data.state == FUTURE -> {
                holder.binding.imageViewPlay.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_icon_small_notify_me
                    )
                )
            }
            data.isViewed == true -> {
                holder.binding.imageViewPlay.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_replay
                    )
                )
            }
        }
        holder.binding.textViewTime.text = data.time.orEmpty()
        holder.binding.textViewStatus.text = data.status.orEmpty()

        holder.binding.ivBookMark.isVisible = data.isBookmarked != null
        setIsSelected(holder.binding.ivBookMark, data.isBookmarked ?: false)
        holder.binding.ivBookMark.setOnClickListener {
            if (data.isBookmarked == true) {
                AlertDialog.Builder(context).apply {
                    setMessage(holder.binding.root.context.getString(R.string.question_remove_bookmark))
                    setPositiveButton(
                        context.getString(R.string.string_yes)
                    ) { dialog, _ ->
                        dialog.dismiss()
                        toggleBookmark(model)
                    }
                    setNegativeButton(
                        context.getString(R.string.string_no)
                    ) { dialog, _ ->
                        dialog.dismiss()
                    }
                    show()
                }
            } else {
                toggleBookmark(model)
            }
        }
        return holder
    }

    private fun toggleBookmark(model: CourseClassesWidgetModel) {
        model.data.isBookmarked = !(model.data.isBookmarked ?: return)

        val eventName = if (model.data.isBookmarked == true) {
            EventConstants.DOUBT_BOOKMARKED
        } else {
            EventConstants.DOUBT_UNBOOKMARKED
        }

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                hashMapOf(
                    EventConstants.WIDGET to TAG,
                    EventConstants.ID to model.data.id.orEmpty()
                )
            )
        )
        DataHandler.INSTANCE.commentRepository.bookmark(
            model.data.id.orEmpty(),
            model.data.assortmentId.orEmpty()
        ).applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({})
        adapter?.removeWidget(model)
    }

    private fun playVideo(data: CourseClassesWidgetData, holder: WidgetHolder) {
        if (!data.deeplink.isNullOrEmpty()) {
            deeplinkAction.performAction(
                holder.binding.root.context,
                data.deeplink,
                source.orEmpty()
            )
        } else {
            if (data.isPremium == true && data.isVip != true) {
                deeplinkAction.performAction(holder.binding.root.context, data.paymentDeeplink)
            } else {
                val currentContext = holder.itemView.context
                if (data.state == RelatedLecturesWidget.LIVE ||
                    DateUtils.isBeforeCurrentTime(data.liveAt?.toLongOrNull()) ||
                    data.state == RelatedLecturesWidget.PAST
                ) {
                    // allow to watch video
                    var pageSource = data.page
                    if (Constants.PAGE_SEARCH_SRP == source) {
                        pageSource = Constants.PAGE_SEARCH_SRP
                    }
                    openVideoPage(currentContext, data.id, pageSource)
                } else {
                    // for future state
                    markInterested(data.id.orEmpty(), false, data.assortmentId.orEmpty(), data.liveAt, 0)
                    showToast(
                        currentContext,
                        data.reminderMessage.orDefaultValue(currentContext.getString(R.string.coming_soon))
                    )
                }
            }
        }
    }

    private fun downloadVideo(holder: WidgetHolder, questionId: String, title: String) {
        val downloadTracker = ExoDownloadTracker.getInstance(holder.binding.root.context)
        downloadTracker.downloadVideo(holder.binding.root.context, questionId, title)
    }

    private fun openVideoPage(context: Context, id: String?, page: String?) {
        actionPerformer?.performAction(OnNextVideoClicked())
        context.startActivity(
            VideoPageActivity.startActivity(
                context = context,
                questionId = id.orEmpty(),
                page = page.orEmpty()
            )
        )
    }

    private fun markInterested(
        id: String,
        @Suppress("SameParameterValue") isReminder: Boolean,
        assortmentId: String,
        liveAt: String?,
        reminderSet: Int?
    ) {
        DataHandler.INSTANCE.courseRepository.markInterested(id, isReminder, assortmentId, liveAt, reminderSet)
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({})
    }

    class WidgetHolder(binding: WidgetCourseClassBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseClassBinding>(binding, widget)
}

class CourseClassesWidgetModel : WidgetEntityModel<CourseClassesWidgetData, WidgetAction>()

@Keep
data class CourseClassesWidgetData(
    @SerializedName("title1") val title: String?,
    @SerializedName("title2") val subtitle: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("is_downloadable") val showDownload: Boolean?,
    @SerializedName("id") val id: String?,
    @SerializedName("state") val state: Int,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("is_premium") val isPremium: Boolean?,
    @SerializedName("is_vip") val isVip: Boolean?,
    @SerializedName("payment_deeplink") val paymentDeeplink: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("page") val page: String?,
    @SerializedName("live_at") val liveAt: String?,
    @SerializedName("live_at_date") val time: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("is_bookmark") var isBookmarked: Boolean?,
    @SerializedName("is_viewed") val isViewed: Boolean?,
    @SerializedName("reminder_message") val reminderMessage: String?,
) : WidgetData()
