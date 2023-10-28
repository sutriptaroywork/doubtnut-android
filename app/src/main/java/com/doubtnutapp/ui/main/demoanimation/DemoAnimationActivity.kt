package com.doubtnutapp.ui.main.demoanimation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.PermissionUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.SendEventOfDemoAnimation
import com.doubtnutapp.base.ShowSampleQuestion
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.databinding.ActivityDemoAnimationBinding
import com.doubtnutapp.domain.camerascreen.entity.CameraSettingEntity
import com.doubtnutapp.domain.camerascreen.entity.DemoAnimationEntity
import com.doubtnutapp.domain.camerascreen.entity.SubjectEntity
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.main.samplequestion.SampleQuestionAdapter
import com.doubtnutapp.utils.NetworkUtils

/**
 * Created by Sachin Saxena on 2020-04-10.
 */

class DemoAnimationActivity :
    BaseBindingActivity<DemoAnimationActivityViewModel, ActivityDemoAnimationBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "DemoAnimationActivity"
        const val INTENT_EXTRA_SCROLL_TO_POSITION = "scrollToPosition"
        const val INTENT_EXTRA_SOURCE = "source"
        fun getStartIntent(context: Context, position: Int, source: String) =
            Intent(context, DemoAnimationActivity::class.java).apply {
                putExtra(INTENT_EXTRA_SCROLL_TO_POSITION, position)
                putExtra(INTENT_EXTRA_SOURCE, source)
            }
    }

    override fun performAction(action: Any) {
        when (action) {
            is ShowSampleQuestion -> {
                viewModel.sendEvent(
                    EventConstants.EVENT_DEMO_QUESTION_CLICKED,
                    hashMapOf(),
                    ignoreSnowplow = true
                )
                openCamera(action.subjectEntity?.imageUrl.orEmpty())
            }
            is SendEventOfDemoAnimation -> {
                viewModel.sendEvent(
                    EventConstants.EVENT_DEMO_ANIMATION_POP_UP_CLICKED,
                    hashMapOf()
                )
            }
        }
    }

    private val scrollToPosition: Int by lazy {
        intent.getIntExtra(INTENT_EXTRA_SCROLL_TO_POSITION, 0) ?: 0
    }
    private val source: String by lazy {
        intent.getStringExtra(INTENT_EXTRA_SOURCE) ?: ""
    }

    private var subjectList: List<SubjectEntity?>? = null

    private val demoAnimationAdapter: DemoAnimationAdapter by lazy {
        DemoAnimationAdapter(
            this,
            "V2"
        )
    }

    private val sampleQuestionAdapter: SampleQuestionAdapter by lazy { SampleQuestionAdapter(this) }

    override fun provideViewBinding(): ActivityDemoAnimationBinding =
        ActivityDemoAnimationBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DemoAnimationActivityViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        viewModel.getDemoAnimation()
        viewModel.getCameraSetting(
            PermissionUtils.hasPermissions(
                this@DemoAnimationActivity,
                arrayOf(Manifest.permission.CAMERA)
            )
        )
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.closeDemo.setOnClickListener {
            viewModel.sendEvent(
                EventConstants.EVENT_DEMO_ANIMATION_POP_UP_CLOSED,
                hashMapOf(),
                ignoreSnowplow = true
            )
            finish()
        }

        binding.sampleQuestionRecyclerView.setOnClickListener {
            subjectList?.let {
                if (it.isNotEmpty()) {
                    performAction(ShowSampleQuestion(it[0]))
                }
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.cameraSettingConfig.observeK(
            this,
            this::onCameraSettingSuccess,
            this::onCameraApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandlerCamera,
            this::updateCameraProgressBarState
        )

        viewModel.demoAnimationList.observeK(
            this,
            this::onDemoAnimationSuccess,
            this::onCameraApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandlerCamera,
            this::updateCameraProgressBarState
        )

        viewModel.message.observeK(
            this,
            this::onMessage,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateCameraProgressBarState
        )
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgressBarState(state: Boolean) {}

    private fun onMessage(screenTitle: String) {}

    private fun onDemoAnimationSuccess(demoAnimationList: List<DemoAnimationEntity>) {
        for ((index, value) in demoAnimationList.withIndex()) {
            demoAnimationList[index].zipFileName = "lottie_animation" + (index % 3) + ".zip"
        }

        setUpDemoAnimationRecyclerView(demoAnimationList)
    }

    private fun onCameraSettingSuccess(cameraSettingEntity: CameraSettingEntity) {
        if (cameraSettingEntity.bottomOverlay?.subjectList != null) {
            subjectList = cameraSettingEntity.bottomOverlay?.subjectList!!
            setUpSampleQuestionRecyclerView(cameraSettingEntity.bottomOverlay?.subjectList!!)
        }
    }

    private fun setUpSampleQuestionRecyclerView(subjectEntityList: List<SubjectEntity?>) {
        binding.sampleQuestionRecyclerView.adapter = sampleQuestionAdapter
        sampleQuestionAdapter.updateData(subjectEntityList.subList(0, 1))
    }

    private fun setUpDemoAnimationRecyclerView(animEntities: List<DemoAnimationEntity>) {
        binding.demoVideoList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.demoVideoList.adapter = demoAnimationAdapter
        demoAnimationAdapter.updateData(animEntities, scrollToPosition)

        binding.demoVideoList.smoothScrollToPosition(scrollToPosition)
    }

    private fun openCamera(cropImageUrl: String?) {
        CameraActivity.getStartIntent(this, TAG).also { intent ->
            cropImageUrl?.let {
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.putExtra(Constants.CROP_IMAGE_URL, it)
            }
            startActivity(intent)
        }
    }

    private fun onCameraApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandlerCamera() {}

    private fun updateCameraProgressBarState(state: Boolean) {}

}