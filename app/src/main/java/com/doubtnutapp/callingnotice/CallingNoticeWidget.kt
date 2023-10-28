package com.doubtnutapp.callingnotice

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.utils.setTextFromHtml
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.CourseBottomSheetFragment
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.CallingNoticeWidgetBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CallingNoticeWidget(context: Context) : BaseBindingWidget<CallingNoticeWidget.WidgetHolder,
        CallingNoticeWidgetModel, CallingNoticeWidgetBinding>(context) {

    companion object {
        const val TAG = "CallingNoticeWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): CallingNoticeWidgetBinding {
        return CallingNoticeWidgetBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CallingNoticeWidgetModel): WidgetHolder {
        if (model.layoutConfig == null) {
            model.layoutConfig =
                if (source == CourseBottomSheetFragment.TAG) {
                    // reducing space than default
                    WidgetLayoutConfig(
                        marginTop = 8,
                        marginBottom = 8,
                        marginLeft = 0,
                        marginRight = 0,
                    )
                } else {
                    WidgetLayoutConfig(
                        marginTop = 18,
                        marginBottom = 18,
                        marginLeft = 0,
                        marginRight = 0,
                    )
                }
        }
        super.bindWidget(holder, model)

        val binding = holder.binding

        val data: CallingNoticeWidgetData = model.data

        binding.ivBackground.loadImage(
            data.bgImage
        )

        binding.iconIv.loadImage(data.icon)

        binding.tvMsg.applyTextColor(data.messageColor)
        binding.tvMsg.applyTextSize(data.messageSize.orEmpty())
        binding.tvMsg.setTextFromHtml(data.message.orEmpty())

        binding.root.setOnClickListener {
            if (source == CourseBottomSheetFragment.TAG) {
                val eventName = if (CourseBottomSheetFragment.isLoadedViaExploreFragment) {
                    EventConstants.EXPLORE_PAGE_STRIP_PREVIEW_CALL_CLICKED
                } else {
                    EventConstants.MPVP_COURSE_BOTTOMSHEET_CALL_US_CLICKED
                }
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        eventName,
                        hashMapOf<String, Any>(
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.FLAG_ID to model.data.flagrId.orEmpty(),
                            EventConstants.VARIANT_ID to model.data.variantId.orEmpty(),
                        ).apply {
                            putAll(model.extraParams ?: hashMapOf())
                        },
                        ignoreBranch = false
                    )
                )
            }
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.GENERIC_CARD_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.SOURCE to source.orEmpty()
                    ).apply {
                        putAll(model.extraParams ?: hashMapOf())
                    }
                )
            )
            if (model.data.deeplink.isNotNullAndNotEmpty()) {
                deeplinkAction.performAction(context, model.data.deeplink)

                if (model.data.deeplink2.isNotNullAndNotEmpty()) {
                    deeplinkAction.performAction(context, model.data.deeplink2)
                }
            } else {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + data.mobile))
                context.startActivity(intent)
            }
        }


        return holder
    }

    class WidgetHolder(binding: CallingNoticeWidgetBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<CallingNoticeWidgetBinding>(binding, widget)
}

class CallingNoticeWidgetModel : WidgetEntityModel<CallingNoticeWidgetData, WidgetAction>()

@Keep
data class CallingNoticeWidgetData(
    @SerializedName("icon_url") val icon: String?,
    @SerializedName("title") val message: String?,
    @SerializedName("title_color") val messageColor: String?,
    @SerializedName("title_size") val messageSize: String?,

    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("deeplink2") val deeplink2: String?,

    @SerializedName("mobile") val mobile: String?,
    var flagrId: String?,
    var variantId: String?,
    @SerializedName("bg_image_url")
    val bgImage: String?,
) : WidgetData()
