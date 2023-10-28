package com.doubtnutapp.addtoplaylist

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemPlaylistSelectionBinding
import com.doubtnutapp.domain.addtoplaylist.entities.PlaylistEntity

/**
 * Created by Anand Gaurav on 2019-11-22.
 */
class AddToPlaylistViewHolder(val binding: ItemPlaylistSelectionBinding) : BaseViewHolder<PlaylistEntity>(binding.root) {

    override fun bind(data: PlaylistEntity) {
        binding.playlistEntity = data
        binding.executePendingBindings()
        binding.checkboxPlaylist.setOnCheckedChangeListener { buttonView, isChecked ->
            data.isChecked = isChecked
        }

        binding.checkboxPlaylist.isChecked = data.isChecked
    }
}
