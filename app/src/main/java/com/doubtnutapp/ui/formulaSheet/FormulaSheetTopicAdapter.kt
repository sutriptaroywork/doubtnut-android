package com.doubtnutapp.ui.formulaSheet

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.addEventNames
import com.doubtnutapp.data.remote.models.FormulaSheetSubjects
import com.doubtnutapp.databinding.ItemFormulaSheetBinding
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils

class FormulaSheetTopicAdapter : RecyclerView.Adapter<FormulaSheetTopicAdapter.ViewHolder>() {

    private var subjectsList = mutableListOf<FormulaSheetSubjects>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_formula_sheet, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val colorIndex = Utils.getColorIndex(position)
        holder.bind(subjectsList[position], colorIndex)
        holder.color()

    }

    override fun getItemCount(): Int {
        return subjectsList.size
    }

    class ViewHolder(var binding: ItemFormulaSheetBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(subjectsList: FormulaSheetSubjects, colorIndex: Int) {
            binding.subjects = subjectsList
            binding.executePendingBindings()

            binding.root.setOnClickListener {

                if (subjectsList.subjectsName == Constants.CHEAT_SHEET) {
                    val intent = Intent(binding.root.context, CheatSheetActivity::class.java)
                    binding.root.context.startActivity(intent)

                } else {
                    val intent =
                        Intent(binding.root.context, FormulaSheetChapterActivity::class.java)
                    intent.putExtra(Constants.FORMULA_SUBJECT_ID, subjectsList.subjectsId)
                    intent.putExtra(Constants.FORMULA_SUBJECT_NAME, subjectsList.subjectsName)
                    intent.putExtra(Constants.FORMULA_SUBJECT_ICON, subjectsList.subjectsImage)
                    intent.putExtra(Constants.COLOR_INDEX, colorIndex)
                    binding.root.context.startActivity(intent)
                    sendEvent(EventConstants.EVENT_NAME_FORMULA_SHEET_SUBJECT_CLICK)
                    sendEvent("""${EventConstants.EVENT_NAME_FORMULA_SHEET_SUBJECT_CLICK}${subjectsList.subjectsName}""")

                }

            }

        }

        fun color() {
            val colorPair = Utils.getColorPair(adapterPosition)
            binding.tvFormulaSheetSubjects.setBackgroundResource(colorPair[1])
            binding.ivFormulaSheetSubjects.setBackgroundResource(colorPair[0])
        }

        private fun sendEvent(eventName: String) {
            binding.root.context?.apply {
                (applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(binding.root.context).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_FORMULA_SHEET_HOME_ACTIVITY)
                    .track()
            }
        }
    }

    fun updateData(subjectsList: ArrayList<FormulaSheetSubjects>) {
        this.subjectsList.addAll(subjectsList)
        notifyDataSetChanged()
    }

}

