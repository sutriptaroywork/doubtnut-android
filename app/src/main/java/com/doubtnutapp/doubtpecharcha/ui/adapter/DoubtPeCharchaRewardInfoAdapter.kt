package com.doubtnutapp.doubtpecharcha.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.databinding.ItemRewardInfoBinding
import com.doubtnutapp.loadImage

class DoubtPeCharchaRewardInfoAdapter(val items: ArrayList<String>, val url: String) :
    RecyclerView.Adapter<DoubtPeCharchaRewardInfoAdapter.DoubtPeCharchaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoubtPeCharchaViewHolder {
        val binding =
            ItemRewardInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DoubtPeCharchaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DoubtPeCharchaViewHolder, position: Int) {
        holder.binding.tvTitle.text = items[position]
        holder.binding.imageViewTick.loadImage(url)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class DoubtPeCharchaViewHolder(val binding: ItemRewardInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}