package com.doubtnutapp.widgetmanager.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OpenSgUserReportMessageFragment
import com.doubtnutapp.base.SgBlockMember
import com.doubtnutapp.base.SgDeleteMessage
import com.doubtnutapp.base.SgRemoveReportedContainer
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.WidgetStudyGroupReportParentBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.studygroup.model.Cta
import com.doubtnutapp.studygroup.model.StudyGroupActions
import com.doubtnutapp.studygroup.ui.fragment.SgAdminDashboardFragment
import com.doubtnutapp.studygroup.ui.fragment.SgUserReportedMessageFragment
import com.doubtnutapp.widgetmanager.WidgetFactory
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class StudyGroupReportParentWidget(context: Context) : BaseBindingWidget<
        StudyGroupReportParentWidget.WidgetHolder, StudyGroupReportParentWidget.Model, WidgetStudyGroupReportParentBinding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var userPreference: UserPreference

    var source: String? = null

    override fun getViewBinding(): WidgetStudyGroupReportParentBinding {
        return WidgetStudyGroupReportParentBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    @SuppressLint("SetTextI18n")
    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        val data: Data = model.data
        val binding = holder.binding
        binding.apply {

            tvTitle.apply {
                isVisible = data.title.isNullOrEmpty().not()
                text = data.title
            }

            tvReasonTitle.apply {
                isVisible = data.reason != null
                text = data.reason?.title
            }

            tvReasons.apply {
                isVisible = data.reason != null
                text = data.reason?.reasons?.joinToString("\n")
                if (source == SgAdminDashboardFragment.STUDY_GROUP_ADMIN_DASHBOARD || source == SgUserReportedMessageFragment.STUDY_GROUP_USER_REPORTED_MESSAGE) {
                    maxLines = 8
                }
            }

            tvWarning.apply {
                isVisible = data.warningMessage.isNullOrEmpty().not()
                text = data.warningMessage
            }

            btCta1.apply {
                isVisible = data.primaryCta != null
                text = data.primaryCta?.title

                setOnClickListener {
                    data.primaryCta?.let {
                        handleCtaClick(
                            cta = data.primaryCta,
                            holder.absoluteAdapterPosition,
                            data.childWidget?.type
                        )
                    }
                }
            }

            btCta2.apply {
                isVisible = data.secondaryCta != null
                text = data.secondaryCta?.title

                setOnClickListener {
                    data.secondaryCta?.let {
                        handleCtaClick(
                            cta = data.secondaryCta,
                            holder.absoluteAdapterPosition,
                            data.childWidget?.type
                        )
                    }
                }
            }

            tvViewMore.apply {
                isVisible = data.isViewMoreAvailable == true
                text = data.viewMoreContainer?.title

                setOnClickListener {
                    actionPerformer?.performAction(
                        OpenSgUserReportMessageFragment(data.viewMoreContainer?.deeplink)
                    )
                }
            }

            if (data.childWidget?.extraParams == null) {
                data.childWidget?.extraParams = hashMapOf()
            }
            data.childWidget?.extraParams?.put(
                Constants.WIDGET_DISPLAY_NAME, data.widgetDisplayName.orEmpty()
            )

            data.childWidget?.let { widgetEntityModel ->
                WidgetFactory.createViewHolder(
                    context, null, widgetEntityModel.type, actionPerformer, source
                )?.let {
                    widgetContainer.removeAllViews()
                    widgetContainer.addView(it.itemView)
                    WidgetFactory.bindViewHolder(it, widgetEntityModel)
                }
            }

            ivClose.setOnClickListener {
                actionPerformer?.performAction(
                    SgRemoveReportedContainer(
                        containerId = data.containerId,
                        containerType = data.containerType,
                        adapterPosition = holder.absoluteAdapterPosition
                    )
                )
            }
        }
        return holder
    }

    private fun handleCtaClick(cta: Cta, adapterPosition: Int, widgetType: String?) {
        if (cta.deeplink != null) {
            deeplinkAction.performAction(widgetViewHolder.binding.root.context, cta.deeplink)
        } else {
            when (cta.action) {
                StudyGroupActions.DELETE, StudyGroupActions.DELETE_ALL -> {
                    actionPerformer?.performAction(
                        SgDeleteMessage(
                            widgetType = widgetType,
                            deleteType = cta.action,
                            messageId = cta.messageId,
                            millis = null,
                            senderId = cta.senderId,
                            confirmationPopup = cta.confirmationPopup,
                            adapterPosition = adapterPosition
                        )
                    )
                }
                StudyGroupActions.BLOCK -> {
                    actionPerformer?.performAction(
                        SgBlockMember(
                            studentId = cta.senderId,
                            name = cta.senderName,
                            confirmationPopup = cta.confirmationPopup,
                            adapterPosition = adapterPosition
                        )
                    )
                }
            }
        }
    }

    class WidgetHolder(binding: WidgetStudyGroupReportParentBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetStudyGroupReportParentBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("container_id") val containerId: String,
        @SerializedName("container_type") val containerType: String,
        @SerializedName("child_widget") var childWidget: WidgetEntityModel<*, *>?,
        @SerializedName("title") val title: String?,
        @SerializedName("warning_message") val warningMessage: String?,
        @SerializedName("reported_at") val reportedAt: String?,
        @SerializedName("is_view_more_available") val isViewMoreAvailable: Boolean?,
        @SerializedName("view_more_container") val viewMoreContainer: ViewMoreContainer?,
        @SerializedName("reason") val reason: ReportReason?,
        @SerializedName("primary_cta") val primaryCta: Cta?,
        @SerializedName("secondary_cta") val secondaryCta: Cta?,
        @SerializedName("widget_display_name") val widgetDisplayName: String?,
    ) : WidgetData()

    @Keep
    data class ReportReason(
        @SerializedName("title") val title: String?,
        @SerializedName("reasons") val reasons: List<String>?,
    )

    @Keep
    data class ViewMoreContainer(
        @SerializedName("title") val title: String?,
        @SerializedName("deeplink") val deeplink: String?,
    )
}