package com.doubtnutapp.ui.answer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ItemDislikeOptionBinding


class DislikeFeedbackAdapter : RecyclerView.Adapter<DislikeFeedbackAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return options?.size ?: 0

    }

    var options: ArrayList<String> = arrayListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate<ItemDislikeOptionBinding>(LayoutInflater.from(parent.context),
                R.layout.item_dislike_option, parent, false))
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(options!![position])
    }

    class ViewHolder constructor(var binding: ItemDislikeOptionBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(option: String) {
            binding.optionss = option
            binding.executePendingBindings()
        }

    }


    fun updateData(options: ArrayList<String>) {
        this.options = options
        notifyDataSetChanged()
    }

}