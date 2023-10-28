package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
sealed class P2pSocketResponse

@Keep
data class DoubtP2PData(
    @SerializedName("deeplink") var deeplink: String?,
    @SerializedName("is_admin") var isAdmin: Boolean?,
    @SerializedName("is_host") var isHost: Boolean?,
    @SerializedName("room_id") var roomId: String?,
    @SerializedName("room_type") var roomType: String?,
) : P2pSocketResponse()

@Keep
data class P2pListMember(
    @SerializedName("members") val members: List<DoubtP2PMember>,
    @SerializedName("reasons") val reasons: List<String>?,
    // if true, feedback not complete, if false complete
    @SerializedName("is_feedback") val isFeedback: Boolean?,
    @SerializedName("is_group_limit_reached") val isGroupLimitReached: Boolean?,
    @SerializedName("max_members") val maxMembers: Int,
    @SerializedName("badge_layout_deeplink") val badgeLayoutDeeplink: String?,
    @SerializedName("meta_data") val chatPageMetaData: ChatPageMetaData?,
    @SerializedName("group_limit_reached_msg") val groupLimitReachedMessage: String?
) : P2pSocketResponse()

@Keep
data class ChatPageMetaData(
    @SerializedName("first_automated_message_text") val firstAutomatedResponse: String?,
    @SerializedName("second_automated_message_text") val secondAutomatedTextMessage: String?,
    @SerializedName("host_response_data") val hostResponseData: AnswerData?,
    @SerializedName("answer_pending_data") val answerPendingData: AnswerData?,
    @SerializedName("answer_accepted_data") val answerAcceptedData: AnswerData?,
    @SerializedName("answer_rejected_data") val answerRejectedData: AnswerData?,
    @SerializedName("answer_mark_solve_data") val answerMarkSolveData: AnswerData?,
    @SerializedName("branch_link") val branchDeeplink: String?,
    @SerializedName("doubt_share_message") val doubtShareMessage: String?,
    @SerializedName("doubtnut_whatsapp_number") val doubtNutWhatsappNumber: String?,
    @SerializedName("notify_on_whatsapp_cta_text") val notifyOnWhatsappTitle: String?,
    @SerializedName("notify_on_whatsapp_message") val notifyOnWhatsappMessage: String?,
    @SerializedName("toast_text_already_solved") val toastTextAlreadySolved: String?,
    @SerializedName("notify_on_whatsapp") val notifyOnWhatsapp: Boolean?,
    @SerializedName("question_text") val questionText: String?,
    @SerializedName("question_image_url") val questionImageUrl: String?,
    @SerializedName("starter_question_text") val starterQuestionText: String?,
    @SerializedName("whatsapp_icon") val whatsappImage: String?,
)

@Keep
data class AnswerData(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?
)

@Parcelize
@Keep
data class DoubtP2PMember(
    @SerializedName("student_id") val studentId: String,
    @SerializedName("image") val imgUrl: String?,
    @SerializedName("name", alternate = ["student_displayname"]) val name: String?,
    @SerializedName("is_host") var isHost: Int?,
    @SerializedName("is_active") var isActive: Int?,
    @SerializedName("is_online") var isOnline: Boolean? = false,
    @SerializedName("solve_stage") var solveStage: Int = 0,
    var isRatingSubmitted: Boolean? = false
) : P2pSocketResponse(), Parcelable

@Keep
data class MemberToJoinData(
    @SerializedName("online_members") val onlineMembers: List<DoubtP2PMember>?
) : P2pSocketResponse()

@Keep
data class DoubtP2PAddMember(
    @SerializedName("is_group_limit_reached") val isGroupLimitReached: Boolean
) : P2pSocketResponse()

@Keep
data class DoubtP2PDisconnect(
    @SerializedName("is_host") val isHost: Boolean
) : P2pSocketResponse()

@Keep
data class DoubtP2PQuestionThumbnail(
    @SerializedName("question_id") val questionId: String?,
    @SerializedName("is_active") val isValid: Boolean,
    @SerializedName("thumbnail_image") val thumbnailImage: String?,
    @SerializedName("ocr_text") val ocrText: String?
) : P2pSocketResponse()
