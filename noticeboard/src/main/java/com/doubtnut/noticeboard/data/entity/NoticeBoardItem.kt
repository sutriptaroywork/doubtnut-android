package com.doubtnut.noticeboard.data.entity

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

//"id": "3",
//"type": "image_title",
//"name": "What’s New",
//"title": "IIT JEE exam will be conducted on 20th may 2021, Please register yourself as soon as possible, this is dummy text to show how it will look",
//"subtitle": "IIT JEE 2020 Exam Date Announcement ",
//"image_link": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/6BA03313-8F5E-315C-6E07-F1C2FFA661DE.webp",
//"cta_text": "Download  Now",
//"caption": "2hr 22m",
//"share_text": "???",
//"deeplink": "???",
@Parcelize
@Keep
data class NoticeBoardItem(
    @SerializedName("id")
    val id: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("subtitle")
    val subtitle: String?,
    @SerializedName("image_link")
    val imageLink: String?,
    @SerializedName("caption")
    val caption: String?,
    @SerializedName("cta_text")
    val ctaText: String?,
    @SerializedName("share_text")
    val shareText: String?,
    @SerializedName("deeplink")
    val deepLink: String?,
    @SerializedName("is_today")
    val isToday: Boolean?
) : Parcelable