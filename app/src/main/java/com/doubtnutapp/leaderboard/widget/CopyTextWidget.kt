package com.doubtnutapp.leaderboard.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.*
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetCopyTextBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.UserUtil
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CopyTextWidget @JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<CopyTextWidget.WidgetHolder, CopyTextWidget.Model,
        WidgetCopyTextBinding>(context) {
    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetCopyTextBinding {
        return WidgetCopyTextBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: Model
    ): WidgetHolder {
        super.bindWidget(holder, model)

        val binding = holder.binding
        binding.root.applyBackgroundTint(model.data.bgStrokeColor)

        binding.tvTitle1.text = model.data.titleOne
        binding.tvTitle1.applyTextColor(model.data.titleOneTextColor)
        binding.tvTitle1.applyTextSize(model.data.titleOneTextSize)

        binding.btnCopy.isVisible = model.data.titleTwo.isNullOrEmpty().not()
        binding.btnCopy.text = model.data.titleTwo
        binding.btnCopy.applyTextColor(model.data.titleTwoTextColor)
        binding.btnCopy.applyTextSize(model.data.titleTwoTextSize)

        binding.root.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CARD_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )

            if (model.data.deeplink.isNullOrEmpty().not()) {
                deeplinkAction.performAction(context, model.data.deeplink)
                if (model.data.toastMessage.isNullOrEmpty().not()) {
                    ToastUtils.makeText(
                        context,
                        model.data.toastMessage.orEmpty(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@setOnClickListener
            }

            context.copy(
                text = model.data.titleOne.orEmpty(),
                toastMessage = model.data.toastMessage
            )
        }
        return holder
    }

    class WidgetHolder(binding: WidgetCopyTextBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCopyTextBinding>(binding, widget)

    companion object {
        const val TAG = "CopyTextWidget"
        const val EVENT_TAG = "copy_text_widget"
    }

    @Keep
    class Model :
        WidgetEntityModel<Data, WidgetAction>()


    @Keep
    data class Data(
        @SerializedName("title_one", alternate = ["title1"])
        val titleOne: String?,
        @SerializedName("title_one_text_size", alternate = ["title1_text_size"])
        val titleOneTextSize: String?,
        @SerializedName("title_one_text_color", alternate = ["title1_text_color"])
        val titleOneTextColor: String?,

        @SerializedName("title_two", alternate = ["title2"])
        val titleTwo: String?,
        @SerializedName("title_two_text_size", alternate = ["title2_text_size"])
        val titleTwoTextSize: String?,
        @SerializedName("title_two_text_color", alternate = ["title2_text_color"])
        val titleTwoTextColor: String?,

        @SerializedName("bg_stroke_color")
        val bgStrokeColor: String?,
        @SerializedName("toast_message")
        val toastMessage: String?,
        @SerializedName("deeplink")
        val deeplink: String?,

        ) : WidgetData()

}


