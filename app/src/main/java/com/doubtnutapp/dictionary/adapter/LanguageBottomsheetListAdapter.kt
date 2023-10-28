package com.doubtnutapp.dictionary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.OnDictionaryLangaugeSelected
import com.doubtnutapp.data.dictionary.Language
import com.doubtnutapp.databinding.ItemLanguageBootomsheetBinding

class LanguageBottomsheetListAdapter(
    val items: ArrayList<Language>,
    val actionPerformer: ActionPerformer
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_language_bootomsheet, parent, false),
            actionPerformer
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(val mView: View, val actionPerformer: ActionPerformer) :
        RecyclerView.ViewHolder(mView) {

        val binding = ItemLanguageBootomsheetBinding.bind(mView)

        fun bind(item: Language) {
            binding.rbLanguage.text = item.text
            if (item.isSelected == true) {
                binding.rbLanguage.isChecked = true
            } else {
                mView.setOnClickListener {
                    actionPerformer.performAction(OnDictionaryLangaugeSelected(item))
                    binding.rbLanguage.isChecked = true
                }
            }
        }
    }
}
