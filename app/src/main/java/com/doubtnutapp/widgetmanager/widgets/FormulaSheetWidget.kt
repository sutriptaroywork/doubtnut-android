package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetFormulaSheetBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class FormulaSheetWidget(context: Context) :
    BaseBindingWidget<FormulaSheetWidget.WidgetHolder, FormulaSheetWidget.Model, WidgetFormulaSheetBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetFormulaSheetBinding {
        return WidgetFormulaSheetBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val data = model.data

        holder.binding.apply {
            rootLayout.updateLayoutParams {
                width = Utils.getWidthFromScrollSize(root.context, data.cardWidth) -
                        (marginStart + marginEnd)
            }

            tvFormulaSheetTitle.text = data.title
            ivFormulaSheet.loadImage(data.imageUrl)

            setOnClickListener {
                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams?.apply {
                    put(Constants.ID, data.id)
                    put(Constants.WIDGET_CLICK_SOURCE, Constants.WIDGET_FORMULA_SHEET)
                }))
                deeplinkAction.performAction(context, data.deeplink)
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetFormulaSheetBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetFormulaSheetBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String,
        @SerializedName("image_url") val imageUrl: String,
        @SerializedName("card_width") val cardWidth: String,
        @SerializedName("deeplink") val deeplink: String,
        @SerializedName("id") val id: String,
    ) : WidgetData()

}