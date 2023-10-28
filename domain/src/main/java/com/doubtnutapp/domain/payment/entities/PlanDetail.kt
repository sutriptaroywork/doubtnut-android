package com.doubtnutapp.domain.payment.entities

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by Anand Gaurav on 2019-12-14.
 */
@Keep
data class PlanDetail(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>,
    @SerializedName("title") val title: String?,
    @SerializedName("header_title") val headerTitle: String?,
    @SerializedName("header_title_size") val headerTitleSize: String?,
    @SerializedName("header_title_color") val headerTitleColor: String?,
    @SerializedName("header_icon") val headerIcon: String?,
    @SerializedName("course_id") val courseId: String?,
    @SerializedName("payment_help") val paymentHelp: PaymentHelp?,
    @SerializedName("is_show") val shouldShowSaleDialog: Boolean?,
    @SerializedName("nudge_id") val nudgeId: Int?,
    @SerializedName("count") val nudgeCount: Int?,
    @SerializedName("voice_url") val voiceUrl: String?,
    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>? = null
)

@Keep
data class PaymentDiscount(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("discount") val discount: String?,
    @SerializedName("label") val label: String?,
    @SerializedName("newAmount") val newAmount: String?,
    @SerializedName("oldAmount") val oldAmount: String?,
    @SerializedName("validityText") val validityText: String?,
    @SerializedName("validityTime") val validityTime: Int?,
    @SerializedName("buttonText") val buttonText: String?,
    @SerializedName("payment_id") val paymentId: String?
)

@Keep
data class PurchasedCourseDetail(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>,
    @SerializedName("title") val title: String?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>? = null,
)

@Keep
data class PaymentHelp(
    @SerializedName("title") val title: String?,
    @SerializedName("list") val list: List<PaymentItem>?,
)

@Keep
data class PaymentItem(
    @SerializedName("name") val name: String?,
    @SerializedName("value") val value: String?
)

@Keep
data class TrialVipResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?
)

@Keep
data class CheckoutData(
    @SerializedName("title") val title: String?,
    @SerializedName("order_info") val orderInfo: OrderInfo?,
    @SerializedName("payment_method_title") val paymentMethodTitle: String?,
    @SerializedName("payment_info") val paymentInfoList: List<PaymentInformation>?,
    @SerializedName("preferred_payment_methods") val preferredPaymentMethods: List<PaymentInformation>?,
    @SerializedName("banners") val APBBannerList: List<ApbBannerItemData>?,
    @SerializedName("payment_link") val paymentLink: PaymentLinkInfo?,
    @SerializedName("payment_help") val paymentHelp: PaymentHelpData?,
    @SerializedName("checkout_audio") val checkoutAudio: CheckoutAudioData?,
    @SerializedName("assortment_type") val assortmentType: String?,
    @SerializedName("preferred_payment_title") val preferredPaymentTitle: String?
)

@Keep
data class CheckoutAudioData(
    @SerializedName("title") val title: String?,
    @SerializedName("audio_url") val audioUrl: String?
)

@Keep
data class OrderInfo(
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("package_duration") val packageDuration: String,
    @SerializedName("variant_id") val variantId: String?
)

@Keep
data class PaymentInformation(
    @SerializedName("method") val method: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("preferred_methods") val preferredMethodsList: List<PreferredMethodsItem>?,
    @SerializedName("more_bank_text") val moreBankText: String?,
    @SerializedName("is_selected") var isSelected: Boolean = false,
    /*Localization Data*/
    @SerializedName("card_localization") val cardLocalizationData: CardLocalizationData?,
    @SerializedName("more_banks_data") val moreBanksData: MoreBanksData?,
    @SerializedName("cod_info") var codInfo: CodInfo?,
    @SerializedName("description") var description: String?,
    @SerializedName("hyper_text") var hyperText: String?,
    @SerializedName("image_ratio") var imageRatio: String?,
    @SerializedName("bank_code") var bankCode: String?,
    @SerializedName("upi_hint") var upiHint: String?,
    @SerializedName("entered_upi") var enteredUpi: String?,
    @SerializedName("info") var info: PaymentMethodInfo?
)

@Keep
data class PaymentMethodInfo(
    @SerializedName("image_urls") var imageUrls: List<String>?,
    @SerializedName("deeplink") var deeplink: String?,
    @SerializedName("title") var title: String?
)

@Keep
data class CardLocalizationData(
    @SerializedName("card_no_hint") val cardHint: String?,
    @SerializedName("expiry_hint") val expiryHint: String?,
    @SerializedName("cvv_hint") val cvvHint: String?,
    @SerializedName("name_hint") val nameHint: String?
)

