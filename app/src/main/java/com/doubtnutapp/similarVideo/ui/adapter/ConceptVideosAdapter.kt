package com.doubtnutapp.similarVideo.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.base.*
import com.doubtnutapp.checkInternetConnection
import com.doubtnutapp.databinding.ItemConceptVideoBinding
import com.doubtnutapp.similarVideo.model.ConceptsVideoList

/**
 * Created by pradip on
 * 28, May, 2019
 **/
class ConceptVideosAdapter(private val actionsPerformer: ActionPerformer?) :
    RecyclerView.Adapter<ConceptVideosAdapter.ViewHolder>() {

    var conceptsVideoList: MutableList<ConceptsVideoList> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_concept_video, parent, false
            )
        ).apply {
            actionPerformer = this@ConceptVideosAdapter.actionsPerformer
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(conceptsVideoList[position])

    }

    override fun getItemCount(): Int {
        return conceptsVideoList.size
    }

    class ViewHolder(var binding: ItemConceptVideoBinding) :
        BaseViewHolder<ConceptsVideoList>(binding.root) {

        override fun bind(data: ConceptsVideoList) {
            binding.conceptVideoData = data
            binding.executePendingBindings()
            color(data.bgColor)
            binding.clickHelperView.setOnClickListener {
                checkInternetConnection(binding.root.context) {
                    if (data.contentLock.isLocked != null && data.contentLock.isLocked) {
                        performAction(OpenPCPopup())
                    } else {
                        performAction(SendConceptVideoClickEvent())
                        performAction(
                            PlayVideo(
                                data.questionId,
                                Constants.PAGE_SIMILAR,
                                "",
                                "",
                                data.resourceType
                            )
                        )
                    }
                }
            }
        }

        fun color(bgColor: String) {
            binding.cardContainer.setBackgroundColor(Color.parseColor(bgColor))
        }
    }

    fun updateDataList(items: List<RecyclerViewItem>) {
        conceptsVideoList.addAll(items as List<ConceptsVideoList>)
        notifyDataSetChanged()
    }
}