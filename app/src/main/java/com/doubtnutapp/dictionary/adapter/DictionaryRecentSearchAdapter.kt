package com.doubtnutapp.dictionary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.SearchWordMeaning
import com.doubtnutapp.databinding.ItemWordSearchBinding

class DictionaryRecentSearchAdapter(
    private val actionPerformer: ActionPerformer,
    private var list: ArrayList<String> = arrayListOf()
) :
    RecyclerView.Adapter<BaseViewHolder<String>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<String> {
        return DictionaryRecentSearchViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_word_search, parent, false
            ),
            actionPerformer
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder<String>, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class DictionaryRecentSearchViewHolder(
        val view: ItemWordSearchBinding,
        val mActionPerformer: ActionPerformer?
    ) :
        BaseViewHolder<String>(view.root) {

        override fun bind(data: String) {
            view.searchName.text = data
            view.root.setOnClickListener {
                mActionPerformer?.performAction(SearchWordMeaning(data))
            }
        }
    }

    fun updateData(searchList: ArrayList<String>) {
        if (!searchList.isNullOrEmpty()) {
            list = searchList
            notifyDataSetChanged()
        }
    }
}
