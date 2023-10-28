package com.doubtnutapp.topicboostergame2.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.SgFriendSelected
import com.doubtnutapp.base.TbgInviteeSelected
import com.doubtnutapp.data.remote.models.topicboostergame2.Friend
import com.doubtnutapp.databinding.ItemTopicBoosterGameInviteFriendBinding
import com.doubtnutapp.hide
import com.doubtnutapp.loadImage
import com.doubtnutapp.show
import com.doubtnutapp.studygroup.ui.activity.StudyGroupActivity
import com.doubtnutapp.studygroup.ui.fragment.SgSelectFriendFragment

/**
 * Created by devansh on 22/06/21.
 */

class InviteFriendViewHolder(itemView: View,val source: String? = null) : BaseViewHolder<Friend>(itemView) {

    private val binding = ItemTopicBoosterGameInviteFriendBinding.bind(itemView)

    override fun bind(data: Friend) {
        with(binding) {
            tvName.text = data.name
            inviteeContainer.setOnClickListener {
                setCheckboxState(!data.isSelected,data)
            }

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                data.isSelected = isChecked
                performAction(TbgInviteeSelected(isChecked, data.studentId))
            }
            checkbox.isChecked = data.isSelected

            if (source == SgSelectFriendFragment.TAG) {
                checkbox.hide()
                ivUserImage.loadImage(data.image)
                ivUserImage.show()
                root.setOnClickListener {
                    performAction(SgFriendSelected(data))
                }
            }
        }
    }

    private fun setCheckboxState(state: Boolean, data: Friend){
        data.isSelected = state
        binding.checkbox.isChecked = state
        performAction(TbgInviteeSelected(state, data.studentId))
    }
}