package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.base.StudyGroupClickToShare
import com.doubtnutapp.databinding.WidgetSgGroupChatBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.studygroup.ui.fragment.SgMyGroupsFragment
import com.doubtnutapp.utils.showToast
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SgGroupChatWidget(context: Context) : BaseBindingWidget<
        SgGroupChatWidget.WidgetHolder,
        SgGroupChatWidget.Model,
        WidgetSgGroupChatBinding>(context) {

    var source: String? = null

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE
            .daggerAppComponent
            .forceUnWrap()
            .inject(this)
    }

    override fun getViewBinding(): WidgetSgGroupChatBinding =
        WidgetSgGroupChatBinding.inflate(LayoutInflater.from(context), this, true)


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
            groupImage.loadImage(data.groupImage, R.drawable.ic_default_group_chat)
            tvGroupName.text = data.groupName
            if (data.isVerified == true) {
                tvGroupName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ContextCompat.getDrawable(root.context, R.drawable.ic_verified_group),
                    null
                )
            } else {
                tvGroupName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }

            val unreadCount = data.unreadCount
            tvCountUnseen.isVisible = unreadCount != null && unreadCount > 0
            tvCountUnseen.text = unreadCount.toString()

            if (data.subtitle.isNotNullAndNotEmpty()) {
                tvSubtitle1.apply {
                    text = data.subtitle
                    setTextColor(Color.GRAY)
                }
                tvSubtitle2.hide()
            } else if (data.lastSentMessageContainer != null) {
                val lastMessageInfo = data.lastSentMessageContainer
                if (lastMessageInfo.senderName.orEmpty().length > 15) {
                    tvSubtitle1.text =
                        lastMessageInfo.senderName.orEmpty().substring(0, 14).plus(".. : ")
                } else {
                    tvSubtitle1.text = lastMessageInfo.senderName.plus(" : ")
                }
                tvSubtitle2.text = lastMessageInfo.message.orDefaultValue()

                val textColor =
                    if (data.unreadCount?.toString()
                            .isNotNullAndNotEmpty()
                    ) Color.BLACK else Color.GRAY
                tvSubtitle2.setTextColor(textColor)
                tvSubtitle1.setTextColor(textColor)
                tvSubtitle2.show()
            } else if (data.groupMemberInfo != null) {
                val groupInfo = data.groupMemberInfo
                tvSubtitle1.apply {
                    text = groupInfo.membersCount
                    setTextColor(Color.GRAY)
                }
                tvSubtitle2.apply {
                    text = groupInfo.lastActive
                    setTextColor(Color.BLACK)
                    show()
                }
            }

            ivMute.isVisible = data.isMute == true
            tvIsAdmin.isVisible = data.isAdmin == 1
            tvTime.isVisible = data.timestamp.isNotNullAndNotEmpty()
            tvTime.text = data.timestamp

            holder.setSelectionStatus(data.isSelected, binding)
            when (source) {
                SgMyGroupsFragment.SHARE -> {
                    setOnClickListener {
                        data.isSelected = data.isSelected.not()
                        actionPerformer?.performAction(
                            StudyGroupClickToShare(
                                position = holder.bindingAdapterPosition,
                                groupId = data.groupId,
                                isSelected = data.isSelected
                            )
                        )
                    }
                }
                else -> {
                    ivSelectGroupTick.hide()
                    setOnClickListener {
                        if (data.isActive == 1 || data.isFaq == true) {
                            deeplinkAction.performAction(binding.root.context, data.deeplink)
                        } else {
                            showToast(binding.root.context, data.toastMessage.orEmpty())
                        }
                    }
                }
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetSgGroupChatBinding, baseWidget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetSgGroupChatBinding>(binding, baseWidget) {
        override fun bindItemPayload(payload: Any) {
            if (payload is Boolean) {
                setSelectionStatus(payload, binding)
            }
        }

        fun setSelectionStatus(isSelected: Boolean, binding: WidgetSgGroupChatBinding) {
            when (isSelected) {
                true -> {
                    binding.apply {
                        ivSelectGroupTick.show()
                        rootLayout.setBackgroundColor(
                            ContextCompat.getColor(
                                root.context,
                                R.color.grey200
                            )
                        )
                    }
                }
                else -> {
                    binding.apply {
                        ivSelectGroupTick.hide()
                        rootLayout.setBackgroundColor(
                            ContextCompat.getColor(
                                root.context,
                                R.color.white
                            )
                        )
                    }
                }
            }
        }
    }

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("group_id") val groupId: String,
        @SerializedName("group_name") val groupName: String,
        @SerializedName("is_admin") val isAdmin: Int,
        @SerializedName("group_type") val groupType: Int?,
        @SerializedName("group_image") val groupImage: String?,
        @SerializedName("left_at") val leftAt: String?,
        @SerializedName("blocked_at") val blockedAt: String?,
        @SerializedName("unread_count") val unreadCount: Int?,
        @SerializedName("timestamp") val timestamp: String?,
        @SerializedName("last_message_sent_at") val lastMessageSentAt: String?,
        @SerializedName("last_sent_time") val lastSentTime: Long?,
        @SerializedName("is_faq") val isFaq: Boolean?,
        @SerializedName("is_mute") val isMute: Boolean?,
        @SerializedName("toast_message") val toastMessage: String?,
        @SerializedName("is_active") val isActive: Int?,
        @SerializedName("invitation_status") val invitationStatus: Int?,
        @SerializedName("cta_text") val ctaText: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("last_sent_message_container") val lastSentMessageContainer: LastMessageInfo?,
        @SerializedName("group_member_info") val groupMemberInfo: GroupMemberInfo?,
        @SerializedName("is_verified") val isVerified: Boolean? = false,
        @SerializedName("is_selected") var isSelected: Boolean = false,
    ) : WidgetData()

    @Keep
    data class LastMessageInfo(
        @SerializedName("message") val message: String?,
        @SerializedName("sender_name") val senderName: String?,
        @SerializedName("sender_id") val senderId: String?,
    )

    @Keep
    data class GroupMemberInfo(
        @SerializedName("members_count") val membersCount: String?,
        @SerializedName("last_active") val lastActive: String?,
    )
}