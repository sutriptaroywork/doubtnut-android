package com.doubtnutapp.data.newlibrary.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2020-01-10.
 */
@Keep
data class ApiLibraryNextVideo(
    @SerializedName("playlistData") val playlistData: ApiPlaylist?,
    @SerializedName("videoData") val videoData: ApiVideoData?
)

@Keep
data class ApiPlaylist(
    @SerializedName("title") val title: String?,
    @SerializedName("playlist_id") val playlistId: String?,
    @SerializedName("is_last") val isLast: String?
)

@Keep
data class ApiVideoData(
    @SerializedName("question_id") val questionId: String?,
    @SerializedName("playlist_id") val playlistId: String?,
    @SerializedName("class") val studentClass: String?,
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("chapter_order") val chapterOrder: String?,
    @SerializedName("ncert_exercise_name") val ncertExerciseName: String?,
    @SerializedName("ocr_text") val ocrText: String?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("parent_id") val parentId: String?,
    @SerializedName("question_tag") val questionTag: String?,
    @SerializedName("thumbnail_img_url") val thumbnailImgUrl: String?,
    @SerializedName("thumbnail_img_url_hindi") val thumbnailImgUrlHindi: String?,
    @SerializedName("doubt") val doubt: String?,
    @SerializedName("resource_type") val resourceType: String?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("bg_color") val bgColor: String?,
    @SerializedName("share") val share: String?,
    @SerializedName("like") val like: String?,
    @SerializedName("views") val views: String?,
    @SerializedName("share_message") val shareMessage: String?,
    @SerializedName("isLiked") val isLiked: Boolean?
)
