package com.doubtnutapp.paymentplan.widgets

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.WidgetCheckoutV2ChildBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.domain.payment.entities.CardLocalizationData
import com.doubtnutapp.domain.payment.entities.PaymentMethodInfo
import com.doubtnutapp.feed.view.ImagesPagerActivity
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.vipplan.ui.CheckoutFragment
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import kotlinx.android.parcel.Parcelize
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 07-10-2021
 */

class CheckoutV2ChildWidget(context: Context) :
    BaseBindingWidget<CheckoutV2ChildWidget.WidgetHolder, CheckoutV2ChildWidgetModel, WidgetCheckoutV2ChildBinding>(
        context
    ) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var disposable: CompositeDisposable

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetCheckoutV2ChildBinding {
        return WidgetCheckoutV2ChildBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.forceUnWrap()?.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CheckoutV2ChildWidgetModel): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(8, 8, 0, 0)
        })
        val binding = holder.binding
        val data = model.data
        if (model.extraParams == null) {
            model.extraParams = hashMapOf()
        }

        if (data.isDisabled == true) {
            binding.disableLayout.isVisible = true
            binding.parentLayout.background = Utils.getShape("#90ebebeb", "#90ebebeb")
        } else {
            binding.disableLayout.isVisible = false
            binding.parentLayout.background = Utils.getShape(
                "#ffffff",
                if (data.isSelected) {
                    "#ea532c"
                } else {
                    "#ffffff"
                }
            )
        }

        (binding.imageViewLogo.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
            data.imageRatio
        binding.imageViewLogo.loadImage(data.imageUrl.orEmpty())
        binding.radioButton.text = data.title.orEmpty()
        binding.radioButton.isChecked = data.isSelected
        binding.btnContinueParent.isVisible = data.isSelected
        binding.btnContinueParent2.isVisible = data.isSelected
        binding.btnContinue.text = data.buttonText.orEmpty()

        binding.tvNew.text = data.hyperText.orEmpty()
        binding.tvNew.isVisible = !data.hyperText.isNullOrBlank()

        if (data.isSelected) {
            disposable.clear()
            binding.btnContinue.setProgress(0)
            handleTimer(data.progress ?: 0, data.autoApplyTimer?.toLongOrNull() ?: 0, binding, data)
        } else {
            disposable.clear()
            data.progress = 0
            binding.btnContinue.setProgress(0)
        }

        binding.root.setOnClickListener {
            if (data.isDisabled == true) {
                return@setOnClickListener
            }
            val currentTime = System.currentTimeMillis()
            if (data.lastClicked != null && (data.lastClicked!! + 500) > currentTime) {
                return@setOnClickListener
            }
            data.lastClicked = currentTime
            if (!data.isSelected) {
                data.progress = 0
            }
            val parentPosition =
                model.extraParams?.get(Constants.POSITION).toString().toIntOrNull() ?: 0
            val childPosition = holder.absoluteAdapterPosition
            val autoApplyTimer = data.autoApplyTimer?.toLongOrNull() ?: 0
            onPaymentContinue(
                data, parentPosition, childPosition,
                if (data.isSelected) {
                    0
                } else {
                    autoApplyTimer
                },
                if (data.isSelected) {
                    false
                } else {
                    autoApplyTimer == 0L
                }
            )
        }

        binding.textViewDesc.isVisible = data.isSelected && !data.subtitle.isNullOrBlank()
        binding.textViewDesc.text = data.subtitle.orEmpty()

        binding.tvInfo.isVisible = !data.info?.title.isNullOrBlank()
        binding.tvInfo.text = data.info?.title.orEmpty()
        binding.tvInfo.setOnClickListener {
            if (data.info != null) {
                if (!data.info?.deeplink.isNullOrBlank()) {
                    deeplinkAction.performAction(context, data.info?.deeplink)
                } else if (!data.info?.imageUrls.isNullOrEmpty()) {
                    context.startActivity(
                        ImagesPagerActivity.getIntent(
                            context,
                            data.info?.imageUrls!!,
                            source = CheckoutFragment.TAG
                        )
                    )
                }
            }
        }

        return holder
    }

    private fun getTimerObservable(count: Long) =
        Observable.interval(0, 1, TimeUnit.MILLISECONDS).take(count)

    private fun handleTimer(
        count: Long,
        progressMax: Long,
        binding: WidgetCheckoutV2ChildBinding,
        data: CheckoutV2ChildWidgetData
    ) {
        if (progressMax == 0L || count == progressMax) {
            binding.btnContinue.maxProgress = 1
            binding.btnContinue.setProgress(1)
        } else {
            binding.btnContinue.maxProgress = progressMax.toInt()
        }
        disposable.add(
            getTimerObservable(progressMax - count)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Long>() {
                    override fun onComplete() {
                        disposable.clear()
                    }

                    override fun onNext(t: Long) {
                        data.progress = t + count
                        binding.btnContinue.setProgress(data.progress?.toInt() ?: 0)
                    }

                    override fun onError(e: Throwable) {
                        disposable.clear()
                    }
                })
        )
    }

    fun onPaymentContinue(
        data: CheckoutV2ChildWidgetData,
        parentPosition: Int,
        childPosition: Int,
        delay: Long,
        ignoreAction: Boolean
    ) {
        if (data.hasDeeplink == true) {
            actionPerformer?.performAction(
                OnDeeplinkPaymentClick(
                    delay,
                    parentPosition, childPosition, data.deeplink.orEmpty(), ignoreAction
                )
            )
        } else {
            when (data.method) {
                "netbanking" -> {
                    actionPerformer?.performAction(
                        OnNetBankingClick(
                            data.dialogData, delay,
                            parentPosition, childPosition, ignoreAction
                        )
                    )
                }
                "netbanking_selected_bank" -> {
                    actionPerformer?.performAction(
                        OnSelectedNetBankingClick(
                            "netbanking", data.code.orEmpty(), delay,
                            parentPosition, childPosition, ignoreAction
                        )
                    )
                }
                "card" -> {
                    actionPerformer?.performAction(
                        OnCardMethodClick(
                            data.dialogData, delay,
                            parentPosition, childPosition, ignoreAction
                        )
                    )
                }
                "upi_collect" -> {
                    actionPerformer?.performAction(
                        OnUpiCollectClick(
                            data.dialogData, delay,
                            parentPosition, childPosition, ignoreAction
                        )
                    )
                }
                "upi" -> {
                    actionPerformer?.performAction(
                        OnUpiPaymentClick(
                            data.method, delay,
                            parentPosition, childPosition, ignoreAction
                        )
                    )
                }
                "wallet" -> {
                    actionPerformer?.performAction(
                        OnWalletClick(
                            data.dialogData, delay,
                            parentPosition, childPosition, ignoreAction
                        )
                    )
                }
                "paytm" -> {
                    actionPerformer?.performAction(
                        OnPaytmWalletPaymentClick(
                            "wallet", data.method, delay,
                            parentPosition, childPosition, ignoreAction
                        )
                    )
                }
                "bank_transfer" -> {

                }
                "payment_link" -> {
                    actionPerformer?.performAction(
                        OnPaymentLinkShareClick(
                            data.method, delay,
                            parentPosition, childPosition, ignoreAction
                        )
                    )
                }
                "upi_intent" -> {
                    actionPerformer?.performAction(
                        OnQrPaymentClick(
                            data.method, delay,
                            parentPosition, childPosition, ignoreAction
                        )
                    )
                }
                "bbps", "COD" -> {
                    //do nothing handled with deeplink
                }
            }
        }
    }

    inner class WidgetHolder(
        binding: WidgetCheckoutV2ChildBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetCheckoutV2ChildBinding>(binding, widget) {
        override fun onViewDetachedFromWindow() {
            super.onViewDetachedFromWindow()
            disposable.clear()
        }
    }
}

