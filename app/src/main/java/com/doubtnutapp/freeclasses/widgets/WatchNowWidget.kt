
package com.doubtnutapp.freeclasses.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.Keep
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.applyBackgroundTint
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.utils.toO1
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.WidgetWatchNowBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.snackbar.Snackbar
import com.google.gson.annotations.SerializedName
import javax.inject.Inject
import kotlin.math.ceil

class WatchNowWidget(context: Context) :
    BaseBindingWidget<WatchNowWidget.WidgetViewHolder,
            WatchNowWidget.Model, WidgetWatchNowBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "WatchNowWidget"
        const val EVENT_TAG = "watch_now_widget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetWatchNowBinding {
        return WidgetWatchNowBinding.inflate(LayoutInflater.from(context), this, true)
    }

    @SuppressLint("SetTextI18n")
    override fun bindWidget(
        holder: WidgetViewHolder,
        model: Model
    ): WidgetViewHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.tvBadge.isVisible = model.data.badgeTitle.isNullOrEmpty().not()
        binding.tvBadge.text = model.data.badgeTitle
        binding.tvBadge.applyTextSize(model.data.badgeTitleTextSize)
        binding.tvBadge.applyTextColor(model.data.badgeTitleTextColor)
        binding.tvBadge.applyBackgroundTint(model.data.badgeBgColor)

        binding.ivImage.applyBackgroundTint(model.data.bgColor1)
        binding.ivImage2.isVisible = model.data.imageUrl1.isNullOrEmpty().not()
        binding.ivImage2.loadImage(model.data.imageUrl1.ifEmptyThenNull())
        binding.ivPlay.isVisible = model.data.imageUrl2.isNullOrEmpty().not()
        binding.ivPlay.loadImage(model.data.imageUrl2.ifEmptyThenNull())

        binding.tvTitle1.isVisible = model.data.titleOne.isNullOrEmpty().not()
        binding.tvTitle1.text = model.data.titleOne
        binding.tvTitle1.applyTextSize(model.data.titleOneTextSize)
        binding.tvTitle1.applyTextColor(model.data.titleOneTextColor)

        binding.tvTitle2.isVisible = model.data.titleTwo.isNullOrEmpty().not()
        binding.tvTitle2.text = model.data.titleTwo
        binding.tvTitle2.applyTextSize(model.data.titleTwoTextSize)
        binding.tvTitle2.applyTextColor(model.data.titleTwoTextColor)

        binding.ivImage3.isVisible = model.data.titleThree.isNullOrEmpty().not()
        binding.tvTitle3.isVisible = model.data.titleThree.isNullOrEmpty().not()
        binding.tvTitle3.text = model.data.titleThree
        binding.tvTitle3.applyTextSize(model.data.titleThreeTextSize)
        binding.tvTitle3.applyTextColor(model.data.titleThreeTextColor)

        binding.ivImage4.isVisible = model.data.titleFour.isNullOrEmpty().not()
        binding.tvTitle4.isVisible = model.data.titleFour.isNullOrEmpty().not()
        binding.tvTitle4.text = model.data.titleFour
        binding.tvTitle4.applyTextSize(model.data.titleFourTextSize)
        binding.tvTitle4.applyTextColor(model.data.titleFourTextColor)

        binding.btnAction.isVisible = model.data.actionTitleOne.isNullOrEmpty().not()
                && model.data.isNotified == null
        binding.btnAction.text = model.data.actionTitleOne
        binding.btnAction.applyTextSize(model.data.actionTitleOneTextSize)
        binding.btnAction.applyTextColor(model.data.actionTitleOneTextColor)

        binding.clNotify.isVisible = model.data.actionTitleOne.isNullOrEmpty().not()
                && model.data.isNotified != null
        binding.clNotify.applyBackgroundTint(if (model.data.isNotified == true) "#808080" else "#ea532c")
        binding.ivBell.setImageDrawable(
            if (model.data.isNotified == true) ContextCompat.getDrawable(
                context,
                R.drawable.ic_bell_reminder_white
            )
            else ContextCompat.getDrawable(context, R.drawable.ic_home_notification)
        )
        binding.tvNotify.text =
            if (model.data.isNotified == true) model.data.actionTitleOneNotified else model.data.actionTitleOne
        binding.tvNotify.applyTextSize(model.data.actionTitleOneTextSize)
        binding.tvNotify.applyTextColor(if (model.data.isNotified == true) "#808080" else "#ea532c")

        binding.root.setOnClickListener {
            deeplinkAction.performAction(context, model.data.deeplink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CARD_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.WIDGET_TITLE to model.data.titleOne.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        binding.btnAction.setOnClickListener {
            deeplinkAction.performAction(context, model.data.actionDeeplink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.CTA_TEXT to model.data.actionTitleOne.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.IS_NOTIFY to true,
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        binding.clNotify.setOnClickListener {
            model.data.isNotified ?: return@setOnClickListener
            checkInternetConnection(context) {
                markInterested(
                    id = model.data.id.orEmpty(),
                    isReminder = true,
                    assortmentId = model.data.assortmentId.orEmpty(),
                    liveAt = model.data.liveAt.toString(),
                    reminderSet = model.data.isNotified?.toO1()
                )
                model.data.isNotified = !(model.data.isNotified ?: return@setOnClickListener)

                binding.clNotify.applyBackgroundTint(if (model.data.isNotified == true) "#808080" else "#ea532c")
                binding.ivBell.setImageDrawable(
                    if (model.data.isNotified == true) ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_bell_reminder_white
                    )
                    else ContextCompat.getDrawable(context, R.drawable.ic_home_notification)
                )
                binding.tvNotify.text =
                    if (model.data.isNotified == true) model.data.actionTitleOneNotified else model.data.actionTitleOne
                binding.tvNotify.applyTextColor(if (model.data.isNotified == true) "#808080" else "#ea532c")

                var message =
                    model.data.reminderMessage.orDefaultValue("Your reminder has been set")

                if (!(model.data.isNotified ?: return@setOnClickListener)) {
                    message = model.data.reminderRemovedMessage.orDefaultValue("Reminder removed")
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

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.CTA_TEXT to model.data.actionTitleOne.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.IS_NOTIFY to true,
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        binding.viewProgressBackground.isVisible =
            model.data.watchedTime != null && model.data.totalTime != null
        binding.clProgress.isVisible =
            model.data.watchedTime != null && model.data.totalTime != null
        binding.tvProgress.isVisible =
            model.data.watchedTime != null && model.data.totalTime != null

        binding.viewProgressBackground.applyBackgroundTint(model.data.progressBackgroundColor)
        binding.viewProgress.applyBackgroundTint(model.data.progressHighlightColor)
        binding.tvProgress.text =
            ceil((model.data.watchedTime?.toFloatOrNull() ?: 0f) /
                    (model.data.totalTime?.toFloatOrNull() ?: 0f) * 100f).toString() + "%"

        if (model.data.watchedTime != null && model.data.totalTime != null) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.clProgress)
            constraintSet.constrainPercentWidth(
                binding.viewProgress.id,
                (model.data.watchedTime?.toFloatOrNull() ?: 0f) /
                        (model.data.totalTime?.toFloatOrNull() ?: 0f)
            )
            constraintSet.applyTo(binding.clProgress)
        }

        return holder
    }

    class WidgetViewHolder(binding: WidgetWatchNowBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetWatchNowBinding>(binding, widget)

    class Model :
        WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("badge_title") val badgeTitle: String?,
        @SerializedName("badge_title_text_size") val badgeTitleTextSize: String?,
        @SerializedName("badge_title_text_color") val badgeTitleTextColor: String?,
        @SerializedName("badge_bg_color") val badgeBgColor: String?,

        @SerializedName("image_url1") val imageUrl1: String?,
        @SerializedName("image_url2") val imageUrl2: String?,
        @SerializedName("bg_color1") val bgColor1: String?,

        @SerializedName("title_one") val titleOne: String?,
        @SerializedName("title_one_text_size") val titleOneTextSize: String?,
        @SerializedName("title_one_text_color") val titleOneTextColor: String?,

        @SerializedName("title_two") val titleTwo: String?,
        @SerializedName("title_two_text_size") val titleTwoTextSize: String?,
        @SerializedName("title_two_text_color") val titleTwoTextColor: String?,

        @SerializedName("title_three") val titleThree: String?,
        @SerializedName("title_three_text_size") val titleThreeTextSize: String?,
        @SerializedName("title_three_text_color") val titleThreeTextColor: String?,

        @SerializedName("title_four") val titleFour: String?,
        @SerializedName("title_four_text_size") val titleFourTextSize: String?,
        @SerializedName("title_four_text_color") val titleFourTextColor: String?,

        @SerializedName("action_title_one") val actionTitleOne: String?,
        @SerializedName("action_title_one_notified") val actionTitleOneNotified: String?,
        @SerializedName("action_title_one_text_size") val actionTitleOneTextSize: String?,
        @SerializedName("action_title_one_text_color") val actionTitleOneTextColor: String?,
        @SerializedName("action_deeplink") val actionDeeplink: String?,

        @SerializedName("progress_highlight_color") val progressHighlightColor: String?,
        @SerializedName("progress_background_color") val progressBackgroundColor: String?,

        @SerializedName("id") val id: String?,
        @SerializedName("assortment_id") val assortmentId: String?,
        @SerializedName("live_at") val liveAt: Long?,
        @SerializedName("watched_time") val watchedTime: String?,
        @SerializedName("total_time") val totalTime: String?,

        @SerializedName("reminder_message") val reminderMessage: String?,
        @SerializedName("reminder_removed_message") val reminderRemovedMessage: String?,
        @SerializedName("is_notified") var isNotified: Boolean?,

        @SerializedName("deeplink") val deeplink: String?,

        ) : WidgetData()

    @Suppress("SameParameterValue")
    fun markInterested(
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
}