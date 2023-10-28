package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.UpdateStudyDostWidget
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.WidgetStudyDostBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by Sachin Saxena on 04/01/21.
 */
class StudyDostWidget(context: Context) : BaseBindingWidget<StudyDostWidget.WidgetHolder,
    StudyDostWidget.Model, WidgetStudyDostBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var userPreference: UserPreference

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    companion object {
        private const val TAG = "StudyDostWidget"

        const val LEVEL_0 = 0
        const val LEVEL_1 = 1
        const val LEVEL_2 = 2
        const val LEVEL_3 = 3
    }

    override fun getViewBinding(): WidgetStudyDostBinding {
        return WidgetStudyDostBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {

        super.bindWidget(holder, model)
        setMargins(
            WidgetLayoutConfig(
                marginTop = 16,
                marginBottom = 16,
                marginLeft = 12,
                marginRight = 12
            )
        )

        val data: Data = model.data
        val binding = holder.binding

        DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { action ->
            when (action) {
                is UpdateStudyDostWidget -> {
                    data.level = defaultPrefs().getInt(Constants.STUDY_DOST_LEVEL, -1)
                    data.description =
                        defaultPrefs().getString(Constants.STUDY_DOST_DESCRIPTION, "")
                    data.image = defaultPrefs().getString(Constants.STUDY_DOST_IMAGE, "")
                    data.ctaText = defaultPrefs().getString(Constants.STUDY_DOST_CTA_TEXT, "")
                    data.deeplink = defaultPrefs().getString(Constants.STUDY_DOST_DEEPLINK, "")
                    data.unreadCount = defaultPrefs().getInt(Constants.STUDY_DOST_UNREAD_COUNT, 0)
                    updateUiBasedOnLevel(data)
                }
            }
        }?.apply {
            compositeDisposable.add(this)
        }

        binding.apply {

            updateUiBasedOnLevel(data)

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.STUDY_DOST_WIDGET_SHOWN,
                    hashMapOf(
                        EventConstants.LEVEL to data.level.toString(),
                        EventConstants.SOURCE to source.orEmpty(),
                    ),
                    ignoreSnowplow = true
                )
            )

            tvChat.setOnClickListener {
                deeplinkAction.performAction(context, data.deeplink)
            }

            tvIgnore.setOnClickListener {
                defaultPrefs().edit {
                    putBoolean(Constants.IGNORE_STUDY_DOST, true)
                }
            }

            studyDostContainer.setOnClickListener {
                when (data.level) {
                    LEVEL_0 -> {
                        requestForStudyDost(data)
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.STUDY_DOST_REQUESTED,
                                hashMapOf(
                                    EventConstants.SOURCE to source.orEmpty(),
                                )
                            )
                        )
                    }
                    LEVEL_2 -> {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.STUDY_DOST_START_CHAT_CLICKED,
                                hashMapOf(
                                    EventConstants.SOURCE to source.orEmpty(),
                                )
                            )
                        )
                        deeplinkAction.performAction(context, data.deeplink)

                        defaultPrefs().edit {
                            putBoolean(Constants.IS_STUDY_DOST_CHAT_STARTED, true)
                        }
                    }
                }
            }
        }

        trackingViewId = data.id

        return holder
    }

    private fun refreshDataIfRequired(data: Data) {
        val levelStoredInPref = defaultPrefs().getInt(Constants.STUDY_DOST_LEVEL, -1)
        val levelFromApi = data.level ?: -1

        if (levelFromApi < levelStoredInPref) {
            data.level = levelStoredInPref
            data.image = defaultPrefs().getString(Constants.STUDY_DOST_IMAGE, null)
            data.ctaText = defaultPrefs().getString(Constants.STUDY_DOST_CTA_TEXT, null)
            data.description = defaultPrefs().getString(Constants.STUDY_DOST_DESCRIPTION, null)
            data.deeplink = defaultPrefs().getString(Constants.STUDY_DOST_DEEPLINK, null)
        }
    }

    private fun updateUiBasedOnLevel(data: Data) {

        refreshDataIfRequired(data)

        widgetViewHolder.binding.apply {

            level0Layout.isVisible = data.level == LEVEL_0
            level1Layout.isVisible = data.level == LEVEL_1
            level2Layout.isVisible = data.level == LEVEL_2
            level3Layout.isVisible = data.level == LEVEL_3

            ivFindStudyDost.isVisible = data.level == LEVEL_0 || data.level == LEVEL_1
            ivStudyDost.isVisible = data.level == LEVEL_2 || data.level == LEVEL_3
            tvChatCount.isVisible = data.level == LEVEL_3

            when (data.level) {
                LEVEL_0 -> {
                    rootLayout.cardElevation = 0F.dpToPx()
                    studyDostContainer.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.bg_study_dost_blue
                        )
                    )
                    ivFindStudyDost.loadImage(data.image)
                    tvTitle1.text = data.description
                    btRequest.text = data.ctaText
                }
                LEVEL_1 -> {
                    rootLayout.cardElevation = 0F.dpToPx()
                    studyDostContainer.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.bg_study_dost_blue
                        )
                    )
                    tvTitle2.text = data.description
                }
                LEVEL_2 -> {
                    rootLayout.cardElevation = 2F.dpToPx()
                    studyDostContainer.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.white
                        )
                    )
                    tvTitle3.text = data.description
                    tvStartChat.text = data.ctaText
                }
                LEVEL_3 -> {
                    rootLayout.cardElevation = 2F.dpToPx()
                    studyDostContainer.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.white
                        )
                    )
                    tvMessage.text = data.description
                    tvChat.text = data.ctaText
                    tvIgnore.text = data.secondaryCtaText
                }
            }

            rootLayout.invalidateOutline()
        }
    }

    private fun requestForStudyDost(data: Data) {
        compositeDisposable.add(
            DataHandler.INSTANCE.studyDostRepository
                .requestForStudyDost()
                .applyIoToMainSchedulerOnSingle()
                .subscribe(
                    {
                        data.description = it.data.description
                        data.level = it.data.level
                        
                        userPreference.updateStudyDostData(it.data)
                        updateUiBasedOnLevel(data)
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        compositeDisposable.clear()
    }

    class WidgetHolder(binding: WidgetStudyDostBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetStudyDostBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String?,
        @SerializedName("is_requested") var isRequested: Boolean?,
        @SerializedName("description") var description: String?,
        @SerializedName("image") var image: String?,
        @SerializedName("level") var level: Int?,
        @SerializedName("cta_text") var ctaText: String?,
        @SerializedName("secondary_cta_text") val secondaryCtaText: String?,
        @SerializedName("deeplink") var deeplink: String?,
        @SerializedName("unread_count") var unreadCount: Int?,
    ) : WidgetData()
}