class CheckoutV2ChildWidgetModel : WidgetEntityModel<CheckoutV2ChildWidgetData, WidgetAction>()

@Keep
data class CheckoutV2ChildWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("method") val method: String,
    @SerializedName("name") val name: String?,
    @SerializedName("more_bank_text") val moreBankText: String?,
    @SerializedName("description") var description: String?,
    @SerializedName("hyper_text") val hyperText: String?,
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("image_ratio") val imageRatio: String,
    @SerializedName("is_selected") var isSelected: Boolean,
    @SerializedName("progress") var progress: Long?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("info") var info: PaymentMethodInfo?,
    @SerializedName("entered_upi") var enteredUpi: String?,
    @SerializedName("card_localization") val cardLocalizationData: CardLocalizationData?,
    @SerializedName("bank_code") var bankCode: String?,
    @SerializedName("code") var code: String?,
    @SerializedName("dialog_data") var dialogData: CheckoutV2DialogData?,
    @SerializedName("has_deeplink") var hasDeeplink: Boolean?,
    @SerializedName("last_clicked") var lastClicked: Long?,
    @SerializedName("is_disabled") var isDisabled: Boolean?,
    @SerializedName("auto_apply_timer") var autoApplyTimer: String?
) : WidgetData()

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
@Parcelize
data class CheckoutV2DialogData(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("button_text") val buttonText: String,
    @SerializedName("netbanking_data") val netbankingData: NetBankingDialogData?,
    @SerializedName("wallet_data") val walletData: WalletDialogData?,
    @SerializedName("card_data") val cardData: CardDialogData?,
    @SerializedName("upi_data") val upiData: UpiDialogData?
) : Parcelable

@Keep
@Parcelize
data class WalletDialogData(
    @SerializedName("items") val items: List<WalletItem>
) : Parcelable

@Keep
@Parcelize
data class UpiDialogData(
    @SerializedName("title") val title: String
) : Parcelable

@Keep
@Parcelize
data class NetBankingDialogData(
    @SerializedName("more_bank_text") val moreBankText: String,
    @SerializedName("more_banks_data") val moreBanksData: MoreBanksData,
    @SerializedName("items") val items: List<NetBankingItem>,
) : Parcelable

@Keep
@Parcelize
data class CardDialogData(
    @SerializedName("info") val info: CardInfoDialogData,
    @SerializedName("help") val help: CardHelpDialogData
) : Parcelable

@Keep
@Parcelize
data class WalletItem(
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("image_ratio") val imageRatio: String
) : Parcelable

@Keep
@Parcelize
data class NetBankingItem(
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("image_ratio") val imageRatio: String
) : Parcelable

@Keep
@Parcelize
data class MoreBanksData(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("items") val items: List<MoreBanksItem>
) : Parcelable

@Keep
@Parcelize
data class MoreBanksItem(
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String
) : Parcelable

@Keep
@Parcelize
data class CardInfoDialogData(
    @SerializedName("card_no_hint") val cardNoHint: String,
    @SerializedName("cvv_hint") val cvvHint: String,
    @SerializedName("expiry_hint") val expiryHint: String,
    @SerializedName("name_hint") val nameHint: String
) : Parcelable

@Keep
@Parcelize
data class CardHelpDialogData(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("image_ratio") val imageRatio: String
) : Parcelable