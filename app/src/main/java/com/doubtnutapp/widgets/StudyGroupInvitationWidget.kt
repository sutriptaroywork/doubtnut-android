package com.doubtnutapp.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp

import com.doubtnutapp.base.CopyLinkToClipBoard
import com.doubtnutapp.base.ShareInviteLink
import com.doubtnutapp.databinding.WidgetStudyGroupInvitationBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class StudyGroupInvitationWidget(context: Context) :
    BaseBindingWidget<StudyGroupInvitationWidget.WidgetHolder,
            StudyGroupInvitationWidget.Model, WidgetStudyGroupInvitationBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetStudyGroupInvitationBinding {
        return WidgetStudyGroupInvitationBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model)

        val data = model.data
        val binding = holder.binding
        binding.tvInvitationMessage.text = data.title

        binding.tvInvitationLink.apply {
            text = data.invitationLink
            setTextColor(Color.BLUE)
        }

        binding.tvActionInvite.isVisible = data.invitationLink.isNullOrEmpty().not()
        binding.tvActionCopyLink.isVisible = data.invitationLink.isNullOrEmpty().not()
        binding.tvInvitationLink.isVisible = data.invitationLink.isNullOrEmpty().not()

        binding.tvActionCopyLink.setOnClickListener {
            actionPerformer?.performAction(CopyLinkToClipBoard(data.invitationLink))
        }

        binding.tvActionInvite.setOnClickListener {
            actionPerformer?.performAction(ShareInviteLink())
        }

        return holder
    }

    class WidgetHolder(binding: WidgetStudyGroupInvitationBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetStudyGroupInvitationBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("link") val invitationLink: String?
    ) : WidgetData()

}