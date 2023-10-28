package com.doubtnutapp.survey.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.doubtnut.core.utils.immediateParentViewModelStoreOwner
import com.doubtnutapp.databinding.FragmentStartSurveyBinding
import com.doubtnutapp.loadImage
import com.doubtnutapp.survey.viewmodel.UserSurveyViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment

private const val ARG_MESSAGE = "message"
private const val ARG_IMAGE = "image"

class StartSurveyFragment : BaseBindingFragment<UserSurveyViewModel, FragmentStartSurveyBinding>() {

    companion object {

        const val TAG = "StartSurveyFragment"

        @JvmStatic
        fun newInstance() = StartSurveyFragment()
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentStartSurveyBinding =
        FragmentStartSurveyBinding.inflate(layoutInflater)

    override fun provideViewModel(): UserSurveyViewModel {
        val userSurveyViewModel: UserSurveyViewModel by viewModels(
            ownerProducer = { immediateParentViewModelStoreOwner }
        ) { viewModelFactory }
        return userSurveyViewModel
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpClickListeners()
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.surveyStartingData.observe(viewLifecycleOwner, Observer { startingData ->
            binding.tvIntroduction.text = startingData.startingHeading
            binding.ivIntroduction.loadImage(startingData.startingImg)
            binding.btContinue.text = startingData.startingButtonText
        })
    }

    private fun setUpClickListeners() {
        binding.btContinue.setOnClickListener {
            viewModel.loadNextQuestion()
        }
    }
}