@Keep
data class MoreBanksData(
    @SerializedName("title") val title: String?,
    @SerializedName("list") val list: List<PreferredMethodsItem>?
)

@Keep
data class PreferredMethodsItem(
    @SerializedName("name") val name: String?,
    @SerializedName("code") val code: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("package_name") val package_name: String?,
    @SerializedName("is_selected") var isSelected: Boolean = false,
    @SerializedName("is_pre_selection_handled") var isPreSelectionHandled: Boolean = false,
)

@Keep
data class ApbBannerItemData(
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("banner_image_url") val bannerImageUrl: String?,
    @SerializedName("type") val type: String?
)

@Keep
data class PaymentLinkInfo(
    @SerializedName("title") val title: String?,
    @SerializedName("link") val payLink: PayLink?,
    @SerializedName("qr") val qrInfo: QrInfo?,
    @SerializedName("bbps") val bbpsInfo: BbpsInfo?
)

@Keep
data class PayLink(
    @SerializedName("title") val title: String?,
    @SerializedName("action_button_text") val actionButtonText: String?,
    @SerializedName("text2") val textShareTitle: String?,
    @SerializedName("text3") val textShareSubTitle: String?,
    @SerializedName("description") val description: List<String>?,

    @SerializedName("is_show") val shouldShowSaleDialog: Boolean?,
    @SerializedName("nudge_id") val nudgeId: Int?,
    @SerializedName("count") val nudgeCount: Int?,
    @SerializedName("head") val header: String?
)

@Keep
data class QrInfo(
    @SerializedName("text2") val title: String,
    @SerializedName("text3") val subTitle: String,
    @SerializedName("action_button_text") val actionButtonText: String
)

@Keep
data class BbpsInfo(
    @SerializedName("text2") val title: String,
    @SerializedName("text3") val subTitle: String,
    @SerializedName("action_button_text") val actionButtonText: String,
    @SerializedName("deeplink") val deeplink: String
)

@Keep
data class PackagePaymentInfo(
    @SerializedName("title") val title: String?,
    @SerializedName("cart_info") val cartInfoList: List<CartInfoItem>?,
    @SerializedName("view_details_text") val paymentDetailsText: String?,
    @SerializedName("action_button") val actionButtonText: String?,
    @SerializedName("wallet_info") val wallet: WalletInfo?,
    @SerializedName("coupon_info") val couponInfo: CouponInfo?,
    @SerializedName("cod") val codInfo: CodInfo?,
)

@Keep
data class CodInfo(
    @SerializedName("is_show") val show: Boolean?,
    @SerializedName("deeplink") val deeplink: String?
)

@Keep
data class CartInfoItem(
    @SerializedName("name") val name: String?,
    @SerializedName("key") val key: String?,
    @SerializedName("value") val value: String?,
    @SerializedName("color") val color: String?
)

@Keep
@Parcelize
data class CouponInfo(
    @SerializedName("title") val title: String?,
    @SerializedName("placeholder_text") val placeholderText: String?,
    @SerializedName("cta_button") val ctaButton: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("status") val status: Boolean?,
    @SerializedName("code") val couponCode: String?,
    @SerializedName("image_url") val couponImageUrl: String?,
    @SerializedName("apply_cta") val applyCTA: String?,
    @SerializedName("dialog_title") val dialogTitle: String?,
    @SerializedName("dialog_subtitle") val dialogSubTitle: String?,
    @SerializedName("remove_coupon") val removeCoupon: Boolean?
) : Parcelable

@Keep
data class WalletInfo(
    @SerializedName("amount") val amount: String?,
    @SerializedName("show_add_money") val showAddMoney: Boolean?,
    @SerializedName("info") val info: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("deeplink_text") val deeplinkText: String?,
    @SerializedName("show_wallet") val showWallet: Int?,
    @SerializedName("total_amount") val totalAmount: WalletTotalAmount?,
    @SerializedName("cash_amount") val cashAmount: WalletCashAmount?,
    @SerializedName("reward_amount") val rewardAmount: WalletRewardAmount?,
    @SerializedName("list") val walletAmounts: List<WalletAmount>?,
)

@Keep
data class WalletTotalAmount(
    @SerializedName("name") val name: String?,
    @SerializedName("value") val value: String?
)

@Keep
data class WalletCashAmount(
    @SerializedName("name") val name: String?,
    @SerializedName("value") val value: String?,
    @SerializedName("tooltip_text") val tooltipText: String?,
)

@Keep
data class WalletRewardAmount(
    @SerializedName("name") val name: String?,
    @SerializedName("value") val value: String?,
    @SerializedName("tooltip_text") val tooltipText: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
)

