package com.doubtnutapp.leaderboard.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.TextViewUtils
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetPadhoAurJeetoBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.UserUtil
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class PadhoAurJeetoWidget @JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<PadhoAurJeetoWidget.WidgetHolder, PadhoAurJeetoWidgetModel,
        WidgetPadhoAurJeetoBinding>(context) {
    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetPadhoAurJeetoBinding {
        return WidgetPadhoAurJeetoBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: PadhoAurJeetoWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)

        val binding = holder.binding

        binding.tvTitle.isVisible = model.data.title.isNullOrEmpty().not()
        binding.tvTitle.text = model.data.title
        binding.tvTitle.applyTextColor(model.data.titleTextColor)
        binding.tvTitle.applyTextSize(model.data.titleTextSize)


        binding.tvSubtitle.isVisible = model.data.subtitle.isNullOrEmpty().not()
        TextViewUtils.setTextFromHtml(
            binding.tvSubtitle,
            model.data.subtitle.orEmpty()
        )
        binding.tvSubtitle.applyTextColor(model.data.subtitleTextColor)
        binding.tvSubtitle.applyTextSize(model.data.subtitleTextSize)

        binding.cvMore.isVisible = model.data.moreText.isNullOrEmpty().not()
        binding.tvMore.text = model.data.moreText
        binding.tvMore.applyTextColor(model.data.moreTextColor)
        binding.tvMore.applyTextSize(model.data.moreTextSize)

        binding.cvMore.setOnClickListener {
            deeplinkAction.performAction(context, model.data.moreTextDeeplink)

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.MORE_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                        EventConstants.CTA_TEXT to model.data.moreText.orEmpty(),
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )

        }

        val adapter = WidgetLayoutAdapter(
            context = context,
            source = source
        )

        model.data.widgets?.forEach {
            if (it.extraParams == null) {
                it.extraParams = HashMap()
            }
            it.extraParams?.put(EventConstants.WIDGET_TYPE, TAG)
        }

        binding.rvMain.adapter = adapter
        val updateDropDownUi = {
            if (model.data.isExpanded == true) {
                binding.ivDropDown.animate().rotation(180f)
                adapter.setWidgets(
                    model.data.widgets
                        .orEmpty()
                )
            } else {
                binding.ivDropDown.animate().rotation(0f)
                adapter.setWidgets(
                    model.data.widgets
                        ?.filter { it !is LeaderboardProgressWidgetModel }
                        .orEmpty()
                )
            }
        }
        updateDropDownUi()

        binding.ivDropDown.setOnClickListener {
            model.data.isExpanded = model.data.isExpanded != true
            updateDropDownUi()
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.DROP_DOWN_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }
        return holder
    }

    class WidgetHolder(binding: WidgetPadhoAurJeetoBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetPadhoAurJeetoBinding>(binding, widget)

    companion object {
        const val TAG = "PadhoAurJeetoWidget"
        const val EVENT_TAG = "padho_aur_jeeto_widget"
    }
}

@Keep
class PadhoAurJeetoWidgetModel :
    WidgetEntityModel<PadhoAurJeetoWidgetData, WidgetAction>()

@Keep
data class PadhoAurJeetoWidgetData(
    @SerializedName("title")
    val title: String?,
    @SerializedName("title_text_size")
    val titleTextSize: String?,
    @SerializedName("title_text_color")
    val titleTextColor: String?,

    @SerializedName("subtitle")
    val subtitle: String?,
    @SerializedName("subtitle_text_size")
    val subtitleTextSize: String?,
    @SerializedName("subtitle_text_color")
    val subtitleTextColor: String?,

    @SerializedName("more_text")
    val moreText: String?,
    @SerializedName("more_text_size")
    val moreTextSize: String?,
    @SerializedName("more_text_color")
    val moreTextColor: String?,
    @SerializedName("more_text_deeplink")
    val moreTextDeeplink: String?,

    @SerializedName("widgets")
    val widgets: List<WidgetEntityModel<*, *>>?,

    @SerializedName("assortment_id")
    val assortmentId: String?,

    @SerializedName("is_expanded")
    var isExpanded: Boolean? = false
) : WidgetData()