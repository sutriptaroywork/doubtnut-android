package com.doubtnutapp.gamification.otheruserprofile.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.gamification.userProfile.entity.DailyAttendanceEntity
import com.doubtnutapp.domain.gamification.userProfile.entity.DailyLeaderboardItemEntity
import com.doubtnutapp.domain.gamification.userProfile.entity.MyBadgesItemEntity
import com.doubtnutapp.domain.gamification.userProfile.model.UserProfileEntity
import com.doubtnutapp.gamification.otheruserprofile.model.OtherUserStatsDataModel
import com.doubtnutapp.gamification.otheruserprofile.model.OthersUserProfileDataModel
import com.doubtnutapp.gamification.userProfileData.mapper.DailyStreakProgressMapper
import com.doubtnutapp.gamification.userProfileData.mapper.LeaderBoardMapper
import com.doubtnutapp.gamification.userProfileData.mapper.UserRecentBadgesMapper
import com.doubtnutapp.gamification.userProfileData.model.DailyAttendanceDataModel
import com.doubtnutapp.gamification.userProfileData.model.DailyLeaderboardItemDataModel
import com.doubtnutapp.gamification.userProfileData.model.MyBadgesItemDataModel
import javax.inject.Inject

class OthersUserProfileMapper @Inject constructor(

        private val leaderBoardMapper: LeaderBoardMapper,
        private val dailyStreakProgressMapper: DailyStreakProgressMapper,
        private val userRecentBadgesMapper: UserRecentBadgesMapper
) : Mapper<UserProfileEntity, OthersUserProfileDataModel> {
    override fun map(srcObject: UserProfileEntity) = with(srcObject) {
        OthersUserProfileDataModel(
                profileImage,
                getLeaderBoard(leaderBoard),
                getRecentBadges(userRecentBadges),
                getDailySteakProgress(dailyStreakProgress),
                userLevel,
                userLifetimePoints.toString(),
                userTodaysPoint.toString(),
                userName.orEmpty(),
                otherUserStats = otherStats.map {
                    OtherUserStatsDataModel(
                            id = it.id,
                            action = it.action,
                            action_display = it.action_display,
                            xp = it.xp,
                            is_active = it.is_active,
                            created_at = it.created_at,
                            actionPage = it.actionPage,
                            count = it.count,
                            activity = it.activity
                    )
                },
                bannerImg = bannerImage,
                studentClass = studentClass
        )
    }


    private fun getDailySteakProgress(dailyStreakProgress: List<DailyAttendanceEntity>?): List<DailyAttendanceDataModel>? = dailyStreakProgress?.map {

        dailyStreakProgressMapper.map(it)
    }

    private fun getRecentBadges(userRecentBadges: List<MyBadgesItemEntity>?): List<MyBadgesItemDataModel>? = userRecentBadges?.map {

        userRecentBadgesMapper.map(it)
    }

    private fun getLeaderBoard(leaderBoard: List<DailyLeaderboardItemEntity>?): List<DailyLeaderboardItemDataModel>? = leaderBoard?.map {
        leaderBoardMapper.map(it)
    }
}