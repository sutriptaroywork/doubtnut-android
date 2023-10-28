package com.doubtnutapp.data.remote.models.topicboostergame2

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by devansh on 30/06/21.
 */

@Keep
data class TbgResult(
    @SerializedName("topic") val topic: String,
    @SerializedName("result_text") val resultText: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("total_score") val totalScore: Int,
    @SerializedName("score") val score: Int,
    @SerializedName("opponent_score") val opponentScore: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("opponent_name") val opponentName: String?,
    @SerializedName("total_correct") val totalCorrect: Int,
    @SerializedName("total_opponent_correct") val totalOpponentCorrect: Int,
    @SerializedName("total_questions") val totalQuestions: Int,
    @SerializedName("is_rank_available") val isRankAvailable: Boolean,
    @SerializedName("rank_text") val rankText: String?,
    @SerializedName("rank") val rank: String?,
    @SerializedName("rank_deeplink") val rankDeeplink: String?,
    @SerializedName("solutions_title") val solutionsTitle: String?,
    @SerializedName("solutions_playlist_id") val solutionsPlaylistId: Long,
    @SerializedName("primary_cta") val primaryCta: String?,
    @SerializedName("secondary_cta_1") val secondaryCta1: String?,
    @SerializedName("secondary_cta_2") val secondaryCta2: String?,
    @SerializedName("primary_cta_deeplink") val primaryCtaDeeplink: String?,
    @SerializedName("secondary_cta_1_deeplink") val secondaryCta1Deeplink: String?,
    @SerializedName("secondary_cta_2_deeplink") val secondaryCta2Deeplink: String?,
    @SerializedName("all_question_ids") val allQuestionIds: List<Long>,
    @SerializedName("correct_question_ids") val correctQuestionIds: List<Long>,
    @SerializedName("is_result_available") val isResultAvailable: Boolean,
    @SerializedName("unavailable_title") val unavailableTitle: String?,
    @SerializedName("unavailable_subtitle") val unavailableSubtitle: String?,
    @SerializedName("unavailable_cta") val unavailableCta: String?,
    @SerializedName("unavailable_deeplink") val unavailableDeeplink: String?,
    @SerializedName("is_level_up") val isLevelUp: Boolean,
    @SerializedName("level_up_container") val levelUpContainer: LevelUpContainer?,
)

@Keep
@Parcelize
data class LevelUpContainer(
    @SerializedName("unlocked_level") val unlockedLevel: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("cta") val cta: String?,
    @SerializedName("cta_deeplink") val ctaDeeplink: String?,
    @SerializedName("coupon_code") val couponCode: String?,
    @SerializedName("image") val image: String,
) : Parcelable
