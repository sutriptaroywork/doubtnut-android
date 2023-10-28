package com.doubtnutapp.ui.course

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.Chapter
import com.doubtnutapp.data.remote.models.ChapterDetail
import com.doubtnutapp.databinding.FragmentCourseDetailBinding
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils

class CourseDetailFragment :
    BaseBindingFragment<CourseDetailViewModel, FragmentCourseDetailBinding>() {

    companion object {
        const val TAG = "CourseDetailFragment"

        fun newInstance(
            index: Int,
            chapters: ArrayList<Chapter>,
            cdnPath: String
        ): CourseDetailFragment {
            val fragment = CourseDetailFragment()
            val args = Bundle()
            args.putInt("index", index)
            args.putParcelableArrayList(Constants.CHAPTER_LIST, chapters)
            args.putString(Constants.CDN_PATH, cdnPath)
            fragment.arguments = args
            return fragment
        }

        fun newInstanceNotification(
            clazz: String,
            course: String,
            chapter: String
        ): CourseDetailFragment {
            val fragment = CourseDetailFragment()
            val args = Bundle()
            args.putString(Constants.CLASS, clazz)
            args.putString(Constants.COURSE, course)
            args.putString(Constants.CHAPTER, chapter)
            fragment.arguments = args
            return fragment
        }
    }

    lateinit var chapterList: ArrayList<Chapter>
    var index: Int = 0

    private lateinit var adapter: ChaptersAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var subtopicsAdapter: SubtopicsAdapter
    private lateinit var eventTracker: Tracker

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        statusbarColor(activity, R.color.course_header_dark)
        eventTracker = getTracker()
        com.doubtnutapp.Log.d(EventConstants.EVENT_NAME_CHAPTER_DETAILS_FRAGMENT)

        adapter = ChaptersAdapter(requireActivity(), eventTracker)
        layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCoursesDetail.layoutManager = layoutManager
        binding.rvCoursesDetail.adapter = adapter

        binding.rvCoursesDetail.addReadyCallback {

            layoutManager.scrollToPositionWithOffset(
                index,
                (binding.rvCoursesDetail.width / 2) - Utils.convertDpToPixel((WIDTH / 2).toFloat())
                    .toInt()
            )


            if (Utils.is21()) {
                requireActivity().startPostponedEnterTransition()
            }
        }

        binding.rvCoursesDetail.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                Log.d("position", position.toString())
                updateChapter(position)
                binding.rvSubtopics.smoothScrollToPosition(0)
                sendEventByChapter(
                    EventConstants.EVENT_NAME_CHAPTER_ITEM_CLICK,
                    adapter.cours!![position].chapter_display!!
                )

            }
        })



        if (requireArguments().getString(Constants.CDN_PATH) != null) {
            index = requireArguments().getInt("index")
            chapterList = requireArguments().getParcelableArrayList(Constants.CHAPTER_LIST)!!

            adapter.isCourseDetail = true
            adapter.detailIndex = index

            adapter.cdnPath(requireArguments().getString(Constants.CDN_PATH).orDefaultValue())
            adapter.updateData(chapterList)
            subtopicsAdapter = SubtopicsAdapter(requireActivity(), eventTracker)
            binding.rvSubtopics.layoutManager = LinearLayoutManager(requireActivity())
            binding.rvSubtopics.adapter = subtopicsAdapter
            binding.rvSubtopics.smoothScrollToPosition(0)
            binding.btnLearnNow.setBackgroundResource(Utils.getColorPair(index)[1])

            fetchChapterDetails()
        } else {
            val clazz = requireArguments().getString(Constants.CLASS).orDefaultValue()
            val course = requireArguments().getString(Constants.COURSE).orDefaultValue()
            val chapter = requireArguments().getString(Constants.CHAPTER).orDefaultValue()
            fetchChapterDetailsNotification(clazz, course, chapter)
        }

    }

    private fun updateChapter(i: Int) {
        index = i
//        adapter.fromNotification = false
        adapter.detailIndex = index
        adapter.notifyDataSetChanged()

        layoutManager.scrollToPositionWithOffset(
            index,
            (binding.rvCoursesDetail.width / 2) - Utils.convertDpToPixel((WIDTH / 2).toFloat())
                .toInt()
        )
        binding.btnLearnNow.setBackgroundResource(Utils.getColorPair(index)[1])

        fetchChapterDetails()
    }

    private fun getIndex(chap: String, chapters: ArrayList<Chapter>): Int {
        var i = 0
        for (chapter in chapters) {
            if (chapter.chapter == chap) {
                return i
            }
            i++
        }
        return 0

    }

    private fun fetchChapterDetailsNotification(clazz: String, course: String, chapter: String) {
        viewModel.getChapters(clazz, course).observe(viewLifecycleOwner, Observer { response ->
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

                    index = getIndex(chapter, response.data.data.chapter_list)
                    Log.d("index", index.toString())
                    chapterList = response.data.data.chapter_list
                    adapter.isCourseDetail = true
                    adapter.fromNotification = true
                    adapter.detailIndex = index
                    val displayMetrics = DisplayMetrics()
                    requireActivity().getWindowManager().getDefaultDisplay()
                        .getMetrics(displayMetrics)
                    adapter.notificationWidth = displayMetrics.widthPixels
                    adapter.cdnPath(response.data.data.cdn_path)
                    adapter.updateData(response.data.data.chapter_list)
                    subtopicsAdapter = SubtopicsAdapter(requireActivity(), eventTracker)
                    binding.rvSubtopics.layoutManager = LinearLayoutManager(requireActivity())
                    binding.rvSubtopics.adapter = subtopicsAdapter
                    binding.btnLearnNow.setBackgroundResource(Utils.getColorPair(index)[1])
                    binding.progressBar.visibility = View.GONE
                    viewModel.getChapterDetails(clazz, course, chapter)
                        .observe(viewLifecycleOwner, Observer { response1 ->
                            when (response1) {
                                is Outcome.Progress -> {
                                    binding.rvSubtopics.visibility = View.GONE
                                    binding.progressBar.visibility = View.VISIBLE
                                }
                                is Outcome.Failure -> {
                                    binding.progressBar.visibility = View.GONE
                                    val dialog = NetworkErrorDialog.newInstance()
                                    dialog.show(requireFragmentManager(), "NetworkErrorDialog")
                                }
                                is Outcome.ApiError -> {
                                    binding.progressBar.visibility = View.GONE
                                    apiErrorToast(response1.e)
                                }
                                is Outcome.Success -> {
                                    binding.rvSubtopics.visibility = View.VISIBLE
                                    binding.progressBar.visibility = View.GONE
                                    setChapterDetails(response1.data.data)
                                }
                            }
                        })
                }
            }
        })
    }


    private fun fetchChapterDetails() {
        val chapter = chapterList[index]
        var course: String

        if (chapter.course == null) {
            course = defaultPrefs(requireActivity()).getString(Constants.STUDENT_COURSE, "")
                .orDefaultValue()
        } else course = chapter.course!!

        viewModel.getChapterDetails(chapter.clazz, course, chapter.chapter)
            .observe(viewLifecycleOwner, Observer { response ->
                when (response) {
                    is Outcome.Progress -> {
                        binding.rvSubtopics.visibility = View.GONE
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
                    is Outcome.BadRequest -> {
                        binding.progressBar.visibility = View.GONE
                        val dialog = BadRequestDialog.newInstance("unauthorized")
                        dialog.show(requireFragmentManager(), "BadRequestDialog")
                    }
                    is Outcome.Success -> {
                        binding.rvSubtopics.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        setChapterDetails(response.data.data)

                    }
                }
            })
    }


    private fun setChapterDetails(detail: ChapterDetail) {
        binding.tvChapter.text = detail.stats.chapter_display
        binding.tvChapterStat.text =
            detail.stats.total_duration!!.toString() + " " + getString(R.string.string_minutes) + " " + detail.stats.total_videos + " " + getString(
                R.string.string_concepts
            )
        binding.ivChapter.setBackgroundResource(Utils.getColorPair(index)[1])
        Glide.with(requireActivity()).load(detail.stats.pathImage).into(binding.ivChapter)
        subtopicsAdapter.colorIndex = Utils.getColorIndex(index)
        subtopicsAdapter.updateData(detail.subtopics)
        subtopicsAdapter.updateChapter(detail.stats.chapter)
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = activity?.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    private fun sendEventByChapter(eventName: String, chapter: String) {
        activity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_CHAPTER_DETAILS_FRAGMENT)
                .addEventParameter(Constants.CHAPTER, chapter)
                .track()
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCourseDetailBinding {
        return FragmentCourseDetailBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): CourseDetailViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }
}
