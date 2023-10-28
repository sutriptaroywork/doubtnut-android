package com.doubtnutapp.data.gamification.userProfile.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-11-17.
 */
@Keep
data class ApiMyBio(
    @SerializedName("title")
    val title: String?,

    @SerializedName("img_url")
    val imageUrl: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("is_achieved")
    val isAchieved: Int?,

    @SerializedName("blur_image")
    val blurImage: String?,

    @SerializedName("action_button")
    val buttonText: String?
)
