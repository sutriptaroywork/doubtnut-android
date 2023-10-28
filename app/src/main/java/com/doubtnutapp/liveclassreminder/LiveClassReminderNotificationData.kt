package com.doubtnutapp.liveclassreminder

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 26/08/20.
 */

@Keep
data class LiveClassReminderNotificationData(
        @SerializedName("title_one") val titleOne: String?,
        @SerializedName("title_two") val titleTwo: String?,
        @SerializedName("title_three") val titleThree: String?,
        @SerializedName("live_at") val liveAt: String?,
        @SerializedName("color_code") val colorCode: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("image_url_bg") val imageUrlBackground: String?,
        @SerializedName("button_text") val buttonText: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("subject") val subject: String?
)