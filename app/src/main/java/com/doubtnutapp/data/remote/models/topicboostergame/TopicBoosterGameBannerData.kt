package com.doubtnutapp.data.remote.models.topicboostergame

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by devansh on 26/2/21.
 */

@Keep
data class TopicBoosterGameBannerData(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("test_uuid") val testUuId: String?,
    @SerializedName("chapter_alias") val chapterAlias: String?,
    @SerializedName("counter") val onlinePlayersCountK: Float?,
    @SerializedName("opponent_image") val opponentImage: String?,
    @SerializedName("opponent_name") val opponentName: String?,
    @SerializedName("background_color_code") val opponentImageBackgroundColor: String?,
    @SerializedName("is_available") val isAvailable: Boolean,
    @SerializedName("additional_details") val additionalDetails: AdditionalDetails,
)

@Keep
@Parcelize
data class AdditionalDetails(
    @SerializedName("variant_id") val variantId: Int,
    @SerializedName("total_questions_quiz") val totalQuestions: Int,
    @SerializedName("expiry") val expiry: Int,
    @SerializedName("is_wallet_reward") val isWalletReward: Boolean,
) : Parcelable
