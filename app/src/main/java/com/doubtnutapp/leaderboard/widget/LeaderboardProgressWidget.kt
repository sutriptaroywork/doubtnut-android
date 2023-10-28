package com.doubtnutapp.leaderboard.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetLeaderboardProgressBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class LeaderboardProgressWidget @JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<LeaderboardProgressWidget.WidgetHolder, LeaderboardProgressWidgetModel,
        WidgetLeaderboardProgressBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetLeaderboardProgressBinding {
        return WidgetLeaderboardProgressBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: LeaderboardProgressWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)

        val binding = holder.binding
        binding.root.applyBackgroundColor(model.data.background)
        binding.root.elevation = model.data.elevation?.toFloatOrNull() ?: 0f
        binding.root.applyStrokeColor(model.data.bgStrokeColor)
        val padding = (model.data.padding?.toIntOrNull() ?: 0).dpToPx()
        binding.root.setContentPadding(
            padding,
            padding,
            padding,
            padding
        )

        binding.tvTitle.isVisible = model.data.title.isNullOrEmpty().not()
        binding.tvTitle.text = model.data.title
        binding.tvTitle.applyTextColor(model.data.titleTextColor)
        binding.tvTitle.applyTextSize(model.data.titleTextSize)

        binding.tvProgressStart.isVisible = model.data.progressText.isNullOrEmpty().not()
        binding.tvProgressStart.text = model.data.progressText
        binding.tvProgressStart.applyTextColor(model.data.progressTextColor)
        binding.tvProgressStart.applyTextSize(model.data.progressTextSize)

        binding.tvProgressEnd.isVisible = model.data.progressTotalText.isNullOrEmpty().not()
        binding.tvProgressEnd.text = model.data.progressTotalText
        binding.tvProgressEnd.applyTextColor(model.data.progressTotalTextColor)
        binding.tvProgressEnd.applyTextSize(model.data.progressTotalTextSize)

        binding.viewProgressBackground.applyBackgroundColor(model.data.progressBarBackgroundColor)
        binding.viewProgress.applyBackgroundColor(model.data.progressBarColor)

        binding.guidelineProgress.setGuidelinePercent((model.data.progress ?: 0f) / 100)

        return holder
    }

    class WidgetHolder(binding: WidgetLeaderboardProgressBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetLeaderboardProgressBinding>(binding, widget)

    companion object {
        const val TAG = "LeaderboardProgressWidget"
        const val EVENT_TAG = "leaderboard_progress_widget"
    }
}

@Keep
class LeaderboardProgressWidgetModel :
    WidgetEntityModel<LeaderboardProgressWidgetData, WidgetAction>()

@Keep
data class LeaderboardProgressWidgetData(
    @SerializedName("background")
    val background: String?,
    @SerializedName("bg_stroke_color")
    val bgStrokeColor: String?,
    @SerializedName("elevation")
    val elevation: String?,
    @SerializedName("padding")
    val padding: String?,

    @SerializedName("title")
    val title: String?,
    @SerializedName("title_text_size")
    val titleTextSize: String?,
    @SerializedName("title_text_color")
    val titleTextColor: String?,

    @SerializedName("progress_text")
    val progressText: String?,
    @SerializedName("progress_text_size")
    val progressTextSize: String?,
    @SerializedName("progress_text_color")
    val progressTextColor: String?,

    @SerializedName("progress_total_text")
    val progressTotalText: String?,
    @SerializedName("progress_total_text_size")
    val progressTotalTextSize: String?,
    @SerializedName("progress_total_text_color")
    val progressTotalTextColor: String?,

    @SerializedName("progress")
    val progress: Float?,
    @SerializedName("progress_bar_color")
    val progressBarColor: String?,
    @SerializedName("progress_bar_background_color")
    val progressBarBackgroundColor: String?,

    ) : WidgetData()