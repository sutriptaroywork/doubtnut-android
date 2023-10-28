package com.doubtnutapp.data.onboarding.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Saxena Saxena on 2020-02-15.
 */
@Keep
data class ApiOnBoardingStatus(
    @SerializedName("isOnboardingCompleted") val isOnboardingCompleted: Boolean,
    @SerializedName("student_language") val studentLanguage: ApiStudentLanguage?,
    @SerializedName("student_class") val studentClass: ApiStudentClass?,
    @SerializedName("isVideoWatched") val isVideoWatched: Boolean,
    @SerializedName("selectedExamBoardsList") val selectedExamBoards: List<ApiSelectedExamBoards>?,
    @SerializedName("pin_exist") val pinExist: Boolean?,
    @SerializedName("pin") val pin: String?,
    @SerializedName("study_dost") val studyDost: ApiStudyDost?,
    @SerializedName("study_group") val studyGroup: ApiStudyGroup?,
    @SerializedName("doubt_p2p") val doubtP2p: ApiDoubtP2p?,
    @SerializedName("khelo_jeeto_v2") val kheloAurJeeto: ApiKheloAurJeeto?,
    @SerializedName("doubt_feed_2") val doubtFeed2: ApiDoubtFeed2?,
    @SerializedName("revision_corner") val revisionCorner: ApiRevisionCorner?,
    @SerializedName("dictionary_data") val dictionary: DictionaryData?,
    @SerializedName("dnr") val dnr: ApiDnr?,
    @SerializedName("default_onboarding_deeplink") val defaultOnboardingDeeplink: String?,
    @SerializedName("gmail_verified") val gmailVerified: Boolean?
) {

    @Keep
    data class ApiStudyDost(

        @SerializedName("image")
        val image: String?,

        @SerializedName("description")
        val description: String?,

        @SerializedName("cta_text")
        val ctaText: String?,

        @SerializedName("unread_count")
        val unreadCount: Int?,

        @SerializedName("level")
        val level: Int?,

        @SerializedName("deeplink")
        val deeplink: String?,

        @SerializedName("is_blocked")
        val isBlocked: Boolean?,
    )

    @Keep
    data class ApiStudyGroup(

        @SerializedName("title")
        val title: String,

        @SerializedName("image")
        val image: String,

        @SerializedName("is_mute")
        val isMute: Boolean,

        @SerializedName("deeplink")
        val deeplink: String,
    )

    @Keep
    data class ApiDoubtP2p(

        @SerializedName("title")
        val title: String,

        @SerializedName("image")
        val image: String,

        @SerializedName("deeplink")
        val deeplink: String
    )

    @Keep
    data class ApiKheloAurJeeto(

        @SerializedName("title")
        val title: String,

        @SerializedName("image")
        val image: String,

        @SerializedName("deeplink")
        val deeplink: String
    )

    @Keep
    data class ApiDoubtFeed2(

        @SerializedName("title")
        val title: String,

        @SerializedName("image")
        val image: String,

        @SerializedName("deeplink")
        val deeplink: String
    )

    @Keep
    data class ApiStudentLanguage(

        @SerializedName("code")
        val code: String,

        @SerializedName("display")
        val display: String,

        @SerializedName("name")
        val name: String
    )

    @Keep
    data class ApiStudentClass(

        @SerializedName("code")
        val code: Int,

        @SerializedName("display")
        val display: String,

        @SerializedName("name")
        val name: String
    )

    @Keep
    data class ApiSelectedExamBoards(
        @SerializedName("ccm_id")
        val ccmId: Int,

        @SerializedName("course")
        val course: String,

        @SerializedName("category")
        val category: String
    )

    @Keep
    data class ApiRevisionCorner(

        @SerializedName("title")
        val title: String,

        @SerializedName("image")
        val image: String,

        @SerializedName("deeplink")
        val deeplink: String
    )

    @Keep
    data class DictionaryData(
        @SerializedName("dictionary_text") val text: String?,
        @SerializedName("dictionary_icon_url") val iconUrl: String?,
        @SerializedName("dictionary_deeplink") val deeplink: String?
    )

    @Keep
    data class ApiDnr(

        @SerializedName("title")
        val title: String,

        @SerializedName("title1")
        val title1: String,

        @SerializedName("image")
        val image: String,

        @SerializedName("description")
        val description: String,

        @SerializedName("cta_text")
        val ctaText: String,

        @SerializedName("deeplink")
        val deeplink: String,

        @SerializedName("srp_sf_engagement_time")
        val srpSfEngagementTime: Long,

        @SerializedName("sf_engagement_time")
        val nonSrpSfEngagementTime: Long,

        @SerializedName("lf_engagement_time")
        val lfEngagementTime: Long
    )
}
