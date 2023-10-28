package com.doubtnutapp.addtoplaylist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.domain.addtoplaylist.entities.PlaylistEntity

/**
 * Created by Anand Gaurav on 2019-11-22.
 */
class AddToPlaylistAdapter(private val actionsPerformer: ActionPerformer?) : RecyclerView.Adapter<BaseViewHolder<PlaylistEntity>>() {

    val listings = mutableListOf<PlaylistEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<PlaylistEntity> {
        return (
            AddToPlaylistViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_playlist_selection, parent, false)
            ) as BaseViewHolder<PlaylistEntity>
            ).also {
            it.actionPerformer = actionsPerformer
        }
    }

    override fun getItemCount(): Int = listings.size

    override fun onBindViewHolder(holder: BaseViewHolder<PlaylistEntity>, position: Int) {
        holder.bind(listings[position])
    }

    fun updateList(recentListings: List<PlaylistEntity>) {
        listings.addAll(recentListings)
        notifyDataSetChanged()
    }
}
