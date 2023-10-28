package com.doubtnutapp.resourcelisting.model

import androidx.annotation.Keep
import com.doubtnutapp.base.AutoplayRecyclerViewItem
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.model.Video

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
data class QuestionMetaDataModel(
    val questionId: String,
    val ocrText: String?,
    val question: String,
    val videoClass: String?,
    val microConcept: String?,
    val questionThumbnailImage: String?,
    val bgColor: String?,
    val doubtField: String?,
    val videoDuration: Int,
    var shareCount: Int,
    var likeCount: Int,
    var isLiked: Boolean,
    val sharingMessage: String?,
    val resourceType: String,
    val views: String?,
    var questionMeta: String?,
    override val videoObj: Video?,
    override val viewType: Int,
    var isFullWidthCard: Boolean = true, // not coming from api
    var label: String? = null, // not coming from api
    var isPlaying: Boolean? = null, // not coming from api
    var labelColor: String? = null, // not coming from api
    var hideOverflowMenu: Boolean = false, // not coming from api
    var heightRatio: Float? = null, // not coming from api
    var ocrTextFontSize: Int? = null, //not coming from api
    val widgetData: WidgetEntityModel<WidgetData, WidgetAction>? = null,
) : AutoplayRecyclerViewItem