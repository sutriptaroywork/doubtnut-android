package com.doubtnutapp.ui.common.address

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AddressFormData(
    @SerializedName("lottie_url")
    val lottieUrl: String?,

    @SerializedName("title")
    val title: String?,
    @SerializedName("title_text_size")
    val titleTextSize: String?,
    @SerializedName("title_text_color")
    val titleTextColor: String?,

    @SerializedName("hint_link")
    val hintLink: String?,
    @SerializedName("hint_link2")
    val hintLink2: String?,
    @SerializedName("hint_full_name")
    val hintFullName: String?,
    @SerializedName("country_code")
    val countryCode: String?,
    @SerializedName("hint_mobile_number")
    val hintMobileNumber: String?,
    @SerializedName("hint_pin_code")
    val hintPinCode: String?,
    @SerializedName("hint_address_one")
    val hintAddressOne: String?,
    @SerializedName("hint_address_two")
    val hintAddressTwo: String?,
    @SerializedName("hint_landmark")
    val hintLandmark: String?,
    @SerializedName("hint_full_address")
    val hintFullAddress: String?,
    @SerializedName("hint_city")
    val hintCity: String?,
    @SerializedName("states")
    val states: List<StateData>?,
    @SerializedName("sizes")
    val sizes: List<StateData>?,
    @SerializedName("submit_text")
    val submitText: String?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>? = null,
)

@Keep
data class StateData(
    @SerializedName("id")
    val id: String?,
    @SerializedName("title")
    val title: String?,
)