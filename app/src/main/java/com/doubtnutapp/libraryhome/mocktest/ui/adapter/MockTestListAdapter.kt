package com.doubtnutapp.libraryhome.mocktest.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.OpenMockTestListActivity
import com.doubtnutapp.databinding.ItemMockTestCourseListLibraryBinding
import com.doubtnutapp.libraryhome.mocktest.model.MockTestCourse
import com.doubtnutapp.loadImage

class MockTestListAdapter(
    private val actionPerformer: ActionPerformer,
    private val context: Context?
) : RecyclerView.Adapter<MockTestListAdapter.ViewHolder>() {

    var items: MutableList<MockTestCourse> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_mock_test_course_list_library, parent, false
            ), context
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position, actionPerformer)
    }

    fun updateData(items: List<MockTestCourse>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: ItemMockTestCourseListLibraryBinding, val context: Context?) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            mockTestCourse: MockTestCourse,
            position: Int,
            actionPerformer: ActionPerformer
        ) {
            binding.mocktestData = mockTestCourse
            binding.actionPerformer = actionPerformer
            binding.openMockTestActivityAction = OpenMockTestListActivity(position)
            binding.executePendingBindings()
            binding.ivCourseImage.loadImage(
                mockTestCourse.mockTestList[0].imageUrl,
                R.drawable.ic_mock_test_classes, R.drawable.ic_mock_test_classes
            )
            binding.tvCourseCount.text = context?.resources?.getQuantityString(
                R.plurals.test_count,
                mockTestCourse.mockTestList.size, mockTestCourse.mockTestList.size
            )
        }
    }
}
