package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.EXPANDED
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.TextViewUtils
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.AllChapterResultClicked
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.ItemAllClassesActionBinding
import com.doubtnutapp.databinding.ItemAllClassesBinding
import com.doubtnutapp.databinding.WidgetAllClassesBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.downloadedVideos.ExoDownloadTracker
import com.doubtnutapp.utils.DateUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnutapp.widgetmanager.widgets.BaseBindingViewHolder
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.RelatedLecturesWidget
import com.doubtnutapp.widgets.PointerTextView
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class AllClassesWidget(context: Context) :
    BaseBindingWidget<BaseBindingViewHolder<WidgetAllClassesBinding>,
        AllClassesWidgetModel, WidgetAllClassesBinding>(context) {

    companion object {
        const val TAG = "AllClassesWidget"
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

    override fun getViewBinding(): WidgetAllClassesBinding {
        return WidgetAllClassesBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = BaseBindingViewHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: BaseBindingViewHolder<WidgetAllClassesBinding>,
        model: AllClassesWidgetModel
    ): BaseBindingViewHolder<WidgetAllClassesBinding> {
        super.bindWidget(holder, model)
        val data: AllClassesWidgetData = model.data
        val binding = holder.viewBinding

        binding.card.strokeColor = Utils.parseColor(model.data.borderColor.orEmpty())
        binding.subjectTv.setTextColor(Utils.parseColor(model.data.borderColor.orEmpty()))
        binding.subjectTv.text = model.data.title.orEmpty()
        binding.topicTv.text = model.data.subtitle.orEmpty()
        binding.lectureCountTv.text = model.data.lectureCount.orEmpty()
        binding.card.setOnClickListener {
            if (model.data.isExpanded == true) {
                binding.ivExpand.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_expand_more
                    )
                )
                binding.recyclerView.visibility = GONE
                data.isExpanded = false
            } else {
                binding.ivExpand.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_expand_less
                    )
                )
                binding.recyclerView.visibility = VISIBLE
                data.isExpanded = true
            }
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.ALL_CLASSESS_EXPAND,
                    hashMapOf<String, Any>().apply {
                        put(EXPANDED, data.isExpanded ?: false)
                        putAll(model.extraParams ?: hashMapOf())
                    }
                )
            )
        }
        if (model.data.isExpanded == true) {
            binding.recyclerView.visibility = VISIBLE
            binding.ivExpand.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_expand_less
                )
            )
        } else {
            binding.recyclerView.visibility = GONE
            binding.ivExpand.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_expand_more
                )
            )
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = AllClassesWidgetAdapter(
            items = ArrayList<AllClassesWidgetAdapterType>().apply {
                addAll(data.items.orEmpty())
                addAll(data.actions.orEmpty())
            },
            actionPerformer = actionPerformer,
            analyticsPublisher = analyticsPublisher,
            deeplinkAction = deeplinkAction,
            extraParams = model.extraParams ?: HashMap(),
            source = source
        )
        return holder
    }

    class AllClassesWidgetAdapter(
        val items: ArrayList<AllClassesWidgetAdapterType>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>,
        val source: String?
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            const val VIEW_TYPE_ALL_CLASSES_ITEM = 1
            const val VIEW_TYPE_ALL_CLASSES_ACTION = 2
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                VIEW_TYPE_ALL_CLASSES_ITEM -> {
                    ViewHolder(
                        ItemAllClassesBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    )
                }
                VIEW_TYPE_ALL_CLASSES_ACTION -> {
                    ActionViewHolder(
                        ItemAllClassesActionBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    )
                }
                else -> throw IllegalArgumentException("Unsupported View type: $viewType")
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when (items[position]) {
                is AllClassesWidgetItem -> VIEW_TYPE_ALL_CLASSES_ITEM
                is AllClassesWidgetAction -> VIEW_TYPE_ALL_CLASSES_ACTION
                else -> -1
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is ViewHolder -> holder.bind()
                is ActionViewHolder -> holder.bind()
            }
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(val binding: ItemAllClassesBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind() {
                val data = items[bindingAdapterPosition] as? AllClassesWidgetItem ?: return

                binding.tvLiveAt.text = data.liveAt.orEmpty()
                binding.tvLiveAt.isVisible = data.liveAt.isNullOrEmpty().not()

                binding.tvTitle.text = data.lectureTitle.orEmpty()
                binding.tvTitle.isVisible = data.lectureTitle.isNullOrEmpty().not()

                val pointers = if (data.titleWithTimestamps.isNullOrEmpty()) {
                    data.title.orEmpty().split("||", "|")
                } else {
                    data.titleWithTimestamps.orEmpty().split("||", "|")
                }

                binding.layoutPointers.hide()
                binding.layoutPointers.removeAllViews()
                binding.textViewSeeMore
                    .setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_drop_down,
                        0
                    )

                listOf(binding.tvTitle, binding.textViewSeeMore).forEach {
                    it.setOnClickListener {
                        if (binding.layoutPointers.isVisible) {
                            binding.layoutPointers.hide()
                            binding.layoutPointers.removeAllViews()
                            binding.textViewSeeMore
                                .setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_drop_down,
                                    0
                                )
                        } else {
                            binding.layoutPointers.show()
                            binding.layoutPointers.removeAllViews()
                            pointers.forEachIndexed { index, text ->
                                PointerTextView(itemView.context).apply {
                                    this.binding.textViewPointer.text = "•"
                                    TextViewUtils.setTextFromHtml(
                                        this.binding.textView,
                                        text
                                    )
                                    this@ViewHolder.binding.layoutPointers.addView(this)

                                    setOnClickListener {
                                        playVideo(
                                            data,
                                            this@ViewHolder,
                                            data.offsetArr?.getOrNull(index),
                                            EventConstants.ALL_CLASSESS_TOPIC_CLICK,
                                            index + 1
                                        )
                                    }
                                }
                            }
                            binding.textViewSeeMore
                                .setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_up_arrow,
                                    0
                                )
                        }
                    }
                }
                binding.progress.progress = data.progress ?: 0
                binding.root.setOnClickListener {
                    actionPerformer?.performAction(
                        AllChapterResultClicked(
                            data,
                            itemCount,
                            position
                        )
                    )
                    playVideo(
                        data,
                        this@ViewHolder,
                        null,
                        EventConstants.ALL_CLASSESS_ViDEO_CLICK
                    )
                }
                if (data.isDownloadable == true) {
                    binding.ivDownloads.visibility = View.VISIBLE
                } else {
                    binding.ivDownloads.visibility = View.INVISIBLE
                }

                binding.ivDownloads.setOnClickListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.EVENT_OFFLINE_DOWNLOAD_START,
                            ignoreFirebase = false,
                            params = hashMapOf(Constants.QUESTION_ID to data.id.orEmpty()),
                            ignoreSnowplow = true
                        )
                    )
                    downloadVideo(this@ViewHolder, data.id.orEmpty(), data.title.orEmpty())
                }
            }

            private fun downloadVideo(
                holder: ViewHolder,
                questionId: String,
                title: String
            ) {
                val downloadTracker = ExoDownloadTracker.getInstance(holder.itemView.context)
                downloadTracker.downloadVideo(holder.itemView.context, questionId, title)
            }

            private fun playVideo(
                data: AllClassesWidgetItem,
                holder: ViewHolder,
                offset: Int?,
                eventName: String,
                itemPosition: Int = -1
            ) {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        eventName,
                        hashMapOf<String, Any>(
                            EventConstants.ASSORTMENT_ID to data.assortmentId.orEmpty(),
                            EventConstants.QUESTION_ID2 to data.id.orEmpty(),
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.ITEM_POSITION to itemPosition,
                        ).apply {
                            putAll(extraParams)
                        }
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
                        deeplinkAction.performAction(holder.itemView.context, data.paymentDeeplink)
                    } else {
                        val currentContext = holder.itemView.context
                        if (data.state == RelatedLecturesWidget.LIVE ||
                            // format for liveAt is different and thus this will always be false.
                            DateUtils.isBeforeCurrentTime(data.liveAt?.toLongOrNull()) ||
                            data.state == RelatedLecturesWidget.PAST
                        ) {
                            // allow to watch video
                            var pageSource = data.page
                            if (Constants.PAGE_SEARCH_SRP == source) {
                                pageSource = Constants.PAGE_SEARCH_SRP
                            }
                            openVideoPage(
                                currentContext, data.id, pageSource,
                                offset
                                    ?: data.offset
                            )
                        } else {
                            // for future state
                            markInterested(data.id.orEmpty(), false, data.assortmentId.orEmpty(), data.liveAt, 0)
                            showToast(currentContext, currentContext.getString(R.string.coming_soon))
                        }
                    }
                }
            }

            private fun openVideoPage(context: Context, id: String?, page: String?, offset: Int?) {
                context.startActivity(
                    VideoPageActivity.startActivity(
                        context = context,
                        questionId = id.orEmpty(),
                        page = page.orEmpty(),
                        startPositionInSeconds = offset?.toLong() ?: 0
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
        }

        inner class ActionViewHolder(val binding: ItemAllClassesActionBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind() {
                val data = items[bindingAdapterPosition] as? AllClassesWidgetAction ?: return
                binding.tvTitle.text = data.title
                binding.tvTitle.applyTextColor(data.titleTextColor)
                binding.tvTitle.applyTextSize(data.titleTextSize)

                binding.root.setOnClickListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.ALL_CLASSESS_ITEM_ACTION,
                            hashMapOf<String, Any>().apply {
                                put(EventConstants.CTA_TITLE, data.title.orEmpty())
                                put(EventConstants.STUDENT_ID, UserUtil.getStudentId())
                                putAll(extraParams)
                            }
                        )
                    )
                    deeplinkAction.performAction(itemView.context, data.deeplink)
                }
            }
        }
    }
}

