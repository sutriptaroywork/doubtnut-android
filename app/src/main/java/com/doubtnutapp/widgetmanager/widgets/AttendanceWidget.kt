package com.doubtnutapp.widgetmanager.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.content.edit
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.reward.RewardPopupModel
import com.doubtnutapp.databinding.WidgetAttendanceBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 9/4/21.
 */

class AttendanceWidget(context: Context)
    : BaseBindingWidget<AttendanceWidget.WidgetHolder, AttendanceWidget.Model,
        WidgetAttendanceBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var gson: Gson

    override fun getViewBinding(): WidgetAttendanceBinding {
        return WidgetAttendanceBinding.inflate(LayoutInflater.from(context), this,true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(),this)
    }

    @SuppressLint("CheckResult")
    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        holder.binding.buttonDismiss.setOnClickListener {
            sendEvent(EventConstants.ATTENDANCE_CARD_CROSS_CLICK, ignoreSnowplow = true)
            holder.binding.rootCardView.hide()
            setUserInteractionDone(true)
        }
        holder.binding.ignoreBtn.setOnClickListener {
            holder.binding.rootCardView.hide()
            setUserInteractionDone(true)
        }
        holder.binding.exploreBtn.setOnClickListener {
            setUserInteractionDone(true)
            DoubtnutApp.INSTANCE.bus()?.send(OpenRewardActivity)
            sendEvent(EventConstants.ATTENDANCE_CARD_EXPLORE_CLICK, ignoreSnowplow = true)
        }

        if (isAttendanceUnmarked() && model.data.isDataFetched.not()) {
            DoubtnutApp.INSTANCE.bus()?.send(GetMarkAttendancePopup)
            model.data.isDataFetched = true
            DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
                when (it) {
                    is ShowMarkAttendanceWidget -> {
                        if (it.rewardPopupModel?.popupHeading == null || it.rewardPopupModel.popupDescription == null) {
                            holder.binding.rootCardView.hide()
                            return@subscribe
                        }

                        if (defaultPrefs().getBoolean(Constants.ATTENDANCE_WIDGET_USER_INTERACTION_DONE, false).not()) {
                            setUserInteractionDone(false)

                            if (it.rewardPopupModel.isDataOnly) {
                                setupUi(it.rewardPopupModel,holder)
                            } else {
                                updateUiAfterMarkAttendance(it.rewardPopupModel,holder)
                            }
                        }
                    }
                    is HideMarkAttendanceWidget -> {
                        holder.binding.rootCardView.hide()
                    }
                }
            }
        } else if (defaultPrefs().getBoolean(Constants.ATTENDANCE_WIDGET_USER_INTERACTION_DONE, true).not()) {
            getCachedPopupData()?.let {
                updateUiAfterMarkAttendance(it,holder)
            }
        }

        return holder
    }

    private fun setupUi(rewardPopupModel: RewardPopupModel, holder : AttendanceWidget.WidgetHolder) {
        holder.binding.rootCardView.show()

        holder.binding.autoAttendanceButtons.isVisible = rewardPopupModel.isRewardPresent == false
        holder.binding.markAttendanceBtn.isVisible = rewardPopupModel.isRewardPresent == true

        if (!rewardPopupModel.isAttendanceMarked && !rewardPopupModel.isRewardPresent) {
            holder.binding.markAttendanceBtn.isVisible = true
            holder.binding.autoAttendanceButtons.isVisible = false
            holder.binding.markAttendanceBtn.text = context.getString(R.string.click_to_mark_attendance)
            holder.binding.markAttendanceBtn.setOnClickListener {
                DoubtnutApp.INSTANCE.bus()?.send(MarkAttendance)
                sendEvent(EventConstants.ATTENDANCE_MANUALLY_MARKED, ignoreSnowplow = true)
            }
        }

        holder.binding.titleDialogMarkAttendance.text = rewardPopupModel.popupHeading
        holder.binding.descriptionDialogMarkAttendance.text = rewardPopupModel.popupDescription
    }

    private fun updateUiAfterMarkAttendance(popupData: RewardPopupModel, holder : AttendanceWidget.WidgetHolder) {
        if (popupData.popupHeading != null && popupData.popupDescription != null) {
            holder.binding.rootCardView.show()

            holder.binding.autoAttendanceButtons.isVisible = popupData.isRewardPresent == false

            holder.binding.markAttendanceBtn.isVisible = popupData.isRewardPresent == true
            holder.binding.markAttendanceBtn.text = context.getString(R.string.get_it_now)
            holder.binding.markAttendanceBtn.setOnClickListener {
                setUserInteractionDone(true)
                DoubtnutApp.INSTANCE.bus()?.send(OpenRewardActivity)
                sendEvent(EventConstants.ATTENDANCE_CARD_MARKED_GET_IT_NOW, ignoreSnowplow = true)
            }

            holder.binding.titleDialogMarkAttendance.text = popupData.popupHeading
            holder.binding.descriptionDialogMarkAttendance.text = popupData.popupDescription
            if (popupData.isRewardPresent) {
                holder.binding.imageView.load(R.drawable.ic_reward_homepage)
            } else {
                holder.binding.imageView.load(R.drawable.ic_attendance_marked)
            }
        }
    }

    private fun isAttendanceUnmarked(): Boolean {
        val lastMarkedAttendanceTime = defaultPrefs().getLong(Constants.LAST_MARKED_DAY, 0)
        return !DateUtils.isToday(lastMarkedAttendanceTime)
    }

    private fun setUserInteractionDone(isDone: Boolean) {
        defaultPrefs().edit {
            putBoolean(Constants.ATTENDANCE_WIDGET_USER_INTERACTION_DONE, isDone)
        }
    }

    private fun getCachedPopupData(): RewardPopupModel? {
        val dataString = defaultPrefs().getString(Constants.REWARD_POPUP_DATA_AFTER_MARK_ATTENDANCE, "").orEmpty()
        return gson.fromJson(dataString, RewardPopupModel::class.java)
    }

    private fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        params[Constants.CURRENT_LEVEL] = userPreference.getRewardSystemCurrentLevel()
        params[Constants.CURRENT_DAY] = userPreference.getRewardSystemCurrentDay()
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }

    class WidgetHolder(binding: WidgetAttendanceBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetAttendanceBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
            @SerializedName("id") val id: String,
            var isDataFetched: Boolean = false,
    ) : WidgetData()
}