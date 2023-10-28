package com.doubtnutapp.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.similarVideo.model.NcertViewItemEntity

/**
 * Created by Anand Gaurav on 2019-10-14.
 */
class NcertViewItemCameraAdapter(private val actionPerformer: ActionPerformer?) : RecyclerView.Adapter<NcertViewItemCameraViewHolder>() {

    var list: MutableList<NcertViewItemEntity> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NcertViewItemCameraViewHolder {
        return NcertViewItemCameraViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_ncert_item_camera_view,
                        parent,
                        false)
        ).also {
            it.actionPerformer = this@NcertViewItemCameraAdapter.actionPerformer
        }
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: NcertViewItemCameraViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun updateData(entity: List<NcertViewItemEntity>) {
        list = entity.toMutableList()
        notifyDataSetChanged()
    }

}