class AllClassesWidgetModel : WidgetEntityModel<AllClassesWidgetData, WidgetAction>()

@Keep
data class AllClassesWidgetData(
    @SerializedName("title1") val title: String?,
    @SerializedName("title2") val subtitle: String?,
    @SerializedName("lecture_count") val lectureCount: String?,
    @SerializedName("is_expanded") var isExpanded: Boolean?,
    @SerializedName("border_color") val borderColor: String?,
    @SerializedName("items") val items: List<AllClassesWidgetItem>?,
    @SerializedName("actions") val actions: List<AllClassesWidgetAction>?,
) : WidgetData()

@Keep
data class AllClassesWidgetItem(
    @SerializedName("lec_title") val lectureTitle: String?,
    @SerializedName("progress") val progress: Int?,
    @SerializedName("title_with_timestamps") val titleWithTimestamps: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("state") val state: Int,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("is_premium") val isPremium: Boolean?,
    @SerializedName("is_vip") val isVip: Boolean?,
    @SerializedName("is_downloadable") val isDownloadable: Boolean?,
    @SerializedName("page") val page: String?,
    @SerializedName("payment_deeplink") val paymentDeeplink: String?,
    @SerializedName("live_at") val liveAt: String?,
    @SerializedName("offset") val offset: Int?,
    @SerializedName("offsets_arr") val offsetArr: List<Int>?
) : AllClassesWidgetAdapterType

@Keep
data class AllClassesWidgetAction(
    @SerializedName("title") val title: String?,
    @SerializedName("title_text_size") val titleTextSize: String?,
    @SerializedName("title_text_color") val titleTextColor: String?,
    @SerializedName("deeplink") val deeplink: String?,
) : AllClassesWidgetAdapterType

interface AllClassesWidgetAdapterType
