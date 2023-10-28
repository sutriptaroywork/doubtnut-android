package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by akshaynandwana on
 * 17, December, 2018
 **/
@Parcelize
data class MockTestSummary(
    @SerializedName("questionbank_id") val questionBankId: String,
    @SerializedName("action_type") val mockTestActionType: String,
    @SerializedName("option_codes") val mockTestOptionCode: String?,
    var isReviewed: Boolean = false,
    var questionNumber: Int = 0
) : Parcelable

@Parcelize
data class MockTestReviewData(
    @SerializedName("button_one_text") val buttonOneText: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("button_two_text") val buttonTwoText: String?,
    @SerializedName("review_color") val reviewColor: String?,
    @SerializedName("count") val reviewCount: String?
) : Parcelable

data class MockTestConfigData(
    @SerializedName("skipped_color") val skippedColor: String?,
    @SerializedName("attempted_color") val attemptedColor: String?,
    @SerializedName("review_color") val reviewColor: String?,
    @SerializedName("correct_color") val correctColor: String?,
    @SerializedName("incorrect_color") val incorrectColor: String?
)

data class MockTestSummaryData(
    @SerializedName("questions") val summaryList: ArrayList<MockTestSummary>?,
    @SerializedName("config_data") val configData: MockTestConfigData?,
    @SerializedName("review_data") val reviewData: MockTestReviewData?
)
