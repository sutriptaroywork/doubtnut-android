package com.doubtnutapp.freeTrialCourse.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class FreeTrialCourseResponse(
    @SerializedName("data")
    val data: ListData
) {

    @Keep
    data class ListData(
        @SerializedName("widgets") val widgets: List<WidgetEntityModel<WidgetData, WidgetAction>>?,
        @SerializedName("title") val pageTitle: String?,
        @SerializedName("pre_purchase_pop_up") val prePurchasePopup: PrePurchasePopupData?,
        @SerializedName("post_purchase_pop_up") val postPurchasePopupData: PostPurchasePopupData?,
    )

    @Keep
    data class PrePurchasePopupData(
        @SerializedName("title") val title: String?,
        @SerializedName("title_size") val titleSize: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("cta1") val cta1: Cta?,
        @SerializedName("cta2") val cta2: Cta?,
    )

    @Keep
    data class PostPurchasePopupData(
        @SerializedName("title") val title: String?,
        @SerializedName("title_size") val titleSize: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("background_color") val bgColor: String?,
        @SerializedName("cta2") val cta2: Cta?,
    )


    @Keep
    data class Cta(
        @SerializedName("title") val title: String,
        @SerializedName("title_color") val titleColor: String,
        @SerializedName("title_size") val titleSize: String,
    )
}