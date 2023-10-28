package com.doubtnutapp.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.*
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetChannelBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.UserUtil
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ChannelWidget(
        context: Context
) : BaseBindingWidget<ChannelWidget.WidgetHolder, ChannelWidget.Model, WidgetChannelBinding>(
        context
) {
    
    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher
    
    @Inject
    lateinit var deeplinkAction: DeeplinkAction
    
    var source: String? = null
    
    override fun getViewBinding(): WidgetChannelBinding {
        return WidgetChannelBinding
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
        
        binding.cvMain.applyBackgroundTint(model.data.backgroundColor, Color.WHITE)
        
        binding.ivIcon.loadImage(model.data.icon)
        binding.cvIcon.isVisible = model.data.icon.isNullOrEmpty().not()
        
        binding.tvTitle.isVisible = model.data.titleOne.isNullOrEmpty().not()
        binding.tvTitle.text = model.data.titleOne
        binding.tvTitle.applyTextColor(model.data.titleOneTextColor)
        binding.tvTitle.applyTextSize(model.data.titleOneTextSize)
        
        binding.btnAction.isVisible = model.data.actionText.isNullOrEmpty().not()
        binding.btnAction.text = model.data.actionText
        binding.btnAction.applyTextColor(model.data.actionTextColor)
        binding.btnAction.applyTextSize(model.data.actionTextSize)
        binding.btnAction.applyBackgroundColor(model.data.actionBackgroundColor)
        
        binding.ivImage.loadImage(model.data.imageUrl)
        binding.ivImage.isVisible = model.data.imageUrl.isNullOrEmpty().not()
        
        if (model.data.deeplink.isNullOrEmpty()) {
            binding.cvMain.applyRippleColor("#00000000")
        } else {
            binding.cvMain.rippleColor = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.grey_light)
            )
        }
        
        binding.cvMain.setOnClickListener {
            deeplinkAction.performAction(context, model.data.deeplink)
            analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                            "${EVENT_TAG}_${EventConstants.CARD_CLICKED}",
                            
                            hashMapOf<String, Any>(
                                    EventConstants.WIDGET to TAG,
                                    EventConstants.WIDGET_TITLE to model.data.titleOne.orEmpty(),
                                    EventConstants.STUDENT_ID to UserUtil.getStudentId()
                            ).apply {
                                putAll(model.extraParams.orEmpty())
                            }
                    )
            )
        }
        
        binding.btnAction.setOnClickListener {
            deeplinkAction.performAction(context, model.data.actionDeepLink)
            analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                            "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                            
                            hashMapOf<String, Any>(
                                    EventConstants.WIDGET to TAG,
                                    EventConstants.CTA_TEXT to model.data.actionText.orEmpty(),
                                    EventConstants.STUDENT_ID to UserUtil.getStudentId()
                            ).apply {
                                putAll(model.extraParams.orEmpty())
                            }
                    )
            )
        }
        
        return holder
    }
    
    class WidgetHolder(binding: WidgetChannelBinding, widget: BaseWidget<*, *>) :
            WidgetBindingVH<WidgetChannelBinding>(binding, widget)
    
    @Keep
    class Model :
            WidgetEntityModel<Data, WidgetAction>()
    
    @Keep
    data class Data(
            @SerializedName("icon")
            val icon: String?,
            
            @SerializedName("title_one", alternate = ["title1"])
            val titleOne: String?,
            @SerializedName("title_one_text_size", alternate = ["title1_text_size"])
            val titleOneTextSize: String?,
            @SerializedName("title_one_text_color", alternate = ["title1_text_color"])
            val titleOneTextColor: String?,
            
            @SerializedName("action_text")
            val actionText: String?,
            @SerializedName("action_text_size")
            val actionTextSize: String?,
            @SerializedName("action_text_color")
            val actionTextColor: String?,
            @SerializedName("action_deep_link", alternate = ["action_deeplink"])
            val actionDeepLink: String?,
            
            @SerializedName("image_url")
            val imageUrl: String?,
            
            @SerializedName("deeplink", alternate = ["deep_link"])
            val deeplink: String?,
            
            @SerializedName("background_color")
            var backgroundColor: String?,
            @SerializedName("action_background_color")
            var actionBackgroundColor: String?,
            
            ) : WidgetData()
    
    
    companion object {
        const val TAG = "ChannelWidget"
        const val EVENT_TAG = "channel_widget"
    }
}
