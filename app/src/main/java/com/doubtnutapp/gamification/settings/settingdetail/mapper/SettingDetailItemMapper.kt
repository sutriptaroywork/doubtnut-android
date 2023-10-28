package com.doubtnutapp.gamification.settings.settingdetail.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.gamification.settings.entity.SettingDetailEntity
import com.doubtnutapp.gamification.settings.settingdetail.model.SettingDetails
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by shrreya on 3/7/19.
 */
@Singleton
class SettingDetailItemMapper @Inject constructor() : Mapper<SettingDetailEntity, SettingDetails> {

    override fun map(srcObject: SettingDetailEntity) = with(srcObject) {
        SettingDetails(
            dataValue
        )

    }
}