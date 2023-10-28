package com.doubtnutapp.matchquestion.ui.fragment.dialog

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.immediateParentViewModelStoreOwner
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.databinding.FragmentAskNoMatchBinding
import com.doubtnutapp.domain.camerascreen.entity.CameraSettingEntity
import com.doubtnutapp.domain.camerascreen.entity.SubjectEntity
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.matchquestion.viewmodel.MatchQuestionViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog

/**
 * Created by Sachin Saxena on 2020-04-10.
 */

class HandWrittenQuestionDialogFragment :
    BaseBindingDialogFragment<MatchQuestionViewModel, FragmentAskNoMatchBinding>() {

    companion object {
        const val TAG = "HandWrittenQuestionDialogFragment"
        fun newInstance() = HandWrittenQuestionDialogFragment()
    }

    private var subjectList: List<SubjectEntity?>? = null

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAskNoMatchBinding =
        FragmentAskNoMatchBinding.inflate(layoutInflater)

    override fun provideViewModel(): MatchQuestionViewModel {
        val matchQuestionViewModel: MatchQuestionViewModel by viewModels(
            ownerProducer = { immediateParentViewModelStoreOwner }
        ) { viewModelFactory }
        return matchQuestionViewModel
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener { dialogInterface, i, keyEvent ->

            if (i == KeyEvent.KEYCODE_BACK && keyEvent?.action == KeyEvent.ACTION_UP) {

                activity?.finish()
            }
            return@setOnKeyListener false
        }

        viewModel.getCameraSetting()
        viewModel.sendEvent(EventConstants.RANDOM_IMAGE_POP_UP_VISIBLE, hashMapOf(), ignoreSnowplow = true)
        setClickListeners()
    }

    override fun setupObservers() {
        viewModel.cameraSettingConfig.observeK(
            this,
            this::onCameraSettingSuccess,
            this::onCameraApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandlerCamera,
            this::updateCameraProgressBarState
        )
    }

    private fun onCameraSettingSuccess(cameraSettingEntity: CameraSettingEntity) {
        cameraSettingEntity.bottomOverlay?.subjectList?.let {
            if (it.isNotEmpty()) {
                subjectList = it
                mBinding?.sampleImage?.loadImageEtx(it[0]?.imageUrl)
            }
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
    }

    private fun setClickListeners() {

        mBinding?.tryDemoQuestion?.setOnClickListener {
            showTrySampleQuestion()
        }

        mBinding?.sampleImage?.setOnClickListener {
            showTrySampleQuestion()
        }

        mBinding?.askQuestion?.setOnClickListener {

            viewModel.sendEvent(EventConstants.RANDOM_IMAGE_POP_UP_CLICKED_ASK_NEW, hashMapOf(), ignoreSnowplow = true)

            openCamera(null)
            dialog?.dismiss()
            activity?.finish()
        }
    }

    private fun showTrySampleQuestion() {

        if (context == null) return

        viewModel.sendEvent(EventConstants.RANDOM_IMAGE_POP_UP_CLICKED_DEMO, hashMapOf(), ignoreSnowplow = true)

        subjectList?.let {

            if (it.isNotEmpty()) {
                openCamera(it[0]?.imageUrl)
                dialog?.dismiss()
                activity?.finish()
            }
        }
    }

    private fun openCamera(cropImageUrl: String?) {
        context?.let { context ->
            CameraActivity.getStartIntent(context, TAG, cropImageUrl).also { intent ->
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val params = view?.layoutParams
        params?.width = getScreenWidth().toInt()
        view?.layoutParams = params
    }

    private fun getScreenWidth(): Double {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        return displayMetrics.widthPixels / 1.2
    }

    private fun onCameraApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandlerCamera() {}

    private fun updateCameraProgressBarState(state: Boolean) {}

}