package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.databinding.WidgetSgPersonalChatBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SgPersonalChatWidget(context: Context) : BaseBindingWidget<
        SgPersonalChatWidget.WidgetHolder,
        SgPersonalChatWidget.Model,
        WidgetSgPersonalChatBinding>(context) {

    var source: String? = null

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE
            .daggerAppComponent
            .forceUnWrap()
            .inject(this)
    }

    override fun getViewBinding(): WidgetSgPersonalChatBinding =
        WidgetSgPersonalChatBinding.inflate(LayoutInflater.from(context), this, true)


    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        model.layoutConfig = WidgetLayoutConfig(
            marginTop = 0,
            marginBottom = 0,
        )
        super.bindWidget(holder, model)

        val data = model.data
        val binding = holder.binding

        with(binding) {
            ivUserImage.loadImage(data.studentImage, R.drawable.ic_default_one_to_one_chat)
            tvUserName.text = data.studentName
            ivMute.isVisible = data.isMute
            val unreadCount = data.unreadCount
            tvCountUnseen.isVisible = unreadCount != null && unreadCount > 0
            tvCountUnseen.text = unreadCount.toString()
            val textColor = if (data.unreadCount != null) Color.BLACK else Color.GRAY
            tvSubtitle.apply {
                text = data.subtitle
                setTextColor(textColor)
            }
            tvTime.isVisible = data.timestamp.isNotNullAndNotEmpty()
            tvTime.text = data.timestamp

            setOnClickListener {
                deeplinkAction.performAction(binding.root.context, data.deeplink)
            }
        }
        return holder
    }


    class WidgetHolder(binding: WidgetSgPersonalChatBinding, baseWidget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetSgPersonalChatBinding>(binding, baseWidget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("other_student_id") val otherStudentId: String,
        @SerializedName("chat_id") val chatId: String,
        @SerializedName("student_name") val studentName: String,
        @SerializedName("student_image") val studentImage: String?,
        @SerializedName("left_at") val leftAt: String?,
        @SerializedName("blocked_at") val blockedAt: String?,
        @SerializedName("unread_count") var unreadCount: Int?,
        @SerializedName("timestamp") val timestamp: String?,
        @SerializedName("last_sent_time") var lastSentTime: Long?,
        @SerializedName("is_faq") val isFaq: Boolean?,
        @SerializedName("is_mute") val isMute: Boolean,
        @SerializedName("toast_message") val toastMessage: String?,
        @SerializedName("is_active") val isActive: Int?,
        @SerializedName("cta_text") val ctaText: String?,
        @SerializedName("subtitle") var subtitle: String?,
        @SerializedName("deeplink") val deeplink: String?,
    ) : WidgetData()
}