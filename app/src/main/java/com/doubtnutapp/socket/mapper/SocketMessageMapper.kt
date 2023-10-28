package com.doubtnutapp.socket.mapper

import android.graphics.Color
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.course.widgets.ImageTextWidget
import com.doubtnutapp.course.widgets.ImageTextWidgetData
import com.doubtnutapp.course.widgets.ImageTextWidgetModel
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.TextWidgetData
import com.doubtnutapp.data.remote.models.TextWidgetModel
import com.doubtnutapp.doubtpecharcha.model.P2PAnswerAcceptModel
import com.doubtnutapp.socket.entity.AttachmentData
import com.doubtnutapp.socket.entity.AttachmentType
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.widgets.*
import javax.inject.Inject

class SocketMessageMapper @Inject constructor(private val userPreference: UserPreference) {

    private fun getAttachmentChildWidget(
        attachmentData: AttachmentData,
        page: String
    ): Pair<WidgetEntityModel<*, *>, String> {
        val attachmentUrl = attachmentData.attachmentUrl
        when (attachmentData.attachmentType) {
            AttachmentType.AUDIO -> {
                return Pair(AudioPlayerWidget.Model().apply {
                    _widgetType = WidgetTypes.TYPE_WIDGET_AUDIO_PLAYER
                    _widgetData = AudioPlayerWidget.Data(
                        audioDuration = attachmentData.audioDuration,
                        attachmentUrl = attachmentUrl
                    )
                }, "Audio Player")
            }
            AttachmentType.IMAGE -> {
                return Pair(ImageCardWidget.Model().apply {
                    _widgetType = WidgetTypes.TYPE_IMAGE_CARD
                    _widgetData = ImageCardWidget.Data(
                        title = attachmentData.title,
                        imageUrl = attachmentUrl,
                        cardRatio = null,
                        isCircle = false,
                        deeplink = "doubtnutapp://full_screen_image?ask_que_uri=${attachmentUrl}&title=" + page,
                        id = WidgetTypes.TYPE_IMAGE_CARD,
                        cardWidth = null,
                        maxImageHeight = 350,
                        cornerRadius = null,
                        titleAlignment = null,
                        scaleType = null,
                    )
                }, "Image")
            }
            AttachmentType.PDF -> {
                return Pair(PdfViewWidget.Model().apply {
                    _widgetType = WidgetTypes.TYPE_WIDGET_PDF_VIEW
                    _widgetData = PdfViewWidget.Data(
                        id = null,
                        title = "PDF",
                        link = attachmentUrl,
                        imageUrl = null,
                        showForwardArrow = false,
                        deeplink = null
                    )
                }, "PDF")
            }
            AttachmentType.VIDEO -> {
                return Pair(StudyGroupVideoCardWidget.Model().apply {
                    _widgetType = WidgetTypes.TYPE_VIDEO_CARD
                    _widgetData = StudyGroupVideoCardWidget.Data(
                        title = attachmentData.title,
                        videoUrl = attachmentData.attachmentUrl,
                        thumbnailUrl = attachmentData.videoThumbnailUrl,
                        deeplink = "doubtnutapp://video_url?url=" + attachmentData.attachmentUrl,
                        id = WidgetTypes.TYPE_VIDEO_CARD,
                        cardWidth = null,
                        maxThumbnailHeight = 350
                    )
                }, "Video")
            }
        }
    }

    fun getAttachmentParentWidget(
        attachmentData: AttachmentData,
        isHost: Int? = null,
        page: String,
        roomId: String?,
        isQuestionMessage: Boolean?=null,
        hostStudentID:String?=null,
        answerAcceptModel: P2PAnswerAcceptModel?=null,
        showShareButton: Boolean=false,
        actionButtonState:Int?=0,
        showStarterText: Boolean?=false
    ): StudyGroupParentWidget.Model {
        val widget = getAttachmentChildWidget(attachmentData, page)
        val childWidget = widget.first
        val displayName = widget.second
        return StudyGroupParentWidget.Model().apply {
            _widgetType = WidgetTypes.TYPE_WIDGET_STUDY_GROUP_PARENT
            _widgetData = StudyGroupParentWidget.Data(
                id = null,
                childWidget = childWidget,
                title = userPreference.getStudentName(),
                visibilityMessage = null,
                studentImageUrl = userPreference.getUserImageUrl(),
                senderStatus = StudyGroupParentWidget.SenderStatus.SELF,
                senderDetail = null,
                ctaText = null,
                isHost = isHost,
                deeplink = null,
                studentId = userPreference.getUserStudentId(),
                widgetDisplayName = displayName,
                roomId = roomId,
                showWhatsappShareButton=false,
                isQuestionMessage = isQuestionMessage,
                solversList = null,
                hostStudentID =hostStudentID ,
                isSolutionAccepted = false,
                answerAcceptModel =answerAcceptModel,
                showShareButton = showShareButton,
                visibleToStudentId = null,
                questionMessageActionButtonState = actionButtonState,
                showStarterText = showStarterText
            )
        }
    }

