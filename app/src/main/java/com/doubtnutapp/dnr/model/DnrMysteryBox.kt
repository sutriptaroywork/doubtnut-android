package com.doubtnutapp.dnr.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DnrMysteryBoxInitialData(
    @SerializedName("toolbar_data") val toolbarData: DnrToolbarData,
    @SerializedName("info_data") val lockedStateData: DnrMysteryBox,
    @SerializedName("loading_state_data") val loadingStateData: VoucherLoadingData,
    @SerializedName("error_state_data") val errorStateData: VoucherErrorData?,
)

@Keep
data class DnrMysteryBox(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("description") val description: String,
    @SerializedName("cta") val cta: String,
    @SerializedName("warning_container") val warningContainer: DnrWarningData?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("selected_voucher_id") val selectedVoucherId: String
)
