package com.doubtnutapp.gamification.mybio.model

import androidx.annotation.Keep

@Keep
data class UserBioLocationDataModel (
        var location: String,
        var lat: String,
        var lon: String,
        var state: String = "",
        var country: String = ""
)