@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.doubtnutapp.ui.formulaSheet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.FormulaSheetGlobalSearch
import com.doubtnutapp.databinding.ItemFormulaSearchVerticalBinding


class FormulaGlobalSearchAdapter : RecyclerView.Adapter<FormulaGlobalSearchAdapter.ViewHolder>() {

    private var formulaSheetSuperChapter: List<FormulaSheetGlobalSearch>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_formula_search_vertical, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(formulaSheetSuperChapter!![position])

    }

    override fun getItemCount(): Int {
        return formulaSheetSuperChapter?.size ?: 0
    }

    class ViewHolder constructor(var binding: ItemFormulaSearchVerticalBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(formulaSheetSuperChapter: FormulaSheetGlobalSearch) {
            binding.searchList = formulaSheetSuperChapter
            binding.titleVerticalAdapter.text = formulaSheetSuperChapter.searchType
            binding.executePendingBindings()
        }
    }

    fun updateData(formulaSheetSuperChapter: List<FormulaSheetGlobalSearch>) {
        this.formulaSheetSuperChapter = formulaSheetSuperChapter
        notifyDataSetChanged()
    }

}

@BindingAdapter("searchList")
fun addPlaylist(rvPlaylistItem: RecyclerView, formulaSheetGlobalSearch: FormulaSheetGlobalSearch) {
    val context = rvPlaylistItem.context

    val adapter = FormulaGlobalSearchListAdapter()
    rvPlaylistItem.layoutManager = LinearLayoutManager(rvPlaylistItem.context)
    rvPlaylistItem.adapter = adapter


    formulaSheetGlobalSearch?.let {
        when {
            formulaSheetGlobalSearch.searchType == Constants.FORMULAS -> formulaSheetGlobalSearch?.formulasList?.let { it1 -> adapter.updateData(it1, formulaSheetGlobalSearch.searchType) }
            formulaSheetGlobalSearch.searchType == Constants.TOPICS -> formulaSheetGlobalSearch.topicList?.let { it1 -> adapter.updateData(it1, formulaSheetGlobalSearch.searchType) }
            formulaSheetGlobalSearch.searchType == Constants.CHAPTERS  -> formulaSheetGlobalSearch.chapterList?.let { it1 -> adapter.updateData(it1, formulaSheetGlobalSearch.searchType) }
            else -> {
            }
        }
    }


}

