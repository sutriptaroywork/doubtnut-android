package com.doubtnutapp.data.profile.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.profile.model.ApiUserProfile
import com.doubtnutapp.domain.profile.UserProfileEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileMapper @Inject constructor() : Mapper<ApiUserProfile, UserProfileEntity> {

    override fun map(srcObject: ApiUserProfile) = with(srcObject) {
        UserProfileEntity(
            studentId,
            studentEmail,
            studentLocale,
            studentImageUrl,
            schoolName,
            studentClass,
            studentTotalVideoViewDuration,
            studentUserName,
            StudentPinCode,
            studentCoaching,
            studentDOB,
            studentCourse,
            studentClassDisplay,
            studentLanguage,
            studentLanguageDisplay
        )
    }
}
