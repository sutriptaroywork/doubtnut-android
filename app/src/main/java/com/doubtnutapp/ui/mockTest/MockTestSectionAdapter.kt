package com.doubtnutapp.ui.mockTest

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.data.remote.models.MockTestSectionData
import com.doubtnutapp.databinding.ItemMockTestReportBinding

class MockTestSectionAdapter : RecyclerView.Adapter<MockTestSectionAdapter.ViewHolder>() {

    var items: MutableList<MockTestSectionData> = mutableListOf()
    var showResult: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMockTestReportBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], showResult)
    }

    fun updateData(items: MutableList<MockTestSectionData>, result: Boolean) {
        this.items = items
        this.showResult = result
        notifyDataSetChanged()
    }

    class ViewHolder constructor(var binding: ItemMockTestReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(testList: MockTestSectionData, showResult: Boolean) {
            binding.tvCorrectCount.text = testList.questionCorrect.orEmpty()
            binding.tvIncorrectCount.text = testList.questionIncorrect.orEmpty()
            binding.tvSubject.text = testList.sectionTitle
            if (showResult) {
                binding.tvSkippedCount.text = testList.questionSkipped.orEmpty()
                binding.tvScore.text = testList.markedScoredInSection.orEmpty()
            } else {
                (binding.tvSkippedCount.layoutParams as? LinearLayout.LayoutParams)?.weight = 0f
                (binding.tvScore.layoutParams as? LinearLayout.LayoutParams)?.weight = 0f
                binding.linearLayout.weightSum = 3.5f
            }
        }
    }

}