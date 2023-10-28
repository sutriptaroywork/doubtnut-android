package com.doubtnutapp.dictionary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemWordBinding

class WordStringAdapter(
    private val list: ArrayList<String> = arrayListOf()
) :
    RecyclerView.Adapter<WordStringAdapter.WordStringViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WordStringViewHolder {
        return WordStringViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_word, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: WordStringViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateData(wordDetails: List<String>) {
        list.clear()
        list.addAll(wordDetails)
        notifyDataSetChanged()
    }

    class WordStringViewHolder(val binding: ItemWordBinding) :
        BaseViewHolder<String>(binding.root) {

        override fun bind(data: String) {
            binding.tvWord.text = data
        }
    }
}