@Keep
data class WalletAmount(
    @SerializedName("name") val name: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("value") val value: String?,
    @SerializedName("value_hex") val valueHex: String?,
    @SerializedName("tooltip_text") val tooltipText: String?,
    @SerializedName("list") val walletAmounts: List<WalletAmount>?,
)

@Keep
data class DoubtPlanDetail(
    @SerializedName("title") val title: String?,
    @SerializedName("subscription") val subscription: Boolean,
    @SerializedName("vip_card") val vipCard: VipCard?,
    @SerializedName("vip_days") val vipDays: VipDays?,
    @SerializedName("feedback_card") val feedbackCard: FeedbackCard?,
    @SerializedName("package") val packageDesc: PackageDesc?,
    @SerializedName("payment_help") val paymentHelp: PaymentInfo?,
    @SerializedName("renewal_info") val renewalInfo: RenewalInfo?,
    @SerializedName("currency_symbol") val currencySymbol: String?
)

@Keep
data class PackageDesc(
    @SerializedName("title") val title: String?,
    @SerializedName("list") val packageInfoList: MutableList<DoubtPackageInfo>?
)

@Keep
data class PaymentInfo(
    @SerializedName("title") val title: String?,
    @SerializedName("list") val list: List<PaymentItem>?
)

@Keep
data class VipCard(
    @SerializedName("title") val title: String?,
    @SerializedName("sub_title") val subTitle: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("title_icon") val titleIcon: String?,
    @SerializedName("days_left") val daysLeft: String?,
    @SerializedName("validity_text") val validityText: String?
)

@Keep
data class VipDays(
    @SerializedName("title") val title: String?,
    @SerializedName("show_more") val showMore: Boolean?,
    @SerializedName("show_more_text") val showMoreText: String?,
    @SerializedName("details_paid") val detailsPaid: VipDetailsPaidReferral?,
    @SerializedName("details_referral") val detailsReferral: VipDetailsPaidReferral?,
    @SerializedName("total_days") val totalDays: VipDetailsPaidReferral?
)

@Keep
data class VipDetailsPaidReferral(
    @SerializedName("title") val title: String?,
    @SerializedName("date_info") val daysInfo: String?,
    @SerializedName("days_earned") val daysEarned: String?
)

@Keep
data class FeedbackCard(
    @SerializedName("title") val title: String?,
    @SerializedName("button_text") val buttonText: String?
)

@Keep
data class DoubtPackageInfo(
    @SerializedName("id") val id: String?,
    @SerializedName("original_amount") val originalAmount: String?,
    @SerializedName("offer_amount") val offerAmount: String?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("off") val off: String?,
    @SerializedName("selected") var selected: Boolean?,
    @SerializedName("variant_id") val variantId: String?
)

@Keep
data class RenewalInfo(
    @SerializedName("title") val title: String?,
    @SerializedName("package") val packageDesc: PackageDesc?
)

@Keep
data class PaymentLinkCreate(
    @SerializedName("txn_id") val txnId: String,
    @SerializedName("share_message") val shareMessage: String?,
    @SerializedName("error_message") val errorMessage: String?,
    @SerializedName("url") val link: String
)

@Keep
data class CouponData(
    @SerializedName("title") val title: String?,
    @SerializedName("heading1") val heading1: String?,
    @SerializedName("btn_cta") val btnText: String?,
    @SerializedName("hint_text") val hintText: String?,
    @SerializedName("heading2") val heading2: String?,
    @SerializedName("coupon_list") val list: List<CouponItem>?
)

@Keep
data class CouponItem(
    @SerializedName("coupon_title") val title: String?,
    @SerializedName("amount_saved") val amountSaved: String?,
    @SerializedName("validity") val validity: String?,
    @SerializedName("btn_cta") val btnText: String?
)

@Keep
@Parcelize
data class PaymentHelpData(
    @SerializedName("page_title") val pageTitle: String?,
    @SerializedName("page_title_icon") val pageTitleIcon: String?,
    @SerializedName("page_title_tooltip") val pageTitleTooltip: String?,
    @SerializedName("content") val content: PaymentHelpContent?,
) : Parcelable

@Keep
@Parcelize
data class PaymentHelpContent(
    @SerializedName("title") val title: String?,
    @SerializedName("list") val paymentHelpItems: List<PaymentHelpItem>?
) : Parcelable

@Keep
@Parcelize
data class PaymentHelpItem(
    @SerializedName("name") val name: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("video_url") val videoUrl: String?,
    @SerializedName("type") val type: String?
) : Parcelable
