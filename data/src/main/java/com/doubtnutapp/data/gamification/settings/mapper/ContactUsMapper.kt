package com.doubtnutapp.data.gamification.settings.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.settings.model.ContactUs
import com.doubtnutapp.domain.gamification.settings.entity.SettingDetailEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactUsMapper @Inject constructor() : Mapper<ContactUs, SettingDetailEntity> {
    override fun map(srcObject: ContactUs) = with(srcObject) {
        SettingDetailEntity(
            contactus
        )
    }
}
