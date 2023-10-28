package com.doubtnutapp.ui.course

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.StudentClass
import com.doubtnutapp.databinding.ItemStudentClassBinding

class SelectClassAdapter: RecyclerView.Adapter<SelectClassAdapter.ViewHolder>(){

    var classes: ArrayList<StudentClass>? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{

        return ViewHolder(DataBindingUtil.inflate<ItemStudentClassBinding>(LayoutInflater.from(parent.context),
                R.layout.item_student_class, parent, false))
    }

    override fun getItemCount(): Int {
        return classes?.size ?: 0
    }

    override fun onBindViewHolder(holder:ViewHolder, position: Int) {
        holder.bind(classes!![position])
    }
    class ViewHolder constructor(var binding: ItemStudentClassBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: StudentClass) {
            binding.studentclass = course
            binding.executePendingBindings()
        }
    }

    fun updateData(classes: ArrayList<StudentClass>) {
        this.classes = classes
        notifyDataSetChanged()
    }


}