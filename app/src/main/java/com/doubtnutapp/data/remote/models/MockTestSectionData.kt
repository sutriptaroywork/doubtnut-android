package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MockTestSectionData(
    @SerializedName("test_id") val sectionTestId: Int,
    @SerializedName("section_code") val sectionCode: String,
    @SerializedName("title") val sectionTitle: String,
    @SerializedName("description") val sectionDescription: String?,
    @SerializedName("type") val sectionType: String?,
    // marking the key as nullable as it is no used and has caused crashes previously
    //  https://console.firebase.google.com/project/doubtnut-e000a/crashlytics/app/android:com.doubtnutapp/issues/ea768f872167f5ff26e4ceef469913e6?time=last-seven-days&sessionEventKey=60F51A520315000158269F67293D3F3F_1564964947312010550
    @SerializedName("duration_in_min") val sectionDurationInMinute: String?,
    @SerializedName("validity") val validity: Int?,
    @SerializedName("is_active") val sectionIsActive: Int,
    @SerializedName("created_on") val sectionCreatedOn: String?,
    @SerializedName("subject_code") val sectionSubjectCode: String?,
    @SerializedName("chapter_code") val sectionChapterCode: String?,
    @SerializedName("mc_code") val sectionMcCode: String?,
    @SerializedName("order_pref") val sectionOrderPref: String?,
    @SerializedName("startingIndex") val sectionStartingIndex: Int,
    @SerializedName("endingIndex") val sectionEndingIndex: Int,
    @SerializedName("marks_scored") val markedScoredInSection: String?,
    @SerializedName("correct") val questionCorrect: String?,
    @SerializedName("skipped") val questionSkipped: String?,
    @SerializedName("incorrect") val questionIncorrect: String?,
    @SerializedName("attempt_limit") val attemptLimit: Int?,
    var isSelected: Boolean = false,
) : Parcelable
