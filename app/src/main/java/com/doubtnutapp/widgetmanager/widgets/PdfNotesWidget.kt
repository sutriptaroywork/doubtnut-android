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
import com.doubtnutapp.databinding.WidgetPdfNotesBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 6/5/21.
 */

class PdfNotesWidget(context: Context) :
    BaseBindingWidget<PdfNotesWidget.WidgetHolder, PdfNotesWidget.Model,WidgetPdfNotesBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val data = model.data
        holder.binding.apply {
            rootLayout.updateLayoutParams {
                width = Utils.getWidthFromScrollSize(holder.binding.root.context, data.cardWidth) -
                        (marginStart + marginEnd)
            }

            tvTitle.text = data.title

            setOnClickListener {
                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams?.apply {
                    put(Constants.ID, data.id)
                    put(Constants.WIDGET_TYPE, WidgetTypes.TYPE_PDF_NOTES)
                }))
                deeplinkAction.performAction(context, data.deeplink)
            }
        }
        return holder
    }

    class WidgetHolder(
        binding: WidgetPdfNotesBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetPdfNotesBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String,
        @SerializedName("card_width") val cardWidth: String,
        @SerializedName("deeplink") val deeplink: String,
        @SerializedName("id") val id: String,
    ) : WidgetData()

    override fun getViewBinding(): WidgetPdfNotesBinding {
        return WidgetPdfNotesBinding.inflate(LayoutInflater.from(context), this, true)
    }
}