package com.doubtnutapp.ui.topperStudyPlan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.analytics.model.StructuredEvent

import com.doubtnutapp.base.PublishSnowplowEvent
import com.doubtnutapp.databinding.LayoutListItemStudyTopicBinding

class StudyTopicAdapter(private val actionPerformer: ActionPerformer)
    : ListAdapter<StudyPlanData.Chapter, StudyTopicAdapter.StudyTopicViewHolder>(DIFF_UTILS) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyTopicAdapter.StudyTopicViewHolder {
        val binder = LayoutListItemStudyTopicBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return StudyTopicViewHolder(binder)
    }

    override fun onBindViewHolder(holder: StudyTopicAdapter.StudyTopicViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    companion object {
        val DIFF_UTILS = object : DiffUtil.ItemCallback<StudyPlanData.Chapter>() {
            override fun areContentsTheSame(oldItem: StudyPlanData.Chapter, newItem: StudyPlanData.Chapter) = oldItem == newItem

            override fun areItemsTheSame(oldItem: StudyPlanData.Chapter, newItem: StudyPlanData.Chapter) = oldItem.id == newItem.id
        }
    }

    inner class StudyTopicViewHolder(private val binder: LayoutListItemStudyTopicBinding) : RecyclerView.ViewHolder(binder.root) {

        fun bind(item: StudyPlanData.Chapter) {
            binder.chapter = item
            binder.root.setOnClickListener {
                val userScore = (item.microConceptViewed * 100) / item.maxMicroConceptViewed
                val chapterName = item.chapterName ?: item.title ?: " "
                actionPerformer.performAction(PublishSnowplowEvent(StructuredEvent(category = EventConstants.CATEGORY_PERSONALIZATION,
                        action = EventConstants.EVENT_NAME_PERSONALIZATION_STUDY_PLAN, property = "Chapter",
                        eventParams = hashMapOf("subject" to item.subject,
                                "score" to userScore,
                                "chapter" to chapterName))))
                it.context.startActivity(ChapterDetailActivity.getStartIntent(it.context, item.id, (item.chapterName
                        ?: item.title).toString()))

            }
        }
    }
}