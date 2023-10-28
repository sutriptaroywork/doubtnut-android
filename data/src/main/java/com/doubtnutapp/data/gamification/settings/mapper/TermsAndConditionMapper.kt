package com.doubtnutapp.data.gamification.settings.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.settings.model.TermsAndCondition
import com.doubtnutapp.domain.gamification.settings.entity.SettingDetailEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TermsAndConditionMapper @Inject constructor() :
    Mapper<TermsAndCondition, SettingDetailEntity> {
    override fun map(srcObject: TermsAndCondition) = with(srcObject) {
        SettingDetailEntity(
            termsandconditions
        )
    }
}
