package com.doubtnutapp.data.gamification.settings.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.settings.model.AboutUs
import com.doubtnutapp.domain.gamification.settings.entity.SettingDetailEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AboutUsMapper @Inject constructor() : Mapper<AboutUs, SettingDetailEntity> {
    override fun map(srcObject: AboutUs) = with(srcObject) {
        SettingDetailEntity(
            aboutus
        )
    }
}
