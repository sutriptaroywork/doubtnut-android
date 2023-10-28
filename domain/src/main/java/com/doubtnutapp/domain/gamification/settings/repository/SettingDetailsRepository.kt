package com.doubtnutapp.domain.gamification.settings.repository

import com.doubtnutapp.domain.gamification.settings.entity.SettingDetailEntity
import io.reactivex.Single

/**
 * Created by shrreya on 3/7/19.
 */
interface SettingDetailsRepository {

    fun getTncDetails(): Single<SettingDetailEntity>
    fun getContactUsDetails(): Single<SettingDetailEntity>
    fun getAboutUsDetails(): Single<SettingDetailEntity>
    fun getPolicyDetails(): Single<SettingDetailEntity>
}
