package com.doubtnutapp.data.resourcelisting.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.ActionData
import com.doubtnutapp.data.newlibrary.model.ApiVideoObj
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
class ApiQuestionMeta(
    @SerializedName("question_id") val questionId: String,
    @SerializedName("ocr_text") val ocrText: String?,
    @SerializedName("question") val question: String,
    @SerializedName("class") val videoClass: String?,
    @SerializedName("microconcept") val microConcept: String?,
    @SerializedName("thumbnail_image") val questionThumbnailImage: String?,
    @SerializedName("bg_color") val bgColor: String?,
    @SerializedName("doubt") val doubtField: String?,
    @SerializedName("duration") val videoDuration: Int,
    @SerializedName("share") val shareCount: Int,
    @SerializedName("like") val likeCount: Int,
    @SerializedName("isLiked") val isLiked: Boolean,
    @SerializedName("share_message") val sharingMessage: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("key_name") val keyName: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("button_bg_color") val buttonBgColor: String?,
    @SerializedName("action_activity") val actionActivity: String?,
    @SerializedName("action_data") val actionData: ActionData?,
    @SerializedName("views") val views: String?,
    @SerializedName("resource_type") val resourceType: String,
    @SerializedName("question_tag") val questionMeta: String?,
    @SerializedName("ref") val ref: String?,
    @SerializedName("video_obj") val videoObj: ApiVideoObj?,
    @SerializedName("widget_data") val widgetData: WidgetEntityModel<WidgetData, WidgetAction>?
)
