package com.doubtnutapp.ui.main.samplequestion

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.domain.camerascreen.entity.SubjectEntity

/**
 * Created by Anand Gaurav on 2019-10-14.
 */
class SampleQuestionAdapter(private val actionPerformer: ActionPerformer) : RecyclerView.Adapter<SampleQuestionViewHolder>() {

    var subjectEntityList: MutableList<SubjectEntity?> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleQuestionViewHolder {
        return SampleQuestionViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_sample_question,
                        parent,
                        false)
        ).also {
            it.actionPerformer = this@SampleQuestionAdapter.actionPerformer
        }
    }

    override fun getItemCount() = subjectEntityList.size

    override fun onBindViewHolder(holder: SampleQuestionViewHolder, position: Int) {
        holder.bind(subjectEntityList[position])
    }

    fun updateData(subjectEntity: List<SubjectEntity?>) {
        subjectEntityList = subjectEntity.toMutableList()
        notifyDataSetChanged()
    }

}
