package com.doubtnutapp.ui.course.microconcept

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.MicroConcept
import com.doubtnutapp.databinding.ActivityMicroconceptsBinding
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.BranchIOUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import org.json.JSONObject

class MicroconceptsActivity :
    BaseBindingActivity<MicroConceptViewModel, ActivityMicroconceptsBinding>() {

    companion object {
        const val INTENT_EXTRA_CLASS = "micro_concept_class"
        const val INTENT_EXTRA_CHAPTER = "micro_concept_chapter"
        const val INTENT_EXTRA_COURSE = "micro_concept_course"
        const val INTENT_EXTRA_SUBTOPIC = "micro_concept_subtopic"
        private val TAG = "MicroConceptsActivity"
    }

    private lateinit var microConceptViewModel: MicroConceptViewModel

    private var concepts: List<MicroConcept>? = null
    private lateinit var title: String

    private lateinit var eventTracker: Tracker

    private fun updateConceptCount(size: Int) {
        binding.tvSubtitle.text = "$size concepts"
    }

    private fun setupObserver() {
        microConceptViewModel.microConceptsLiveData.observeK(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )
    }

    private fun setupRecyclerView(microConcepts: List<MicroConcept>) {
        val adapter = MicroconceptListAdapter(this@MicroconceptsActivity, eventTracker)
        binding.rvConcepts.layoutManager = LinearLayoutManager(this)
        binding.rvConcepts.adapter = adapter
        adapter.updateData(microConcepts)

        binding.rvConcepts.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        binding.rvConcepts.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {

                val intent = VideoPageActivity.startActivity(
                    this@MicroconceptsActivity,
                    adapter.topics!![position].mc_id,
                    "",
                    "",
                    Constants.PAGE_SC,
                    adapter.topics!![position].clazz.toString(),
                    true,
                    "",
                    "",
                    false
                )
                sendEventByQID(
                    EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
                    adapter.topics!![position].mc_id
                )
                sendCleverTapEventByQID(
                    EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
                    adapter.topics!![position].mc_id,
                    Constants.PAGE_SC
                )

                // Send this event to Branch
//                BranchIOUtils.userCompletedAction(
//                    EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
//                    JSONObject().apply {
//                        put(
//                            EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
//                            adapter.topics!![position].mc_id
//                        )
//                    })

                startActivity(intent)
            }
        })
    }

    private fun getSubtopicData() {

        val clazz = intent.getStringExtra(INTENT_EXTRA_CLASS) ?: ""
        val course = intent.getStringExtra(INTENT_EXTRA_COURSE) ?: ""
        val chapter = intent.getStringExtra(INTENT_EXTRA_CHAPTER) ?: ""
        val subtopic = intent.getStringExtra(INTENT_EXTRA_SUBTOPIC) ?: ""

        microConceptViewModel.getMicroConcepts(clazz, course, chapter, subtopic)

    }

    private fun onSuccess(listMicroConcept: List<MicroConcept>) {
        setupRecyclerView(listMicroConcept)
        updateConceptCount(listMicroConcept.size)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun updateProgressBarState(state: Boolean) {
        binding.progress.setVisibleState(state)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun sendEvent(eventName: String) {
        this@MicroconceptsActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@MicroconceptsActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_MICRO_CONCEPTS_ACTIVITY)
                .track()
        }
    }

    private fun sendEventByQID(eventName: String, qid: String) {
        this@MicroconceptsActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@MicroconceptsActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_MICRO_CONCEPTS_ACTIVITY)
                .addEventParameter(Constants.QUESTION_ID, qid)
                .track()
        }
    }

    private fun sendCleverTapEventByQID(eventName: String, qid: String, pageName: String) {
        this@MicroconceptsActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@MicroconceptsActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_MICRO_CONCEPTS_ACTIVITY)
                .addEventParameter(Constants.QUESTION_ID, qid)
                .addEventParameter(Constants.PAGE, pageName)
                .addEventParameter(Constants.STUDENT_CLASS, getStudentClass())
                .cleverTapTrack()
        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this@MicroconceptsActivity.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    override fun provideViewBinding(): ActivityMicroconceptsBinding {
        return ActivityMicroconceptsBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): MicroConceptViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        statusbarColor(this, R.color.pink_dark)
        eventTracker = getTracker()

        concepts = intent.getParcelableArrayListExtra("concepts")
        title = intent.getStringExtra(INTENT_EXTRA_SUBTOPIC) ?: ""

        binding.tvTitle.text = title

        sendEvent(EventConstants.EVENT_NAME_MICRO_CONCEPTS_PAGE)

        microConceptViewModel =
            ViewModelProviders.of(this, viewModelFactory).get(MicroConceptViewModel::class.java)

        setupObserver()


        this.concepts?.let {
            setupRecyclerView(it)
            updateConceptCount(it.size)
        } ?: getSubtopicData()
    }

}
