package com.doubtnutapp.ui.games

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.databinding.LayoutListItemGamesBinding
import com.doubtnutapp.loadImage

class GamesAdapter(private val gamesEventManager: GamesEventManager) :
    ListAdapter<GamesData.Data, GamesAdapter.GamesViewHolder>(DIFF_UTILS) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GamesViewHolder {
        val binder = LayoutListItemGamesBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return GamesViewHolder(binder, gamesEventManager)
    }

    override fun onBindViewHolder(holder: GamesViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    companion object {
        val DIFF_UTILS = object : DiffUtil.ItemCallback<GamesData.Data>() {
            override fun areContentsTheSame(oldItem: GamesData.Data, newItem: GamesData.Data) =
                oldItem.id == newItem.id

            override fun areItemsTheSame(oldItem: GamesData.Data, newItem: GamesData.Data) =
                oldItem.id == newItem.id
        }
    }

    inner class GamesViewHolder(
        private val binder: LayoutListItemGamesBinding,
        private val gamesEventManager: GamesEventManager
    ) : RecyclerView.ViewHolder(binder.root) {

        fun bind(item: GamesData.Data) {
            binder.ivImage.loadImage(item.imageUrl)
            binder.root.setOnClickListener {
                gamesEventManager.sendGameClickEvent(item.title)
                it.context.startActivity(GamePlayerActivity.getIntent(it.context, item))
            }
        }
    }
}