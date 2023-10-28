package com.doubtnutapp.widgetmanager.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.format.DateUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.PopupWindow
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.applyBackgroundColor
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.referral.data.entity.DoubtP2pPageMetaData
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.*
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.TextWidgetData
import com.doubtnutapp.databinding.WidgetStudyGroupParentBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.doubtpecharcha.model.P2PAnswerAcceptModel
import com.doubtnutapp.doubtpecharcha.ui.LayoutAnswerAcceptView
import com.doubtnutapp.doubtpecharcha.ui.activity.DoubtP2pActivity
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.studygroup.model.StudyGroupActions
import com.doubtnutapp.studygroup.ui.fragment.SgPersonalChatFragment
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.widgetmanager.WidgetFactory
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.google.android.material.shape.CornerFamily
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.widget_study_group_parent.view.*
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StudyGroupParentWidget(context: Context) : BaseBindingWidget<
        StudyGroupParentWidget.WidgetHolder, StudyGroupParentWidget.Model, WidgetStudyGroupParentBinding>(
    context
) {

    companion object {
        const val EXTRA_BOTTOM_MARGIN = 80
        const val EVENT_TAG = "sg_parent_widget"

        const val STATE_ANSWER_MARK_PENDING = 0
        const val STATE_ACCEPTANCE_PENDING = "PENDING"
        const val STATE_ACCEPTANCE_ACCEPTED = "ACCEPTED"
        const val STATE_ACCEPTANCE_REJECTED = "REJECTED"
        const val STATE_ACCEPTANCE_REJECTED_AND_SOLVED = "REJECTED_AND_SOLVED"
        const val DOUBTNUT_STUDENT_ID = "DOUBTNUT_STUDENT_ID"
        const val STATE_UNMARKED = "UNMARKED"

        const val STATE_QUESTION_PENDING = 1
        const val STATE_QUESTION_SOLVING = 2
        const val STATE_QUESTION_SOLVED = 3
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var whatsAppSharing: WhatsAppSharing

    var source: String? = null

    override fun getViewBinding(): WidgetStudyGroupParentBinding {
        return WidgetStudyGroupParentBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        val data = model.data
        val binding = holder.binding
        data.senderStatus = when (data.studentId) {
            userPreference.getUserStudentId() -> {
                SenderStatus.SELF
            }
            null -> {
                SenderStatus.DOUBTNUT
            }
            else -> {
                SenderStatus.OTHER
            }
        }
        binding.apply {
            tvVisibilityMessage.text = data.visibilityMessage

            val diff = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - data.createdAt)
            tvTime.text = if (diff <= 0) {
                context.getString(R.string.seconds_ago)
            } else {
                DateUtils.getRelativeDateTimeString(
                    context, data.createdAt, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS,
                    DateUtils.FORMAT_SHOW_TIME
                )
            }

            if (data.ctaText.isNullOrEmpty().not()) {
                tvCta.apply {
                    show()
                    text = data.ctaText
                    setOnClickListener {
                        deeplinkAction.performAction(context, data.deeplink)
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                                hashMapOf<String, Any>(
                                    EventConstants.MESSAGE_ID to data.id.orEmpty(),
                                    EventConstants.GROUP_ID to data.roomId.orEmpty(),
                                    EventConstants.CTA_TEXT to data.ctaText.orEmpty(),
                                    EventConstants.EVENT_NAME_DEEPLINK to data.deeplink.orEmpty(),
                                    EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                                    Constants.WIDGET_DISPLAY_NAME to data.widgetDisplayName.orEmpty()
                                ).apply {
                                    putAll(model.extraParams.orEmpty())
                                },
                                ignoreSnowplow = false,
                                ignoreMoengage = false,
                                ignoreFirebase = false
                            )
                        )
                    }
                }
            } else {
                tvCta.hide()
            }

            val cornerRadius = 16f.dpToPx()

            when (data.senderStatus) {
                SenderStatus.SELF -> {
                    ivSender.hide()
                    tvTitle.hide()
                    tvSenderDetail.hide()

                    cardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        horizontalBias = 1f
                    }
                    cardView.shapeAppearanceModel = cardView.shapeAppearanceModel.toBuilder()
                        .setAllCorners(CornerFamily.ROUNDED, cornerRadius)
                        .setTopRightCornerSize(0f)
                        .build()
                    cardView.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.green_ebffda)
                    )
                }
                SenderStatus.OTHER -> {
                    ivSender.show()
                    tvTitle.show()

                    data.isHost?.let {
                        when (it) {
                            1 -> tvTitle.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_host_symbol,
                                0
                            )
                            else -> tvTitle.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_helper_symbol,
                                0
                            )
                        }
                    }

                    tvSenderDetail.hide()

                    ivSender.loadImage(
                        data.studentImageUrl,
                        R.drawable.ic_profile_placeholder, R.drawable.ic_profile_placeholder
                    )

                    cardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        horizontalBias = 0f
                    }
                    cardView.shapeAppearanceModel = cardView.shapeAppearanceModel.toBuilder()
                        .setAllCorners(CornerFamily.ROUNDED, cornerRadius)
                        .setTopLeftCornerSize(0f)
                        .build()
                    cardView.setCardBackgroundColor(Color.WHITE)

                    tvTitle.textSize = 12f
                    tvTitle.setTextColor(ContextCompat.getColor(context, R.color.red_ca330c))
                    tvTitle.text = data.title
                }
                SenderStatus.DOUBTNUT -> {
                    ivSender.show()
                    if (data.title.isNotNullAndNotEmpty()) {
                        tvTitle.show()
                    } else {
                        tvTitle.hide()
                    }
                    tvTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    tvSenderDetail.hide()

                    ivSender.load(R.drawable.ic_logo_dn)
                    ivSender.setBackgroundColor(Color.WHITE)

                    cardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        horizontalBias = 0f
                    }
                    cardView.shapeAppearanceModel = cardView.shapeAppearanceModel.toBuilder()
                        .setAllCorners(CornerFamily.ROUNDED, cornerRadius)
                        .setTopLeftCornerSize(0f)
                        .build()
                    cardView.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.orange_ffe1d9)
                    )

                    tvTitle.textSize = 12f
                    tvTitle.setTextColor(Color.BLACK)
                    tvTitle.text = data.title

                    tvSenderDetail.text = data.senderDetail
                }
            }

            ivSender.setOnClickListener {
                FragmentWrapperActivity.userProfile(
                    context,
                    data.studentId.orEmpty(), "doubt_p2p"
                )
            }

            if (data.childWidget == null) {
                data.childWidget = StudyGroupFeatureUnavailableWidget.Model()
            }

            if (data.childWidget?.extraParams == null) {
                data.childWidget?.extraParams = hashMapOf()
            }
            data.childWidget?.extraParams?.put(
                Constants.WIDGET_DISPLAY_NAME, data.widgetDisplayName.orEmpty()
            )

            data.childWidget?.let { widgetEntityModel ->
                WidgetFactory.createViewHolder(
                    context, null, widgetEntityModel.type, object : ActionPerformer {
                        override fun performAction(action: Any) {
                            when (action) {
                                is SgChildWidgetLongClick -> showPopup(
                                    view = widgetContainer,
                                    data = data,
                                    floatArray = action.lastTouchDownXY
                                )

                                is OpenAudioPlayerDialog -> actionPerformer?.performAction(
                                    OpenAudioPlayerDialog(
                                        audioDuration = action.audioDuration,
                                        audioUrl = action.audioUrl
                                    )
                                )
                                else -> {
                                }
                            }
                        }
                    }, source
                )?.let {
                    widgetContainer.removeAllViews()
                    widgetContainer.addView(it.itemView)
                    WidgetFactory.bindViewHolder(it, widgetEntityModel)
                }
            }

            val lastTouchDownXY = FloatArray(2)
            setOnTouchListener(OnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    lastTouchDownXY[0] = event.x
                    lastTouchDownXY[1] = event.y - height
                }
                return@OnTouchListener false
            })

            setOnLongClickListener { view ->
                showPopup(view, data, lastTouchDownXY)
                true
            }


            if (data.showShareButton == true && data.studentId == userPreference.getUserStudentId()) {
                imageViewShareButton.show()
                imageViewShareButton.setOnClickListener {
                    actionPerformer?.performAction(
                        OnDoubtPeCharchaQuestionShareButtonClicked()
                    )
                }
            } else {
                imageViewShareButton.hide()
            }
        }

        if (data.showWhatsappShareButton == true) {
            actionButton.show()
            tvActionButton.applyBackgroundColor("#54b726")
            binding.tvActionButton.text =
                DoubtP2pPageMetaData.notifyOnWhatsappTitle ?: "Notify on Whatsapp"
            binding.ivActionButton.visibility = View.VISIBLE
            binding.ivActionButton.loadImage(DoubtP2pPageMetaData.whatsappImage.orEmpty())
        } else {
            actionButton.hide()
            binding.ivActionButton.visibility = View.GONE
        }


        data.isQuestionMessage?.let {
            if (data.studentId == userPreference.getUserStudentId()) {
                actionButton.visibility = View.GONE
                return@let
            }
            if (it) {
                actionButton.visibility = View.VISIBLE
                tvActionButton.text = context.getString(R.string.solve_now)
                data.solversList?.let {
                    for (item in it) {
                        if (item == userPreference.getUserStudentId()) {
                            setSolvedStateForActionButton(binding)
                        }
                    }
                }
                data.isSolutionAccepted?.let {
                    if (it) {
                        setSolvedStateForActionButton(binding)
                    }
                }
            } else {
                actionButton.hide()
            }

            data.questionMessageActionButtonState?.let { questionMessageState ->
                if (data.studentId == userPreference.getUserStudentId()) {
                    actionButton.hide()
                    return@let
                }
                when (questionMessageState) {
                    STATE_QUESTION_SOLVING -> {
                        setSolvingStateForActionButton(binding)
                    }

                    STATE_QUESTION_SOLVED -> {
                        setSolvedStateForActionButton(binding)
                    }
                }
            } ?: kotlin.run {
                actionButton.hide()
            }
        }




        if (data.showWhatsappShareButton == true) {
            actionButton.show()
        }

        if (data.showStarterText == true) {
            val text = DoubtP2pPageMetaData.starterQuestionText ?: "Please help me solve this"
            binding.tvStarterText.text = text
            binding.tvStarterText.show()
        } else {
            binding.tvStarterText.hide()
        }


        binding.actionButton.setOnClickListener {
            handleActionButtonClick(binding, data)
        }


        data.answerAcceptModel?.let {
            binding.layoutAnswerAccept.show()
            binding.layoutAnswerAccept.render(data, userPreference.getUserStudentId(),
                object : LayoutAnswerAcceptView.LayoutAnswerAcceptButtonClickListener {
                    override fun onYesClicked(state: String, isHost: Boolean) {
                        handleLayoutAnswerAcceptViewYesClickListeners(state, isHost, data)
                    }

                    override fun onNoClicked(state: String, isHost: Boolean) {
                        handleLayoutAnswerAcceptViewNoClickListeners(state, isHost, data)
                    }

                    override fun onSubtitleClicked(state: String, isHost: Boolean) {
                        if (state == StudyGroupParentWidget.STATE_ACCEPTANCE_REJECTED && !isHost) {
                            DoubtnutApp.INSTANCE.bus()
                                ?.send(OnSolveNowAfterRejection(data.createdAt))
                        }
                    }
                })

        } ?: run {
            binding.layoutAnswerAccept.hide()

        }


        return holder
    }

    private fun handleLayoutAnswerAcceptViewYesClickListeners(
        state: String, isHost: Boolean,
        data: Data
    ) {
        if (state == STATE_ACCEPTANCE_PENDING && isHost) {
            DoubtnutApp.INSTANCE.bus()?.send(OnSolutionAccepted(data.createdAt, data.studentId))
        } else if (state == STATE_UNMARKED && !isHost) {
            DoubtnutApp.INSTANCE.bus()?.send(OnSolutionMarkedAsFinal(data.createdAt))
        }
    }

    private fun handleLayoutAnswerAcceptViewNoClickListeners(
        state: String, isHost: Boolean,
        data: Data
    ) {
        if (state == STATE_ACCEPTANCE_PENDING && isHost) {
            DoubtnutApp.INSTANCE.bus()?.send(OnSolutionRejected(data.createdAt, data.studentId))
        } else if (state == STATE_UNMARKED && !isHost) {
            DoubtnutApp.INSTANCE.bus()?.send(OnThisIsNotMyAnswerClicked(data.createdAt))
        }
    }

    private fun handleActionButtonClick(binding: WidgetStudyGroupParentBinding, data: Data) {
        if (data.showWhatsappShareButton == true) {
            val message = URLEncoder.encode(
                DoubtP2pPageMetaData.whatsappNotifyText.orEmpty(),
                "UTF-8"
            )
            val deeplink = "doubtnutapp://whatsapp?external_url" +
                    "=https://api.whatsapp.com/send?phone=${DoubtP2pPageMetaData.doubtnut_whatsapp_number.orEmpty()}" +
                    "&text=${message}"
            deeplinkAction.performAction(context, deeplink)
        } else if (data.isSolutionAccepted == true) {
            val toastMessage =
                DoubtP2pPageMetaData.toastAlreadySolved
                    ?: context.getString(R.string.question_already_solved)
            showToast(context, toastMessage)
        } else {
            data.questionMessageActionButtonState?.let { questionMessageState ->
                when (questionMessageState) {
                    STATE_QUESTION_SOLVING -> {
                        tvActionButton.setOnClickListener {
                            //Do nothing
                        }
                    }
                    STATE_QUESTION_SOLVED -> {
                        val toastMessage =
                            DoubtP2pPageMetaData.toastAlreadySolved
                                ?: context.getString(R.string.question_already_solved)
                        showToast(context, toastMessage)
                    }
                    else -> {
                        binding.apply {
                            DoubtnutApp.INSTANCE.bus()
                                ?.send(OnSolveNowButtonClicked(data.createdAt))
                            tvActionButton.text = "Solving"
                            actionButton.isEnabled = false
                            tvActionButton.setBackgroundColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.grey_808080
                                )
                            )
                        }
                    }
                }
            }
        }

    }

    private fun setSolvedStateForActionButton(binding: WidgetStudyGroupParentBinding) {
        binding.apply {
            actionButton.visibility = View.VISIBLE
            tvActionButton.text = "Solved"
            tvActionButton.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.green
                )
            )
        }
    }

    private fun setSolvingStateForActionButton(binding: WidgetStudyGroupParentBinding) {
        binding.apply {
            tvActionButton.text = "Solving"
            actionButton.isEnabled = false
            actionButton.show()
            tvActionButton.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.grey_808080
                )
            )
        }
    }

    @SuppressLint("InflateParams")
    private fun showPopup(
        view: View,
        data: Data,
        floatArray: FloatArray,
    ) {

        if (source == DoubtP2pActivity.DOUBT_P2P) return

        val popupWindowView =
            LayoutInflater.from(context).inflate(R.layout.popup_long_press, null, true)
        val widgetType = data.childWidget?.type
        val mMessageId = data.id
        val mSenderId = data.studentId
        val millis = data.createdAt

        val popupWindow = PopupWindow(view.context)
        popupWindow.contentView = popupWindowView

        popupWindow.width = LayoutParams.WRAP_CONTENT
        popupWindow.height = LayoutParams.WRAP_CONTENT
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 20f
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.LTGRAY))

        popupWindowView.findViewById<ConstraintLayout>(R.id.copyLayout).isVisible =
            widgetType == WidgetTypes.TYPE_TEXT_WIDGET

        when (data.senderStatus) {

            SenderStatus.SELF -> {
                popupWindowView.findViewById<ConstraintLayout>(R.id.reportLayout).hide()
            }

            SenderStatus.OTHER -> {
                popupWindowView.findViewById<ConstraintLayout>(R.id.deleteLayout).hide()
            }

            SenderStatus.DOUBTNUT -> {
                return
            }
        }

        if (source == SgPersonalChatFragment.SOURCE_PERSONAL_CHAT) {
            popupWindowView.findViewById<ConstraintLayout>(R.id.reportLayout).hide()
        }

        popupWindowView.findViewById<ConstraintLayout>(R.id.copyLayout).setOnClickListener {
            val textData =
                if (data.childWidget?.type == WidgetTypes.TYPE_TEXT_WIDGET) data.childWidget?.data as TextWidgetData else null
            if (textData != null) {
                actionPerformer?.performAction(
                    SgCopyMessage(
                        messageToCopy = textData.title,
                        toastMessage = R.string.sg_text_copied,
                        errorMessage = R.string.sg_copy_message_error_without_join
                    )
                )
            }
            popupWindow.dismiss()
        }

        popupWindowView.findViewById<ConstraintLayout>(R.id.deleteLayout).setOnClickListener {
            actionPerformer?.performAction(
                SgDeleteMessage(
                    widgetType = widgetType,
                    deleteType = StudyGroupActions.DELETE,
                    messageId = mMessageId,
                    millis = if (mMessageId == null) millis else null,
                    senderId = if (mMessageId == null) mSenderId else null,
                    confirmationPopup = null,
                    adapterPosition = widgetViewHolder.absoluteAdapterPosition
                )
            )
            popupWindow.dismiss()
        }

        popupWindowView.findViewById<ConstraintLayout>(R.id.reportLayout).setOnClickListener {
            actionPerformer?.performAction(
                SgReportMessage(
                    messageId = mMessageId,
                    senderId = mSenderId,
                    millis = millis
                )
            )
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(
            view,
            floatArray[0].toInt(),
            floatArray[1].toInt() - EXTRA_BOTTOM_MARGIN,
            Gravity.BOTTOM
        )
    }

    class WidgetHolder(binding: WidgetStudyGroupParentBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetStudyGroupParentBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("child_widget") var childWidget: WidgetEntityModel<*, *>?,
        @SerializedName("title") val title: String?, // Student Name
        @SerializedName("_id") var id: String?,
        @SerializedName("visibility_message") val visibilityMessage: String?,
        @SerializedName("student_img_url") val studentImageUrl: String?,
        @SerializedName("type") var senderStatus: Int,
        @SerializedName("sender_detail") val senderDetail: String?,
        @SerializedName("show_starter_text") val showStarterText: Boolean?,
        @SerializedName("cta_text") val ctaText: String?,
        @SerializedName("is_host") val isHost: Int? = 0,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("created_at") val createdAt: Long = System.currentTimeMillis(),
        @SerializedName("student_id") val studentId: String?,
        @SerializedName("widget_display_name") val widgetDisplayName: String?,
        @SerializedName("room_id") var roomId: String?,
        @SerializedName("show_whatsapp_share_button") val showWhatsappShareButton: Boolean?,// for p2p doubt
        @SerializedName("is_question_message") var isQuestionMessage: Boolean?,// for p2p doubt
        @SerializedName("solvers_list") var solversList: ArrayList<String>?,// for p2p doubt
        @SerializedName("is_solution_accepted") var isSolutionAccepted: Boolean?,
        @SerializedName("is_solved_by_user") var isSolvedByUser: Boolean? = false,
        @SerializedName("host_student_id") var hostStudentID: String?,// for p2p doubt
        @SerializedName("show_share_button") var showShareButton: Boolean?,// for p2p doubt
        @SerializedName("visible_to") var visibleToStudentId: String?,// for p2p doubt
        @SerializedName("question_message_action_button_state") var questionMessageActionButtonState: Int? = 0,// for p2p doubt
        @SerializedName("show_answer_mark_layout") var showAnswerMarkLayout: Boolean? = false,
        @SerializedName("answer_accept_data") // for p2p doubt
        var answerAcceptModel: P2PAnswerAcceptModel?,

        ) : WidgetData()

    @Keep
    data class LayoutAnswerAccept(
        @SerializedName("title") val title: String,
        @SerializedName("subtitle") val subtitle: String,
        @SerializedName("cta_yes") val ctaYes: Boolean?,
        @SerializedName("cta_no") val cta_yes: Boolean?,
    )

    object SenderStatus {
        const val SELF = 0
        const val OTHER = 1
        const val DOUBTNUT = 2
    }
}