package com.doubtnutapp.doubtpecharcha.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.doubtpecharcha.ui.viewholder.DoubtP2pHelperAnimationViewHolder
import com.doubtnutapp.doubtpecharcha.ui.viewholder.DoubtP2pHostAnimationViewHolder
import com.doubtnutapp.utils.Utils

/**
 * Created by Sachin Saxena on 2020-04-10.
 */
class DoubtP2pAnimationAdapter(private val isHost: Boolean? = false) :
    RecyclerView.Adapter<BaseViewHolder<String>>() {

    var p2pAnimationList: MutableList<String> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<String> {
        return if (isHost == true) {
            DoubtP2pHostAnimationViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_host_animation, parent, false)
            )
        } else {
            DoubtP2pHelperAnimationViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_helper_animation, parent, false)
            )
        }
    }

    override fun getItemCount() = p2pAnimationList.size

    override fun onBindViewHolder(holder: BaseViewHolder<String>, position: Int) {
        holder.bind(p2pAnimationList[position])
        if (isHost == true) {
            Utils.setWidthBasedOnPercentage(
                holder.itemView.context, holder.itemView, "1.15", R.dimen.spacing_zero
            )
        }
    }

    fun updateData(animationList: List<String>) {
        p2pAnimationList.clear()
        p2pAnimationList.addAll(animationList)
        notifyDataSetChanged()
    }
}
