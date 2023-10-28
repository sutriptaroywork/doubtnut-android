package com.doubtnutapp.ui.formulaSheet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.*
import com.doubtnutapp.data.remote.models.FormulaSheetSuperChapter
import com.doubtnutapp.databinding.ItemFormulaSheetChapterListBinding

class FormulaSheetChapterListAdapter : RecyclerView.Adapter<FormulaSheetChapterListAdapter.ViewHolder>() {

    var  chapterDataList = mutableListOf<FormulaSheetSuperChapter.FormulaSheetChapter>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_formula_sheet_chapter_list, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind( chapterDataList[position])

    }

    override fun getItemCount(): Int {
        return  chapterDataList.size
    }

    class ViewHolder(var binding: ItemFormulaSheetChapterListBinding) : RecyclerView.ViewHolder(binding.root) {


        fun bind( chapterDataList: FormulaSheetSuperChapter.FormulaSheetChapter) {
            binding.chapterList=  chapterDataList
            binding.executePendingBindings()

        }
    }

    fun updateData( chapterDataList: ArrayList<FormulaSheetSuperChapter.FormulaSheetChapter>) {
        this.chapterDataList = chapterDataList
        notifyDataSetChanged()
    }


}

