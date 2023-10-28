package com.doubtnutapp.data.gamification.settings.repository

import com.doubtnutapp.data.gamification.settings.mapper.*
import com.doubtnutapp.domain.gamification.settings.entity.SettingDetailEntity
import com.doubtnutapp.domain.gamification.settings.repository.SettingDetailsRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by shrreya on 3/7/19.
 */
class SettingDetailRepositoryImpl @Inject constructor(
    private val settingDetailService: SettingDetailService,
    private val aboubtUsMapper: AboutUsMapper,
    private val contactUsMapper: ContactUsMapper,
    private val termsAndConditionMapper: TermsAndConditionMapper,
    private val privacyMapper: PrivacyMapper
) : SettingDetailsRepository {

    override fun getTncDetails(): Single<SettingDetailEntity> {
        return settingDetailService.getTermsAndConditions().map {
            termsAndConditionMapper.map(it.data)
        }
    }

    override fun getAboutUsDetails(): Single<SettingDetailEntity> {
        return settingDetailService.getAboutUsData().map {
            aboubtUsMapper.map(it.data)
        }
    }

    override fun getContactUsDetails(): Single<SettingDetailEntity> {
        return settingDetailService.getContactUsData().map {
            contactUsMapper.map(it.data)
        }
    }

    override fun getPolicyDetails(): Single<SettingDetailEntity> {
        return settingDetailService.getPrivacyPolicy().map {
            privacyMapper.map(it.data)
        }
    }
}
