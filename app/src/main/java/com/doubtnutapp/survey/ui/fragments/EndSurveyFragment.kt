package com.doubtnutapp.survey.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.doubtnut.core.utils.immediateParentViewModelStoreOwner
import com.doubtnutapp.databinding.FragmentEndSurveyBinding
import com.doubtnutapp.loadImage
import com.doubtnutapp.survey.viewmodel.UserSurveyViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment

class EndSurveyFragment : BaseBindingFragment<UserSurveyViewModel, FragmentEndSurveyBinding>() {

    companion object {
        const val TAG = "EndSurveyFragment"
        fun newInstance() = EndSurveyFragment()
    }

    private var dismissSurveyListener: DismissSurveyListener? = null

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEndSurveyBinding =
        FragmentEndSurveyBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): UserSurveyViewModel {
        val vm: UserSurveyViewModel by viewModels(
            ownerProducer = { immediateParentViewModelStoreOwner }
        ) { viewModelFactory }
        return vm
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        mBinding?.btNext?.setOnClickListener {
            dismissSurveyListener?.dismissSurvey()
        }
    }

    fun setDismissSurveyListener(dismissSurveyListener: DismissSurveyListener) {
        this.dismissSurveyListener = dismissSurveyListener
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.surveyEndingData.observe(viewLifecycleOwner, Observer { endData ->
            mBinding?.tvTitle?.text = endData.endingHeading
            mBinding?.tvMessage?.text = endData.endingSubHeading
            mBinding?.ivEndSurvey?.loadImage(endData.endingImg)
            mBinding?.btNext?.text = endData.endingButtonText
        })
    }

}