package com.doubtnutapp.libraryhome.coursev3.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.CourseFilterTypeData

class CourseSelectDropDownMenu(private val context: Context, val list: List<CourseFilterTypeData>) : PopupWindow(context) {
    private var rvCategory: RecyclerView? = null
    private var dropdownAdapter: CourseFilterDropDownAdapter? = null
    fun setCategorySelectedListener(filterSelectedListener: CourseFilterDropDownAdapter.FilterSelectedListener?) {
        dropdownAdapter?.setCategorySelectedListener(filterSelectedListener)
    }

    private fun setupView() {
        val view: View = LayoutInflater.from(context).inflate(R.layout.course_pop_up_filter, null)
        setBackgroundDrawable(context.getDrawable(R.drawable.capsule_stroke_grey_solid_white))
        rvCategory = view.findViewById(R.id.rvCategory)
        rvCategory?.setHasFixedSize(true)
        rvCategory?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvCategory?.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        dropdownAdapter = CourseFilterDropDownAdapter(list)
        rvCategory?.adapter = dropdownAdapter
        contentView = view
    }

    init {
        setupView()
    }
}