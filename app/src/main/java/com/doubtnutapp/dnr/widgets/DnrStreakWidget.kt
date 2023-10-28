package com.doubtnutapp.dnr.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.doubtnutapp.R
import com.doubtnutapp.databinding.WidgetDnrStreakBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.hide
import com.doubtnutapp.load
import com.doubtnutapp.show
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName

class DnrStreakWidget(context: Context) : BaseBindingWidget<
    DnrStreakWidget.WidgetHolder,
    DnrStreakWidget.Model,
    WidgetDnrStreakBinding>(context) {

    var source: String? = null

    override fun getViewBinding(): WidgetDnrStreakBinding =
        WidgetDnrStreakBinding.inflate(LayoutInflater.from(context), this, true)

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val binding = holder.binding
        val data = model.data

        binding.apply {

            setOnClickListener(null)

            ivScratchCard.hide()
            ivTick.hide()
            animationCircleStreak.hide()
            ivTick.imageTintList = null

            tvDay.text = if (data.isCurrentDay) {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

                // Required more width for the longer text(4.2x is calculated on the bases of hit and trial)
                layoutParams.width = Utils.getWidthFromScrollSize(context, "4.2x")
                buildSpannedString { bold { append("${context.getString(R.string.today)}${" "}${data.dayNumber}") } }
            } else {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams.width = Utils.getWidthFromScrollSize(context, data.cardWidth ?: "6x")
                data.dayNumber
            }

            when (data.isMarked) {
                true -> {
                    ivBackground.load(R.drawable.reward_green_ring)
                    ivTick.show()
                    ivTick.imageTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
                }
                false -> {
                    ivBackground.load(R.drawable.reward_grey_white)
                }
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetDnrStreakBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetDnrStreakBinding>(binding, widget)

    class Model : WidgetEntityModel<DnrStreak, WidgetAction>()

    @Keep
    data class DnrStreak(
        @SerializedName("day_number") val dayNumber: String,
        @SerializedName("show_gift") var showGift: Boolean = false,
        @SerializedName("is_marked") var isMarked: Boolean = false,
        @SerializedName("card_width") val cardWidth: String?,
        @SerializedName("is_current_day") var isCurrentDay: Boolean = false,
    ) : WidgetData()
}
