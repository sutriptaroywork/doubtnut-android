package com.doubtnutapp.dnr.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class VoucherInitialData(
    @SerializedName("toolbar_data") val toolbarData: DnrToolbarData,
    @SerializedName("voucher_image_url") val voucherImageUrl: String?,
    @SerializedName("voucher_image_background_color") val voucherImageBackgroundColor: String?,
    @SerializedName("info_data") val lockedStateData: VoucherLockedData?,
    @SerializedName("redeemed_details") val unlockedStateData: VoucherUnlockedData?,
    @SerializedName("warning_container") val warningContainer: DnrWarningData?,
    @SerializedName("loading_state_data") val loadingStateData: VoucherLoadingData?,
    @SerializedName("error_state_data") val errorStateData: VoucherErrorData?,
)

open class VoucherStateData

@Keep
data class VoucherLockedData(
    @SerializedName("title") val title: String,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("offer_title") val offerTitle: String?,
    @SerializedName("offer_description") val offerDescription: String?,
    @SerializedName("info_items") val voucherInfoItems: List<VoucherInfoItem>?,
    @SerializedName("cta") val cta: String?,
    var voucherStateLayoutVisibility: VoucherStateLayoutVisibility? = null,
) : VoucherStateData()

@Keep
data class VoucherInfoItem(
    @SerializedName("title") val title: String,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("description") val description: String?,
    var isExpandable: Boolean = false,
)

@Keep
data class VoucherUnlockedData(
    @SerializedName("title") val title: String?,
    @SerializedName("expire_on") val expireOn: String?,
    @SerializedName("voucher_code") val voucherCode: String?,
    @SerializedName("copy_code_text") val copyCodeText: String?,
    @SerializedName("cta") val cta: String?,
    @SerializedName("deeplink") val deeplink: String?,
    var voucherStateLayoutVisibility: VoucherStateLayoutVisibility? = null,
) : VoucherStateData()

@Keep
data class VoucherErrorData(
    @SerializedName("title") val title: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("cta") val cta: String?,
    var voucherStateLayoutVisibility: VoucherStateLayoutVisibility? = null,
) : VoucherStateData()

@Keep
data class VoucherLoadingData(
    @SerializedName("description") val description: String?,
    @SerializedName("cta") val cta: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("duration") val duration: Long,
    @SerializedName("animation_file_name") val animationFileName: String,
    var isLoading: Boolean = false,
    var voucherStateLayoutVisibility: VoucherStateLayoutVisibility? = null,
) : VoucherStateData()

@Keep
data class DnrWarningData(
    @SerializedName("description") val description: String?,
    @SerializedName("description_color") val descriptionColor: String?,
    @SerializedName("background_color") val backgroundColor: String?,
    var deeplink: String? = null,
    var voucherStateLayoutVisibility: VoucherStateLayoutVisibility? = null,
) : VoucherStateData()

@Keep
data class VoucherStateLayoutVisibility(
    val isDetailLayoutVisible: Boolean,
    val isErrorLayoutVisible: Boolean,
    val isLoadingLayoutVisible: Boolean,
)
