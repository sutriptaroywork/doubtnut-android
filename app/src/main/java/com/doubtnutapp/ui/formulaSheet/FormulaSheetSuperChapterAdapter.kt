@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.doubtnutapp.ui.formulaSheet

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.data.remote.models.FormulaSheetSuperChapter
import com.doubtnutapp.databinding.ItemFormulasheetVerticalBinding
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils


class FormulaSheetVerticalAdapter : RecyclerView.Adapter<FormulaSheetVerticalAdapter.ViewHolder>() {

    private var formulaSheetSuperChapter: ArrayList<FormulaSheetSuperChapter>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_formulasheet_vertical, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val colorIndex = Utils.getColorIndex(position)
        holder.bind(formulaSheetSuperChapter!![position], colorIndex)

    }

    override fun getItemCount(): Int {
        return formulaSheetSuperChapter?.size ?: 0
    }

    class ViewHolder constructor(var binding: ItemFormulasheetVerticalBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(formulaSheetSuperChapter: FormulaSheetSuperChapter, colorIndex: Int) {
            binding.titleVerticalAdapter.text = formulaSheetSuperChapter.superChapterName
            binding.colorindex = colorIndex
            binding.superTopic = formulaSheetSuperChapter
            binding.executePendingBindings()
        }
    }

    fun updateData(formulaSheetSuperChapter: ArrayList<FormulaSheetSuperChapter>) {
        this.formulaSheetSuperChapter = formulaSheetSuperChapter
        notifyDataSetChanged()
    }

}

@BindingAdapter("superChapterName", "superChapterImage", "chapterList")
fun addPlaylist(rvPlaylistItem: RecyclerView, superChapterName: String, superChapterImage: String?, list: ArrayList<FormulaSheetSuperChapter.FormulaSheetChapter>?) {
    val context = rvPlaylistItem.context

    val adapter = FormulaSheetChapterListAdapter()
    rvPlaylistItem.layoutManager = LinearLayoutManager(rvPlaylistItem.context)
    rvPlaylistItem.adapter = adapter

    rvPlaylistItem.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
        override fun onItemClick(position: Int, view: View) {
            val intent = Intent(context, FormulaSheetFormulasActivity::class.java)
            intent.putExtra(Constants.CHAPTER_ID, adapter.chapterDataList[position].chapterId)
            intent.putExtra(Constants.FORMULA_SUBJECT_ID, adapter.chapterDataList[position].chapterSubjectId)
            intent.putExtra(Constants.CLICKED_ITEM_NAME, adapter.chapterDataList[position].chapterName)
            context.startActivity(intent)
            sendEvent(EventConstants.EVENT_NAME_FORMULA_CHAPTER_CLICK)
            sendEvent(EventConstants.EVENT_NAME_FORMULA_CHAPTER_CLICK+superChapterName)
        }

        private fun sendEvent(eventName: String) {
            context?.apply {
                (applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                        .addNetworkState(NetworkUtils.isConnected(context).toString())
                        .addStudentId(value = getStudentId())
                        .addScreenName(EventConstants.PAGE_FORMULA_SHEET_CHAPTER_ACTIVITY)
                        .track()
            }
        }
    })

    list?.let {
        adapter.updateData(list)
    }


}

