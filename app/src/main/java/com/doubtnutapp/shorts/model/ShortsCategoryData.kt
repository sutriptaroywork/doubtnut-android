package com.doubtnutapp.shorts.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ShortsCategoryData(
    @SerializedName("title")
    val title: String?,
    @SerializedName("title_color")
    val titleColor: String?,
    @SerializedName("title_size")
    val titleSize: String?,
    @SerializedName("subtitle")
    val subtitle: String?,
    @SerializedName("subtitle_color")
    val subtitleColor: String?,
    @SerializedName("subtitle_size")
    val subtitleSize: String?,
    @SerializedName("close_icon_url")
    val closeIconUrl: String?,
    @SerializedName("categories")
    val categories: List<CategoryData>?,
    @SerializedName("category_color")
    val categoryColor: String?,
    @SerializedName("category_size")
    val categorySize: String?,
    @SerializedName("is_bold")
    val isCategoryBold: Boolean?,
    @SerializedName("text_button")
    val textButton: String?,
    @SerializedName("minimum_categories")
    val minimumCategories: Int?,
    @SerializedName("extra_params")
    val extraParams: HashMap<String, Any>?
)

@Keep
data class CategoryData(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("value")
    val value: String?,
    @SerializedName("is_selected")
    var isSelected: Boolean?,
    @SerializedName("is_mutable")
    val isMutable:Boolean?
)
