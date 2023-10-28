package com.doubtnutapp.quiztfs.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.quiztfs.LiveQuestionsClass

/**
 * Created by Mehul Bisht on 26-08-2021
 */
class ClassSelectDropDownMenu(private val context: Context, val list: List<LiveQuestionsClass>) :
    PopupWindow(context) {
    private var rvClass: RecyclerView? = null
    private var dropdownAdapter: ClassSelectDropDownAdapter? = null
    fun setClassSelectedListener(classSelectedListener: ClassSelectDropDownAdapter.ClassSelectedListener) {
        dropdownAdapter?.setClassSelectedListener(classSelectedListener)
    }

    private fun setupView() {
        val view: View = LayoutInflater.from(context).inflate(R.layout.course_pop_up_filter, null)
        setBackgroundDrawable(context.getDrawable(R.drawable.capsule_stroke_grey_solid_white))
        rvClass = view.findViewById(R.id.rvCategory)
        rvClass?.setHasFixedSize(true)
        rvClass?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvClass?.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        dropdownAdapter = ClassSelectDropDownAdapter(list)
        rvClass?.adapter = dropdownAdapter
        contentView = view
    }

    init {
        setupView()
    }
}