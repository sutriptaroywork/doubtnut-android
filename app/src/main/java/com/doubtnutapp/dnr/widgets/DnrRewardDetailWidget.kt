package com.doubtnutapp.dnr.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.databinding.WidgetDnrRewardDetailBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
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
class DnrRewardDetailWidget(context: Context) :
    BaseBindingWidget<
        DnrRewardDetailWidget.WidgetHolder,
        DnrRewardDetailWidget.Model,
        WidgetDnrRewardDetailBinding>(context) {

    companion object {
        private const val TAG = "DnrRewardDetailWidget"
    }

    var source: String? = null

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetDnrRewardDetailBinding {
        return WidgetDnrRewardDetailBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 16, 16)
            }
        )
        val data = model.data
        val binding = holder.binding

        binding.apply {
            if (data.backgroundColor.isValidColorCode()) {
                parentLayout.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor(data.backgroundColor))
            }

            tvTitle.apply {
                text = data.title
                if (data.titleColor.isValidColorCode()) {
                    setTextColor(Color.parseColor(data.titleColor))
                }
            }

            ivCoin.loadImageEtx(data.dnrImage.orEmpty())

            tvCount.apply {
                text = data.dnr
                if (data.dnrColor.isValidColorCode()) {
                    setTextColor(Color.parseColor(data.dnrColor))
                }
            }

            tvUnit.apply {
                text = data.dnrUnit
                if (data.dnrUnitColor.isValidColorCode()) {
                    setTextColor(Color.parseColor(data.dnrUnitColor))
                }
            }

            setOnClickListener {
                deeplinkAction.performAction(context, data.deeplink)
            }
        }

        return holder
    }

    class WidgetHolder(binding: WidgetDnrRewardDetailBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDnrRewardDetailBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("dnr_image") val dnrImage: String?,
        @SerializedName("dnr") val dnr: String?,
        @SerializedName("dnr_color") val dnrColor: String?,
        @SerializedName("dnr_unit") val dnrUnit: String?,
        @SerializedName("dnr_unit_color") val dnrUnitColor: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("deeplink") val deeplink: String?
    ) : WidgetData()
}
