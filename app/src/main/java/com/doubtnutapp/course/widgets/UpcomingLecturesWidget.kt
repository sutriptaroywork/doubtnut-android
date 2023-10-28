package com.doubtnutapp.course.widgets

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent

import com.doubtnutapp.base.RefreshUI
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.ItemUpcomingLecturesBinding
import com.doubtnutapp.databinding.WidgetUpcomingLecturesBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.DateUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class UpcomingLecturesWidget(context: Context) :
    BaseBindingWidget<UpcomingLecturesWidget.WidgetHolder,
        UpcomingLecturesWidget.UpcomingLectureWidgetModel, WidgetUpcomingLecturesBinding>(context) {

    companion object {
        private const val TAG = "UpcomingLecturesWidget"
        const val FUTURE = 0
        const val LIVE = 1
        const val PAST = 2
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetUpcomingLecturesBinding {
        return WidgetUpcomingLecturesBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: UpcomingLectureWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val data: UpcomingLectureWidgetData = model.data
        val binding = holder.binding

        binding.textViewTitleMain.text = data.title.orEmpty()
        if (data.title.isNullOrBlank()) {
            binding.textViewTitleMain.hide()
        } else {
            binding.textViewTitleMain.show()
        }
        if (!data.tabs.any { it.isSelected == true }) {
            data.tabs.firstOrNull()?.isSelected = true
        }
        val list = data.tabs.firstOrNull { it.isSelected == true }?.list.orEmpty()
        if (data.hideTab == true) {
            binding.tabLayout.hide()
        } else {
            binding.tabLayout.show()
        }
        binding.tabLayout.clearOnTabSelectedListeners()
        binding.tabLayout.removeAllTabs()
        binding.tabLayout.run {
            data.tabs.forEach {
                addTab(
                    newTab()
                        .apply {
                            text = it.title
                        }
                )
            }
        }
        binding.rvItems.layoutManager = LinearLayoutManager(
            context, RecyclerView.HORIZONTAL,
            false
        )
        val adapter = Adapter(
            list, holder.itemView.context,
            actionPerformer, analyticsPublisher, deeplinkAction,
            true,
            model.extraParams
                ?: HashMap(),
            data.title.orEmpty(), data.isResourcePage ?: false, source
        )
        binding.rvItems.adapter = adapter
        binding.tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    var selectedList = listOf<UpcomingLectureItem>()
                    data.tabs.forEachIndexed { index, courseDetailTabData ->
                        if (index == tab.position) {
                            selectedList = courseDetailTabData.list.orEmpty()
                            courseDetailTabData.isSelected = true
                        } else {
                            courseDetailTabData.isSelected = false
                        }
                    }
                    binding.rvItems.scrollToPosition(0)
                    adapter.items = selectedList
                    adapter.notifyDataSetChanged()

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            TAG + EventConstants.FILTER_TAB + EventConstants.EVENT_ITEM_CLICK,
                            hashMapOf<String, Any>(
                                EventConstants.EVENT_NAME_ID to tab.text.toString(),
                                EventConstants.WIDGET to TAG
                            ).apply {
                                putAll(model.extraParams ?: HashMap())
                            },
                            ignoreSnowplow = true
                        )
                    )
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }
            }
        )

        var selectedPosition: Int = -1
        if (data.tabs.size == 2) {
            if (!data.tabs[0].list.isNullOrEmpty() && data.tabs[1].list.isNullOrEmpty()) {
                selectedPosition = 0
                binding.tabLayout.visibility = INVISIBLE
            } else if (!data.tabs[1].list.isNullOrEmpty() && data.tabs[0].list.isNullOrEmpty()) {
                selectedPosition = 1
                binding.tabLayout.visibility = INVISIBLE
            } else if (!data.tabs[0].list.isNullOrEmpty() && !data.tabs[1].list.isNullOrEmpty()) {
                binding.tabLayout.show()
                selectedPosition = data.tabs.indexOfFirst { it.isSelected == true }
            }
        } else {
            binding.tabLayout.show()
            selectedPosition = data.tabs.indexOfFirst { it.isSelected == true }
        }
        if (selectedPosition != -1) {
            binding.tabLayout.getTabAt(selectedPosition)?.select()
        }
        widgetViewHolder.itemView.setBackgroundColor(Utils.parseColor(data.bgColor))
        val params = binding.textViewTitleMain.layoutParams as MarginLayoutParams
        if (!data.logo.isNullOrEmpty()) {
            binding.ivLogo.visibility = VISIBLE
            binding.ivLogo.loadImageEtx(data.logo)
            params.leftMargin = 24
        } else {
            binding.ivLogo.visibility = GONE
            params.leftMargin = 32
        }
        return holder
    }

    class Adapter(
        var items: List<UpcomingLectureItem>,
        val context: Context,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        private val setWidth: Boolean,
        val extraParams: HashMap<String, Any>,
        val title: String,
        val isResourcePage: Boolean,
        val source: String?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_upcoming_lectures, parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            if (setWidth && items.size > 1) {
                Utils.setWidthBasedOnPercentage(holder.itemView.context, holder.itemView, "1.1", R.dimen.spacing)
            }
            binding.textViewSubject.background = Utils.getShape(
                items[position].color.orEmpty(),
                items[position].color.orEmpty(),
                4f
            )

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

            if (items[position].interested.isNullOrBlank()) {
                binding.textViewRegisteredUser.visibility = View.INVISIBLE
            } else {
                binding.textViewRegisteredUser.visibility = View.VISIBLE
            }

            binding.cardContainer.background = Utils.getGradientView(
                items[position].startGd.orDefaultValue("#232a4f"),
                items[position].midGd.orDefaultValue("#232a4f"),
                items[position].endGd.orDefaultValue("#232a4f")
            )

            holder.itemView.setOnClickListener {
                if (items[position].showEMIDialog == true) {
                    val dialog = EMIReminderDialog.newInstance(Integer.parseInt(items[position].assortmentId.orEmpty()))
                    dialog.show((holder.itemView.context as AppCompatActivity).supportFragmentManager, EMIReminderDialog.TAG)
                    (holder.itemView.context as AppCompatActivity).supportFragmentManager.executePendingTransactions()
                    dialog.dialog?.setOnDismissListener {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.EMI_REMINDER_CLOSE,
                                hashMapOf<String, Any>(
                                    EventConstants.ASSORTMENT_ID to items[position].assortmentId.orEmpty()
                                )
                            )
                        )
                        dialog.dismiss()
                        playUpcomingLectures(holder, position)
                        actionPerformer?.performAction(RefreshUI())
                    }
                } else {
                    playUpcomingLectures(holder, position)
                }
            }
            when (items[position].buttonState) {
                "notify_me" -> {
                    binding.buttonNotifyMeReminder.text = items[position].button?.text.orEmpty()
                    binding.layoutNotifyMeReminder.visibility = View.VISIBLE
                    binding.buttonChapter.visibility = View.GONE
                    binding.layoutDemoVideo.visibility = View.GONE
                    binding.layoutMultipleButtons.visibility = View.GONE
                    binding.layoutNotifyMeReminder.background = Utils.getShape(
                        "#ffffff",
                        "#ea532c",
                        8f,
                        1
                    )
                    binding.layoutNotifyMeReminder.setOnClickListener {
                        setReminder(
                            holder.itemView.context, items.getOrNull(position)?.reminderMessage,
                            items[position].id, items[position].assortmentId.orEmpty(), items.getOrNull(position)?.liveAt, 1
                        )
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                TAG + EventConstants.EVENT_ITEM_CLICK + EventConstants.REMINDER,
                                hashMapOf<String, Any>(
                                    EventConstants.EVENT_NAME_ID to items[position].id.toString(),
                                    EventConstants.WIDGET to TAG,
                                    EventConstants.WIDGET_TITLE to title
                                ).apply {
                                    putAll(extraParams)
                                },
                                ignoreSnowplow = true
                            )
                        )
                    }
                }
                "chapter" -> {
                    binding.buttonChapter.text = items[position].button?.text.orEmpty()
                    binding.buttonChapter.visibility = View.VISIBLE
                    binding.layoutDemoVideo.visibility = View.GONE
                    binding.layoutMultipleButtons.visibility = View.GONE
                    binding.layoutNotifyMeReminder.visibility = View.GONE
                    binding.buttonChapter.background = Utils.getShape(
                        "#ffffff",
                        "#000000",
                        8f,
                        2
                    )
                    binding.buttonChapter.setOnClickListener {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                TAG + EventConstants.EVENT_ITEM_CLICK + "Chapter",
                                hashMapOf<String, Any>(
                                    EventConstants.EVENT_NAME_ID to items[position].id.toString(),
                                    EventConstants.WIDGET to TAG,
                                    EventConstants.WIDGET_TITLE to title
                                ).apply {
                                    putAll(extraParams)
                                },
                                ignoreSnowplow = true
                            )
                        )
                    }
                }
                "multiple" -> {
                    binding.layoutMultipleButtons.visibility = View.VISIBLE
                    binding.buttonChapter.visibility = View.GONE
                    binding.layoutNotifyMeReminder.visibility = View.GONE
                    binding.layoutDemoVideo.visibility = View.GONE
                    if (items[position].button?.text.isNullOrBlank()) {
                        binding.button.visibility = View.INVISIBLE
                    } else {
                        binding.button.visibility = View.VISIBLE
                    }
                    if (items[position].showReminder == true) {
                        binding.buttonReminder2.visibility = View.VISIBLE
                    } else {
                        binding.buttonReminder2.visibility = View.GONE
                    }

                    binding.buttonReminder2.isSelected = items[position].isReminderSet == 1

                    binding.button.text = items[position].button?.text.orEmpty()

                    binding.button.background = Utils.getShape(
                        "#ffffff",
                        "#000000",
                        8f,
                        2
                    )

                    binding.buttonReminder2.background = Utils.getShape(
                        "#ffffff",
                        "#ea532c",
                        8f,
                        1
                    )

                    binding.buttonReminder2.setOnClickListener {
                        it.isSelected = !it.isSelected
                        items[position].isReminderSet = if (it.isSelected) 1 else 0

                        setReminder(
                            holder.itemView.context, items.getOrNull(position)?.reminderMessage,
                            items[position].id, items[position].assortmentId.orEmpty(), items[position].liveAt, if (it.isSelected) 1 else 0
                        )
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                TAG + EventConstants.EVENT_ITEM_CLICK + EventConstants.REMINDER,
                                hashMapOf<String, Any>(
                                    EventConstants.EVENT_NAME_ID to items[position].id.toString(),
                                    EventConstants.WIDGET to TAG
                                ).apply {
                                    putAll(extraParams)
                                },
                                ignoreSnowplow = true
                            )
                        )
                    }
                    binding.button.setOnClickListener {
                        deeplinkAction.performAction(holder.itemView.context, items[position].button?.deeplink, source.orEmpty())
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                TAG + EventConstants.EVENT_ITEM_CLICK + "Course",
                                hashMapOf<String, Any>(
                                    EventConstants.EVENT_NAME_ID to items[position].id.toString(),
                                    EventConstants.WIDGET to TAG,
                                    EventConstants.WIDGET_TITLE to title
                                ).apply {
                                    putAll(extraParams)
                                },
                                ignoreSnowplow = true
                            )
                        )
                    }
                }
                "demo_video" -> {
                    binding.layoutDemoVideo.visibility = View.VISIBLE
                    binding.layoutMultipleButtons.visibility = View.GONE
                    binding.buttonChapter.visibility = View.GONE
                    binding.layoutNotifyMeReminder.visibility = View.GONE
                    binding.buttonDemoVideo.text = items[position].button?.text.orEmpty()
                    binding.demoVideoImage.loadImageEtx(items[position].button?.imageUrl.orEmpty())
                    binding.layoutDemoVideo.background = Utils.getShape(
                        "#ffffff",
                        "#ea532c",
                        8f,
                        1
                    )
                    binding.layoutDemoVideo.setOnClickListener {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                TAG + EventConstants.EVENT_ITEM_CLICK + "DemoVideo",
                                hashMapOf<String, Any>(
                                    EventConstants.EVENT_NAME_ID to items[position].id.toString(),
                                    EventConstants.WIDGET to TAG,
                                    EventConstants.WIDGET_TITLE to title
                                ).apply {
                                    putAll(extraParams)
                                },
                                ignoreSnowplow = true
                            )
                        )
                        playUpcomingLectures(holder, position)
                    }
                }
                else -> {
                    binding.layoutDemoVideo.visibility = View.GONE
                    binding.layoutMultipleButtons.visibility = View.GONE
                    binding.buttonChapter.visibility = View.GONE
                    binding.layoutNotifyMeReminder.visibility = View.GONE
                }
            }
        }

        override fun getItemCount(): Int = items.size

        private fun setReminder(context: Context, reminderMessage: String?, id: String?, assortmentId: String?, liveAt: String?, reminderSet: Int?) {
            checkInternetConnection(context) {
                var message = reminderMessage.orDefaultValue("Your reminder has been set")
                if (reminderSet != 1) {
                    message = "Reminder removed"
                }
                (context as? Activity)?.let { context ->
                    Snackbar.make(
                        context.findViewById(android.R.id.content),
                        message,
                        Snackbar.LENGTH_LONG
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
                markInterested(id.orEmpty(), true, assortmentId.orEmpty(), liveAt, reminderSet)
            }
        }

        private fun playUpcomingLectures(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            if (item.isPremium == true && item.isVip != true) {
                deeplinkAction.performAction(holder.itemView.context, item.paymentDeeplink)
            } else {
                val data = items[position]
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
                        items[position].id.orEmpty(), false,
                        items[position].assortmentId.orEmpty(), items[position].liveAt, 0
                    )
                    showToast(currentContext, currentContext.getString(R.string.coming_soon))
                }
            }
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    TAG + EventConstants.EVENT_ITEM_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.EVENT_NAME_ID to items[position].id.toString(),
                        EventConstants.WIDGET to TAG,
                        EventConstants.IS_LIVE to items[position].state.toString(),
                        EventConstants.WIDGET_TITLE to title
                    ).apply {
                        putAll(extraParams)
                    },
                    ignoreSnowplow = true
                )
            )
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

        private fun markInterested(id: String, isReminder: Boolean, assortmentId: String, liveAt: String?, reminderSet: Int?) {
            DataHandler.INSTANCE.courseRepository.markInterested(id, isReminder, assortmentId, liveAt, reminderSet)
                .applyIoToMainSchedulerOnCompletable()
                .subscribeToCompletable({})
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemUpcomingLecturesBinding.bind(itemView)
        }
    }

    class WidgetHolder(binding: WidgetUpcomingLecturesBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetUpcomingLecturesBinding>(binding, widget)

    class UpcomingLectureWidgetModel : WidgetEntityModel<UpcomingLectureWidgetData, WidgetAction>()

    @Keep
    data class UpcomingLectureWidgetData(
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("logo") val logo: String?,
        @SerializedName("widget_bg_color") val bgColor: String?,
        @SerializedName("hide_tab") val hideTab: Boolean?,
        @SerializedName("filters") val tabs: List<UpcomingLectureTabData>,
        @SerializedName("is_resource_page") val isResourcePage: Boolean?
    ) : WidgetData()

    @Keep
    data class UpcomingLectureTabData(
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("is_selected") var isSelected: Boolean? = false,
        @SerializedName("list") val list: List<UpcomingLectureItem>?
    )

    @Keep
    data class UpcomingLectureItem(
        @SerializedName("id") val id: String?,
        @SerializedName("button_state") val buttonState: String?,
        @SerializedName("detail_id") val detailId: String?,
        @SerializedName("top_title") val topTitle: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("subject") val subject: String?,
        @SerializedName("live_text") val liveText: String?,
        @SerializedName("title1") val title1: String?,
        @SerializedName("title2") val title2: String?,
        @SerializedName("students") val students: String?,
        @SerializedName("color") val color: String?,
        @SerializedName("show_reminder") val showReminder: Boolean?,
        @SerializedName("is_reminder_set") var isReminderSet: Int?,
        @SerializedName("reminder_message") val reminderMessage: String?,
        @SerializedName("reminder_link") val reminderLink: String?,
        @SerializedName("page") val page: String?,
        @SerializedName("start_gd") val startGd: String?,
        @SerializedName("mid_gd") val midGd: String?,
        @SerializedName("end_gd") val endGd: String?,
        @SerializedName("image_bg_card") val imageBgCard: String?,
        @SerializedName("button") val button: ButtonData?,
        @SerializedName("is_premium") val isPremium: Boolean?,
        @SerializedName("is_vip") val isVip: Boolean?,
        @SerializedName("board") val board: String?,
        @SerializedName("interested") val interested: String?,
        @SerializedName("bottom_title") val bottomTitle: String?,
        @SerializedName("duration") val duration: String?,
        @SerializedName("remaining") val remaining: String?,
        @SerializedName("live_at") val liveAt: String?,
        @SerializedName("is_last_resource") val isLastResource: Boolean,
        @SerializedName("state") val state: Int,
        @SerializedName("payment_deeplink") val paymentDeeplink: String?,
        @SerializedName("assortment_id") val assortmentId: String?,
        @SerializedName("show_emi_dialog") val showEMIDialog: Boolean?
    ) {
        @Keep
        data class ButtonData(
            @SerializedName("text") val text: String,
            @SerializedName("button_image") val imageUrl: String?,
            @SerializedName("action") val action: WidgetAction?,
            @SerializedName("deeplink") val deeplink: String?
        )
    }
}
