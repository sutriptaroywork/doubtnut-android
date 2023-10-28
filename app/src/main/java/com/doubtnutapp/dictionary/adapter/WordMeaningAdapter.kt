package com.doubtnutapp.dictionary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.dictionary.WordDefinition
import com.doubtnutapp.data.dictionary.WordMeaning
import com.doubtnutapp.databinding.ItemDictionaryMeaningBinding

class WordMeaningAdapter(
    private val actionPerformer: ActionPerformer?,
    private val list: ArrayList<WordMeaning> = arrayListOf()
) :
    RecyclerView.Adapter<BaseViewHolder<WordMeaning>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<WordMeaning> {
        return WordMeaningViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_dictionary_meaning, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder<WordMeaning>, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateData(wordDetails: List<WordMeaning>) {
        list.clear()
        list.addAll(wordDetails)
        notifyDataSetChanged()
    }

    class WordMeaningViewHolder(val binding: ItemDictionaryMeaningBinding) :
        BaseViewHolder<WordMeaning>(binding.root) {
        override fun bind(data: WordMeaning) {
            binding.tvPartOfSpeech.text = data.partOfSpeech.orEmpty()
            binding.rvWordDefinition.adapter =
                WordDefinitionAdapter(data.definitions.orEmpty() as ArrayList<WordDefinition>)
        }
    }
}
