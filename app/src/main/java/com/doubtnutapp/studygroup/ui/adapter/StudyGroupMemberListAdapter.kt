package com.doubtnutapp.studygroup.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.studygroup.model.GroupMember
import com.doubtnutapp.studygroup.ui.viewholder.StudyGroupMemberListViewHolder

class StudyGroupMemberListAdapter(
    private val actionPerformer: ActionPerformer,
    private val userStatus: Int
) : RecyclerView.Adapter<BaseViewHolder<GroupMember>>() {

    val memberList = mutableListOf<GroupMember>()
    var lastItemPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<GroupMember> {
        return StudyGroupMemberListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_study_group_members, parent, false), userStatus
        ).apply {
            actionPerformer = this@StudyGroupMemberListAdapter.actionPerformer
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<GroupMember>, position: Int) {
        holder.bind(memberList[position])
    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    fun addGroupMembers(groupMemberList: List<GroupMember>) {
        if (groupMemberList.isEmpty()) return
        val startPosition = if (lastItemPosition == RecyclerView.NO_POSITION) 0 else lastItemPosition
        this.memberList.addAll(groupMemberList)
        notifyItemRangeInserted(startPosition, groupMemberList.size)
        lastItemPosition = memberList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateGroupMembers(groupMemberList: List<GroupMember>) {
        this.memberList.clear()
        this.memberList.addAll(groupMemberList)
        notifyDataSetChanged()
        lastItemPosition = memberList.size
    }

    fun removeAt(position: Int) {
        this.memberList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateItemAtPosition(position: Int, adminStatus: Int) {
        memberList[position].isAdmin = adminStatus
        notifyItemChanged(position)
    }
}