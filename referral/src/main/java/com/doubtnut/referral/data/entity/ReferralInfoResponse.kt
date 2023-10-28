package com.doubtnut.referral.data.entity

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
data class ReferralInfoResponse(
    @SerializedName("deeplink")
    val deeplink: String?,
    @SerializedName("finish_activity")
    val finishActivity: Boolean?,

    @SerializedName("widgets", alternate = ["data"])
    val widgets: List<WidgetEntityModel<*, *>>?,

    @SerializedName("title")
    val title: String?,
    @SerializedName("mobile")
    val mobile: String?,

    @SerializedName("button")
    val buttonData: ButtonData?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>?
)

@Keep
@Parcelize
data class ButtonData(
    @SerializedName("text")
    val text: String?,
    @SerializedName("icon")
    val icon: String?,
    @SerializedName("bg_color")
    val bgColor: String?,
    @SerializedName("share_message")
    val shareMessage: String?,
    @SerializedName("share_contact_batch_size")
    val shareContactBatchSize: Int?
) : Parcelable
