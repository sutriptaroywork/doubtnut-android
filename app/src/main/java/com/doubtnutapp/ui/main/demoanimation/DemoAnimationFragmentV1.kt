package com.doubtnutapp.ui.main.demoanimation

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.PagerSnapHelper
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.SendEventOfDemoAnimation
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.camera.viewmodel.CameraActivityViewModel
import com.doubtnutapp.databinding.FragmentDemoAnimationV1Binding
import com.doubtnutapp.domain.camerascreen.entity.DemoAnimationEntity
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog

/**
 * Created by Sachin Saxena on 2020-04-10.
 */

class DemoAnimationFragmentV1 :
    BaseBindingFragment<CameraActivityViewModel, FragmentDemoAnimationV1Binding>(),
    ActionPerformer {

    companion object {
        const val TAG = "DemoAnimationFragmentV1"
        const val SCROLL_TO_POSITION = "scrollToPosition"
        const val SOURCE = "source"

        fun newInstance(position: Int, source: String) = DemoAnimationFragmentV1().apply {
            val bundle = Bundle()
            bundle.putInt(SCROLL_TO_POSITION, position)
            bundle.putString(SOURCE, source)
            arguments = bundle
        }
    }

    private val scrollToPosition: Int by lazy {
        arguments?.getInt(SCROLL_TO_POSITION, 0) ?: 0
    }
    private val source: String by lazy {
        arguments?.getString(SOURCE, "") ?: ""
    }

    private val demoAnimationAdapter: DemoAnimationAdapter by lazy {
        DemoAnimationAdapter(
            this,
            "V1"
        )
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDemoAnimationV1Binding =
        FragmentDemoAnimationV1Binding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): CameraActivityViewModel =
        activityViewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setClickListeners()
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.demoAnimationList.observeK(
            this,
            this::onDemoAnimationSuccess,
            this::onCameraApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandlerCamera,
            this::updateCameraProgressBarState
        )
    }

    private fun onDemoAnimationSuccess(demoAnimationList: List<DemoAnimationEntity>) {
        for ((index, value) in demoAnimationList.withIndex()) {
            demoAnimationList[index].zipFileName = "lottie_animation" + (index % 3) + ".zip"
        }
        setUpDemoAnimationRecyclerView(demoAnimationList)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
    }

    private fun setClickListeners() {
        binding.closeDemo.setOnClickListener {
            if (activity == null) return@setOnClickListener
            viewModel.removeDemoAnimationFragment()
            viewModel.sendEvent(EventConstants.EVENT_DEMO_ANIMATION_POP_UP_CLOSED, hashMapOf(), ignoreSnowplow = true)
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

    private fun getScreenHeight(): Double {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        return displayMetrics.heightPixels / 2.0
    }

    private fun setUpDemoAnimationRecyclerView(animEntities: List<DemoAnimationEntity>) {
        mBinding ?: return
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.demoVideoList)
        demoAnimationAdapter.updateData(animEntities, scrollToPosition)
        binding.demoVideoList.adapter = demoAnimationAdapter
        binding.circleIndicator.attachToRecyclerView(binding.demoVideoList, snapHelper)
        binding.demoVideoList.scrollToPosition(scrollToPosition)
    }

    private fun onCameraApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandlerCamera() {}

    private fun updateCameraProgressBarState(state: Boolean) {}

    override fun performAction(action: Any) {
        when (action) {
            is SendEventOfDemoAnimation -> {
                if (activity == null) return
                viewModel.sendEvent(EventConstants.EVENT_DEMO_ANIMATION_POP_UP_CLICKED, hashMapOf(), ignoreSnowplow = true)
            }
        }
    }

}