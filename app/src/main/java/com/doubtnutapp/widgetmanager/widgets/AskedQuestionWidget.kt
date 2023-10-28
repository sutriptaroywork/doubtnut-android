package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import com.doubtnut.core.utils.gone
import com.doubtnut.core.utils.isNotNullAndNotEmpty2
import com.doubtnut.core.utils.visible
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetAskedQuestionBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.doubtpecharcha.ui.activity.DoubtP2pActivity
import com.doubtnutapp.matchquestion.ui.activity.FullImageViewActivity
import com.doubtnutapp.studygroup.ui.fragment.SgAdminDashboardFragment
import com.doubtnutapp.studygroup.ui.fragment.SgChatFragment
import com.doubtnutapp.studygroup.ui.fragment.SgPersonalChatFragment
import com.doubtnutapp.studygroup.ui.fragment.SgUserReportedMessageFragment
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 10/5/21.
 */

class AskedQuestionWidget(context: Context) :
    BaseBindingWidget<AskedQuestionWidget.WidgetHolder, AskedQuestionWidget.Model,
            WidgetAskedQuestionBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    private var isImageDownloaded = false

    override fun getViewBinding(): WidgetAskedQuestionBinding {
        return WidgetAskedQuestionBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        if (source != SgChatFragment.STUDY_GROUP && source != SgPersonalChatFragment.SOURCE_PERSONAL_CHAT) {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val data = model.data
        val binding = holder.binding
        binding.apply {
            if (source == SgChatFragment.STUDY_GROUP) {
                rootLayout.setPadding(0)
                questionContainer.cardElevation = 0f
            }

            rootLayout.updateLayoutParams {
                width = if (data.cardWidth != null) {
                    Utils.getWidthFromScrollSize(holder.itemView.context, data.cardWidth) -
                            (marginStart + marginEnd)
                } else {
                    ViewGroup.LayoutParams.MATCH_PARENT
                }
            }
            questionContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                if ((source == SgChatFragment.STUDY_GROUP || source == SgPersonalChatFragment.SOURCE_PERSONAL_CHAT) && data.questionImage.isNullOrEmpty()) {
                    dimensionRatio = null
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    dimensionRatio = data.cardRatio ?: "5.7"
                    height = 0
                }
            }

            if (data.questionImage.isNotNullAndNotEmpty2()) {
                tvQuestion.hide()
                ivQuestion.show()
                when (source) {
                    SgPersonalChatFragment.SOURCE_PERSONAL_CHAT,
                    SgChatFragment.STUDY_GROUP,
                    SgAdminDashboardFragment.STUDY_GROUP_ADMIN_DASHBOARD,
                    SgUserReportedMessageFragment.STUDY_GROUP_USER_REPORTED_MESSAGE -> {
                        val isAutoDownloadEnabledInStudyGroup =
                            defaultPrefs().getBoolean(
                                Constants.SG_IMAGE_AUTO_DOWNLOAD, false
                            )
                        if (isAutoDownloadEnabledInStudyGroup || data.autoDownloadImage) {
                            downloadImage(url = data.questionImage)
                        } else {
                            downloadImage(url = data.questionImage, onlyFromCache = true)
                        }
                    }
                    DoubtP2pActivity.DOUBT_P2P -> {
                        val isAutoDownloadEnabledInP2p =
                            defaultPrefs().getBoolean(Constants.P2P_IMAGE_AUTO_DOWNLOAD, false)
                        if (isAutoDownloadEnabledInP2p || data.autoDownloadImage) {
                            downloadImage(url = data.questionImage)
                        } else {
                            downloadImage(url = data.questionImage, onlyFromCache = true)
                        }
                    }
                    else -> {
                        downloadImage(url = data.questionImage)
                    }
                }
            } else {
                ivQuestion.hide()
                tvQuestion.show()
                tvQuestion.text = data.questionText
            }

            setOnClickListener {
                if (data.questionImage.isNotNullAndNotEmpty() && isImageDownloaded.not()) {
                    downloadImage(data.questionImage)
                    return@setOnClickListener
                }
                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams?.apply {
                    put(Constants.ID, data.id)
                }))
                if (data.deeplink != null) {
                    deeplinkAction.performAction(context, data.deeplink)
                } else if (data.questionImage != null) {
                    context.startActivity(
                        FullImageViewActivity.getStartIntent(
                            context,
                            data.questionImage
                        )
                    )
                }
            }
        }
        return holder
    }

    private fun downloadImage(url: String?, onlyFromCache: Boolean = false) {
        widgetViewHolder.binding.apply {
            if (onlyFromCache) {
                ivQuestion.loadImageFromCache(
                    url = url,
                    placeholder = R.drawable.ic_blur_background,
                    onLoadFailed = {
                        isImageDownloaded = false
                        downloadImage.visible()
                    },
                    onResourceReady = {
                        isImageDownloaded = true
                        downloadImage.gone()
                    }
                )
            } else {
                ivQuestion.loadImageEtx(
                    url = url,
                    onLoadFailed = {
                        isImageDownloaded = false
                        downloadImage.visible()
                    },
                    onResourceReady = {
                        isImageDownloaded = true
                        downloadImage.gone()
                    }
                )
            }
        }
    }

    class WidgetHolder(binding: WidgetAskedQuestionBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetAskedQuestionBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("question_text") val questionText: String?,
        @SerializedName("question_image") val questionImage: String?,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("id") val id: String,
        @SerializedName("auto_download_image") val autoDownloadImage: Boolean = false
    ) : WidgetData()
}