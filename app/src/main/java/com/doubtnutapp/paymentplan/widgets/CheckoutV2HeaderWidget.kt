package com.doubtnutapp.paymentplan.widgets

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnAudioToggle
import com.doubtnutapp.base.ShowCheckoutV2Dialog
import com.doubtnutapp.databinding.WidgetCheckoutV2HeaderBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 07-10-2021
 */

class CheckoutV2HeaderWidget(context: Context) :
    ActionPerformer,
    BaseBindingWidget<CheckoutV2HeaderWidget.WidgetHolder, CheckoutV2HeaderWidgetModel, WidgetCheckoutV2HeaderBinding>(
        context
    ) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetCheckoutV2HeaderBinding {
        return WidgetCheckoutV2HeaderBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.forceUnWrap()?.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: CheckoutV2HeaderWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(22, 2, 16, 16)
        })
        val data = model.data
        holder.binding.apply {
            parentLayout.background = Utils.getShape("#ffffff", "#ffffff")
            title.text = data.title
            subtitle.isVisible = !data.subtitle.isNullOrBlank()
            subtitle.text = data.subtitle.orEmpty()
            price.text = data.price
            root.setOnClickListener {
                actionPerformer?.performAction(ShowCheckoutV2Dialog(data.dialogData))
            }
            subtitle.apply {
                setCompoundDrawablesWithIntrinsicBounds(
                    when {
                        data.audioUrl.isNullOrBlank() -> {
                            0
                        }
                        data.isAudioPlaying == true -> {
                            R.drawable.ic_volume
                        }
                        else -> {
                            R.drawable.ic_volume_off
                        }
                    }, 0, 0, 0
                )
                compoundDrawables.getOrNull(0)?.setTint(
                    ContextCompat.getColor(
                        context,
                        R.color.grey_808080
                    )
                )
            }
            subtitle.setOnClickListener {
                when {
                    data.audioUrl.isNullOrBlank() -> {
                        // do nothing
                    }
                    data.isAudioPlaying == true -> {
                        data.isAudioPlaying = false
                        actionPerformer?.performAction(OnAudioToggle(false, data.audioUrl))
                    }
                    else -> {
                        data.isAudioPlaying = true
                        actionPerformer?.performAction(OnAudioToggle(true, data.audioUrl))
                    }
                }
                subtitle.setCompoundDrawablesWithIntrinsicBounds(
                    when {
                        data.audioUrl.isNullOrBlank() -> {
                            0
                        }
                        data.isAudioPlaying == true -> {
                            R.drawable.ic_volume
                        }
                        else -> {
                            R.drawable.ic_volume_off
                        }
                    }, 0, 0, 0
                )
                subtitle.compoundDrawables.getOrNull(0)?.setTint(
                    ContextCompat.getColor(
                        context,
                        R.color.grey_808080
                    )
                )
            }
        }
        return holder
    }

    class WidgetHolder(
        binding: WidgetCheckoutV2HeaderBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetCheckoutV2HeaderBinding>(binding, widget)
}

@Keep
class CheckoutV2HeaderWidgetModel :
    WidgetEntityModel<CheckoutV2HeaderWidgetData, WidgetAction>()

@Keep
data class CheckoutV2HeaderWidgetData(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("price") val price: String,
    @SerializedName("audio_url") val audioUrl: String?,
    @SerializedName("is_audio_playing") var isAudioPlaying: Boolean?,
    @SerializedName("dialog_data") val dialogData: CheckoutV2HeaderDialogData
) : WidgetData()

@Keep
@Parcelize
data class CheckoutV2HeaderDialogData(
    @SerializedName("title") val title: String,
    @SerializedName("bottom_text") val bottomText: String,
    @SerializedName("price") val price: String,
    @SerializedName("price_breakup") val priceBreakup: List<CheckoutV2HeaderDialogDataItem>
) : Parcelable

@Keep
@Parcelize
data class CheckoutV2HeaderDialogDataItem(
    @SerializedName("title") val title: String,
    @SerializedName("amount") val amount: String
) : Parcelable