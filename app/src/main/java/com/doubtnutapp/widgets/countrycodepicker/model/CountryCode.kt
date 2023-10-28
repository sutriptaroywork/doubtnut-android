package com.doubtnutapp.widgets.countrycodepicker.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by devansh on 27/11/20.
 */

@Keep
data class CountryCodeList(
        @SerializedName("countries") val countries: List<CountryCode>
)

@Keep
@Parcelize
data class CountryCode(
        @SerializedName("name") val name: String,
        @SerializedName("english_name") val englishName: String,
        @SerializedName("name_code") val nameCode: String,
        @SerializedName("phone_code") val phoneCode: String
) : Parcelable {

    companion object {
        const val PLUS_APPENDED_COUNTRY_CODE_INDIA = "+91"
    }

    val plusAppendedPhoneCode: String
        get() = "+$phoneCode"

    fun matches(query: String): Boolean {
        return name.contains(query, true)
                || plusAppendedPhoneCode.contains(query, true)
                || nameCode.contains(query, true)
    }
}