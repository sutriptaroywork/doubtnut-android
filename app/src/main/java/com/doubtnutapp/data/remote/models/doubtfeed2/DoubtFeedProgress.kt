package com.doubtnutapp.data.remote.models.doubtfeed2

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 10/5/21.
 */

@Keep
data class DoubtFeedProgress(
    @SerializedName("completed_tasks_count") val completedTasksCount: Int,
    @SerializedName("total_tasks") val totalTasks: Int,
    @SerializedName("progress_text") val progressText: String?,
    @SerializedName("type") val type: String,
    @SerializedName("heading_text") val headingText: String,
    @SerializedName("heading_image") val headingImage: String?,
    @SerializedName("description") val description: String,
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("button_deeplink") val buttonDeeplink: String?,
    @SerializedName("student_image") val studentImage: String?,
    @SerializedName("is_completed") val isCompleted: Boolean,
) {
    companion object {
        const val TYPE_YESTERDAYS_TOP = "yesterdays_top"
        const val TYPE_TODAYS_TOP_PROGRESS = "todays_top_progress"
        const val TYPE_TODAYS_TOP_COMPLETED = "todays_top_completed"
    }
}
