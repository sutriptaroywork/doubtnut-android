package com.doubtnutapp.course.widgets

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent

import com.doubtnutapp.base.RefreshUI
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.ItemLiveClassCarouselBinding
import com.doubtnutapp.databinding.WidgetLiveClassCarouselBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.downloadedVideos.ExoDownloadTracker
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

class LiveClassCarouselWidget(context: Context) :
    BaseBindingWidget<LiveClassCarouselWidget.WidgetHolder,
        LiveClassCarouselWidgetModel, WidgetLiveClassCarouselBinding>(context) {

    companion object {
        const val FUTURE = 0
        const val LIVE = 1
        const val PAST = 2
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: LiveClassCarouselWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data: LiveClassCarouselWidgetData = model.data
        val binding = holder.binding

        binding.textViewTitleMain.text = data.title.orEmpty()

        if (data.showViewAll.toBoolean()) {
            binding.textViewViewAll.show()
            binding.textViewViewAll.setOnClickListener {
                deeplinkAction.performAction(it.context, data.deeplink, model.type)
            }
        } else {
            binding.textViewViewAll.hide()
        }

        val setWidth: Boolean
        val orientation: Int
        if (model.data.scrollDirection == "vertical") {
            orientation = RecyclerView.VERTICAL
            setWidth = false
        } else {
            orientation = RecyclerView.HORIZONTAL
            setWidth = true
        }

        binding.rvItems.layoutManager = LinearLayoutManager(context, orientation, false)
        val actionActivity = model.action?.actionActivity.orDefaultValue()
        binding.rvItems.adapter = Adapter(
            data.items.orEmpty(),
            actionActivity, analyticsPublisher, deeplinkAction, setWidth, actionPerformer, source.orEmpty()
        )
        trackingViewId = data.id
        return holder
    }

    class Adapter(
        val items: List<LiveClassCarouselItem>,
        val actionActivity: String,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val setWidth: Boolean,
        val actionPerformer: ActionPerformer?,
        val source: String?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemLiveClassCarouselBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (setWidth && items.size > 1) {
                Utils.setWidthBasedOnPercentage(holder.itemView.context, holder.itemView, "1.1", R.dimen.spacing)
            }
            val binding = holder.binding
            binding.textViewSubject.background = Utils.getShape(
                items[position].color.orEmpty(),
                items[position].color.orEmpty(),
                4f
            )

            binding.button.background = Utils.getShape(
                "#ffffff",
                "#000000",
                8f,
                2
            )

            binding.buttonReminder.background = Utils.getShape(
                "#ffffff",
                "#ea532c",
                8f,
                1
            )

            binding.buttonDownload.background = Utils.getShape(
                "#ffffff",
                "#ea532c",
                8f,
                1
            )

            if (items[position].isDownloadable) {
                binding.buttonDownload.show()
            } else {
                binding.buttonDownload.hide()
            }

            binding.textViewTitleInfo.text = items[position].title1.orEmpty()
            binding.textViewSubject.text = items[position].subject
            binding.textViewBottomInfo.text = items[position].bottomTitle.orEmpty()

            val timeText = if (!items[position].remaining.isNullOrBlank()) {
                items[position].remaining.orEmpty()
            } else {
                items[position].topTitle.orEmpty()
            }

            binding.textViewTimeInfo.text = timeText
            binding.textViewDuration.toggleVisibilityAndSetText(items[position].duration)

            if (items[position].state == LIVE) {
                binding.textViewTimeInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_live_icon_small, 0, 0, 0)
                binding.textViewTimeInfo.setTextColor(Utils.parseColor("#ffffff"))
            } else {
                binding.textViewTimeInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calendar, 0, 0, 0)
                binding.textViewTimeInfo.setTextColor(Utils.parseColor("#ffc700"))
            }

            binding.textViewFacultyInfo.text = items[position].title2

            binding.imageViewFaculty.loadImageEtx(items[position].imageUrl.orEmpty())
            binding.imageViewBackground.loadImageEtx(items[position].imageBgCard.orEmpty())

            binding.textViewRegisteredUser.text = items[position].interested.orEmpty()
            when (items[position].lockState) {
                1 -> {
                    binding.textViewRegisteredUser.visibility = View.INVISIBLE
                    binding.imageViewLock.setImageDrawable(
                        ContextCompat.getDrawable(
                            holder.itemView.context,
                            R.drawable.ic_tag_light_locked
                        )
                    )
                }
                2 -> {
                    binding.textViewRegisteredUser.visibility = View.INVISIBLE
                    binding.imageViewLock.setImageDrawable(
                        ContextCompat.getDrawable(
                            holder.itemView.context,
                            R.drawable.ic_tag_light_unlocked
                        )
                    )
                }
                else -> {
                    binding.imageViewLock.setImageDrawable(null)
                    if (items[position].interested.isNullOrBlank()) {
                        binding.textViewRegisteredUser.visibility = View.INVISIBLE
                    } else {
                        binding.textViewRegisteredUser.visibility = View.VISIBLE
                    }
                }
            }

            if (items[position].showReminder == true) {
                binding.buttonReminder.visibility = View.VISIBLE
            } else {
                binding.buttonReminder.visibility = View.GONE
            }

            binding.buttonReminder.isSelected = items[position].isReminderSet == 1

            binding.button.text = items[position].button?.text.orEmpty()

            if (items[position].button?.text.isNullOrBlank()) {
                binding.button.visibility = View.INVISIBLE
            } else {
                binding.button.visibility = View.VISIBLE
            }

            binding.buttonDownload.isVisible = FeaturesManager.isFeatureEnabled(holder.itemView.context, Features.OFFLINE_VIDEOS) && items[position].isDownloadable

            binding.buttonDownload.setOnClickListener {
                val isSubscribed = items[position].isVip == true
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_OFFLINE_DOWNLOAD_START,
                        ignoreFirebase = false,
                        params = hashMapOf(
                            Constants.IS_SUBSCRIBED to isSubscribed,
                            Constants.QUESTION_ID to items[position].id.orEmpty()
                        ),
                        ignoreSnowplow = true
                    )
                )
                if (isSubscribed) {
                    downloadVideo(holder, items[position])
                } else {
                    ToastUtils.makeText(holder.itemView.context, "You're not subscribed to this course, subscribe to download", Toast.LENGTH_SHORT).show()
                    deeplinkAction.performAction(holder.itemView.context, items[position].paymentDeeplink)
                }
            }

            binding.buttonReminder.setOnClickListener {
                checkInternetConnection(holder.itemView.context) {
                    it.isSelected = !it.isSelected
                    items[position].isReminderSet = if (it.isSelected) 1 else 0

                    if (it.isSelected) {
                        (holder.itemView.context as? Activity)?.let { context ->
                            Snackbar.make(
                                context.findViewById(android.R.id.content),
                                items.getOrNull(position)
                                    ?.reminderMessage
                                    .orDefaultValue("Your reminder has been set"),
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

                    markInterested(
                        items[position].id.orEmpty(), true,
                        items[position].assortmentId.orEmpty(), items[position].liveAt, if (it.isSelected) 1 else 0
                    )
                }
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.REMINDER_CARD_CLICK,
                        hashMapOf(
                            EventConstants.WIDGET to "LiveClassCarouselWidget",
                            EventConstants.SUBJECT to items[position].subject.orEmpty(),
                            EventConstants.TEACHER_NAME to items[position].title2.orEmpty(),
                            EventConstants.BOARD to items[position].board.orEmpty()
                        ),
                        ignoreSnowplow = true
                    )
                )
            }

            binding.cardContainer.background = Utils.getGradientView(
                items[position].startGd.orDefaultValue("#232a4f"),
                items[position].midGd.orDefaultValue("#232a4f"),
                items[position].endGd.orDefaultValue("#232a4f")
            )

            holder.itemView.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.LIVE_FREE_VIDEO_CLICK,
                        hashMapOf(
                            EventConstants.SOURCE to items[position].id.orEmpty(),
                            EventConstants.WIDGET to "LiveClassCarouselWidget",
                            EventConstants.SUBJECT to items[position].subject.orEmpty(),
                            EventConstants.TEACHER_NAME to items[position].title2.orEmpty(),
                            EventConstants.BOARD to items[position].board.orEmpty()
                        ),
                        ignoreSnowplow = true
                    )
                )
                val item = items[position]
                if (item.isPremium == true && item.isVip != true) {
                    deeplinkAction.performAction(holder.itemView.context, item.paymentDeeplink)
                } else if (items[position].showEMIDialog == true) {
                    val emiReminderDialog = EMIReminderDialog.newInstance(Integer.parseInt(item.assortmentId.orEmpty()))
                    emiReminderDialog.show((holder.itemView.context as AppCompatActivity).supportFragmentManager, EMIReminderDialog.TAG)
                    (holder.itemView.context as AppCompatActivity).supportFragmentManager.executePendingTransactions()
                    emiReminderDialog.dialog?.setOnDismissListener {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.EMI_REMINDER_CLOSE,
                                hashMapOf<String, Any>(
                                    EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty()
                                )
                            )
                        )
                        emiReminderDialog.dismiss()
                        actionPerformer?.performAction(RefreshUI())
                        playVideo(item, holder)
                    }
                } else {
                    playVideo(item, holder)
                }
            }

            binding.button.setOnClickListener {
                deeplinkAction.performAction(holder.itemView.context, items[position].button?.deeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.LIVE_CLASS_GO_TO_COURSE_CLICK,
                        hashMapOf(
                            EventConstants.WIDGET to "LiveClassCarouselWidget",
                            EventConstants.SUBJECT to items[position].subject.orEmpty(),
                            EventConstants.TEACHER_NAME to items[position].title2.orEmpty(),
                            EventConstants.BOARD to items[position].board.orEmpty()
                        ),
                        ignoreSnowplow = true
                    )
                )
            }
        }

        private fun playVideo(data: LiveClassCarouselItem, holder: RecyclerView.ViewHolder) {
            val currentContext = holder.itemView.context
            if (data.state == LIVE ||
                DateUtils.isBeforeCurrentTime(data.liveAt?.toLongOrNull()) ||
                data.state == PAST
            ) {
                // allow to watch video
                openVideoPage(currentContext, data.id, data.page)
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

        private fun downloadVideo(holder: RecyclerView.ViewHolder, liveClassCarouselItem: LiveClassCarouselItem) {
            val downloadTracker = ExoDownloadTracker.getInstance(holder.itemView.context)
            downloadTracker.downloadVideo(holder.itemView.context, liveClassCarouselItem.id.orEmpty(), liveClassCarouselItem.topTitle.orEmpty())
        }

        private fun markInterested(id: String, isReminder: Boolean, assortmentId: String, liveAt: String?, reminderSet: Int?) {
            DataHandler.INSTANCE.courseRepository.markInterested(id, isReminder, assortmentId, liveAt, reminderSet)
                .applyIoToMainSchedulerOnCompletable()
                .subscribeToCompletable({})
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

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemLiveClassCarouselBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(
        binding: WidgetLiveClassCarouselBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetLiveClassCarouselBinding>(binding, widget)

    override fun getViewBinding(): WidgetLiveClassCarouselBinding {
        return WidgetLiveClassCarouselBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class LiveClassCarouselWidgetModel : WidgetEntityModel<LiveClassCarouselWidgetData, WidgetAction>()

@Keep
data class LiveClassCarouselWidgetData(
    @SerializedName("items") val items: List<LiveClassCarouselItem>?,
    @SerializedName("title") val title: String?,
    @SerializedName("_id") val id: String,
    @SerializedName("link_text") val linkText: String?,
    @SerializedName("scroll_direction") val scrollDirection: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("show_view_all") val showViewAll: Int
) : WidgetData()

@Keep
data class LiveClassCarouselItem(
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
    @SerializedName("is_reminder_set") var isReminderSet: Int?,
    @SerializedName("button") val button: ButtonData?,
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
    @SerializedName("is_downloadable") val isDownloadable: Boolean,
    @SerializedName("payment_deeplink") val paymentDeeplink: String?,
    @SerializedName("lock_state") val lockState: Int?,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("show_emi_dialog") val showEMIDialog: Boolean?,
    @SerializedName("deeplink") val deeplink: String?
) {
    @Keep
    data class ButtonData(
        @SerializedName("text") val text: String,
        @SerializedName("action") val action: WidgetAction?,
        @SerializedName("deeplink") val deeplink: String?
    )
}
