package com.doubtnutapp.ui.main.samplequestion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.ShowSampleQuestion
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.camera.viewmodel.CameraActivityViewModel
import com.doubtnutapp.databinding.FragmentSampleQuestionBinding
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.domain.camerascreen.entity.CameraSettingEntity
import com.doubtnutapp.domain.camerascreen.entity.SubjectEntity
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.BitmapUtils
import dagger.android.AndroidInjection
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class SampleQuestionFragment :
    BaseBindingFragment<CameraActivityViewModel, FragmentSampleQuestionBinding>(),
    ActionPerformer {

    override fun performAction(action: Any) {
        if (action is ShowSampleQuestion) {

            if (activity == null) return

            defaultPrefs().edit {
                putBoolean(CameraActivity.SHOULD_SHOW_SAMPLE_QUESTION_BOTTOM_LAYOUT, false)
            }

            viewModel.sendEvent(EventConstants.EVENT_DEMO_QUESTION_CLICKED, hashMapOf(), ignoreSnowplow = true)
            trySampleQuestion(action.subjectEntity?.imageUrl.orEmpty())
        }
    }

    private var subjectList: List<SubjectEntity?>? = null

    private val disposable: CompositeDisposable = CompositeDisposable()

    val adapter: SampleQuestionAdapter by lazy { SampleQuestionAdapter(this) }

    companion object {

        const val TAG = "SampleQuestionFragment"

        fun newInstance() = SampleQuestionFragment().apply {
            val bundle = Bundle()
            arguments = bundle
        }
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSampleQuestionBinding =
        FragmentSampleQuestionBinding.inflate(layoutInflater)

    override fun provideViewModel(): CameraActivityViewModel {
        val cameraViewModel: CameraActivityViewModel by viewModels(
            ownerProducer = { requireActivity() }
        ) { viewModelFactory }
        return cameraViewModel
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setClickListeners()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(activity)
        super.onCreate(savedInstanceState)
    }

    private fun setClickListeners() {
        binding.closeDemoQuestion.setOnClickListener {
            viewModel.removeSampleQuestionFragment()
        }

        binding.demoQuestionRecyclerView.setOnClickListener {
            showTrySampleQuestion()
        }

        binding.demoQuestionTitle.setOnClickListener {
            showTrySampleQuestion()
        }

        binding.tryDemoQuestion.setOnClickListener {
            showTrySampleQuestion()
        }
    }

    private fun showTrySampleQuestion() {
        subjectList?.let {
            if (it.isNotEmpty()) {
                performAction(ShowSampleQuestion(it[0]))
            }
        }
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

        if (cameraSettingEntity.bottomOverlay?.subjectList != null) {
            subjectList = cameraSettingEntity.bottomOverlay?.subjectList!!
            setUpDemoQuestionRecyclerView(cameraSettingEntity.bottomOverlay?.subjectList!!)
        }

    }

    private fun setUpDemoQuestionRecyclerView(subjectEntityList: List<SubjectEntity?>) {
        binding.demoQuestionRecyclerView.adapter = adapter
        adapter.updateData(subjectEntityList)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
    }

    private fun trySampleQuestion(imageUrl: String) {
        context?.let { currentContext ->
            disposable.add(Single.fromCallable {
                BitmapUtils.getBitmapFromUrl(currentContext, imageUrl)
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ currentBitmap ->
                    if (currentBitmap != null) {
                        BitmapUtils.convertBitmapToByteArray(currentBitmap) { byteArray ->
                            byteArray?.let {
                                viewModel.processCameraImage(it)
                                viewModel.removeSampleQuestionFragment()
                            }
                        }
                    }
                }, {
                    context?.let { currentContext ->
                        ToastUtils.makeText(
                            currentContext,
                            currentContext.getString(R.string.somethingWentWrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            )
        }
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }

    private fun onCameraApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandlerCamera() {}

    private fun updateCameraProgressBarState(state: Boolean) {}

}