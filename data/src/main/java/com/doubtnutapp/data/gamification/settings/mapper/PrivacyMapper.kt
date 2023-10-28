package com.doubtnutapp.data.gamification.settings.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.settings.model.PrivacyData
import com.doubtnutapp.domain.gamification.settings.entity.SettingDetailEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrivacyMapper @Inject constructor() : Mapper<PrivacyData, SettingDetailEntity> {
    override fun map(srcObject: PrivacyData) = with(srcObject) {
        SettingDetailEntity(
            privacy
        )
    }
}
