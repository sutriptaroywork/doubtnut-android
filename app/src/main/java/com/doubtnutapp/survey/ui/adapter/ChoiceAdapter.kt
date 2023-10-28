package com.doubtnutapp.survey.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.survey.model.ChoiceViewItem
import com.doubtnutapp.survey.ui.viewholder.MultipleChoiceViewHolder
import com.doubtnutapp.survey.ui.viewholder.SingleChoiceViewHolder

/**
 * Created by Sachin Saxena on 01/06/20.
 */

class ChoiceAdapter(
    private val mActionPerformer: ActionPerformer,
    @LayoutRes private val layoutRes: Int = R.layout.item_single_choice
) : RecyclerView.Adapter<BaseViewHolder<ChoiceViewItem>>() {

    var optionList: MutableList<ChoiceViewItem> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ChoiceViewItem> {
        return if (viewType == R.layout.item_single_choice) {
            SingleChoiceViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(layoutRes, parent, false)
            ).apply {
                actionPerformer = mActionPerformer
            }
        } else {
            MultipleChoiceViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(layoutRes, parent, false)
            ).apply {
                actionPerformer = mActionPerformer
            }
        }
    }

    override fun getItemCount(): Int = optionList.size

    override fun onBindViewHolder(holder: BaseViewHolder<ChoiceViewItem>, position: Int) {
        holder.bind(optionList[position])
    }

    override fun getItemViewType(position: Int): Int = layoutRes

    fun updateOptions(options: MutableList<ChoiceViewItem>) {
        this.optionList = options
        notifyDataSetChanged()
    }

}