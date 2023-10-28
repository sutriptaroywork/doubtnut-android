package com.doubtnutapp.gamification.settings.profilesetting.ui

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes

/**
 * Created by shrreya on 29/6/19.
 */
@Keep
data class ProfileSettingsItems(val settingOptionType: String,
                                @DrawableRes val image: Int,
                                @StringRes val name: Int)