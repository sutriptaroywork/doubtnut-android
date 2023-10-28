package com.doubtnutapp.gamification.otheruserprofile.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.databinding.ItemOtherUserActivityBinding
import com.doubtnutapp.gamification.otheruserprofile.model.OtherUserStatsDataModel


class OtherUserStatsViewHolder(var binding: ItemOtherUserActivityBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: OtherUserStatsDataModel) {
        binding.otherStat = data
        binding.executePendingBindings()
    }

}