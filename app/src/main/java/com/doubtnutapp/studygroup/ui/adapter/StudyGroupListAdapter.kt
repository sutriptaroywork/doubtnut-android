package com.doubtnutapp.studygroup.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.studygroup.model.StudyGroup
import com.doubtnutapp.studygroup.ui.viewholder.StudyGroupListViewHolder

class StudyGroupListAdapter(
    private val actionPerformer: ActionPerformer,
    private val toInvite: Boolean? = false,
    private val isScreenOpenToSelectGroups: Boolean? = false
) : RecyclerView.Adapter<BaseViewHolder<StudyGroup>>() {

    private val groupList = mutableListOf<StudyGroup>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<StudyGroup> {
        return StudyGroupListViewHolder(
            itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_study_group_list, parent, false),
            toInvite = toInvite, isScreenOpenToSelectGroups = isScreenOpenToSelectGroups
        ).apply {
            actionPerformer = this@StudyGroupListAdapter.actionPerformer
        }
    }

    override fun getItemCount(): Int = groupList.size

    override fun onBindViewHolder(holder: BaseViewHolder<StudyGroup>, position: Int) {
        holder.bind(groupList[position])
    }

    fun updateGroupList(groupList: List<StudyGroup>) {
        this.groupList.clear()
        this.groupList.addAll(groupList)
        notifyItemRangeInserted(0, groupList.size)
    }

    fun updateGroupAtPosition(group: StudyGroup, position: Int) {
        this.groupList[position] = group
        notifyItemChanged(position)
    }
}