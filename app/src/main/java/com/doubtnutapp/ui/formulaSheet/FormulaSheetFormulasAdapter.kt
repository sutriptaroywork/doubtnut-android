package com.doubtnutapp.ui.formulaSheet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.FormulaSheetFormulas
import com.doubtnutapp.databinding.ItemFormulaSheetFormulasBinding

class FormulaSheetFormulasAdapter : RecyclerView.Adapter<FormulaSheetFormulasAdapter.ViewHolder>() {

    private var formulaList = mutableListOf<FormulaSheetFormulas>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_formula_sheet_formulas, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(formulaList[position])

    }

    override fun getItemCount(): Int {
        return formulaList.size
    }

    class ViewHolder(var binding: ItemFormulaSheetFormulasBinding) : RecyclerView.ViewHolder(binding.root) {


        fun bind(FormulaList: FormulaSheetFormulas) {
            binding.formulas = FormulaList
            binding.executePendingBindings()

        }
    }

    fun updateData(FormulaList: ArrayList<FormulaSheetFormulas>) {
        this.formulaList.addAll(FormulaList)
        notifyDataSetChanged()
    }


}


@BindingAdapter("chapterList")
fun addPlaylist(rvFormulasItem: RecyclerView, list: ArrayList<FormulaSheetFormulas.FormulasList>?) {
    val adapter = FormulaSheetFormulaListAdapter()
    rvFormulasItem.layoutManager = LinearLayoutManager(rvFormulasItem.context)
    rvFormulasItem.adapter = adapter

    list?.let {
        adapter.updateFormulaData(list)
    }


}

