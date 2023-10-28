package com.doubtnutapp.studygroup.ui.viewholder

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.StudyGroupClick
import com.doubtnutapp.databinding.ItemStudyGroupListBinding
import com.doubtnutapp.hide
import com.doubtnutapp.loadImage
import com.doubtnutapp.show
import com.doubtnutapp.studygroup.model.StudyGroup

class StudyGroupListViewHolder(
    itemView: View,
    val toInvite: Boolean? = false,
    val isScreenOpenToSelectGroups: Boolean? = false
) :
    BaseViewHolder<StudyGroup>(itemView) {

    private val binding = ItemStudyGroupListBinding.bind(itemView)

    override fun bind(data: StudyGroup) {

        binding.apply {

            groupImage.loadImage(data.groupImage)
            tvGroupName.text = data.groupName
            tvMyGroup.isVisible = data.isAdmin == 1

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

            data.subtitle?.let {
                tvLastSent.show()
                tvLastSent.text = it
            } ?: tvLastSent.hide()

            when (isScreenOpenToSelectGroups) {
                true -> {
                    tvInvitationStatus.hide()
                    ivNotification.hide()
                    when (data.isSelected) {
                        true -> {
                            binding.ivSelectGroupTick.show()
                            binding.rootLayout.setBackgroundColor(
                                ContextCompat.getColor(
                                    binding.root.context,
                                    R.color.grey200
                                )
                            )
                        }
                        else -> {
                            binding.ivSelectGroupTick.hide()
                            binding.rootLayout.setBackgroundColor(
                                ContextCompat.getColor(
                                    binding.root.context,
                                    R.color.white
                                )
                            )
                        }
                    }
                }
                else -> {
                    ivSelectGroupTick.hide()
                    tvInvitationStatus.apply {
                        // invitation status -> 0 - (Invite), 1 - (Invite Sent), 2 - (Member)
                        when (toInvite) {
                            true -> {
                                tvInvitationStatus.show()
                                when (data.invitationStatus) {
                                    0 -> {
                                        text = context.resources.getString(R.string.invite)
                                        setTextColor(ContextCompat.getColor(context, R.color.white))
                                        setBackgroundColor(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.tomato
                                            )
                                        )
                                    }
                                    1 -> {
                                        text = context.resources.getString(R.string.invite_sent)
                                        setTextColor(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.green_56bd5b
                                            )
                                        )
                                        setBackgroundColor(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.white
                                            )
                                        )
                                    }
                                    2 -> {
                                        text = context.resources.getString(R.string.member)
                                        setTextColor(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.warm_grey
                                            )
                                        )
                                        setBackgroundColor(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.white
                                            )
                                        )
                                    }
                                }
                                // set visibility of notification icon
                                ivNotification.hide()
                            }
                            else -> {
                                hide()
                                // set visibility of notification icon
                                ivNotification.isVisible = data.isMute == true
                            }
                        }
                    }
                }
            }

            root.setOnClickListener {
                when (toInvite) {
                    true -> {
                        if (data.invitationStatus != 0) return@setOnClickListener
                        handleClick(data)
                    }
                    else -> {
                        handleClick(data)
                    }
                }
            }
        }
    }

    private fun handleClick(data: StudyGroup) {
        actionPerformer?.performAction(
            StudyGroupClick(
                position = absoluteAdapterPosition,
                data = data
            )
        )
    }
}