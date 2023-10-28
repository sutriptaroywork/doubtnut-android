package com.doubtnutapp.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 08/04/20.
 */
@Keep
data class BottomListData(
        @SerializedName("id") val id: String?,
        @SerializedName("name") val name: String?,
        @SerializedName("description") val description: String?,
        @SerializedName("is_last") val isLast: String?,
        @SerializedName("parent") val parent: String?,
        @SerializedName("resource_type") val resourceType: String?,
        @SerializedName("student_class") val studentClass: String?,
        @SerializedName("subject") val subject: String?,
        @SerializedName("main_description") val mainDescription: String?
)