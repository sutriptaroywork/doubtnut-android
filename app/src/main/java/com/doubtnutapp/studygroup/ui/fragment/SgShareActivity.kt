package com.doubtnutapp.studygroup.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.ActivitySgShareBinding
import com.doubtnutapp.doubtpecharcha.model.QuestionData
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.studygroup.model.SgUserBannedBottomSheet
import com.doubtnutapp.studygroup.viewmodel.StudyGroupListViewModel
import com.doubtnutapp.studygroup.viewmodel.StudyGroupViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.videoPage.model.QuestionToShare
import kotlinx.android.synthetic.main.fragment_otp_over_call.*
import javax.inject.Inject

class SgShareActivity : BaseBindingActivity<StudyGroupListViewModel,
        ActivitySgShareBinding>(), ActionPerformer {

    companion object {
        const val TAG = "SgShareBottomSheetFragment"
        private const val INTENT_EXTRA_QUESTION_DATA = "question_data"
        private const val DOUBT_PE_CHARCHA_QUESTION_DEEPLINK = "doubt_pe_charcha_question_deeplink"
        private const val DOUBT_PE_CHARCHA_QUESTION_MESSAGE = "doubt_pe_charcha_question_message"
        fun getStartIntent(context: Context, questionData: QuestionToShare): Intent =
            Intent(context, SgShareActivity::class.java).apply {
                putExtra(INTENT_EXTRA_QUESTION_DATA, questionData)
            }

        fun getStartIntentDoubtPeCharchaQuestion(
            context: Context,
            message: String?,
            deeplink: String?,
        ): Intent =
            Intent(context, SgShareActivity::class.java).apply {
                putExtra(DOUBT_PE_CHARCHA_QUESTION_MESSAGE, message)
                putExtra(DOUBT_PE_CHARCHA_QUESTION_DEEPLINK, deeplink)
            }
    }

    @Inject
    lateinit var userPreference: UserPreference

    private lateinit var studyGroupViewModel: StudyGroupViewModel

    private val questionData: QuestionToShare? by lazy {
        intent?.getParcelableExtra(
            INTENT_EXTRA_QUESTION_DATA
        )
    }

    private val doubtPeCharchaQuestionDeeplink: String? by lazy {
        intent?.getStringExtra(
            DOUBT_PE_CHARCHA_QUESTION_DEEPLINK
        )
    }
    private val doubtPeCharchaMessage: String? by lazy {
        intent?.getStringExtra(
            DOUBT_PE_CHARCHA_QUESTION_MESSAGE
        )
    }

    override fun providePageName(): String = TAG

    override fun provideViewModel(): StudyGroupListViewModel {
        studyGroupViewModel = viewModelProvider(viewModelFactory)
        return viewModelProvider(viewModelFactory)
    }

    override fun provideViewBinding(): ActivitySgShareBinding =
        ActivitySgShareBinding.inflate(layoutInflater)

    override fun setupView(savedInstanceState: Bundle?) {
        updateUi()
        setUpListeners()
        // Check if user is banned from study group, show them non cancellable bottom sheet
        studyGroupViewModel.checkUserBannedStatus()
    }

    override fun setupObservers() {
        super.setupObservers()
        studyGroupViewModel.userBannedStatusLiveData.observeEvent(this) {
            if (it.isBanned && it.bottomSheet != null) {
                showUserBannedStatusBottomSheet(it.bottomSheet)
            }
        }

        viewModel.enableSendBtnStatus.observe(this) {
            binding.btSend.isEnabled = it
        }

        viewModel.messageToMultipleGroups.observe(this) { isSubmitted ->
            if (isSubmitted) {
                if(doubtPeCharchaQuestionDeeplink.isNotNullAndNotEmpty()){
                    toast(R.string.doubt_shared_successfully)
                }
                else {
                    toast(R.string.video_shared_successfully)
                }
                viewModel.sendEvent(
                    EventConstants.MESSAGE_SHARED_TO_SG,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.SHARED_CONTENT, "video_solution")
                        put(EventConstants.QUESTION_ID, questionData?.questionId.orEmpty())
                    }, ignoreSnowplow = true
                )
                finish()
            }
        }

        viewModel.progressLiveData.observe(this) {
            binding.btSend.isEnabled = it.not()
        }
    }

    private fun showUserBannedStatusBottomSheet(bottomSheet: SgUserBannedBottomSheet?) {
        bottomSheet ?: return
        val userBannedStatusBottomSheet =
            SgUserBannedStatusBottomSheetFragment.newInstance(bottomSheet)
        userBannedStatusBottomSheet.show(
            supportFragmentManager,
            SgUserBannedStatusBottomSheetFragment.TAG
        )
    }

    private fun setUpListeners() {
        binding.apply {
            btSend.setOnClickListener {
                questionData?.let { questionToShare ->
                    sendVideoThumbnail(
                        imageUrl = questionToShare.thumbnail,
                        ocrText = questionToShare.ocrText,
                        questionId = questionToShare.questionId,
                    )
                    viewModel.sendEvent(
                        EventConstants.SG_VIDEO_SHARE,
                        hashMapOf<String, Any>().apply {
                            put(Constants.QUESTION_ID, questionToShare.questionId)
                        })
                }

                doubtPeCharchaQuestionDeeplink?.let { questionToShare ->
                    val message = "${doubtPeCharchaMessage}\n${doubtPeCharchaQuestionDeeplink}"
                    sendDoubtPeCharchaQuestion(message)
                }

            }

            ivBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun sendVideoThumbnail(imageUrl: String, ocrText: String, questionId: String) {
        val videoWidget = viewModel.getVideoThumbnailParentWidget(
            imageUrl = imageUrl, ocrText = ocrText, questionId = questionId,
            page = Constants.PAGE_STUDY_GROUP,
            roomId = null
        )
        viewModel.postMessageToMultipleGroups(videoWidget)
    }

    private fun sendDoubtPeCharchaQuestion(message: String) {
        val videoWidget =
            viewModel.getParentTextWidget(message, null, userPreference.getUserStudentId())
        viewModel.postMessageToMultipleGroups(videoWidget)
    }

    private fun updateUi() {
        binding.apply {
            createGroupLayout.isVisible = true
            btSend.isEnabled = false
            binding.apply {
                val selectFriendFragment = SgMyGroupsFragment.newInstance(SgMyGroupsFragment.SHARE)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.shareVideoFragmentContainer, selectFriendFragment).commit()
            }
        }
    }

    override fun performAction(action: Any) {}
}