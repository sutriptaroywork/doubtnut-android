package com.doubtnutapp.course.widgets

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetNextScreenBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.hide
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class NextScreenWidget(context: Context) :
    BaseBindingWidget<NextScreenWidget.NextScreenWidgetHolder,
            NextScreenWidget.NextScreenWidgetModel, WidgetNextScreenBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private var countDownTimer: CountDownTimer? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bindWidget(
        holder: NextScreenWidgetHolder,
        model: NextScreenWidgetModel
    ): NextScreenWidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = WidgetLayoutConfig(
                model.layoutConfig?.marginTop ?: 6,
                model.layoutConfig?.marginBottom ?: 6,
                model.layoutConfig?.marginLeft ?: 0,
                model.layoutConfig?.marginRight ?: 0
            )
        })
        val data: NextScreenWidgetData = model.data
        val binding = holder.binding
        binding.apply {
            if (data.isExpanded == true) {
                mainLayout.setPadding(0, 36f.dpToPx().toInt(), 0, 36f.dpToPx().toInt())
                animationTimer.visibility = VISIBLE
                animationTimer.playAnimation()
                countDownTimer =
                    object : CountDownTimer(System.currentTimeMillis() + 4000, 4000) {
                        override fun onTick(millisUntilFinished: Long) {
                            animationTimer.playAnimation()
                        }

                        override fun onFinish() {

                        }
                    }
                countDownTimer?.start()
            } else {
                animationTimer.visibility = View.GONE
                countDownTimer?.cancel()
            }
            tvTitle.text = data.title.toString()
            tvTitle.setTextColor(Utils.parseColor(data.titleColor.orDefaultValue("#000000")))
            tvTitle.textSize = data.titleSize ?: 18f
            if (data.subtitle.isNullOrEmpty()) {
                tvSubtitle.hide()
            } else {
                tvSubtitle.text = data.subtitle.toString()
                tvSubtitle.setTextColor(Utils.parseColor(data.subtitleColor.orDefaultValue("#000000")))
            }
            tvSubtitle.textSize = data.subtitleSize ?: 14f
            if (data.imageUrl.isNullOrEmpty()) {
                ivEnglish.hide()
            } else {
                ivEnglish.loadImageEtx(data.imageUrl)
            }
            ivArrow.loadImageEtx(data.arrowImageUrl)
            mainLayout.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        model.type + EventConstants.WIDGET_ITEM_CLICK,
                        hashMapOf<String, Any>().apply {
                            putAll(model.extraParams ?: HashMap())
                        }
                    )
                )
                deeplinkAction.performAction(context, data.deeplink.orEmpty())
            }
        }
        return holder
    }


    override fun setupViewHolder() {
        this.widgetViewHolder = NextScreenWidgetHolder(getViewBinding(), this)
    }

    class NextScreenWidgetHolder(
        binding: WidgetNextScreenBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetNextScreenBinding>(binding, widget)

    class NextScreenWidgetModel : WidgetEntityModel<NextScreenWidgetData, WidgetAction>()

    @Keep
    data class NextScreenWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("title_size") val titleSize: Float?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_color") val subtitleColor: String?,
        @SerializedName("subtitle_size") val subtitleSize: Float?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("arrow_image_url") val arrowImageUrl: String?,
        @SerializedName("bg_color") val bgColor: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("is_expanded") val isExpanded: Boolean?,
    ) : WidgetData()

    override fun getViewBinding(): WidgetNextScreenBinding {
        return WidgetNextScreenBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        countDownTimer?.cancel()
    }
}
