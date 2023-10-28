package com.doubtnutapp.paymentplan.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.doubtnut.core.utils.underline
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetCheckoutV2ParentBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 07-10-2021
 */

class CheckoutV2ParentWidget(context: Context) :
    BaseBindingWidget<CheckoutV2ParentWidget.WidgetHolder, CheckoutV2ParentWidgetModel, WidgetCheckoutV2ParentBinding>(
        context
    ) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetCheckoutV2ParentBinding {
        return WidgetCheckoutV2ParentBinding
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
        model: CheckoutV2ParentWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(22, 2, 16, 16)
        })
        val binding = holder.binding
        val data = model.data
        val adapter = WidgetLayoutAdapter(
            context,
            actionPerformer,
            source = null
        )

        if (model.extraParams == null) {
            model.extraParams = hashMapOf()
        }
        model.extraParams?.put(Constants.POSITION, holder.absoluteAdapterPosition)

        data.items.map {
            if (it.extraParams == null) {
                it.extraParams = hashMapOf()
            }
            it.extraParams?.putAll(model.extraParams ?: hashMapOf())
        }

        binding.title.text = data.title
        binding.rvParentWidgets.adapter = adapter

        binding.textViewToggle.underline()

        if (data.isExpanded == true) {
            adapter.setWidgets(data.items)
            binding.textViewToggle.text = data.toggleTitle.orEmpty()
            binding.textViewToggle.apply {
                setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_up_arrow, 0)
                compoundDrawables.getOrNull(2)?.setTint(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                )
            }
        } else {
            adapter.setWidgets(data.items.take(data.initialMaxLength ?: 10))
            binding.textViewToggle.text = data.toggleInitialTitle.orEmpty()
            binding.textViewToggle.apply {
                setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_drop_down, 0)
                compoundDrawables.getOrNull(2)?.setTint(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                )
            }
        }

        binding.textViewToggle.isVisible =
            !data.toggleTitle.isNullOrBlank() && !data.toggleInitialTitle.isNullOrBlank()

        binding.textViewToggle.setOnClickListener {
            if (data.isExpanded == true) {
                adapter.setWidgets(data.items.take(data.initialMaxLength ?: 10))
                binding.textViewToggle.text = data.toggleInitialTitle.orEmpty()
                data.isExpanded = false
                binding.textViewToggle.apply {
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_drop_down, 0)
                    compoundDrawables.getOrNull(2)?.setTint(
                        ContextCompat.getColor(
                            context,
                            R.color.black
                        )
                    )
                }
            } else {
                adapter.setWidgets(data.items)
                binding.textViewToggle.text = data.toggleTitle.orEmpty()
                data.isExpanded = true
                binding.textViewToggle.apply {
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_up_arrow, 0)
                    compoundDrawables.getOrNull(2)?.setTint(
                        ContextCompat.getColor(
                            context,
                            R.color.black
                        )
                    )
                }
            }
        }

        return holder
    }

    class WidgetHolder(
        binding: WidgetCheckoutV2ParentBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetCheckoutV2ParentBinding>(binding, widget)
}

class CheckoutV2ParentWidgetModel : WidgetEntityModel<CheckoutV2ParentWidgetData, WidgetAction>()

@Keep
data class CheckoutV2ParentWidgetData(
    @SerializedName("title") val title: String,
    @SerializedName("toggle_title") val toggleTitle: String?,
    @SerializedName("toggle_initial_title") val toggleInitialTitle: String?,
    @SerializedName("initial_max_length") val initialMaxLength: Int?,
    @SerializedName("is_expanded") var isExpanded: Boolean?,
    @SerializedName("items") val items: List<WidgetEntityModel<*, *>>
) : WidgetData()