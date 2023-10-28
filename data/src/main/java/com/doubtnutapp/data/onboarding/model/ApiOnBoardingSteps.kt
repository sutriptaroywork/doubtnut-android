package com.doubtnutapp.data.onboarding.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiOnBoardingSteps(

    @SerializedName("steps")
    val steps: List<ApiOnBoardingStep>?,

    @SerializedName("ask_question")
    val askQuestion: Boolean,

    @SerializedName("ask_button_text")
    val askButtonText: String?,

    @SerializedName("ask_button_active_message")
    val askButtonActiveMessage: String?,

    @SerializedName("ask_button_inactive_message")
    val askButtonInactiveMessage: String?,

    @SerializedName("is_final_submit")
    val isFinalSubmit: Boolean?
)

@Keep
data class ApiUserDetails(

    @SerializedName("image")
    val image: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("message")
    val message: String
)

@Keep
data class ApiOnBoardingStep(

    @SerializedName("type")
    val type: String,

    @SerializedName("title")
    val title: String?,

    @SerializedName("image")
    val image: String?,

    @SerializedName("message")
    val message: String?,

    @SerializedName("is_active")
    var isActive: Boolean,

    @SerializedName("is_multi_select")
    val isMultiSelect: Boolean,

    @SerializedName("is_submitted")
    val isSubmitted: Boolean,

    @SerializedName("step_items")
    val stepsItems: List<ApiOnBoardingStepItem>?,

    @SerializedName("progress_details")
    val progressDetails: ApiProgress?,

    @SerializedName("error_message")
    val errorMessage: String?,

    @SerializedName("collapsing_details")
    val collapsingDetails: ApiCollapsingDetails?,

    var viewType: Int,

    var isExpanded: Boolean,

    var currentStep: Int,

    var totalSteps: Int
)

@Keep
data class ApiCollapsingDetails(

    @SerializedName("collapsing_index")
    val collapsingIndex: Int,

    @SerializedName("collapsing_item")
    val collapsingItem: ApiOnBoardingStepItem
)

@Keep
data class ApiProgress(

    @SerializedName("image")
    val image: String,

    @SerializedName("message")
    val message: String
)

@Keep
data class ApiOnBoardingStepItem(

    @SerializedName("id")
    val id: Int?,

    @SerializedName("code")
    val code: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("sub_title")
    val subTitle: String?,

    @SerializedName("type")
    val type: String,

    @SerializedName("is_active")
    var isActive: Boolean,

    var rightArrow: Boolean,

    var viewType: Int
)
