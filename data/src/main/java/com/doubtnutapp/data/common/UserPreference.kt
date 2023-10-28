package com.doubtnutapp.data.common

import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.domain.login.entity.IntroEntity
import com.doubtnutapp.domain.newlibrary.entities.LibraryHistoryEntity

interface UserPreference {

    fun isTrickyQuestionButtonShown(): Boolean

    fun isUserSeenBadgeScreenOnce(): Boolean

    fun getUserStudentId(): String

    fun setIsCameraScreenShownToTrue()

    fun setIsTrickyQuestionButtonToTrue()

    fun getUserClass(): String

    fun getStudentName(): String

    fun updateStudentName(name: String)

    fun getSelectedLanguage(): String

    fun updateLanguage(languageCode: String, title: String)

    fun getSelectedDisplayLanguage(): String

    fun setUserBadgeScreenOnceToTrue()

    fun updateClass(className: String, classDisplay: String)

    fun getUserCourse(): String

    fun getUserProfileData(): HashMap<String, String>

    fun getUserProfileDataAndOldStudentId(): Pair<HashMap<String, String>, String>

    fun updateUserProfileData(
        userName: String,
        email: String,
        school: String,
        pincode: String,
        coaching: String,
        dob: String,
        imageUrl: String?
    )

    fun getUserLoggedIn(): Boolean

    fun logOutUser()

    fun getDailyStreakRegisterEvent(): DailyStreakDate

    fun setDailyStreakRegisterEvent(dailyStreakDate: DailyStreakDate)

    fun isBottomNavigationButtonClicked(key: String): Boolean

    fun setBottomNavigationButtonClicked(key: String)

    fun getGcmRegistrationId(): String

    fun getCameraScreenVisitCount(): Long

    fun putUserData(
        studentId: String,
        onBoardingVideoId: String,
        introList: List<IntroEntity>,
        studentUserName: String
    )

    fun getIsOnBoardingCompleted(): Boolean

    fun putOnBoardingCompleted()

    fun putFcmTokenUpdatedOnServerStatus(status: Boolean)

    fun onCleverTapSetUp(studentId: String)

    fun putLibraryHistory(libraryHistoryEntity: LibraryHistoryEntity)

    fun getLibraryHistory(): LibraryHistoryEntity?

    fun getLastShownInAppPopUp(): Long

    fun setLastShownInAppPopUp(timeInMillis: Long)

    fun updateOnBoardingData(apiOnBoardingStatus: ApiOnBoardingStatus)

    fun updateClassAndLanguage(apiOnBoardingStatus: ApiOnBoardingStatus)

    fun updateStudyDostData(apiStudyDost: ApiOnBoardingStatus.ApiStudyDost?)

    fun updateStudyGroupData(apiStudyGroup: ApiOnBoardingStatus.ApiStudyGroup?)

    fun getStudyGroupData(): ApiOnBoardingStatus.ApiStudyGroup?

    // Start Doubt Pe Charcha V2
    fun updateDoubtP2pData(apiDoubtP2p: ApiOnBoardingStatus.ApiDoubtP2p?)

    fun getDoubtP2pData(): ApiOnBoardingStatus.ApiDoubtP2p?

    fun setDoubtP2pNotificationEnabled(enable: Boolean)

    fun isDoubtP2pNotificationEnabled(): Boolean
    // End Doubt Pe Charcha V2

    // Start - Khelo Aur Jeeto V2
    fun updateKheloAurJeetoData(apiKheloAurJeeto: ApiOnBoardingStatus.ApiKheloAurJeeto?)

    fun getKheloAurJeetoData(): ApiOnBoardingStatus.ApiKheloAurJeeto?
    // End - Khelo Aur Jeeto V2

    // region Khelo Aur Jeeto V2
    fun updateDoubtFeed2Data(apiDoubtFeed2: ApiOnBoardingStatus.ApiDoubtFeed2?)

    fun getDoubtFeed2Data(): ApiOnBoardingStatus.ApiDoubtFeed2?
    //endregion

    // Start Study group notification
    fun setStudyGroupNotificationMuteStatus(isMute: Boolean)

    fun isStudyGroupNotificationMuted(): Boolean
    // End Study group notification

    // Start Revision Corner
    fun updateRevisionCornerData(apiRevisionCorner: ApiOnBoardingStatus.ApiRevisionCorner?)

    fun getRevisionCornerData(): ApiOnBoardingStatus.ApiRevisionCorner?
    // End Revision Corner

    // Start Dnr
    fun updateDnrData(apiDnr: ApiOnBoardingStatus.ApiDnr?)

    fun getDnrData(): ApiOnBoardingStatus.ApiDnr
    // End Dnr8

    fun getUserHasWatchedVideo(): Boolean

    fun putUserHasWatchedVideo(state: Boolean)

    fun getVideoWatchedCount(): Int

    fun putVideoWatchedCount(count: Int)

    fun getVideoWatchedOnDayZero(): Boolean

    fun putVideoWatchedOnDayZero(state: Boolean)

    fun getVideoWatchedAfterDayTwo(): Boolean

    fun putVideoWatchedAfterDayTwo(state: Boolean)

    fun getThreeVideosWatchedInTwoDays(): Boolean

    fun putThreeVideosWatchedInTwoDays(state: Boolean)

    fun getUserSelectedExams(): String

    fun putUserSelectedExams(exams: String)

    fun getUserSelectedBoard(): String

    fun putUserSelectedBoard(board: String)

    fun putCcmId(ccmId: String)

    fun getCcmId()

    fun getUserHasWatchedEtoosVideo(): Boolean

    fun putUserHasWatchedEtoosVideo(state: Boolean)

    fun isEmulator(): Boolean

    fun hasPlayService(): Boolean

    fun getLastDayBranchEventSend(): Int

    fun putLastDayBranchEventSend(day: Int)

    fun getLastPaymentInitiateTime(): Long

    fun putLastPaymentInitiateTime(time: Long)

    fun getAccessToken(): String

    fun setAppExitDialogShownInCurrentSession(shown: Boolean)

    fun getAppExitDialogShownInCurrentSession(): Boolean

    fun setCameraScreenNavigationDataFetchedInCurrentSession(isFetched: Boolean)

    fun getCameraScreenNavigationDataFetchedInCurrentSession(): Boolean

    fun getUserImageUrl(): String

    fun getRewardSystemCurrentLevel(): Int

    fun getRewardSystemCurrentDay(): Int

    fun putRewardSystemCurrentLevel(currentLevel: Int)

    fun putRewardSystemCurrentDay(currentDay: Int)

    fun putShowRewardSystemScratchCardReminder(scratchCardStatus: Boolean)

    fun getShowRewardSystemScratchCardReminder(): Boolean

    fun setDoubtFeedAvailable(isAvailable: Boolean)

    fun isDoubtFeedAvailable(): Boolean

    fun setDoubtP2PHomeWidgetVisibility(visibility: Boolean)

    fun isDoubtP2PHomeWidgetVisibility(): Boolean

    fun getQuestionAskCount(): Long

    fun getGaid(): String

    fun setJourneyCountDataAsString(journeyCountMap: Map<String, Int>?)

    fun getJourneyCountDataAsString(): String

    fun getUserJourneyCountForKey(key: String) : Int

    fun setUserJourneyCountForKey(key: String, value: Int)
}
