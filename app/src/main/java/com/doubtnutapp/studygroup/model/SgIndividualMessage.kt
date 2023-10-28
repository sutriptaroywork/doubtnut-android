package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class PersonalChatMessage(
    @SerializedName("rows") val messageList: List<WidgetEntityModel<*, *>?>?,
    @SerializedName("offsetCursor") val offsetCursor: String?,
    @SerializedName("page") val page: Int,
)

@Keep
data class PersonalChatWrappedMessage(
        @SerializedName("rows") val messageList: List<PersonalChatWrapper>?,
        @SerializedName("offsetCursor") val offsetCursor: String?,
        @SerializedName("page") val page: Int,
)

@Keep
data class PersonalChatWrapper(
    @SerializedName("message") val message: WidgetEntityModel<*, *>?,
    @SerializedName("room_id") val roomId: String?,
    @SerializedName("room_type") val roomType: String?,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("_id") val id: String? = null,
    @SerializedName("millis") val millis: Long? = null,
)