    fun getVideoThumbnailParentWidget(
        imageUrl: String,
        ocrText: String?,
        questionId: String,
        page: String,
        isHost: Int? = null,
        roomId: String?,
        hostStudentID: String?=null,
        answerAcceptModel: P2PAnswerAcceptModel?=null
    ): StudyGroupParentWidget.Model {
        val videoThumbnailChildWidget = StudyGroupLiveClassWidget.Model().apply {
            _widgetType = WidgetTypes.TYPE_WIDGET_STUDY_GROUP_LIVE_CLASS
            _widgetData = StudyGroupLiveClassWidget.Data(
                imageUrl = imageUrl,
                ocrText = ocrText,
                deeplink = "doubtnutapp://video?qid=$questionId&page=$page",
                questionId = questionId
            )
        }
        return StudyGroupParentWidget.Model().apply {
            _widgetType = WidgetTypes.TYPE_WIDGET_STUDY_GROUP_PARENT
            _widgetData = StudyGroupParentWidget.Data(
                id = null,
                childWidget = videoThumbnailChildWidget,
                title = userPreference.getStudentName(),
                visibilityMessage = null,
                studentImageUrl = userPreference.getUserImageUrl(),
                senderStatus = StudyGroupParentWidget.SenderStatus.SELF,
                senderDetail = null,
                ctaText = null,
                isHost = isHost,
                deeplink = null,
                studentId = userPreference.getUserStudentId(),
                widgetDisplayName = videoThumbnailChildWidget.type,
                roomId = roomId,
                showWhatsappShareButton=false,
                isQuestionMessage = false,
                solversList = null,
                hostStudentID=hostStudentID,
                isSolutionAccepted = false,
                answerAcceptModel = answerAcceptModel,
                showShareButton=false,
                visibleToStudentId = null,
                showStarterText = false
            )
        }
    }

    fun getTextParentWidget(
        message: String,
        isHost: Int? = null,
        roomId: String?,
        studentName: String? = userPreference.getStudentName(),
        studentUrl: String? = userPreference.getUserImageUrl(),
        studentId: String? = userPreference.getUserStudentId(),
        showWhatsappShareButton:Boolean=false,
        isFirstMessage: Boolean? = null,
        doubtPeCharchaAnswerAcceptModel:P2PAnswerAcceptModel?=null,
        hostStudentID: String?=null,
        showShareButton:Boolean=false,
        visibleToStudent:String?=null,
        actionButtonState:Int?=0,
        showAnswerMarkLayout:Boolean?=false,
        showStarterText:Boolean?=false
    ): StudyGroupParentWidget.Model {
        val textChildWidget = TextWidgetModel().apply {
            _widgetType = WidgetTypes.TYPE_TEXT_WIDGET
            _widgetData = TextWidgetData(
                title = message,
                htmlTitle = null,
                textColor = null,
                textSize = null,
                backgroundColor = null,
                isBold = null,
                gravity = null,
                linkify = true,
                startTimeInMillis = null,
                layoutPadding = null,
                deeplink = null,
                forceHideRightIcon = null,
            )
        }
        return StudyGroupParentWidget.Model().apply {
            _widgetType = WidgetTypes.TYPE_WIDGET_STUDY_GROUP_PARENT
            _widgetData = StudyGroupParentWidget.Data(
                id = null,
                childWidget = textChildWidget,
                title = studentName,
                visibilityMessage = null,
                studentImageUrl =  studentUrl,
                senderStatus = StudyGroupParentWidget.SenderStatus.SELF,
                showStarterText =showStarterText ,
                senderDetail = null,
                ctaText = null,
                isHost = isHost,
                deeplink = null,
                studentId = studentId,
                widgetDisplayName = textChildWidget.type,
                roomId = roomId,
                showWhatsappShareButton = showWhatsappShareButton,
                isQuestionMessage = isFirstMessage,
                solversList=null,
                hostStudentID=hostStudentID,
                isSolutionAccepted =false,
                answerAcceptModel = doubtPeCharchaAnswerAcceptModel,
                showShareButton = showShareButton,
                visibleToStudentId = visibleToStudent,
                questionMessageActionButtonState = actionButtonState,
                showAnswerMarkLayout = showAnswerMarkLayout
            )
        }
    }

    fun getImageTextWidget(
        message: String,
        isHost: Int? = null,
        roomId: String?,
        studentName: String? = userPreference.getStudentName(),
        studentUrl: String? = userPreference.getUserImageUrl(),
        studentId: String? =userPreference.getUserStudentId(),
        isFirstMessage: Boolean? = null,
        hostStudentID: String?=null,
        visibleToStudent:String?=null,
        showStarterText:Boolean?=false,
        imageUrl: String?,
        showWhatsappShareButton: Boolean?=false
    ): StudyGroupParentWidget.Model{
        val textChildWidget = ImageTextWidgetModel().apply {
            _widgetType = WidgetTypes.TYPE_WIDGET_IMAGE_TEXT
            _widgetData = ImageTextWidgetData(
                title = "",
                subtitle = message,
                isTitleCenter = false,
                titleTextSize = "14",
                imageUrl = null,
                imageUrl2 = null,
                imageUrl3 = imageUrl,
                subtitleTextSize = "14",
                bgColor = "#ffffff",
                cta = null,
                textColor = "#202020",
                titleBold = false,
                deeplink = "",
                marginTop = "10"
            )
        }

       return StudyGroupParentWidget.Model().apply {
            _widgetType = WidgetTypes.TYPE_WIDGET_STUDY_GROUP_PARENT
            _widgetData = StudyGroupParentWidget.Data(
                id = null,
                childWidget = textChildWidget,
                title = studentName,
                visibilityMessage = null,
                studentImageUrl =  studentUrl,
                senderStatus = StudyGroupParentWidget.SenderStatus.SELF,
                showStarterText =showStarterText ,
                senderDetail = null,
                ctaText = null,
                isHost = isHost,
                deeplink = null,
                studentId = studentId,
                widgetDisplayName = textChildWidget.type,
                roomId = roomId,
                showWhatsappShareButton = showWhatsappShareButton,
                isQuestionMessage = isFirstMessage,
                solversList=null,
                hostStudentID=hostStudentID,
                isSolutionAccepted =false,
                answerAcceptModel = null,
                showShareButton = false,
                visibleToStudentId = visibleToStudent,
                questionMessageActionButtonState = null,
                showAnswerMarkLayout = false
            )
        }
    }
}