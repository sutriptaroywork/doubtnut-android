package com.doubtnutapp.gamification.settings.profilesetting.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.gamification.settings.SettingsConstansts
import com.doubtnutapp.screennavigator.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by shrreya on 1/7/19.
 */
@Singleton
class ProfileSettingScreenMapper @Inject constructor() : Mapper<String, Screen> {

    override fun map(option: String): Screen = when (option) {
        SettingsConstansts.LOGIN -> LoginScreen
        SettingsConstansts.TERMS_AND_CONDITIOND -> TermsAndConditionsScreen
        SettingsConstansts.PRIVACY_POLICY -> PrivacyPolicyScreen
        SettingsConstansts.ABOUT_US -> AboutUsScreen
        SettingsConstansts.RATE_US -> RateUsScreen
        SettingsConstansts.CONTACT_US -> ContactUsScreen
        SettingsConstansts.HOW_TO_USE_DOUBTNUT -> HowToUseDoubtNutScreen

        else -> NoScreen

    }
}