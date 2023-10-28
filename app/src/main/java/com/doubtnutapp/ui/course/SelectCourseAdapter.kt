package com.doubtnutapp.ui.course

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.Course
import com.doubtnutapp.databinding.ItemCourseButtonBinding

class SelectCourseAdapter: RecyclerView.Adapter<SelectCourseAdapter.ViewHolder>(){

    var courses: ArrayList<Course>? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{

        return ViewHolder(DataBindingUtil.inflate<ItemCourseButtonBinding>(LayoutInflater.from(parent.context),
                R.layout.item_course_button, parent, false))
    }

    override fun getItemCount(): Int {
        return courses?.size ?: 0
    }

    override fun onBindViewHolder(holder:ViewHolder, position: Int) {
        holder.bind(courses!![position])
    }
    class ViewHolder constructor(var binding: ItemCourseButtonBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: Course) {
            binding.course = course
            binding.executePendingBindings()
        }
    }

    fun updateData(courses: ArrayList<Course>) {
        this.courses = courses
        notifyDataSetChanged()
    }


}