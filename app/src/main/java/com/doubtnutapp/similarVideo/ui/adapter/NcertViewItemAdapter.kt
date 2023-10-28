package com.doubtnutapp.similarVideo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.similarVideo.model.NcertViewItemEntity
import com.doubtnutapp.similarVideo.viewholder.NcertViewItemViewHolder

/**
 * Created by Anand Gaurav on 2019-10-14.
 */
class NcertViewItemAdapter(private val actionPerformer: ActionPerformer?) : RecyclerView.Adapter<NcertViewItemViewHolder>() {

    var list: MutableList<NcertViewItemEntity> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NcertViewItemViewHolder {
        return NcertViewItemViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_ncert_item_view,
                        parent,
                        false)
        ).also {
            it.actionPerformer = this@NcertViewItemAdapter.actionPerformer
        }
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: NcertViewItemViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun updateData(entity: List<NcertViewItemEntity>) {
        list = entity.toMutableList()
        notifyDataSetChanged()
    }

}
