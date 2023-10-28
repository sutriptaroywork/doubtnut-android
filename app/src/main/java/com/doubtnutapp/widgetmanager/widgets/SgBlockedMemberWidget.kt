package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.SgBlockMember
import com.doubtnutapp.databinding.WidgetSgBlockedMemberBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.loadImage
import com.doubtnutapp.studygroup.model.ConfirmationPopup
import com.doubtnutapp.studygroup.ui.activity.StudyGroupActivity
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.widget_sg_blocked_member.view.*
import javax.inject.Inject

class SgBlockedMemberWidget(context: Context) : BaseBindingWidget<
        SgBlockedMemberWidget.WidgetHolder,
        SgBlockedMemberWidget.Model,
        WidgetSgBlockedMemberBinding>(context), View.OnClickListener  {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null
    var deeplink: String = ""
    private val binding = lazy { getViewBinding() }

    init {
        DoubtnutApp.INSTANCE
            .daggerAppComponent
            .forceUnWrap()
            .inject(this)
    }

    override fun getViewBinding(): WidgetSgBlockedMemberBinding =
        WidgetSgBlockedMemberBinding.inflate(LayoutInflater.from(context), this, true)

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
        deeplink = data.deeplink.orEmpty()
        binding.apply {
            tvUserName.apply {
                text = data.studentName
                setOnClickListener(this@SgBlockedMemberWidget)
            }
            ivUserImage.apply {
                loadImage(data.image)
                setOnClickListener(this@SgBlockedMemberWidget)
            }
                tvTime.text = data.blockedAt
                tvUnblock.apply {
                    text = data.ctaText
                    setOnClickListener {
                        actionPerformer?.performAction(SgBlockMember(
                                studentId = data.studentId,
                                name = data.studentName,
                                confirmationPopup = data.confirmationPopup,
                                roomId = data.chatId,
                                adapterPosition = holder.absoluteAdapterPosition,
                                actionSource = StudyGroupActivity.ActionSource.PERSONAL_CHAT,
                                actionType = StudyGroupActivity.ActionType.UNBLOCK
                        ))
                    }
                }
            }
        return holder
    }

    class WidgetHolder(binding: WidgetSgBlockedMemberBinding, baseWidget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetSgBlockedMemberBinding>(binding, baseWidget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
            @SerializedName("student_id") val studentId: String,
            @SerializedName("chat_id") val chatId: String,
            @SerializedName("student_name") val studentName: String,
            @SerializedName("blocked_at") val blockedAt: String,
            @SerializedName("image") val image: String?,
            @SerializedName("cta_text") val ctaText: String?,
            @SerializedName("deeplink") val deeplink: String?,
            @SerializedName("confirmation_pop_up") val confirmationPopup: ConfirmationPopup?,
    ) : WidgetData()

    override fun onClick(v: View?) {
        when (v){
             widgetViewHolder.binding.tvUserName, widgetViewHolder.binding.ivUserImage -> {
                deeplinkAction.performAction(context, deeplink)
            }
        }
    }

}