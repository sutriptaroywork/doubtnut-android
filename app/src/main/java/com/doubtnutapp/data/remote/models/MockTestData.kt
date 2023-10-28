package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MockTestData(
    @SerializedName("course") val course: String,
    @SerializedName("tests") val mockTestList: ArrayList<TestDetails>
) : Parcelable
