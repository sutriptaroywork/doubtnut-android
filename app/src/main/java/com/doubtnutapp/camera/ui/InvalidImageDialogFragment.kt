package com.doubtnutapp.camera.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.doubtnut.core.data.LottieAnimDataStore
import com.doubtnut.core.utils.LottieAnimationViewUtils.applyAnimationFromUrl
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.databinding.FragmentInvalidImageDialogBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class InvalidImageDialogFragment :
    BaseBindingDialogFragment<DummyViewModel, FragmentInvalidImageDialogBinding>() {

    companion object {

        const val TAG = "InvalidImageDialogFragment"
        const val CALLING_SCREEN = "CALLING_SCREEN"

        fun newInstance(callingScreen: String? = null) = InvalidImageDialogFragment().apply {
            val bundle = Bundle()
            bundle.putString(CALLING_SCREEN, callingScreen)
            arguments = bundle
        }
    }

    @Inject
    lateinit var lottieAnimDataStore: LottieAnimDataStore

    private var callingScreen: String? = null

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentInvalidImageDialogBinding =
        FragmentInvalidImageDialogBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBinding?.apply {
            lifecycleScope.launchWhenResumed {
                val invalidImageAnimationUrl =
                    lottieAnimDataStore.invalidImageAnimationUrl.firstOrNull()
                demoVideo.applyAnimationFromUrl(invalidImageAnimationUrl)
            }

            mainLayout.setOnClickListener {
                dismissScreen()
            }
            dialogCard.setOnClickListener(null)
            buttonAskQuestion.setOnClickListener {
                dismissScreen()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar)
        callingScreen = arguments?.getString(CALLING_SCREEN)
    }

    private fun dismissScreen() {
        dismiss()
        if (callingScreen == CropQuestionActivity.TAG) {
            activity?.finish()
        }
    }
}
