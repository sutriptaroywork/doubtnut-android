package com.doubtnutapp.domain.splashscreen.entity

import androidx.annotation.Keep

@Keep
data class OnBoardingStatus(

    val isOnBoardingComplete: Boolean,

    val studentLanguage: StudentLanguage?,

    val studentClass: StudentClass?,

    val isVideoWatched: Boolean,

    val selectedExamBoards: List<SelectedExamBoards>?,

    val pinExist: Boolean? = false,

    val pin: String? = null,

    val studyDost: StudyDost?,

    val studyGroup: StudyGroup?,

    val doubtP2p: DoubtP2p?,

    val kheloAurJeeto: KheloAurJeeto?,

    val doubtFeed2: DoubtFeed2?,

    val revisionCorner: RevisionCorner?,

    val dictionary: Dictionary?,

    val dnr: Dnr?,

    val defaultOnboardingDeeplink: String?
) {

    @Keep
    data class StudyDost(
        val image: String?,

        val description: String?,

        val unreadCount: Int?,

        val level: Int?,

        val deeplink: String?,

        val ctaText: String?,
    )

    @Keep
    data class StudyGroup(

        val title: String,

        val image: String,

        val isMute: Boolean,

        val deeplink: String
    )

    @Keep
    data class DoubtP2p(

        val title: String,

        val image: String,

        val deeplink: String?
    )

    @Keep
    data class KheloAurJeeto(

        val title: String,

        val image: String,

        val deeplink: String?
    )

    @Keep
    data class DoubtFeed2(

        val title: String,

        val image: String,

        val deeplink: String?,
    )

    @Keep
    data class StudentLanguage(

        val code: String,

        val display: String,

        val name: String
    )

    @Keep
    data class StudentClass(

        val code: Int,

        val name: String,

        val display: String
    )

    @Keep
    data class SelectedExamBoards(
        val ccmId: Int,

        val course: String,

        val category: String

    )

    @Keep
    data class RevisionCorner(

        val title: String,

        val image: String,

        val deeplink: String?,
    )

    @Keep
    data class Dictionary(

        val title: String,

        val image: String,

        val deeplink: String?,
    )

    @Keep
    data class Dnr(

        val title: String,

        val title1: String,

        val image: String,

        val description: String,

        val ctaText: String,

        val deeplink: String,

        val srpSfEngagementTime: Long,

        val nonSrpSfEngagementTime: Long,

        val lfEngagementTime: Long,
    )
}
