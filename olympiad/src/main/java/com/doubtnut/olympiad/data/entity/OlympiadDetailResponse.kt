package com.doubtnut.olympiad.data.entity

import androidx.annotation.Keep
import com.doubtnut.core.entitiy.BaseUiData
import com.google.gson.annotations.SerializedName

@Keep
data class OlympiadDetailResponse(
    @SerializedName("header_title")
    val headerTitle: String?,
    @SerializedName("deeplink")
    val deeplink: String?,
    @SerializedName("finish_activity")
    val finishActivity: Boolean?,

    @SerializedName("highlight_color")
    val highlightColor: String?,

    @SerializedName("top_container")
    val topContainer: BaseUiData?,
    @SerializedName("details_container")
    val detailsContainer: DetailsContainerData?,
    @SerializedName("know_more")
    val knowMore: BaseUiData?,
    @SerializedName("term_and_condition")
    val termAndCondition: BaseUiData?,
    @SerializedName("cta_info")
    val ctaInfo: BaseUiData?,
    @SerializedName("cta")
    val cta: BaseUiData?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>?
)

@Keep
data class DetailsContainerData(
    @SerializedName("title_one", alternate = ["title1"])
    val titleOne: String?,
    @SerializedName("title_one_text_size", alternate = ["title1_text_size"])
    val titleOneTextSize: String?,
    @SerializedName("title_one_text_color", alternate = ["title1_text_color"])
    val titleOneTextColor: String?,

    @SerializedName("title_two", alternate = ["title2"])
    val titleTwo: String?,
    @SerializedName("title_two_text_size", alternate = ["title2_text_size"])
    val titleTwoTextSize: String?,
    @SerializedName("title_two_text_color", alternate = ["title2_text_color"])
    val titleTwoTextColor: String?,

    @SerializedName("label_text_size")
    val labelTextSize: String?,
    @SerializedName("label_text_color")
    val labelTextColor: String?,
    @SerializedName("value_text_size")
    val valueTextSize: String?,
    @SerializedName("value_text_color")
    val valueTextColor: String?,

    @SerializedName("label_edit_text_size")
    val labelEditTextSize: String?,
    @SerializedName("label_edit_text_color")
    val labelEditTextColor: String?,
    @SerializedName("value_edit_text_size")
    val valueEditTextSize: String?,
    @SerializedName("value_edit_text_color")
    val valueEditTextColor: String?,
    @SerializedName("hint_text_color")
    val hintTextColor: String?,

    @SerializedName("items")
    val items: List<DetailsContainerItem>?,
)

@Keep
data class DetailsContainerItem(
    @SerializedName("key")
    val key: String?,
    @SerializedName("label")
    val label: String?,
    @SerializedName("value")
    var value: String?,
    @SerializedName("is_editable")
    val isEditable: Boolean?,
    @SerializedName("hint")
    val hint: String?,
    @SerializedName("input_type")
    val inputType: Int?,
    @SerializedName("max_length")
    val maxLength: Int?,
)