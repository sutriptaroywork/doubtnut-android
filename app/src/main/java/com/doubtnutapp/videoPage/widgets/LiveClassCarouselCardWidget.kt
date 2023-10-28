package com.doubtnutapp.videoPage.widgets

import android.app.Activity
import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.WidgetLiveClassCarouselCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.utils.DateUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.snackbar.Snackbar
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

/**
 * Created by devansh on 22/10/20.
 */

class LiveClassCarouselCardWidget(context: Context) : BaseBindingWidget<LiveClassCarouselCardWidget.WidgetHolder,
        LiveClassCarouselCardWidget.Model,WidgetLiveClassCarouselCardBinding>(context) {

    companion object {
        const val FUTURE = 0
        const val LIVE = 1
        const val PAST = 2
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val mWidgetName: String = this::class.simpleName.orEmpty()

    var source: String? = null

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {

        when(model.extraParams?.get(Constants.ORIENTATION) ?: CardsOrientation.VERTICAL.name) {
            CardsOrientation.VERTICAL -> { /* UI designed keeping this in mind */ }

            CardsOrientation.HORIZONTAL -> {
                Utils.setWidthBasedOnPercentage(context, holder.itemView, "1.1", R.dimen.spacing)
                super.bindWidget(holder, model.apply {
                    this.layoutConfig = WidgetLayoutConfig(
                            marginTop = 5,
                            marginBottom = 9,
                            marginLeft = 0,
                            marginRight = 0
                    )
                })
            }
        }

        val data = model.data
        val binding = holder.binding

        val parentId = (model.extraParams?.get(Constants.PARENT_ID) as? String).orDefaultValue()

        binding.textViewSubject.background = Utils.getShape(
                data.color.orEmpty(),
                data.color.orEmpty(),
                4f)

        binding.button.background = Utils.getShape(
                "#ffffff",
                "#000000",
                8f,
                2)

        binding.buttonReminder.background = Utils.getShape(
                "#ffffff",
                "#ea532c",
                8f,
                1)

        binding.textViewTitleInfo.text = data.title1.orEmpty()
        binding.textViewSubject.text = data.subject
        binding.textViewBottomInfo.text = data.bottomTitle.orEmpty()

        val timeText = if (!data.remaining.isNullOrBlank()) {
            data.remaining.orEmpty()
        } else {
            data.topTitle.orEmpty()
        }

        binding.textViewTimeInfo.text = timeText
        binding.textViewDuration.toggleVisibilityAndSetText(data.duration)

        if (data.state == LIVE) {
            binding.textViewTimeInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_live_icon_small, 0, 0, 0)
            binding.textViewTimeInfo.setTextColor(Utils.parseColor("#ffffff"))
        } else {
            binding.textViewTimeInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calendar, 0, 0, 0)
            binding.textViewTimeInfo.setTextColor(Utils.parseColor("#ffc700"))
        }

        binding.textViewFacultyInfo.text = data.title2

        binding.imageViewFaculty.loadImageEtx(data.imageUrl.orEmpty())
        binding.imageViewBackground.loadImageEtx(data.imageBgCard.orEmpty())

        binding.textViewRegisteredUser.text = data.interested.orEmpty()
        when (data.lockState) {
            1 -> {
                binding.textViewRegisteredUser.visibility = View.INVISIBLE
                binding.imageViewLock.setImageDrawable(
                        ContextCompat.getDrawable(holder.itemView.context,
                                R.drawable.ic_tag_light_locked))
            }
            2 -> {
                binding.textViewRegisteredUser.visibility = View.INVISIBLE
                binding.imageViewLock.setImageDrawable(
                        ContextCompat.getDrawable(holder.itemView.context,
                                R.drawable.ic_tag_light_unlocked))
            }
            else -> {
                binding.imageViewLock.setImageDrawable(null)
                if (data.interested.isNullOrBlank()) {
                    binding.textViewRegisteredUser.visibility = View.INVISIBLE
                } else {
                    binding.textViewRegisteredUser.visibility = View.VISIBLE
                }
            }
        }

        if (data.showReminder == true) {
            binding.buttonReminder.visibility = View.VISIBLE
        } else {
            binding.buttonReminder.visibility = View.GONE
        }

        binding.buttonReminder.isSelected = data.isReminderSet == 1

        binding.button.text = data.button?.text.orEmpty()

        if (data.button?.text.isNullOrBlank()) {
            binding.button.visibility = View.INVISIBLE
        } else {
            binding.button.visibility = View.VISIBLE
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


        binding.buttonReminder.setOnClickListener {
            checkInternetConnection(holder.itemView.context) {
                it.isSelected = !it.isSelected
                data.isReminderSet = if (it.isSelected) 1 else 0

                var message =
                    data.reminderMessage.orDefaultValue("Your reminder has been set")
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
                            context.getDrawable(R.drawable.bg_capsule_black_90)
                        setActionTextColor(ContextCompat.getColor(context, R.color.redTomato))
                        val textView =
                            this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                        textView.setTextColor(ContextCompat.getColor(context, R.color.white))
                        show()
                    }
                }

                markInterested(
                    data.id.orEmpty(), true,
                    data.assortmentId.orEmpty(), data.liveAt, if (it.isSelected) 1 else 0
                )
            }
            analyticsPublisher.publishEvent(
                    AnalyticsEvent(EventConstants.REMINDER_CARD_CLICK,
                            hashMapOf(
                                    EventConstants.WIDGET to mWidgetName,
                                    EventConstants.SUBJECT to data.subject.orEmpty(),
                                    EventConstants.TEACHER_NAME to data.title2.orEmpty(),
                                    EventConstants.BOARD to data.board.orEmpty()
                            ), ignoreSnowplow = true)
            )
        }

        binding.cardContainer.background = Utils.getGradientView(
                data.startGd.orDefaultValue("#232a4f"),
                data.midGd.orDefaultValue("#232a4f"),
                data.endGd.orDefaultValue("#232a4f")
        )

        holder.itemView.setOnClickListener {
            analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                            EventConstants.LIVE_CLASS_VIDEO_PLAYED,
                            hashMapOf(
                                    EventConstants.SOURCE to data.id.orEmpty(),
                                    EventConstants.WIDGET to mWidgetName,
                                    EventConstants.SUBJECT to data.subject.orEmpty(),
                                    EventConstants.TEACHER_NAME to data.title2.orEmpty(),
                                    EventConstants.BOARD to data.board.orEmpty()
                            )
                    )
            )
            val item = data
            if (item.isPremium == true && item.isVip != true) {
                deeplinkAction.performAction(holder.itemView.context, item.paymentDeeplink)
            } else {
                val currentContext = holder.itemView.context
                if (data.state == LIVE
                        || DateUtils.isBeforeCurrentTime(data.liveAt?.toLongOrNull())
                        || data.state == PAST) {
                    //allow to watch video
                    openVideoPage(
                            context = currentContext,
                            id = data.id,
                            page = data.page,
                            parentId = parentId
                    )
                } else {
                    //for future state
                    markInterested(data.id.orEmpty(), false,
                            data.assortmentId.orEmpty(), data.liveAt, 0)
                    if (!data.isLastResource) {
                        deeplinkAction.performAction(holder.itemView.context, item.deeplink)
                    } else {
                        showToast(currentContext, currentContext.getString(R.string.coming_soon))
                    }
                }
            }
        }

        binding.button.setOnClickListener {
            deeplinkAction.performAction(holder.itemView.context, data.button?.deeplink)
            analyticsPublisher.publishEvent(
                    AnalyticsEvent(EventConstants.LIVE_CLASS_GO_TO_COURSE_CLICK,
                            hashMapOf<String, Any>(
                                    EventConstants.WIDGET to mWidgetName,
                                    EventConstants.SUBJECT to data.subject.orEmpty(),
                                    EventConstants.TEACHER_NAME to data.title2.orEmpty(),
                                    EventConstants.BOARD to data.board.orEmpty()
                            ).apply {
                                putAll(model.extraParams ?: hashMapOf())
                            })
            )
        }
        return holder
    }

    private fun markInterested(id: String, isReminder: Boolean, assortmentId: String, liveAt: String?, reminderSet: Int?) {
        DataHandler.INSTANCE.courseRepository.markInterested(id, isReminder, assortmentId, liveAt, reminderSet)
                .applyIoToMainSchedulerOnCompletable()
                .subscribeToCompletable({})
    }

    private fun openVideoPage(context: Context, id: String?, page: String?, parentId: String = "",
                              source: String = Constants.LIVE_CLASS_SIMILAR_VIDEO_PAGE) {
        context.startActivity(VideoPageActivity.startActivity(
                context = context,
                questionId = id.orEmpty(),
                page = page.orEmpty(),
                source = source,
                parentId = parentId)
        )
    }

    class WidgetHolder(
        binding: WidgetLiveClassCarouselCardBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetLiveClassCarouselCardBinding>(binding, widget)

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
            @SerializedName("payment_deeplink") val paymentDeeplink: String?,
            @SerializedName("lock_state") val lockState: Int?,
            @SerializedName("target_exam") val targetExam: String?,
            @SerializedName("bg_exam_tag") val bgExamTag: String?,
            @SerializedName("text_color_exam_tag") val textColorExamTag: String?,
            @SerializedName("assortment_id") val assortmentId: String?,
            @SerializedName("deeplink") val deeplink: String?
    ) : WidgetData()

    @Keep
    data class ButtonData(
        @SerializedName("text") val text: String,
        @SerializedName("action") val action: WidgetAction?,
        @SerializedName("deeplink") val deeplink: String?
    )

    override fun getViewBinding(): WidgetLiveClassCarouselCardBinding {
        return WidgetLiveClassCarouselCardBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

@Parcelize
enum class CardsOrientation : Parcelable {
    VERTICAL, HORIZONTAL
}