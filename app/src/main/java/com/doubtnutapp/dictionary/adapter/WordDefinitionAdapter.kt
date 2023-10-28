package com.doubtnutapp.dictionary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.dictionary.WordDefinition
import com.doubtnutapp.databinding.ItemWordDeifinitionBinding
import com.doubtnutapp.hide
import com.doubtnutapp.show

class WordDefinitionAdapter(
    private val list: ArrayList<WordDefinition> = arrayListOf()
) :
    RecyclerView.Adapter<WordDefinitionAdapter.WordDefinitionViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WordDefinitionViewHolder {
        return WordDefinitionViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_word_deifinition, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: WordDefinitionViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateData(wordDetails: List<WordDefinition>) {
        list.clear()
        list.addAll(wordDetails)
        notifyDataSetChanged()
    }

    class WordDefinitionViewHolder(val binding: ItemWordDeifinitionBinding) :
        BaseViewHolder<WordDefinition>(binding.root) {

        override fun bind(data: WordDefinition) {
            if (!data.definition.isNullOrEmpty()) {
                binding.tvDefinition.show()
                binding.tvDefinition.text =
                    "${absoluteAdapterPosition + 1}. ${data.definition}"
            } else {
                binding.tvDefinition.hide()
            }
            if (!data.localizedText.isNullOrEmpty()) {
                binding.tvLocalizedDefinition.text = data.localizedText.orEmpty()
                binding.tvLocalizedDefinition.show()
            } else {
                binding.tvLocalizedDefinition.hide()
            }

            if (!data.example.isNullOrEmpty()) {
                binding.tvLabelExample.show()
                binding.tvExample.show()
                binding.tvExample.text = data.example
            } else {
                binding.tvLabelExample.hide()
                binding.tvExample.hide()
            }

            if (!data.synonyms.isNullOrEmpty()) {
                binding.rvSimilar.show()
                binding.tvLabelSimilar.show()
                binding.rvSimilar.layoutManager =
                    ChipsLayoutManager.newBuilder(binding.root.context)
                        .setScrollingEnabled(true)
                        .build()
                binding.rvSimilar.isNestedScrollingEnabled = true
                binding.rvSimilar.adapter =
                    WordStringAdapter(data.synonyms.orEmpty() as ArrayList<String>)
            } else {
                binding.rvSimilar.hide()
                binding.tvLabelSimilar.hide()
            }
            if (!data.antonyms.isNullOrEmpty()) {
                binding.rvOpposite.show()
                binding.tvLabelOpposite.show()
                binding.rvOpposite.layoutManager =
                    ChipsLayoutManager.newBuilder(binding.root.context)
                        .setOrientation(ChipsLayoutManager.HORIZONTAL)
                        .setMaxViewsInRow(3)
                        .build()
                binding.rvOpposite.adapter =
                    WordStringAdapter(data.antonyms.orEmpty() as ArrayList<String>)
            } else {
                binding.rvOpposite.hide()
                binding.tvLabelOpposite.hide()
            }
        }
    }
}
