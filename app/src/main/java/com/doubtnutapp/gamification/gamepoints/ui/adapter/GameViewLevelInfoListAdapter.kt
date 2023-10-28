package com.doubtnutapp.gamification.gamepoints.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.gamification.gamepoints.model.ViewLevelInfoItemDataModel
import com.doubtnutapp.gamification.gamepoints.ui.viewholder.GameViewLevelInfoHolder

class GameViewLevelInfoListAdapter(private val milestones: List<ViewLevelInfoItemDataModel>) : RecyclerView.Adapter<GameViewLevelInfoHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewLevelInfoHolder {
        milestones.reversed()
        return GameViewLevelInfoHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_gamification_view_level_info, parent, false)
        )
    }

    override fun getItemCount() = milestones.size

    override fun onBindViewHolder(holder: GameViewLevelInfoHolder, position: Int) {
        holder.bind(milestones[position])
    }
}