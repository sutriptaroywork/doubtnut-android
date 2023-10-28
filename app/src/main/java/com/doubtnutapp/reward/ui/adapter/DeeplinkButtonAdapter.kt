package com.doubtnutapp.reward.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.data.remote.models.reward.DeeplinkButton
import com.doubtnutapp.reward.ui.viewholder.BottomDeeplinkViewHolder

class DeeplinkButtonAdapter(private val actionsPerformer: ActionPerformer?)
    : RecyclerView.Adapter<BottomDeeplinkViewHolder>() {

    private val buttons = mutableListOf<DeeplinkButton>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomDeeplinkViewHolder {
        return BottomDeeplinkViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_bottom_deeplinks, parent, false)
        ).apply { actionPerformer = actionsPerformer }
    }

    override fun onBindViewHolder(holder: BottomDeeplinkViewHolder, position: Int) {
        holder.bind(buttons[position])
    }

    override fun getItemCount(): Int = buttons.size

    fun updateList(buttons: List<DeeplinkButton>) {
        this.buttons.clear()
        this.buttons.addAll(buttons)
        notifyDataSetChanged()
    }
}