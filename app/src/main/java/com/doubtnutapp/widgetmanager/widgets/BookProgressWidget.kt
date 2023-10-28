package com.doubtnutapp.widgetmanager.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.databinding.WidgetBookPorgressBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.disableSeek
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.Utils
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by devansh on 3/2/21.
 */

class BookProgressWidget(context: Context)
    : BaseBindingWidget<BookProgressWidget.WidgetHolder, BookProgressWidget.Model,
        WidgetBookPorgressBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetBookPorgressBinding {
       return WidgetBookPorgressBinding.inflate(LayoutInflater.from(context), this,true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(),this)
    }

    @SuppressLint("SetTextI18n")
    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val data = model.data
        val binding = holder.binding

        binding.apply {
            val width = Utils.getWidthFromScrollSize(holder.itemView.context, data.cardWidth) -
                    (cardView.marginStart + cardView.marginEnd)

            ivBook.updateLayoutParams {
                this.width = width
            }
            requestLayout()

            ivBook.loadImage(data.imageUrl)
            tvProgress.text = "${data.progress}/${data.maxProgress}"

            seekBar.apply {
                max = data.maxProgress
                progress = data.progress
                disableSeek()
            }

            cardView.setOnClickListener {
                DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(model.extraParams))
                deeplinkAction.performAction(context, data.deeplink)
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetBookPorgressBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetBookPorgressBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
            @SerializedName("id") val id: String?,
            @SerializedName("image_url") val imageUrl: String,
            @SerializedName("deeplink") val deeplink: String?,
            @SerializedName("progress") val progress: Int,
            @SerializedName("max_progress") val maxProgress: Int,
            @SerializedName("card_width") val cardWidth: String
    ) : WidgetData()
}