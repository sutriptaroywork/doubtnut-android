package com.doubtnutapp.dnr.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.databinding.WidgetDnrEarningDetailBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.isValidColorCode
import com.doubtnutapp.loadImageEtx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 18/10/21.
 */
class DnrEarningsDetailWidget(context: Context) :
    BaseBindingWidget<
        DnrEarningsDetailWidget.WidgetHolder,
        DnrEarningsDetailWidget.Model,
        WidgetDnrEarningDetailBinding>(context) {

    companion object {
        private const val TAG = "DnrRedeemVoucherWidget"
    }

    var source: String? = null

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding() = WidgetDnrEarningDetailBinding
        .inflate(LayoutInflater.from(context), this, true)

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: Model
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val data = model.data
        val binding = holder.binding

        binding.apply {
            parentLayout.apply {
                if (data.backgroundColor.isValidColorCode()) {
                    setBackgroundColor(Color.parseColor(data.backgroundColor))
                }
                if (data.borderColor.isValidColorCode()) {
                    strokeColor = Color.parseColor(data.borderColor)
                }
            }

            title.apply {
                text = data.title
                if (data.titleColor.isValidColorCode()) {
                    title.setTextColor(Color.parseColor(data.titleColor))
                }
            }

            subtitle.apply {
                text = data.subTitle
                if (data.subtitleColor.isValidColorCode()) {
                    setTextColor(Color.parseColor(data.subtitleColor))
                }
            }

            ivCoin.apply {
                isVisible = data.dnrImage.isNullOrEmpty().not()
                loadImageEtx(data.dnrImage.orEmpty())
            }

            setOnClickListener {
                if (data.deeplink.isNullOrEmpty()) return@setOnClickListener
                deeplinkAction.performAction(context, data.deeplink)
            }
        }

        return holder
    }

    class WidgetHolder(binding: WidgetDnrEarningDetailBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDnrEarningDetailBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("subtitle") val subTitle: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("subtitle_color") val subtitleColor: String?,
        @SerializedName("dnr_image") val dnrImage: String?,
        @SerializedName("border_color") val borderColor: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("deeplink") val deeplink: String?
    ) : WidgetData()
}
