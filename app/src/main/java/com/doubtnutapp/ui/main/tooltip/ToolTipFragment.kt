package com.doubtnutapp.ui.main.tooltip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.camera.viewmodel.CameraActivityViewModel
import com.doubtnutapp.databinding.FragmentTooltipBinding
import com.doubtnutapp.domain.camerascreen.entity.CameraSettingEntity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.Utils
import dagger.android.AndroidInjection
import kotlinx.coroutines.delay

class ToolTipFragment : Fragment(), ActionPerformer {

    override fun performAction(action: Any) {}

    private lateinit var viewModel: CameraActivityViewModel

    private var clickedPosition = 0

    private lateinit var binding: FragmentTooltipBinding

    companion object {

        const val TAG = "ToolTipFragment"

        fun newInstance() = ToolTipFragment().apply {
            val bundle = Bundle()
            arguments = bundle
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(activity)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTooltipBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =
            ViewModelProvider(requireActivity()).get(CameraActivityViewModel::class.java)
        setUpObservers()
        setClickListeners()
    }

    override fun onStart() {
        super.onStart()
        val params = view?.layoutParams
        params?.width = (Utils.screenWidth / 1.8).toInt()
        view?.layoutParams = params
    }

    private fun setClickListeners() {
        binding.closeToolTip.setOnClickListener {

            if (activity == null) return@setOnClickListener

            viewModel.sendEvent(EventConstants.EVENT_JANE_KAISE_CROSSED, hashMapOf())
            viewModel.closeToolTipFragment()
        }

        binding.toolTipButton.setOnClickListener {

            if (activity == null) return@setOnClickListener

            viewModel.launchDemoAnimation(clickedPosition)
            viewModel.sendEvent(EventConstants.EVENT_JANE_KAISE_CLICKED, hashMapOf(), ignoreSnowplow = true)
        }
    }

    private fun setUpObservers() {
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

        if (!cameraSettingEntity.cameraButtonHint?.content.isNullOrEmpty()
            && cameraSettingEntity.cameraButtonHint?.durationSec?.toIntOrNull() != null
            && cameraSettingEntity.cameraButtonHint?.durationSec != "0"
        ) {

            val textList = cameraSettingEntity.cameraButtonHint?.content
            textList?.let { list ->
                if (list.isNotEmpty()) {
                    val initialDuration =
                        cameraSettingEntity.cameraButtonHint?.durationSec?.toIntOrNull()?.toLong()
                            ?: 1000L
                    var duration =
                        cameraSettingEntity.cameraButtonHint?.durationSec?.toIntOrNull()?.toLong()
                            ?: 1000L
                    for (i in 0..10) {
                        for ((index, value) in list.withIndex()) {
                            lifecycleScope.launchWhenStarted {
                                delay(duration)
                                clickedPosition = index + 1
                                binding.textViewTooltipTitle.text = value
                            }
                            duration += initialDuration
                        }
                    }
                }
            }
        }

    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
    }

    private fun onCameraApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandlerCamera() {}

    private fun updateCameraProgressBarState(
        @Suppress("UNUSED_PARAMETER") state: Boolean
    ) {
    }
}