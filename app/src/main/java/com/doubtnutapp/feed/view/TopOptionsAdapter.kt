package com.doubtnutapp.feed.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.remote.models.feed.TopOptionWidgetItem

/**
 * Created by Sachin Saxena on 01/06/20.
 */

class TopOptionsAdapter(
    private val isItemWidthFixed: Boolean = true,
    private val actionPerformer: ActionPerformer2? = null
) : RecyclerView.Adapter<BaseViewHolder<TopOptionWidgetItem>>() {

    private var optionList: List<TopOptionWidgetItem> = listOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<TopOptionWidgetItem> {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_top_option_new, parent, false)
        if (isItemWidthFixed.not()) {
            view.updateLayoutParams {
                width = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }
        return TopOptionViewHolder(view).apply {
            actionPerformer = this@TopOptionsAdapter.actionPerformer
        }
    }

    override fun getItemCount(): Int = optionList.size

    override fun onBindViewHolder(holder: BaseViewHolder<TopOptionWidgetItem>, position: Int) {
        holder.bind(optionList[position])
    }

    fun updateOptions(options: List<TopOptionWidgetItem>) {
        this.optionList = options
        notifyDataSetChanged()
    }

}