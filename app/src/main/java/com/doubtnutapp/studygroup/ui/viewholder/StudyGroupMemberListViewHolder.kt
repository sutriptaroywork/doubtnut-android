package com.doubtnutapp.studygroup.ui.viewholder

import android.view.View
import androidx.core.content.ContextCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.SgBlockMember
import com.doubtnutapp.base.SgMemberLongPress
import com.doubtnutapp.base.SgReportMember
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.ItemStudyGroupMembersBinding
import com.doubtnutapp.studygroup.model.GroupMember
import com.doubtnutapp.studygroup.model.StudyGroupMemberType
import javax.inject.Inject


class StudyGroupMemberListViewHolder(
        itemView: View,
        private val userStatus: Int? = 0,
) : BaseViewHolder<GroupMember>(itemView) {

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var userPreference: UserPreference

    private val binding = ItemStudyGroupMembersBinding.bind(itemView)
    private val context by lazy { binding.root.context }

    override fun bind(data: GroupMember) {
        binding.apply {
            ivGroupMember.loadImage(data.image, R.drawable.ic_default_one_to_one_chat)
            val memberName = if (data.studentId == userPreference.getUserStudentId())
                context.resources.getString(R.string.you)
            else
                data.name
            tvGroupMemberName.text = memberName
            tvIsAdmin.apply {
                show()
                when (data.isAdmin) {
                    StudyGroupMemberType.Admin.type -> {
                        text = context.resources.getString(R.string.sg_group_admin)
                        setTextColor(ContextCompat.getColor(context, R.color.violet_541389))
                    }
                    StudyGroupMemberType.SubAdmin.type -> {
                        text = context.resources.getString(R.string.sg_sub_admin)
                        setTextColor(ContextCompat.getColor(context, R.color.violet_541389))
                    }
                    else -> {
                        when (userStatus) {
                            StudyGroupMemberType.Admin.type, StudyGroupMemberType.SubAdmin.type -> {
                                show()
                                text = context.resources.getString(R.string.sg_action_block)
                                setTextColor(ContextCompat.getColor(context,
                                        R.color.tomato))
                                setOnClickListener {
                                    if (data.isAdmin != 1) {
                                        actionPerformer?.performAction(SgBlockMember(
                                                studentId = data.studentId,
                                                name = data.name,
                                                confirmationPopup = null,
                                                adapterPosition = absoluteAdapterPosition
                                        ))
                                    }
                                }
                            }
                            else -> {
                                if (data.studentId != userPreference.getUserStudentId()) {
                                    show()
                                    text =
                                            binding.root.context.resources.getString(R.string.report_sg)
                                    setTextColor(ContextCompat.getColor(binding.root.context,
                                            R.color.tomato))
                                    setOnClickListener {
                                        if (data.isAdmin != 1) {
                                            actionPerformer?.performAction(SgReportMember(
                                                    data.studentId,
                                                    data.name.orEmpty()))
                                        }
                                    }
                                } else {
                                    hide()
                                }
                            }
                        }
                    }
                }
            }
            root.setOnClickListener {
                openStudentProfile(data.studentId)
            }

            binding.root.setOnLongClickListener {
                actionPerformer?.performAction(SgMemberLongPress(data.isAdmin, data.studentId, binding.tvGroupMemberName, layoutPosition))
                true
            }
        }
    }

    private fun openStudentProfile(studentId: String) {

        analyticsPublisher.publishEvent(
                AnalyticsEvent(
                        EventConstants.SG_VIEW_PROFILE_CLICKED,
                        hashMapOf()
                )
        )

        FragmentWrapperActivity.userProfile(
                binding.root.context,
                studentId, "study_group"
        )
    }
}