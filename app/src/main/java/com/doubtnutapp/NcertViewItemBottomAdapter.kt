package com.doubtnutapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.similarVideo.model.NcertViewItemEntity

/**
 * Created by Anand Gaurav on 2019-10-14.
 */
class NcertViewItemBottomAdapter(private val actionPerformer: ActionPerformer?) :
    RecyclerView.Adapter<NcertViewItemBottomViewHolder>() {

    var list: MutableList<NcertViewItemEntity> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NcertViewItemBottomViewHolder {
        return NcertViewItemBottomViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_ncert_item_bottom_view,
                parent,
                false
            )
        ).also {
            it.actionPerformer = this@NcertViewItemBottomAdapter.actionPerformer
        }
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: NcertViewItemBottomViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun updateData(entity: List<NcertViewItemEntity>) {
        list = entity.toMutableList()
        notifyDataSetChanged()
    }

}
