package com.doubtnutapp.libraryhome.library.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter

class LibraryTabAdapter(
    fragmentManager: FragmentManager
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    companion object {
        const val TAG_MY_COURSES = "my_courses"
        const val TAG_CHECK_ALL_COURSES = "check_all_courses"
        const val TAG_TIMETABLE = "timetable"
        const val TAG_FREE_CLASSES = "free_classes"
        const val TAG_LIBRARY = "library"
        const val TAG_DAILY_QUIZ = "daily_quiz"
        const val TAG_MOCK_TEST = "mock_test"
        const val TAG_TEACHERS = "teachers"
    }

    private val fragmentList: MutableList<Fragment> = mutableListOf()
    private val titleList: MutableList<String> = mutableListOf()
    val fragmentTags: MutableList<String> = mutableListOf()

    override fun getItem(position: Int): Fragment = fragmentList[position]

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getCount(): Int = fragmentList.size

    override fun getPageTitle(position: Int): CharSequence? = titleList[position]

    fun updateTabs(
        titleList: List<String>,
        fragmentTags: List<String>,
        fragmentList: MutableList<Fragment>
    ) {
        this.titleList.clear()
        this.fragmentTags.clear()
        this.fragmentList.clear()

        this.titleList.addAll(titleList)
        this.fragmentTags.addAll(fragmentTags)
        this.fragmentList.addAll(fragmentList)

        notifyDataSetChanged()
    }

    fun updateTabAtPosition(title: String, tag: String, fragment: Fragment, position: Int) {
        titleList.removeAt(position)
        fragmentTags.removeAt(position)
        fragmentList.removeAt(position)

        fragmentList.add(position, fragment)
        titleList.add(position, title)
        fragmentTags.add(position, tag)

        notifyDataSetChanged()
    }
}