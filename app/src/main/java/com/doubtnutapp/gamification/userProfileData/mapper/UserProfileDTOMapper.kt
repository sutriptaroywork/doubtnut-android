package com.doubtnutapp.gamification.userProfileData.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.gamification.userProfile.entity.DailyAttendanceEntity
import com.doubtnutapp.domain.gamification.userProfile.entity.DailyLeaderboardItemEntity
import com.doubtnutapp.domain.gamification.userProfile.entity.MyBadgesItemEntity
import com.doubtnutapp.domain.gamification.userProfile.entity.MyBioEntity
import com.doubtnutapp.domain.gamification.userProfile.model.UserProfileEntity
import com.doubtnutapp.gamification.userProfileData.model.*
import javax.inject.Inject

class UserProfileDTOMapper @Inject constructor(
        private val userRecentBadgesMapper: UserRecentBadgesMapper,
        private val dailyStreakProgressMapper: DailyStreakProgressMapper,
        private val leaderBoardMapper: LeaderBoardMapper
) : Mapper<UserProfileEntity, UserProfileDTO> {
    override fun map(srcObject: UserProfileEntity) = with(srcObject) {
        UserProfileDTO(
                getUserInfoPoints(profileImage, userName.orEmpty(), isLoggedIn, pointsToEarned),
                getProfileDataList(leaderBoard, dailyStreakProgress, userRecentBadges, userTodaysPoint, userLifetimePoints, coins, userLevel, myBioEntity)
        )
    }

    private fun getUserInfoPoints(profileImage: String?, userName: String, isLoggedIn: Boolean, pointsToEarned: String): UserProfileInfoDataModel = UserProfileInfoDataModel(profileImage, userName, isLoggedIn, pointsToEarned)


    private fun getRecentBadges(userRecentBadges: List<MyBadgesItemEntity>?): List<MyBadgesItemDataModel>? = userRecentBadges?.map {

        userRecentBadgesMapper.map(it)
    }

    private fun getProfileDataList(leaderboardItemEntity: List<DailyLeaderboardItemEntity>?,
                                   dailyStreakProgress: List<DailyAttendanceEntity>?, userBadegs: List<MyBadgesItemEntity>?, userTodaysPoint: String?,
                                   userLifeTimePoints: String, coins: Int, userLevel: String,
                                   myBioEntity: MyBioEntity?): UserProfileDataModel = UserProfileDataModel(getLeaderBoard(leaderboardItemEntity),
            getDailySteakProgress(dailyStreakProgress), getRecentBadges(userBadegs), userTodaysPoint, userLifeTimePoints, coins, userLevel,
            myBioEntity?.let {
                MyBio(title = it.title.orEmpty(), imageUrl = it.imageUrl.orEmpty(), description = it.description.orEmpty(), isAchieved = it.isAchieved
                        ?: 0, blurImage = it.blurImage.orEmpty(), buttonText = it.buttonText.orEmpty())
            })

    private fun getDailySteakProgress(dailyStreakProgress: List<DailyAttendanceEntity>?): List<DailyAttendanceDataModel>? = dailyStreakProgress?.map {
        dailyStreakProgressMapper.map(it)
    }


    private fun getLeaderBoard(leaderBoard: List<DailyLeaderboardItemEntity>?): List<DailyLeaderboardItemDataModel>? = leaderBoard?.map {
        leaderBoardMapper.map(it)
    }

}