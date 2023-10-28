package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetTfsAnalysisBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TfsAnalysisWidget(context: Context) : BaseBindingWidget<TfsAnalysisWidget.WidgetViewHolder,
    TfsAnalysisWidget.TfsAnalysisWidgetModel, WidgetTfsAnalysisBinding>(context) {

    companion object {
        const val TAG = "TfsAnalysisWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetTfsAnalysisBinding {
        return WidgetTfsAnalysisBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: TfsAnalysisWidgetModel
    ): WidgetViewHolder {
        super.bindWidget(holder, model)

        val binding = holder.binding
        val data: TfsAnalysisWidgetData = model.data

        binding.ivPlay.loadImageEtx(data.iconUrl.orEmpty())
        binding.tvtTitle.text = data.title.orEmpty()
        binding.tvSubtitle.text = data.subtitle.orEmpty()

        binding.tvTagOne.text = data.tagOne.orEmpty()
        binding.tvTagOne.isVisible = !data.tagOne.isNullOrBlank()
        binding.tvTagOne.background = Utils.getShape(
            colorString = data.tagOneColor.orEmpty(),
            strokeColor = data.tagOneColor.orEmpty()
        )

        binding.tvTagTwo.text = data.tagTwo.orEmpty()
        binding.tvTagTwo.isVisible = !data.tagTwo.isNullOrBlank()
        binding.tvTagTwo.background = Utils.getShape(
            colorString = data.tagTwoColor.orEmpty(),
            strokeColor = data.tagTwoColor.orEmpty()
        )

        binding.tvTagThree.text = data.tagThree.orEmpty()
        binding.tvTagThree.isVisible = !data.tagThree.isNullOrBlank()
        binding.tvTagThree.background = Utils.getShape(
            colorString = data.tagThreeColor.orEmpty(),
            strokeColor = data.tagThreeColor.orEmpty()
        )

        binding.root.setOnClickListener {
            deeplinkAction.performAction(holder.itemView.context, data.deeplink.orEmpty())
        }

        binding.ivPlay.setOnClickListener {
            deeplinkAction.performAction(holder.itemView.context, data.deeplinkIcon.orEmpty())
        }
        return holder
    }

    class WidgetViewHolder(binding: WidgetTfsAnalysisBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTfsAnalysisBinding>(binding, widget)

    class TfsAnalysisWidgetModel : WidgetEntityModel<TfsAnalysisWidgetData, WidgetAction>()
}

@Keep
data class TfsAnalysisWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("tag_one") val tagOne: String?,
    @SerializedName("tag_two") val tagTwo: String?,
    @SerializedName("tag_three") val tagThree: String?,
    @SerializedName("tag_one_color") val tagOneColor: String?,
    @SerializedName("tag_two_color") val tagTwoColor: String?,
    @SerializedName("tag_three_color") val tagThreeColor: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("deeplink_icon") val deeplinkIcon: String?
) : WidgetData()
