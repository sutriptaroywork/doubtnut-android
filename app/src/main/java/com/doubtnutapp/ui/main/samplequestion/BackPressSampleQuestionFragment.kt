package com.doubtnutapp.ui.main.samplequestion

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.camera.viewmodel.CameraActivityViewModel
import com.doubtnutapp.databinding.FragmentBackPressSampleQuestionBinding
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.domain.camerascreen.entity.CameraSettingEntity
import com.doubtnutapp.domain.camerascreen.entity.SubjectEntity
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.BitmapUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class BackPressSampleQuestionFragment : BaseBindingDialogFragment<CameraActivityViewModel, FragmentBackPressSampleQuestionBinding>(), View.OnClickListener {

    private var subjectList: List<SubjectEntity?>? = null

    private val disposable: CompositeDisposable = CompositeDisposable()

    companion object {

        const val TAG = "BackPressSampleQuestionFragment"
        private const val CLOSE_ACTIVITY = "close_activity_on_back_press"

        fun newInstance(closeActivityOnBackPress: Boolean = false): BackPressSampleQuestionFragment {

            val fragment = BackPressSampleQuestionFragment()

            val bundle = Bundle().apply {
                putBoolean(CLOSE_ACTIVITY, closeActivityOnBackPress)
            }
            fragment.arguments = bundle

            return fragment
        }
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBackPressSampleQuestionBinding =
        FragmentBackPressSampleQuestionBinding.inflate(layoutInflater)

    override fun provideViewModel(): CameraActivityViewModel =
        activityViewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        setClickListeners()
        viewModel.sendEvent(EventConstants.EVENT_CAMERA_BACK_PRESS_POP_UP_VISIBLE, hashMapOf(), true)
    }

    private fun setClickListeners() {
        binding.tryButton.setOnClickListener(this)
        binding.sampleQuestionLayout.setOnClickListener(this)
        binding.skipSampleQuestion.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(getScreenWidth().toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun getScreenWidth(): Double {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        return displayMetrics.widthPixels / 1.5
    }

    private fun showTrySampleQuestion() {
        subjectList?.let {
            if (it.isNotEmpty()) {
                trySampleQuestion(it[0]?.imageUrl!!)
            }
        }
    }

    override fun setupObservers() {
        viewModel.cameraSettingConfig.observeK(this,
                this::onCameraSettingSuccess,
                this::onCameraApiError,
                this::unAuthorizeUserError,
                this::ioExceptionHandlerCamera,
                this::updateCameraProgressBarState
        )
    }

    private fun onCameraSettingSuccess(cameraSettingEntity: CameraSettingEntity) {

        subjectList = cameraSettingEntity.bottomOverlay?.subjectList

        subjectList?.let {
            if (it.isNotEmpty()) {
                binding.sampleQuestionImage.loadImage(it[0]?.imageUrl, null, null)
            }
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
    }

    private fun trySampleQuestion(imageUrl: String) {

        if (context == null) return

        disposable.add(Single.fromCallable {
            val currentBitmap = BitmapUtils.getBitmapFromUrl(requireContext(), imageUrl)
            if (currentBitmap != null) {
                BitmapUtils.getImageByteArray(currentBitmap)
            } else {
                null
            }

        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ it ->

                    if (context == null) return@subscribe

                    if (it != null) {
                        viewModel.processCameraImage(it)
                    } else {
                        ToastUtils.makeText(requireContext(), getString(R.string.somethingWentWrong), Toast.LENGTH_SHORT).show()
                    }
                    dismiss()
                }, {}))

    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    private fun onCameraApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandlerCamera() {}

    private fun updateCameraProgressBarState(state: Boolean) {}

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tryButton, R.id.sampleQuestionLayout -> {

                if (activity == null) return

                defaultPrefs().edit {
                    putBoolean(CameraActivity.SHOULD_SHOW_BACK_PRESS_SAMPLE_QUESTION, false)
                }
                viewModel.sendEvent(EventConstants.EVENT_CAMERA_BACK_PRESS_POP_UP_AGREED, hashMapOf(), ignoreSnowplow = true)
                showTrySampleQuestion()
            }

            R.id.skipSampleQuestion -> {
                viewModel.sendEvent(EventConstants.EVENT_CAMERA_BACK_PRESS_POP_UP_DENIED, hashMapOf(), ignoreSnowplow = true)
                if (arguments?.getBoolean(CLOSE_ACTIVITY) == true) {
                    requireActivity().onBackPressed()
                } else {
                    dismiss()
                }
            }
        }
    }

}