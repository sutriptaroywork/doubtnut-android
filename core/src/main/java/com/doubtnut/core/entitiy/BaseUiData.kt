package com.doubtnut.core.entitiy

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BaseUiData(
    @SerializedName("id")
    val id: String?,
    @SerializedName("icon", alternate = ["icon_url"])
    val icon: String?,
    @SerializedName("image", alternate = ["image_url"])
    val imageUrl: String?,
    @SerializedName("background_color", alternate = ["bg_color"])
    val backgroundColor: String?,

    @SerializedName("title_one", alternate = ["title1", "title"])
    val titleOne: String?,
    @SerializedName("title_one_text_size", alternate = ["title1_text_size", "title_text_size"])
    val titleOneTextSize: String?,
    @SerializedName("title_one_text_color", alternate = ["title1_text_color", "title_text_color"])
    val titleOneTextColor: String?,

    @SerializedName("deeplink", alternate = ["deep_link"])
    val deeplink: String?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>?
)
