package com.doubtnutapp.gamification.otheruserprofile.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.gamification.otheruserprofile.model.OthersUserProfileDataModel

class AchievedBadgesViewHolder(itemView: View, requiredWidth: Int) : BaseViewHolder<OthersUserProfileDataModel>(itemView) {

    init {
        itemView.layoutParams.width = requiredWidth
    }

    override fun bind(data: OthersUserProfileDataModel) {
    }
}