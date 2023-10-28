package com.doubtnutapp.gamification.gamepoints.mapper

import com.doubtnutapp.base.*
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.screennavigator.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionTypeToScreenMapper @Inject constructor() : Mapper<Any, Screen> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun map(action: Any) = when (action) {
        is OpenPage -> {
            when (action.actionType) {
                "openForum" -> ForumScreen
                "OpenLibrary" -> LibraryScreen
                "OpenCamera" -> CameraScreen
                "OpenQuiz" -> QuizScreen
                "OpenEditProfile" -> UpdateProfileScreen
                "OpenPointsHistory" -> OpenPointsHistoryScreen
                "OpenMockTest" -> MockTestScreen
                "GamePointsScreen" -> GamePointsScreen

                else -> NoScreen
            }
        }
        is OpenOthersProfile -> OthersProfileScreen
        is OpenDailyStreakPage -> DailyStreakScreen
        is OpenLeaderBoardActivity -> OpenLeaderBoardScreen
        is OpenBadgesActivity -> BadgesScreen

        else -> NoScreen
    }


}
