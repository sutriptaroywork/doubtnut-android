package com.doubtnutapp.dnr.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Mehul Bisht on 01/11/21
 */
@Keep
data class DnrSpinTheWheelInitialData(
    @SerializedName("toolbar_data") val toolbarData: DnrToolbarData,
    @SerializedName("info_data") val lockedStateData: DnrSpinTheWheel,
    @SerializedName("loading_state_data") val loadingStateData: VoucherLoadingData,
    @SerializedName("error_state_data") val errorStateData: VoucherErrorData?,
    @SerializedName("better_luck_state") val betterLuckNextTimeData: VoucherErrorData?,
)

@Keep
data class DnrSpinTheWheel(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("description") val description: String,
    @SerializedName("cta") val cta: String,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("warning_container") val warningContainer: DnrWarningData?,
    @SerializedName("items", alternate = ["item"]) val items: List<DnrSpinWheelItem>,
    @SerializedName("selected_index") val selectedIndex: Int,
    @SerializedName("selected_voucher_id") val selectedVoucherId: String
)

@Keep
data class DnrSpinWheelItem(
    @SerializedName("color") val color: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("image_height") val imageHeight: Int?,
    @SerializedName("image_width") val imageWidth: Int?
)
