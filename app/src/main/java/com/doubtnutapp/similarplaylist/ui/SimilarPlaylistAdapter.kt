package com.doubtnutapp.similarplaylist.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.domain.similarVideo.entities.SimilarPlaylistVideoEntity
import com.doubtnutapp.eventmanager.CommonEventManager

class SimilarPlaylistAdapter(
    private val actionsPerformer: ActionPerformer,
    private val commonEventManager: CommonEventManager,
    private val sourceTag: String
) : RecyclerView.Adapter<SimilarPlayListItemViewHolder>(), Filterable {

    var similarVideo: MutableList<SimilarPlaylistVideoEntity> = mutableListOf()
    var similarVideoFiltered: MutableList<SimilarPlaylistVideoEntity> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SimilarPlayListItemViewHolder {
        return SimilarPlayListItemViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_similar_playlist,
                parent,
                false
            )
        ).also {
            it.actionPerformer = this.actionsPerformer
        }
    }

    override fun getItemCount(): Int = similarVideoFiltered.size

    override fun onBindViewHolder(holder: SimilarPlayListItemViewHolder, position: Int) {
        holder.bind(similarVideoFiltered[position])
    }

    fun updateData(similarVideo: List<SimilarPlaylistVideoEntity>) {
        this.similarVideo.addAll(similarVideo)
        this.similarVideoFiltered.addAll(similarVideo)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                val filteredList = ArrayList<SimilarPlaylistVideoEntity>()
                for (row in similarVideo) {
                    if (row.packageId.equals(charString, true)) {
                        filteredList.add(row)
                    }
                }
                similarVideoFiltered = filteredList
                val filterResults = FilterResults()
                filterResults.values = similarVideoFiltered
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                similarVideoFiltered = filterResults.values as ArrayList<SimilarPlaylistVideoEntity>
                notifyDataSetChanged()
            }
        }
    }

}
