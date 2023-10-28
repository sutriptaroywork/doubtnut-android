package com.doubtnutapp.gamification.gamepoints.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.gamification.gamepoints.model.ActionConfigDataItemDataModel
import com.doubtnutapp.gamification.gamepoints.ui.viewholder.GamePointsViewHolder

class GamePointsListAdapter(
    private val actionPerformer: ActionPerformer,
    private val actionsList: List<ActionConfigDataItemDataModel>
) : RecyclerView.Adapter<GamePointsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GamePointsViewHolder {
        return GamePointsViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_gamification_view_points_info,
                parent,
                false
            )
        ).also {
            it.actionPerformer = actionPerformer
        }

    }

    override fun getItemCount() = actionsList.size

    override fun onBindViewHolder(holder: GamePointsViewHolder, position: Int) {
        holder.bind(actionsList[position])
    }
}