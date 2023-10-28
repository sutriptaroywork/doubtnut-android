package com.doubtnutapp.db.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.JsonArray

/**
 * Created by Sachin Saxena on 2020-04-21.
 */

@Keep
@Entity(tableName = "match_question")
data class LocalMatchQuestion(

    @PrimaryKey
    @ColumnInfo(name = "question_id")
    val questionId: String,

    @ColumnInfo(name = "matched_count")
    val matchedCount: Int,

    @ColumnInfo(name = "question_image")
    val questionImage: String?,

    @ColumnInfo(name = "ocr_text")
    val ocrText: String,

    @ColumnInfo(name = "popup_deeplink")
    val popupDeeplink: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "notification_id")
    val notificationId: Int,

    @ColumnInfo(name = "is_blur")
    val isBlur: Boolean?,

    @ColumnInfo(name = "youtube_flag")
    val youtubeFlag: Int?,

    @ColumnInfo(name = "auto_play")
    val autoPlay: Boolean?,

    @ColumnInfo(name = "auto_play_duration")
    val autoPlayDuration: Long?,

    @ColumnInfo(name = "auto_play_initiation")
    val autoPlayInitiation: Long?,

    @ColumnInfo(name = "is_image_blur")
    val isImageBlur: Boolean?,

    @ColumnInfo(name = "is_image_handwritten")
    val isImageHandwritten: Boolean?,

    @Embedded
    val liveTabData: LocalLiveTabData,
)

@Keep
data class LocalLiveTabData(
    @ColumnInfo(name = "tab_text")
    val tabText: String,
)

@Keep
@Entity(tableName = "match_facet")
data class LocalMatchFacet(

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "chapterAlias")
    val chapterAlias: String,

    @ColumnInfo(name = "data")
    val data: JsonArray?,

    @ColumnInfo(name = "isSelected")
    val isSelected: Boolean,

    @ColumnInfo(name = "match_question_id")
    val matchQuestionId: String
)

@Keep
@Entity(tableName = "match_question_list")
data class LocalMatchQuestionList(

    @PrimaryKey(autoGenerate = true)
    var _id: Long = 0,

    @ColumnInfo(name = "id")
    var id: String,

    @ColumnInfo(name = "class")
    val clazz: String?,

    @ColumnInfo(name = "chapter")
    val chapter: String?,

    @ColumnInfo(name = "question_thumbnail")
    val questionThumbnail: String,

    @ColumnInfo(name = "question_thumbnail_new")
    val questionThumbnailNew: String?,

    @Embedded(prefix = "source_")
    val source: LocalMatchedQuestionSource?,

    @Embedded(prefix = "canvas_")
    val canvas: LocalCanvas,

    @ColumnInfo(name = "html")
    val html: String?,

    @ColumnInfo(name = "resource_type")
    val resourceType: String,

    @ColumnInfo(name = "match_question_id")
    val matchQuestionId: String,

    @ColumnInfo(name = "answer_id")
    val answerId: Long?,

    @Embedded(prefix = "resource_")
    val resource: LocalVideoResource?
) {

    @Keep
    data class LocalMatchedQuestionSource(

        @ColumnInfo(name = "ocr_text")
        val ocrText: String?,

        @ColumnInfo(name = "is_exact_match")
        val isExactMatch: Boolean?,

        @Embedded(prefix = "top_left_")
        val topLeft: LocalUiConfiguration?,

        @Embedded(prefix = "top_right_")
        val topRight: LocalUiConfiguration?,

        @Embedded(prefix = "bottom_left_")
        val bottomLeft: LocalUiConfiguration?,

        @Embedded(prefix = "bottom_center_")
        val bottomCenter: LocalUiConfiguration?,

        @Embedded(prefix = "bottom_right_")
        val bottomRight: LocalUiConfiguration?
    )

    @Keep
    data class LocalCanvas(

        @ColumnInfo(name = "background_color")
        val backgroundColor: String?,

        @Embedded(prefix = "canvas_corner_radius_")
        val cornerRadius: LocalCornerRadius?,

        @Embedded(prefix = "canvas_padding_")
        val padding: LocalPadding?,

        @Embedded(prefix = "canvas_margin_")
        val margin: LocalMargin?,

        @ColumnInfo(name = "stroke_color")
        val strokeColor: String?,

        @ColumnInfo(name = "stroke_width")
        val strokeWidth: Int?
    )

    @Keep
    data class LocalUiConfiguration(

        @ColumnInfo(name = "text")
        val text: String?,

        @ColumnInfo(name = "text_size")
        val textSize: String?,

        @ColumnInfo(name = "is_bold")
        val isBold: Boolean?,

        @Embedded(prefix = "ui_config_corner_radius_")
        val cornerRadius: LocalCornerRadius?,

        @Embedded(prefix = "ui_config_padding_")
        val padding: LocalPadding?,

        @Embedded(prefix = "ui_config_margin_")
        val margin: LocalMargin?,

        @ColumnInfo(name = "text_gravity")
        val textGravity: Double?,

        @ColumnInfo(name = "text_color")
        val textColor: String?,

        @ColumnInfo(name = "stroke_color")
        val strokeColor: String?,

        @ColumnInfo(name = "stroke_width")
        val strokeWidth: Int?,

        @ColumnInfo(name = "icon_link")
        val iconLink: String?,

        @ColumnInfo(name = "icon_height")
        val iconHeight: Int?,

        @ColumnInfo(name = "icon_width")
        val iconWidth: Int?,

        @ColumnInfo(name = "background_color")
        val backgroundColor: String?,

        @ColumnInfo(name = "width_percentage")
        val widthPercentage: Double?,
    )

    @Keep
    data class LocalCornerRadius(

        @ColumnInfo(name = "top_left")
        val topLeft: Double?,

        @ColumnInfo(name = "top_right")
        val topRight: Double?,

        @ColumnInfo(name = "bottom_right")
        val bottomRight: Double?,

        @ColumnInfo(name = "bottom_left")
        val bottomLeft: Double?
    )

    @Keep
    data class LocalPadding(

        @ColumnInfo(name = "top")
        val top: Int?,

        @ColumnInfo(name = "right")
        val right: Int?,

        @ColumnInfo(name = "bottom")
        val bottom: Int?,

        @ColumnInfo(name = "left")
        val left: Int?
    )

    @Keep
    data class LocalMargin(

        @ColumnInfo(name = "top")
        val top: Int?,

        @ColumnInfo(name = "right")
        val right: Int?,

        @ColumnInfo(name = "bottom")
        val bottom: Int?,

        @ColumnInfo(name = "left")
        val left: Int?
    )

    @Keep
    data class LocalVideoResource(
        @ColumnInfo(name = "video_name")
        val videoName: String?,

        @ColumnInfo(name = "resource")
        val resource: String,

        @ColumnInfo(name = "drm_scheme")
        val drmScheme: String?,

        @ColumnInfo(name = "drm_license_url")
        val drmLicenseUrl: String?,

        @ColumnInfo(name = "media_type")
        val mediaType: String?,

        @ColumnInfo(name = "offset")
        val offset: Long?
    )
}
