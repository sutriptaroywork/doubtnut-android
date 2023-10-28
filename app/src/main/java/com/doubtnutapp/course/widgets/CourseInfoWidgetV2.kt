package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.sharing.entities.BranchShareData
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnut.core.utils.applyAutoSizeTextTypeUniformWithConfiguration
import com.doubtnut.core.utils.applyStrike
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.ActivateVipTrial
import com.doubtnutapp.base.OnPdfDownloadOrShareClick
import com.doubtnutapp.databinding.WidgetCourseInfoV2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseInfoWidgetV2
constructor(
    context: Context,
) : BaseBindingWidget<CourseInfoWidgetV2.WidgetHolder, CourseInfoWidgetV2Model, WidgetCourseInfoV2Binding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var whatsAppSharing: WhatsAppSharing

    var source: String? = null

    override fun getViewBinding(): WidgetCourseInfoV2Binding {
        return WidgetCourseInfoV2Binding
            .inflate(LayoutInflater.from(context), this, true)
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.forceUnWrap()?.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: CourseInfoWidgetV2Model
    ): WidgetHolder {
        if (model.layoutConfig == null) {
            model.layoutConfig = if (source == CourseBottomSheetFragment.TAG) {
                // removing bottom and
                WidgetLayoutConfig(
                    marginLeft = 0,
                    marginTop = if (holder.bindingAdapterPosition == 0) 10 else 0,
                    marginRight = 0,
                    marginBottom = 0,
                )
            } else {
                WidgetLayoutConfig(
                    marginLeft = 0,
                    marginTop = if (holder.bindingAdapterPosition == 0) 10 else 0,
                    marginRight = 0,
                    marginBottom = 8,
                )
            }
        }
        super.bindWidget(holder, model)

        val binding = holder.binding

        val data = model.data

        binding.tvTitle.text = data.title.orEmpty()
        binding.tvSubtitle.text = data.subtitle.orEmpty()
        binding.tvSeatsTag.text = data.seatsText.orEmpty()
        binding.tvFaqTag.text = data.faqText.orEmpty()

        binding.tvTitle.applyAutoSizeTextTypeUniformWithConfiguration(data.titleTextSize)
        binding.tvSubtitle.applyTextSize(data.subtitleTextSize)

        binding.tvTitle.applyTextColor(data.titleTextColor)
        binding.tvSubtitle.applyTextColor(data.subtitleTextColor)

        binding.tvTextOne.text = model.data.textOne
        binding.tvTextTwo.text = model.data.textTwo

        binding.tvTextOne.applyTextSize(model.data.textOneSize)
        binding.tvTextTwo.applyTextSize(model.data.textTwoSize)

        binding.tvTextOne.applyTextColor(model.data.textOneColor)
        binding.tvTextTwo.applyTextColor(model.data.textTwoColor)

        binding.tvTextOne.isVisible = model.data.textOne.isNullOrEmpty().not()
        binding.tvTextTwo.isVisible = model.data.textTwo.isNullOrEmpty().not()

        binding.tvTextOne.applyStrike(model.data.textOneStrikeThrough)
        binding.tvTextTwo.applyStrike(model.data.textTwoStrikeThrough)

        binding.tvSeatsTag.background = Utils.getShape(
            data.seatsTagBgColor.orEmpty(),
            data.seatsTagBgColor.orEmpty(),
            4f
        )
        binding.tvFaqTag.background = Utils.getShape(
            data.faqTagBgColor.orEmpty(),
            data.faqTagBgColor.orEmpty(),
            4f
        )

        binding.ivDownload.isVisible = !data.downloadUrl.isNullOrBlank()

        binding.ivDownload.setOnClickListener {
            if (!data.downloadUrl.isNullOrBlank())
                actionPerformer?.performAction(
                    OnPdfDownloadOrShareClick(
                        data.downloadUrl,
                        Constants.TYPE_DOWNLOAD
                    )
                )
        }

        binding.ivWhatsappShare.isVisible = data.shareData != null

        binding.ivWhatsappShare.setOnClickListener {
            val shareData = data.shareData ?: return@setOnClickListener
            whatsAppSharing.shareOnWhatsApp(
                ShareOnWhatApp(
                    shareData.channel.orEmpty(),
                    featureType = shareData.featureName,
                    campaign = shareData.campaignId.orEmpty(),
                    imageUrl = shareData.shareImageUrl.orEmpty(),
                    controlParams = shareData.controlParams ?: hashMapOf(),
                    bgColor = "#000000",
                    sharingMessage = shareData.shareMessage.orEmpty(),
                    questionId = ""
                )
            )
            whatsAppSharing.startShare(context)
        }

        binding.tvFaqTag.setOnClickListener {
            deeplinkAction.performAction(holder.itemView.context, data.faqDeepLink)
        }

        binding.tvSeatsTag.setOnClickListener {
            if (source == CourseBottomSheetFragment.TAG) {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.MPVP_COURSE_BOTTOMSHEET_SEATS_CLICKED,
                        hashMapOf(
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                            EventConstants.FLAG_ID to model.data.flagrId.orEmpty(),
                            EventConstants.VARIANT_ID to model.data.variantId.orEmpty(),
                        )
                    )
                )
            }

            deeplinkAction.performAction(holder.itemView.context, data.seatsDeepLink)
        }

        if (!data.trialText.isNullOrEmpty()) {
            binding.btnTrial.visibility = View.VISIBLE
            binding.btnTrial.text = data.trialText.orEmpty()
            binding.btnTrial.setOnClickListener {
                if (source == CourseBottomSheetFragment.TAG && model.data.isBuyNowBtn == true) {
                    val eventName = if (CourseBottomSheetFragment.isLoadedViaExploreFragment) {
                        EventConstants.EXPLORE_PAGE_STRIP_PREVIEW_BUY_NOW_CLICKED
                    } else {
                        EventConstants.MPVP_COURSE_BOTTOMSHEET_BUY_NOW_CLICKED
                    }
                    val event = AnalyticsEvent(
                        eventName,
                        hashMapOf<String, Any>(
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                            EventConstants.FLAG_ID to model.data.flagrId.orEmpty(),
                            EventConstants.VARIANT_ID to model.data.variantId.orEmpty(),
                            EventConstants.CTA_TEXT to data.trialText.orEmpty(),
                        ).apply {
                            putAll(model.extraParams.orEmpty())
                        }, ignoreMoengage = false
                    )
                    analyticsPublisher.publishEvent(event)
                    if (eventName == EventConstants.MPVP_COURSE_BOTTOMSHEET_BUY_NOW_CLICKED) {
                        val countToSendEvent: Int = Utils.getCountToSend(
                            RemoteConfigUtils.getEventInfo(),
                            EventConstants.MPVP_COURSE_BOTTOMSHEET_BUY_NOW_CLICKED
                        )
                        val eventCopy = event.copy()
                        repeat((0 until countToSendEvent).count()) {
                            analyticsPublisher.publishBranchIoEvent(eventCopy)
                        }
                    }
                }
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.NCP_TRIAL_BUTTON_TOP_CLICKED,
                        hashMapOf<String, Any>(
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                            EventConstants.FLAG_ID to model.data.flagrId.orEmpty(),
                            EventConstants.VARIANT_ID to model.data.variantId.orEmpty(),
                            EventConstants.CTA_TEXT to data.trialText.orEmpty(),
                        ).apply {
                            putAll(model.extraParams.orEmpty())
                        }
                    )
                )
                if (!data.btnDeeplink.isNullOrEmpty()) {
                    deeplinkAction.performAction(context, data.btnDeeplink.orEmpty())
                } else {

                    actionPerformer?.performAction(ActivateVipTrial(model.data.assortmentId.orEmpty()))
                }
            }
        } else {
            binding.btnTrial.visibility = View.INVISIBLE
        }

        return holder
    }

    class WidgetHolder(binding: WidgetCourseInfoV2Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseInfoV2Binding>(binding, widget)
}

