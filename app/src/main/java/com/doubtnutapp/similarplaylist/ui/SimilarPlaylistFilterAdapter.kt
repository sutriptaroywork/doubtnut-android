package com.doubtnutapp.similarplaylist.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.domain.similarVideo.entities.SimilarPlaylistTabEntity

/**
 * Created by Anand Gaurav on 2019-12-02.
 */
class SimilarPlaylistFilterAdapter(private val tabList: List<SimilarPlaylistTabEntity>,
                                   private val actionPerformer: ActionPerformer,
                                   private val recyclerViewChildren: RecyclerView) : RecyclerView.Adapter<SimilarPlaylistFilterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimilarPlaylistFilterViewHolder {
        return SimilarPlaylistFilterViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_similar_playlist_filter,
                        parent,
                        false), recyclerViewChildren
        ).also {
            it.actionPerformer = this@SimilarPlaylistFilterAdapter.actionPerformer
        }
    }

    override fun getItemCount() = tabList.size

    override fun onBindViewHolder(holder: SimilarPlaylistFilterViewHolder, position: Int) {
        holder.bind(tabList[position])
    }

    fun updateTagSelection(position: Int) {
        tabList.forEachIndexed { index, subjectTagViewItem ->
            subjectTagViewItem.isSelected = index == position
        }
        notifyDataSetChanged()
    }
}