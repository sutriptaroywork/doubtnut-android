package com.doubtnutapp.ui.formulaSheet

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.FormulaSheetFormulas
import com.doubtnutapp.databinding.ItemFormulaSheetFormulaListBinding
import com.doubtnutapp.hide
import com.doubtnutapp.show

class FormulaSheetFormulaListAdapter :
    RecyclerView.Adapter<FormulaSheetFormulaListAdapter.ViewHolder>() {

    private var formulaItemList = mutableListOf<FormulaSheetFormulas.FormulasList>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_formula_sheet_formula_list, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(formulaItemList[position])
    }

    override fun getItemCount(): Int {
        return formulaItemList.size
    }

    class ViewHolder(var binding: ItemFormulaSheetFormulaListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(formulaItemList: FormulaSheetFormulas.FormulasList) {
            binding.formulaItemList = formulaItemList

            if (!formulaItemList.formulaItemHTML.isNullOrEmpty()) {
                binding.formulaItemImage.hide()
                binding.formulaItemView.show()
                binding.progressBar.hide()
                val webSettings = binding.formulaItemView.settings
                webSettings.javaScriptEnabled = true
                val html = formulaItemList.formulaItemHTML?.replace("color:white", "color:black")
                binding.formulaItemView.loadDataWithBaseURL(
                    null,
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )

            } else {
                binding.formulaItemImage.show()
                binding.formulaItemView.hide()
            }

            val builder = StringBuilder()
            if (formulaItemList.shortCutName == null && formulaItemList.constantsName == null) {
                binding.tvVieweShortCuts.hide()
                binding.lines.hide()
            } else {
                if (formulaItemList.shortCutName != null && formulaItemList.shortCutName.isNotEmpty()) {
                    for (shortCut in formulaItemList?.shortCutName) {
                        builder.append("\u25A0\t\t" + shortCut.formulaItemShortCutShortName + " = " + shortCut.formulaItemShortCutFullName + "\n")
                    }
                }
                if (formulaItemList.constantsName != null && formulaItemList.constantsName.isNotEmpty()) {
                    for (constantsName in formulaItemList?.constantsName) {
                        builder.append("\u25A0\t\t" + constantsName.formulaItemShortCutShortName + " = " + constantsName.formulaItemConstantsValue + "\n")
                    }
                }

                binding.tvVieweConstants.text = builder.toString()

            }

            binding.tvAddToCheatsheet.setOnClickListener {
                val intent = Intent(binding.root.context, CheatSheetActivity::class.java)
                intent.putExtra(Constants.FORMULA_ITEM_ID, formulaItemList.formulaItemId)
                intent.putExtra(Constants.SEARCH_TYPE, Constants.FORMULAS)
                binding.root.context.startActivity(intent)
            }


            binding.executePendingBindings()

        }
    }

    fun updateFormulaData(FormulaItemList: ArrayList<FormulaSheetFormulas.FormulasList>) {
        this.formulaItemList = FormulaItemList
        notifyDataSetChanged()
    }

}

