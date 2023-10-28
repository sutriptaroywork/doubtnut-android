package com.doubtnutapp.paymentplan.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnDnWalletPaymentClick
import com.doubtnutapp.base.OnWalletToggle
import com.doubtnutapp.databinding.ItemCheckoutV2WalletBinding
import com.doubtnutapp.databinding.WidgetCheckoutV2WalletBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.hide
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 21-10-2021
 */

class CheckoutV2WalletWidget(context: Context) :
    BaseBindingWidget<CheckoutV2WalletWidget.WidgetHolder, CheckoutV2WalletWidgetModel, WidgetCheckoutV2WalletBinding>(
        context
    ) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetCheckoutV2WalletBinding {
        return WidgetCheckoutV2WalletBinding
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
        model: CheckoutV2WalletWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(22, 2, 16, 16)
        })
        val data = model.data

        holder.binding.apply {
            parentLayout.background = Utils.getShape("#ffffff", "#ffffff")
            title.text = data.title
            if (data.tooltipText != null) {
                tooltip.visibility = View.VISIBLE
                tooltip.setOnClickListener {
                    if (groupToolTip.isVisible) {
                        groupToolTip.visibility = View.GONE
                    } else {
                        groupToolTip.visibility = View.VISIBLE
                        titleToolTip.text = data.tooltipText

                        ivCloseTooltip.setOnClickListener {
                            groupToolTip.hide()
                        }
                    }
                }
            }
            val adapter = Adapter(
                data.items.orEmpty(),
                model.extraParams ?: HashMap(),
                actionPerformer
            )
            rv.adapter = adapter
            btnContinue.isVisible = !data.buttonText.isNullOrBlank()
            tvBottom.isVisible = !data.bottomText.isNullOrBlank()
            btnContinue.text = data.buttonText.orEmpty()
            tvBottom.text = data.bottomText.orEmpty()
            btnContinue.setOnClickListener {
                if (!data.buttonText.isNullOrBlank()) {
                    actionPerformer?.performAction(
                        OnDnWalletPaymentClick("netbanking", 0)
                    )
                }
            }

        }

        return holder
    }

    class Adapter(
        val items: List<CheckoutV2WalletItem>,
        val extraParams: HashMap<String, Any>,
        val actionPerformer: ActionPerformer?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        inner class ViewHolder(private val binding: ItemCheckoutV2WalletBinding) :
            RecyclerView.ViewHolder(binding.root) {

            val checkbox: CheckBox = binding.checkBox
            val subtitle: TextView = binding.subtitle
            val amountText: TextView = binding.amountText

            fun bind(item: CheckoutV2WalletItem) {
                checkbox.isClickable = false
                checkbox.text = item.title
                checkbox.isChecked = item.isSelected == true
                if (item.subtitle.isNotNullAndNotEmpty()) {
                    subtitle.visibility = View.VISIBLE
                    subtitle.text = item.subtitle
                }
                amountText.text = item.amountText
                binding.root.setOnClickListener {
                    actionPerformer?.performAction(OnWalletToggle(item.key, !item.isSelected))
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemCheckoutV2WalletBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )

            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items.get(position)
            holder.bind(item)
        }

        override fun getItemCount(): Int = items.size
    }

    class WidgetHolder(
        binding: WidgetCheckoutV2WalletBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetCheckoutV2WalletBinding>(binding, widget)
}

@Keep
class CheckoutV2WalletWidgetModel : WidgetEntityModel<CheckoutV2WalletWidgetData, WidgetAction>()

@Keep
data class CheckoutV2WalletWidgetData(
    @SerializedName("title") val title: String,
    @SerializedName("tooltip_text") val tooltipText: String?,
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("bottom_text") val bottomText: String?,
    @SerializedName("items") val items: List<CheckoutV2WalletItem>?
) : WidgetData()

@Keep
data class CheckoutV2WalletItem(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("key") val key: String,
    @SerializedName("is_selected") var isSelected: Boolean,
    @SerializedName("amount_text") val amountText: String
)