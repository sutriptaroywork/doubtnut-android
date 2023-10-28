package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.ConnectToPeer
import com.doubtnutapp.databinding.WidgetMatchPageBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.isValidColorCode
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class MatchPageWidget(context: Context) : BaseBindingWidget<
    MatchPageWidget.WidgetHolder,
    MatchPageWidget.Model,
    WidgetMatchPageBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.forceUnWrap()?.inject(this)
    }

    override fun getViewBinding(): WidgetMatchPageBinding =
        WidgetMatchPageBinding.inflate(LayoutInflater.from(context), this, true)

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        model.layoutConfig = if (model.layoutConfig == null)
            WidgetLayoutConfig(
                marginTop = 21,
                marginLeft = 0,
                marginRight = 0,
                marginBottom = 21,
            ) else model.layoutConfig

        super.bindWidget(holder, model)

        val binding = holder.binding
        val data = model.data

        with(binding) {
            if (data.backgroundColor.isValidColorCode()) {
                root.setBackgroundColor(Utils.parseColor(data.backgroundColor, Color.YELLOW))
            }
            tvTitle.apply {
                text = data.title
                if (data.titleColor.isValidColorCode()) {
                    setTextColor(Utils.parseColor(data.titleColor, Color.BLACK))
                }
                isVisible = data.title.isNotNullAndNotEmpty()
            }

            tvSubtitle.apply {
                text = data.subtitle
                if (data.subtitleColor.isValidColorCode()) {
                    setTextColor(Utils.parseColor(data.subtitleColor, Color.BLACK))
                }
                isVisible = data.subtitle.isNotNullAndNotEmpty()
            }

            buttonConnectNow.apply {
                text = data.cta
                setOnClickListener {
                    if (data.deeplink.isNotNullAndNotEmpty()) {
                        deeplinkAction.performAction(context, data.deeplink)
                    } else {
                        actionPerformer?.performAction(ConnectToPeer())
                    }
                }
                isVisible = data.cta.isNotNullAndNotEmpty()
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetMatchPageBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetMatchPageBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String,
        @SerializedName("title_color") val titleColor: String,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_color") val subtitleColor: String?,
        @SerializedName("cta") val cta: String?,
        @SerializedName("background_color") val backgroundColor: String?,
    ) : WidgetData()
}
