package com.doubtnutapp.ui.course

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentChapterBinding
import com.doubtnutapp.ui.FragmentHolderActivity
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId


class ChaptersFragment : BaseBindingFragment<ChapterViewModel, FragmentChapterBinding>() {

    companion object {
        const val TAG = "ChaptersFragment"

        fun newInstance() = ChaptersFragment()

    }

    private lateinit var adapter: ChaptersAdapter
    private lateinit var eventTracker: Tracker

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        statusbarColor(activity, R.color.course_header_dark)
        com.doubtnutapp.Log.d(EventConstants.EVENT_NAME_TOPICS_IN_BOTTOM_CLICK)

        eventTracker = getTracker()
        if (defaultPrefs(requireActivity()).getString(Constants.STUDENT_COURSE, "") == "") {
            showCourseSheet(getStudentClass())
            sendEvent(EventConstants.EVENT_NAME_SHOW_COURSE_SHEET)
        }

        binding.tvClass.text =
            defaultPrefs(requireActivity()).getString(Constants.STUDENT_CLASS_DISPLAY, "")
        binding.tvCourse.text =
            defaultPrefs(requireActivity()).getString(Constants.STUDENT_COURSE, "")

        adapter = ChaptersAdapter(requireActivity(), eventTracker)
        binding.rvChapters.layoutManager = GridLayoutManager(requireActivity(), 3)
        binding.rvChapters.adapter = adapter

        binding.rvChapters.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {

                val intent = Intent(activity, FragmentHolderActivity::class.java)
                intent.action = Constants.NAVIGATE_COURSE_DETAIL
                intent.putExtra("index", position)
                Log.d("index", position.toString())
                intent.putParcelableArrayListExtra(Constants.CHAPTER_LIST, adapter.cours)
                intent.putExtra(Constants.CDN_PATH, adapter.cdnPath)
                activity!!.startActivity(intent)
                sendEventByChapter(
                    EventConstants.EVENT_NAME_CHAPTER_ITEM_CLICK,
                    adapter.cours!![position].chapter_display!!
                )
                sendEvent(EventConstants.EVENT_NAME_CHAPTER_DETAILS_FRAGMENT)

            }
        })

        if (defaultPrefs(requireActivity()).getString(Constants.STUDENT_COURSE, "") != "") {
            fetchChapters()
        }

        binding.tvClass.setOnClickListener {
            showClassSheet()
            sendEvent(EventConstants.EVENT_NAME_SHOW_CLASS_SHEET)
        }
        binding.tvCourse.setOnClickListener {
            showCourseSheet(getStudentClass())
            sendEvent(EventConstants.EVENT_NAME_SHOW_COURSE_SHEET)

        }
        binding.chapterBackImageView.setOnClickListener {
            activity?.apply {
                requireActivity().onBackPressed()
            }
        }
    }

    private fun fetchChapters() {
        viewModel.getChapters().observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(requireFragmentManager(), "NetworkErrorDialog")
                }
                is Outcome.ApiError -> {
                    binding.progressBar.visibility = View.GONE
                    apiErrorToast(response.e)

                }
                is Outcome.Success -> {

                    adapter.cdnPath(response.data.data.cdn_path)
                    adapter.updateData(response.data.data.chapter_list)
                    binding.progressBar.visibility = View.GONE
                }
            }
        })
    }

    fun showCourseSheet(clazz: String) {

        viewModel.getCourses(clazz).observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(requireFragmentManager(), "NetworkErrorDialog")
                }
                is Outcome.ApiError -> {
                    binding.progressBar.visibility = View.GONE
                    apiErrorToast(response.e)

                }is Outcome.BadRequest -> {
                binding.progressBar.visibility = View.GONE
                val dialog = BadRequestDialog.newInstance("unauthorized")
                dialog.show(requireFragmentManager(), "BadRequestDialog")
            }
                is Outcome.Success -> {
                    binding.progressBar.visibility = View.GONE

                    if(response.data.data.size==1){
                        defaultPrefs(requireActivity()).edit {
                            putString(Constants.STUDENT_COURSE, response.data.data[0].course)
                        }
                        updateCourse()
                    }else{
                        val dialog = SelectCourseDialog.newInstance(response.data.data)
                        dialog.show(childFragmentManager, "NcertPlaylistDetailsDialog")

                    }



                }
            }
        })

    }

    private fun showClassSheet() {
        val dialog = SelectClassDialog.newInstance()
        dialog.show(childFragmentManager, "SelectClassDialog")
    }


    fun updateCourse() {
        binding.tvCourse.let {
            binding.tvCourse.text =
                defaultPrefs(requireActivity()).getString(Constants.STUDENT_COURSE, "")
            binding.tvClass.text =
                defaultPrefs(requireActivity()).getString(Constants.STUDENT_CLASS_DISPLAY, "")
            fetchChapters()
            requireActivity().setResult(Activity.RESULT_OK)
        }

        viewModel.updateClassCourse().observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Outcome.Progress -> {
                }
                is Outcome.Failure -> {
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(requireFragmentManager(), "NetworkErrorDialog")
                }
                is Outcome.ApiError -> {
                    apiErrorToast(response.e)

                }
                is Outcome.BadRequest -> {
//                    progressbar.visibility = View.GONE
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(requireFragmentManager(), "BadRequestDialog")
                }
                is Outcome.Success ->{
                  toast(getString(R.string.details_update_successfully))
                }
            }

        })
    }


    private fun getTracker(): Tracker {
        val doubtnutApp = activity?.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    private fun sendEvent(eventName : String){
        activity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_CHAPTER_FRAGMENT)
                    .track()
        }
    }
    private fun sendEventByChapter(eventName : String, chapter : String){
        activity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_CHAPTER_FRAGMENT)
                .addEventParameter(Constants.CHAPTER, chapter)
                .track()
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentChapterBinding {
        return FragmentChapterBinding.inflate(layoutInflater, container, false)

    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): ChapterViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
    }


}
