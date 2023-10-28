package com.doubtnutapp.data.gamification.userProfile.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.gamificationbadges.userProfile.mapper.DailyLeaderBoardEntityMapper
import com.doubtnutapp.data.gamification.userProfile.PointsFormat
import com.doubtnutapp.data.gamification.userProfile.model.ApiDailyAttendanceItem
import com.doubtnutapp.data.gamification.userProfile.model.ApiDailyLeaderboardItem
import com.doubtnutapp.data.gamification.userProfile.model.ApiGamificationUserProfile
import com.doubtnutapp.data.gamification.userProfile.model.ApiMyBadgesItem
import com.doubtnutapp.domain.gamification.userProfile.entity.*
import com.doubtnutapp.domain.gamification.userProfile.model.UserProfileEntity
import javax.inject.Inject

class UserProfileEntityMapper @Inject constructor(

    private val leaderBoardEntityMapper: DailyLeaderBoardEntityMapper,
    private val dailyStreakProgressEntityMapper: DailyAttendanceEntityMapper,
    private val userRecentBadgesEntityMapper: MyBadgesEntityMapper
) : Mapper<ApiGamificationUserProfile, UserProfileEntity> {
    override fun map(srcObject: ApiGamificationUserProfile) = with(srcObject) {
        UserProfileEntity(
            profileImage,
            getLeaderBoard(leaderBoard),
            getRecentBadges(userRecentBadges),
            getDailySteakProgress(dailyStreakProgress),
            userLevel,
            PointsFormat.Format(userLifetimePoints),
            PointsFormat.Format(userTodaysPoint),
            userName,
            pointsToEarned,
            userEmail,
            userSchoolName,
            userPincode,
            userCoaching,
            userDob,
            coins,
            otherStats = othersStats?.map {
                UserOtherStats(
                    id = it.id,
                    action = it.action.orEmpty(),
                    action_display = it.actionDisplay.orEmpty(),
                    xp = it.xp,
                    is_active = it.isActive,
                    created_at = it.createdAt.orEmpty(),
                    actionPage = it.actionPage.orEmpty(),
                    count = it.count ?: 0,
                    activity = it.activity.orEmpty()
                )
            } ?: emptyList(),
            bannerImage = bannerImg,
            studentClass = studentClass,
            myBioEntity = completeness?.let {
                MyBioEntity(
                    title = it.title, imageUrl = it.imageUrl, description = it.description, isAchieved = it.isAchieved,
                    blurImage = it.blurImage, buttonText = it.buttonText
                )
            },
            subscriptionStatus = subscriptionStatus, subscriptionImageUrl = subscriptionImageUrl,
            mobileNumber = mobileNumber, countryCode = countryCode,
            board = board, exams = exams,
        )
    }

    private fun getDailySteakProgress(dailyStreakProgress: List<ApiDailyAttendanceItem>?): List<DailyAttendanceEntity>? = dailyStreakProgress?.map {

        dailyStreakProgressEntityMapper.map(it)
    }

    private fun getRecentBadges(userRecentBadges: List<ApiMyBadgesItem>?): List<MyBadgesItemEntity>? = userRecentBadges?.map {

        userRecentBadgesEntityMapper.map(it)
    }

    private fun getLeaderBoard(leaderBoard: List<ApiDailyLeaderboardItem>?): List<DailyLeaderboardItemEntity>? = leaderBoard?.map {
        leaderBoardEntityMapper.map(it)
    }
}
