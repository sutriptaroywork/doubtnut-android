package com.doubtnutapp.widgetmanager.widgets

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.OnboardingEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent

import com.doubtnutapp.base.RefreshUI
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.RelatedLectureWidgetItem
import com.doubtnutapp.data.remote.models.RelatedLecturesWidgetData
import com.doubtnutapp.data.remote.models.RelatedLecturesWidgetModel
import com.doubtnutapp.databinding.ItemRelatedLecturesBinding
import com.doubtnutapp.databinding.WidgetRelatedLecturesBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.domain.homefeed.interactor.OnboardingData
import com.doubtnutapp.downloadedVideos.ExoDownloadTracker
import com.doubtnutapp.training.CustomToolTip
import com.doubtnutapp.training.OnboardingManager
import com.doubtnutapp.ui.mockTest.MockTestSubscriptionActivity
import com.doubtnutapp.utils.DateUtils
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.utils.setMargins
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class RelatedLecturesWidget(context: Context) :
    BaseBindingWidget<RelatedLecturesWidget.WidgetHolder,
            RelatedLecturesWidgetModel, WidgetRelatedLecturesBinding>(context) {

    companion object {
        private const val TAG = "RelatedLecturesWidget"
        const val FUTURE = 0
        const val LIVE = 1
        const val PAST = 2
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetRelatedLecturesBinding {
        return WidgetRelatedLecturesBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: RelatedLecturesWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        setMargins(WidgetLayoutConfig(0, 0, 0, 0))
        val data: RelatedLecturesWidgetData = model.data
        if (!data.items.isNullOrEmpty()) {
            widgetViewHolder.binding.rootLayout.setBackgroundColor(
                Utils.parseColor(
                    data.bgColor, R.color.blue_66e0eaff
                )
            )
        }
        widgetViewHolder.binding.titleTv.setVisibleState(!data.title.isNullOrEmpty())
        widgetViewHolder.binding.titleTv.text = data.title.orEmpty()
        widgetViewHolder.itemView.setVisibleState(!data.items.isNullOrEmpty())
        widgetViewHolder.binding.rvRelatedLectures.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL, false
        )
        widgetViewHolder.binding.rvRelatedLectures.isNestedScrollingEnabled = false
        widgetViewHolder.binding.rvRelatedLectures.adapter = Adapter(
            model,
            actionPerformer,
            analyticsPublisher,
            model.extraParams ?: HashMap(),
            deeplinkAction,
            source
        )

        return holder
    }

    class Adapter(
        val model: RelatedLecturesWidgetModel,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val extraParams: HashMap<String, Any>,
        val deeplinkAction: DeeplinkAction,
        val source: String?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_related_lectures, parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = model.data.items.orEmpty()[position]
            if (model.isOnboardingEnabled == true) {
                showToolTip(holder, position, holder.itemView.context, holder.binding.ivDownloads)
            }
            holder.binding.tvTime.setVisibleState(!data.duration.isNullOrEmpty())
            holder.binding.tvTime.text = data.duration
            holder.binding.tvSubject.text = data.subject
            holder.binding.tvLectureName.text = data.title
            holder.binding.tvDate.text = data.liveDate
            holder.binding.tvTeacherName.setVisibleState(!data.subtitle.isNullOrEmpty())
            holder.binding.tvTopic.setVisibleState(!data.topics.isNullOrEmpty())
            holder.binding.ivReminder.setVisibleState(!data.reminderLink.isNullOrEmpty())
            if (data.liveText.isNullOrEmpty()) {
                holder.binding.tvLiveTime.visibility = GONE
                holder.binding.tvLiveTime.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.blue_66e0eaff
                    )
                )
                holder.binding.tvSubject.setMargins(
                    ViewUtils.dpToPx(0f, holder.itemView.context).toInt(),
                    ViewUtils.dpToPx(14f, holder.itemView.context).toInt(), 0, 0
                )
            } else {
                holder.binding.tvLiveTime.visibility = VISIBLE
                holder.binding.tvLiveTime.setBackgroundColor(Utils.parseColor(data.liveTextColor))
                holder.binding.tvSubject.setMargins(
                    ViewUtils.dpToPx(12f, holder.itemView.context).toInt(),
                    ViewUtils.dpToPx(14f, holder.itemView.context).toInt(), 0, 0
                )
            }
            holder.binding.tvLiveTime.text = data.liveText
            holder.binding.tvTeacherName.text = data.subtitle
            if (data.state == LIVE) {
                holder.binding.tvTeacherName.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.lirary_searchbar_text
                    )
                )
                holder.binding.tvNowPlaying.text = data.topics
                holder.binding.tvNowPlaying.visibility = View.VISIBLE
                holder.binding.tvTopic.visibility = View.INVISIBLE
            } else {
                holder.binding.tvTeacherName.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.grey_777785
                    )
                )
                holder.binding.tvTopic.text = data.topics
                holder.binding.tvNowPlaying.visibility = View.GONE
                holder.binding.tvTopic.visibility = View.VISIBLE
            }
            holder.binding.ivReminder.background = Utils.getShape(
                "#ffffff",
                "#ea532c",
                8f,
                1
            )
            holder.binding.ivPlayLecture.loadImage(data.imageUrl)
            if (FeaturesManager.isFeatureEnabled(
                    holder.itemView.context,
                    Features.OFFLINE_VIDEOS
                ) && data.isDownloadable
            ) {
                holder.binding.ivDownloads.visibility = View.VISIBLE
            } else {
                holder.binding.ivDownloads.visibility = View.INVISIBLE
            }

            holder.binding.ivDownloads.setOnClickListener {
                val isSubscribed = data.isVip == true
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_OFFLINE_DOWNLOAD_START,
                        ignoreFirebase = false,
                        params = hashMapOf(
                            Constants.IS_SUBSCRIBED to isSubscribed,
                            Constants.QUESTION_ID to data.id.orEmpty()
                        ),
                        ignoreSnowplow = true
                    )
                )
                if (isSubscribed) {
                    downloadVideo(holder, data.id.orEmpty(), data.title.orEmpty())
                } else {
                    ToastUtils.makeText(
                        holder.itemView.context,
                        "You're not subscribed to this course, subscribe to download",
                        Toast.LENGTH_SHORT
                    ).show()
                    deeplinkAction.performAction(holder.itemView.context, data.paymentDeeplink)
                }
            }
            holder.binding.ivReminder.setOnClickListener {
                checkInternetConnection(holder.itemView.context) {
                    (holder.itemView.context as? Activity)?.let { context ->
                        Snackbar.make(
                            context.findViewById(android.R.id.content),
                            model.data.items.orEmpty().getOrNull(position)
                                ?.reminderMessage
                                .orDefaultValue("Your reminder has been set"),
                            Snackbar.LENGTH_SHORT
                        ).apply {
                            this.view.background =
                                context.getDrawable(R.drawable.bg_capsule_black_90)
                            setActionTextColor(ContextCompat.getColor(context, R.color.redTomato))
                            val textView =
                                this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                            textView.setTextColor(ContextCompat.getColor(context, R.color.white))
                            show()
                        }
                    }
                    markInterested(
                        data.id.orEmpty(),
                        true,
                        data.assortmentId.orEmpty(),
                        data.liveAt,
                        1
                    )
                }
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(TAG + EventConstants.EVENT_ITEM_CLICK + EventConstants.REMINDER,
                        hashMapOf<String, Any>(
                            EventConstants.EVENT_NAME_ID to data.id.toString(),
                            EventConstants.WIDGET to TAG
                        ).apply {
                            putAll(extraParams)
                        }, ignoreSnowplow = true
                    )
                )
            }

            if (data.type.equals("mock-test")) {
                holder.binding.tvStartTest.visibility = VISIBLE
                holder.binding.tvStartTest.text = data.link.orEmpty()
                holder.binding.tvStartTest.setOnClickListener {
                    if (data.state == LIVE) {
                        holder.itemView.context.startActivity(
                            MockTestSubscriptionActivity.getStartIntent(
                                holder.itemView.context,
                                data.id.orDefaultValue().toInt(), false
                            )
                        )
                    } else {
                        showToast(
                            holder.itemView.context,
                            holder.itemView.context.getString(R.string.coming_soon)
                        )
                    }
                }
            } else {
                holder.binding.tvStartTest.visibility = GONE
            }

            holder.itemView.setOnClickListener {
                onCardClicked(holder, position, source.orEmpty())
            }
        }

        private fun onCardClicked(holder: RecyclerView.ViewHolder, position: Int, source: String) {
            val data = model.data.items.orEmpty()[position]
            if (data.type.equals("mock-test")) {
                if (data.isPremium == true && data.isVip != true) {
                    deeplinkAction.performAction(
                        holder.itemView.context,
                        data.paymentDeeplink,
                        source
                    )
                } else {
                    if (data.state == LIVE) {
                        holder.itemView.context.startActivity(
                            MockTestSubscriptionActivity.getStartIntent(
                                holder.itemView.context,
                                data.id.orDefaultValue().toInt(), false
                            )
                        )
                    } else {
                        showToast(
                            holder.itemView.context,
                            holder.itemView.context.getString(R.string.coming_soon)
                        )
                    }
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(TAG + EventConstants.EVENT_ITEM_CLICK + "MockTest",
                            hashMapOf<String, Any>(
                                EventConstants.EVENT_NAME_ID to data.id.toString(),
                                EventConstants.WIDGET to TAG
                            ).apply {
                                putAll(extraParams)
                            }, ignoreSnowplow = true
                        )
                    )
                }
            } else if (data.showEMIDialog == true) {
                val emiReminderDialog =
                    EMIReminderDialog.newInstance(Integer.parseInt(data.assortmentId.orEmpty()))
                emiReminderDialog.show(
                    (holder.itemView.context as AppCompatActivity).supportFragmentManager,
                    EMIReminderDialog.TAG
                )
                (holder.itemView.context as AppCompatActivity).supportFragmentManager.executePendingTransactions()
                emiReminderDialog.dialog?.setOnDismissListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.EMI_REMINDER_CLOSE,
                            hashMapOf<String, Any>(
                                EventConstants.ASSORTMENT_ID to data.assortmentId.orEmpty()
                            )
                        )
                    )
                    emiReminderDialog.dismiss()
                    actionPerformer?.performAction(RefreshUI())
                    playRelatedLecture(data, holder)
                }
            } else {
                playRelatedLecture(data, holder)
            }
        }

        private fun showToolTip(
            holder: RecyclerView.ViewHolder,
            position: Int,
            context: Context,
            view: View
        ) {
            val onboardingList: List<OnboardingData>? =
                DoubtnutApp.INSTANCE.onboardingList.orEmpty()
            if (onboardingList != null && onboardingList.size >= 3) {
                val onboardingData: OnboardingData? = onboardingList[3]
                if (onboardingData != null) {
                    if (position == onboardingData.position ?: 1) {
                        OnboardingManager(context as FragmentActivity, onboardingData.id
                            ?: 0, onboardingData.title.orEmpty(),
                            onboardingData.description.orEmpty(),
                            onboardingData.buttonText.orEmpty(), {
                                onToolTipCLicked(onboardingData.deeplink, context)
                            }, {
                                onToolTipCLicked(onboardingData.deeplink, context)
                            }, CustomToolTip(context), onboardingData.audioUrl
                        ).launchTourGuide(view)
                        model.isOnboardingEnabled = false
                    }
                }
            }
        }

        private fun onToolTipCLicked(deeplink: String?, context: Context) {
            if (!deeplink.isNullOrEmpty()) {
                deeplinkAction.performAction(context, deeplink)
            } else {
                ((context as Activity).applicationContext as DoubtnutApp).bus()
                    ?.send(OnboardingEvent("topic"))
            }
        }

        private fun playRelatedLecture(
            data: RelatedLectureWidgetItem,
            holder: RecyclerView.ViewHolder
        ) {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(TAG + EventConstants.EVENT_ITEM_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.EVENT_NAME_ID to data.id.toString(),
                        EventConstants.WIDGET to TAG
                    ).apply {
                        putAll(extraParams)
                    }, ignoreSnowplow = true
                )
            )

            if (!data.deeplink.isNullOrEmpty()) {
                deeplinkAction.performAction(
                    holder.itemView.context,
                    data.deeplink,
                    source.orEmpty()
                )
            } else {
                if (data.isPremium == true && data.isVip != true) {
                    deeplinkAction.performAction(
                        holder.itemView.context,
                        data.paymentDeeplink,
                        source.orEmpty()
                    )
                } else {
                    val currentContext = holder.itemView.context
                    if (data.state == LIVE
                        || DateUtils.isBeforeCurrentTime(data.liveAt?.toLongOrNull())
                        || data.state == PAST
                    ) {
                        //allow to watch video
                        var pageSource = data.page
                        if (Constants.PAGE_SEARCH_SRP == source) {
                            pageSource = Constants.PAGE_SEARCH_SRP
                        }
                        openVideoPage(currentContext, data.id, pageSource)
                    } else {
                        //for future state
                        markInterested(
                            data.id.orEmpty(),
                            false,
                            data.assortmentId.orEmpty(),
                            data.liveAt,
                            0
                        )
                        showToast(currentContext, currentContext.getString(R.string.coming_soon))
                    }
                }
            }
        }

        private fun downloadVideo(
            holder: RecyclerView.ViewHolder,
            questionId: String,
            title: String
        ) {
            val downloadTracker = ExoDownloadTracker.getInstance(holder.itemView.context)
            downloadTracker.downloadVideo(holder.itemView.context, questionId, title)
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

        override fun getItemCount(): Int = model.data.items.orEmpty().size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemRelatedLecturesBinding.bind(itemView)
        }

    }

    class WidgetHolder(binding: WidgetRelatedLecturesBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetRelatedLecturesBinding>(binding, widget)

}