@Keep
class CourseInfoWidgetV2Model :
    WidgetEntityModel<CourseInfoWidgetV2Data, WidgetAction>()

@Keep
data class CourseInfoWidgetV2Data(
    @SerializedName("title")
    val title: String?,
    @SerializedName("title_text_size")
    val titleTextSize: String?,
    @SerializedName("title_text_color")
    val titleTextColor: String?,

    @SerializedName("subtitle")
    val subtitle: String?,
    @SerializedName("subtitle_text_size")
    val subtitleTextSize: String?,
    @SerializedName("subtitle_text_color")
    val subtitleTextColor: String?,

    @SerializedName("text_one")
    val textOne: String?,
    @SerializedName("text_one_size")
    val textOneSize: String?,
    @SerializedName("text_one_color")
    val textOneColor: String?,
    @SerializedName("text_one_strike_through")
    val textOneStrikeThrough: Boolean?,

    @SerializedName("text_two")
    val textTwo: String?,
    @SerializedName("text_two_size")
    val textTwoSize: String?,
    @SerializedName("text_two_color")
    val textTwoColor: String?,
    @SerializedName("text_two_strike_through")
    val textTwoStrikeThrough: Boolean?,

    @SerializedName("tag_one_text")
    val seatsText: String?,
    @SerializedName("tag_one_deeplink")
    val seatsDeepLink: String?,
    @SerializedName("tag_one_bg_color")
    val seatsTagBgColor: String?,
    @SerializedName("tag_two_text")
    val faqText: String?,
    @SerializedName("tag_two_deeplink")
    val faqDeepLink: String?,
    @SerializedName("tag_two_bg_color")
    val faqTagBgColor: String?,

    @SerializedName("btn_text")
    val trialText: String?,
    @SerializedName("btn_deeplink")
    val btnDeeplink: String?,
    @SerializedName("is_buy_now_btn")
    val isBuyNowBtn: Boolean?,
    @SerializedName("assortment_id")
    val assortmentId: String?,
    @SerializedName("download_url")
    val downloadUrl: String?,
    @SerializedName("share_data")
    val shareData: BranchShareData?,

    var flagrId: String?,
    var variantId: String?
) : WidgetData()
