package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.doubtnutapp.course.widgets.VpaWidget
import com.doubtnutapp.data.remote.models.topicboostergame2.Faq
import com.doubtnutapp.domain.payment.entities.ApbBannerItemData
import com.doubtnutapp.domain.payment.entities.PaymentLinkInfo
import com.doubtnutapp.domain.payment.entities.WalletInfo
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 24/11/20.
 */
@Keep
data class WalletData(
    @SerializedName("title") val title: String?,
    @SerializedName("wallet") val walletInfo: WalletInfo?,
    @SerializedName("payment_link") val paymentLinkInfo: PaymentLinkInfo?,
    @SerializedName("payment_help") val faq: Faq?,
    @SerializedName("wallet_use") val walletUse: WalletUse?,
    @SerializedName("banners") val APBBannerList: List<ApbBannerItemData>?,
    @SerializedName("vpa_obj") val vpaObj: VpaWidget.Data?,
)

@Keep
data class WalletUse(
    @SerializedName("title") val title: String?,
    @SerializedName("list") val list: List<WalletUseItem>?
)

@Keep
data class WalletUseItem(
    @SerializedName("image") val image: String?,
    @SerializedName("name") val name: String?
)
