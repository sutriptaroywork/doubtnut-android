package com.doubtnutapp.dnr.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.annotation.Keep
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.setMargins
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.databinding.WidgetDnrRedeemVoucherBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.isValidColorCode
import com.doubtnutapp.loadImageEtx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import com.skydoves.balloon.extensions.dp
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 19/10/21.
 */
class DnrRedeemVoucherWidget(context: Context) :
    BaseBindingWidget<
            DnrRedeemVoucherWidget.WidgetHolder,
            DnrRedeemVoucherWidget.Model,
            WidgetDnrRedeemVoucherBinding>(context) {

    companion object {
        private const val TAG = "DnrRedeemVoucherWidget"
    }

    var source: String? = null

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding() = WidgetDnrRedeemVoucherBinding
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
            if (data.backgroundColor.isValidColorCode()) {
                parentLayout.setBackgroundColor(Color.parseColor(data.backgroundColor))
            }
            ivVoucher.loadImageEtx(data.voucherImage.orEmpty())

            voucherLayout.apply {
                if (data.voucherBackgroundColor.isValidColorCode()) {
                    setBackgroundColor(Color.parseColor(data.voucherBackgroundColor))
                }
            }

            // if image_background_color is valid/available then set it to voucher layout too
            if (data.imageBackGroundColor.isValidColorCode()) {
                ivVoucher.setBackgroundColor(Color.parseColor(data.imageBackGroundColor))
                voucherLayout.setBackgroundColor(Color.parseColor(data.imageBackGroundColor))
            }
            else{
                if (data.voucherBackgroundColor.isValidColorCode()) {
                    setBackgroundColor(Color.parseColor(data.voucherBackgroundColor))
                }
                ivVoucher.setBackgroundColor(Color.TRANSPARENT)
            }



            if(data.cardCornerRadius!=null){
                rootCardView.radius = data.cardCornerRadius.toFloat().dpToPx()
            }
            else{
                rootCardView.radius = 0f
            }

            tvTitle.apply {
                text = data.title
                if (data.titleColor.isValidColorCode()) {
                    setTextColor(Color.parseColor(data.titleColor))
                }
                // if subtitle is empty center align title
                if (data.subtitle.isNullOrEmpty()) {
                    val topMargin = 35.dpToPx()
                    val marginStart = 10.dpToPx()
                    setMargins(marginStart, topMargin, 0, 0)
                }

                if (data.titleTextSize.isNotNullAndNotEmpty()) {
                    textSize = data.titleTextSize!!.toFloat()
                }

                data.isTitleMultiline?.let {
                    if (it) {
                        maxLines = 3
                    }
                }?: run {
                    maxLines = 1
                }
            }

            tvSubtitle.apply {
                text = data.subtitle
                if (data.subTitleColor.isValidColorCode()) {
                    setTextColor(Color.parseColor(data.subTitleColor))
                }

                data.isSubtitleMultiline?.let {
                    if (it) {
                        maxLines = 3
                    }
                } ?: run {
                        maxLines = 1
                    }

                if (data.subtitleTextSize.isNotNullAndNotEmpty()) {
                    textSize = data.subtitleTextSize!!.toFloat()
                }
            }

            data.imageCardElevation?.let {
                voucherCardView.elevation = it.toFloat()
            }

            // hide coins card
            data.layoutRedirectVisibility?.let {
                if (!it) {
                    layoutRedirect.visibility = View.GONE
                }
            }

            data.imageHeight?.let { cardHeight ->
                if (cardHeight > 0) {
                    voucherCardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        height = cardHeight.dpToPx()
                        width = cardHeight.dpToPx()
                    }
                    ivVoucher.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        height = cardHeight.dpToPx()
                        width = cardHeight.dpToPx()
                    }
                    ivVoucher.scaleType = ImageView.ScaleType.FIT_XY
                }
            }

            layoutRedirect.apply {
                if (data.ctaBackgroundColor.isValidColorCode()) {
                    setBackgroundColor(Color.parseColor(data.ctaBackgroundColor))
                }
            }

            // reduce bottom margin for voucher card when coins rounded card and subtitle is not visible
            if (data.subtitle.isNullOrEmpty() && data.layoutRedirectVisibility != null &&
                !data.layoutRedirectVisibility
            ) {
                voucherCardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    val marginStart = 15.dpToPx()
                    val marginBottom = 5.dpToPx()
                    setMargins(marginStart, 0, 0, marginBottom)
                }
            }

            tvRedirectText.apply {
                text = data.cta
                if (data.ctaColor.isValidColorCode()) {
                    setTextColor(Color.parseColor(data.ctaColor))
                }
            }

            ivCoin.apply {
                isVisible = data.dnrImage.isNullOrEmpty().not()
                loadImageEtx(data.dnrImage.orEmpty())
            }

            setOnClickListener {
                deeplinkAction.performAction(context, data.deeplink)
            }
        }

        return holder
    }

    class WidgetHolder(binding: WidgetDnrRedeemVoucherBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDnrRedeemVoucherBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("title_text_size") val titleTextSize: String?,
        @SerializedName("is_title_multiline") var isTitleMultiline: Boolean?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_text_size") val subtitleTextSize: String?,
        @SerializedName("is_subtitle_multiline") var isSubtitleMultiline: Boolean?,
        @SerializedName("subtitle_color") val subTitleColor: String?,
        @SerializedName("dnr_image") val dnrImage: String?,
        @SerializedName("image_card_elevation") var imageCardElevation: Int?,
        @SerializedName("cta") val cta: String?,
        @SerializedName("cta_color") val ctaColor: String?,
        @SerializedName("cta_background_color") val ctaBackgroundColor: String?,
        @SerializedName("voucher_image") val voucherImage: String?,
        @SerializedName("voucher_background_color") val voucherBackgroundColor: String?,
        @SerializedName("image_background_color") val imageBackGroundColor: String?,
        @SerializedName("visibility_layout_redirect") val layoutRedirectVisibility: Boolean?,
        @SerializedName("image_height") val imageHeight: Int?,
        @SerializedName("parent_card_corner_radius") val cardCornerRadius: Int?,
        @SerializedName("deeplink") val deeplink: String?,
    ) : WidgetData()
}
