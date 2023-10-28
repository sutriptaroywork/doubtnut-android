package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.databinding.WidgetInviteFriendsBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class InviteFriendWidget(context: Context) : BaseBindingWidget<InviteFriendWidget.WidgetHolder,
        InviteFriendWidget.InviteFriendWidgetModel, WidgetInviteFriendsBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetInviteFriendsBinding {
        return WidgetInviteFriendsBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: InviteFriendWidgetModel): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = WidgetLayoutConfig(
                model.layoutConfig?.marginTop ?: 16,
                model.layoutConfig?.marginBottom ?: 0,
                model.layoutConfig?.marginLeft ?: 0,
                model.layoutConfig?.marginRight ?: 0
            )
        })
        val binding = holder.binding
        val data = model.data
        binding.tvShare.text = model.data.title.orEmpty()
        if (data.imageUrl.isNotNullAndNotEmpty()) {
            binding.ivShare.loadImageEtx(data.imageUrl.orEmpty())
        }
        binding.mainLayout.setOnClickListener {
            deeplinkAction.performAction(context, data.deeplink.orEmpty())
        }
        return holder
    }

    class WidgetHolder(binding: WidgetInviteFriendsBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetInviteFriendsBinding>(binding, widget)

    class InviteFriendWidgetModel : WidgetEntityModel<InviteFriendWidgetData, WidgetAction>()

    @Keep
    data class InviteFriendWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("share_icon_url") val imageUrl: String?,
        @SerializedName("deeplink") val deeplink: String?
    ) : WidgetData()

}