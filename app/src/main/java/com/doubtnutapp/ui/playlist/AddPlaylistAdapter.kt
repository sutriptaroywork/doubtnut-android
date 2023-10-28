package com.doubtnutapp.ui.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.Library
import com.doubtnutapp.data.remote.models.Playlist
import com.doubtnutapp.databinding.ItemListPlaylistBinding

class AddPlaylistAdapter : RecyclerView.Adapter<AddPlaylistAdapter.ViewHolder>() {

    var  name: ArrayList<Playlist>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate<ItemListPlaylistBinding>(LayoutInflater.from(parent.context),
                R.layout.item_list_playlist, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(name!![position])

    }

    override fun getItemCount(): Int {
        return name?.size ?: 0
    }

    class ViewHolder constructor(var binding: ItemListPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playList: Playlist) {
            binding.playlist = playList
            binding.executePendingBindings()
        }

    }

    fun updateData(topics: ArrayList<Playlist>) {
        this.name = topics
        notifyDataSetChanged()
    }

}