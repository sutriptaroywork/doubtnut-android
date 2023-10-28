package com.doubtnutapp.ui.formulaSheet

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.data.remote.models.FormulaSheetGlobalSearch
import com.doubtnutapp.databinding.ItemGlobalSearchListBinding
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId


class FormulaGlobalSearchListAdapter : RecyclerView.Adapter<FormulaGlobalSearchListAdapter.ViewHolder>() {

    var  searchTopicList = mutableListOf<FormulaSheetGlobalSearch.FormulaSheetGlobalList>()
    lateinit var searchType: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_global_search_list, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind( searchTopicList[position], searchType)

    }

    override fun getItemCount(): Int {
        return  searchTopicList.size
    }

    class ViewHolder(var binding: ItemGlobalSearchListBinding) : RecyclerView.ViewHolder(binding.root) {

        val context = binding.root.context

        fun bind(searchTopicList: FormulaSheetGlobalSearch.FormulaSheetGlobalList, searchType: String) {
            binding.searchList = searchTopicList
            binding.executePendingBindings()

            binding.rootAutocomplete.setOnClickListener {
                val intent = Intent(context, GlobalSearchFormulasActivity::class.java)
                intent.putExtra(Constants.CHAPTER_ID, searchTopicList.globalSearchId)
                intent.putExtra(Constants.FORMULA_SUBJECT_ID, searchTopicList.globalSearchSubjectId)
                intent.putExtra(Constants.SUPER_CHAPTER_NAME, searchTopicList.globalSearchName)
                intent.putExtra(Constants.SEARCH_TYPE, searchType)
                context.startActivity(intent)
                sendEvent(EventConstants.EVENT_NAME_FORMULA_CHAPTER_CLICK)
                sendEvent(EventConstants.EVENT_NAME_FORMULA_CHAPTER_CLICK + searchTopicList.globalSearchName)
            }
        }

            private fun sendEvent(eventName: String) {
                context?.apply {
                    (applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                            .addNetworkState(NetworkUtils.isConnected(context).toString())
                            .addStudentId(value = getStudentId())
                            .addScreenName(EventConstants.PAGE_FORMULA_SHEET_GLOBAL_SEARCH_ACTIVITY)
                            .track()
                }
            }



    }

    fun updateData(searchTopicList: List<FormulaSheetGlobalSearch.FormulaSheetGlobalList>, searchType: String) {
        this.searchTopicList = searchTopicList as MutableList<FormulaSheetGlobalSearch.FormulaSheetGlobalList>
        this.searchType = searchType
        notifyDataSetChanged()
    }


}

