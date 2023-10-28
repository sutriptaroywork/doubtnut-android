package com.doubtnutapp.domain.login.entity

import androidx.annotation.Keep

/**
 * Created by Anand Gaurav on 2019-08-13.
 */
@Keep
data class IntroEntity(
    val type: String,
    val video: String,
    val questionId: String
)
