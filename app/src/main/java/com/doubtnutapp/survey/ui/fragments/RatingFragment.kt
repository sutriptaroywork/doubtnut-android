package com.doubtnutapp.survey.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.doubtnut.core.utils.immediateParentViewModelStoreOwner
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentRatingBinding
import com.doubtnutapp.survey.viewmodel.UserSurveyViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams


class RatingFragment : BaseBindingFragment<UserSurveyViewModel, FragmentRatingBinding>() {

    companion object {

        const val TAG = "RatingFragment"

        @JvmStatic
        fun newInstance(position: Int) =
            RatingFragment().apply {
                val bundle = Bundle()
                bundle.putInt(UserSurveyBottomSheetFragment.ITEM_POSITION, position)
                arguments = bundle
            }
    }

    private var thumbPosition: Int? = null

    private val questionPosition: Int? by lazy {
        arguments?.getInt(UserSurveyBottomSheetFragment.ITEM_POSITION)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRatingBinding {
        return FragmentRatingBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): UserSurveyViewModel {
        val vm: UserSurveyViewModel by viewModels(
            ownerProducer = { immediateParentViewModelStoreOwner }
        ) { viewModelFactory }
        return vm
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpListeners()
    }

    private fun setUpListeners() {
        mBinding?.btNext?.setOnClickListener {

            if (viewModel.questionList.value == null || questionPosition == null) return@setOnClickListener

            when {
                thumbPosition != null -> {
                    viewModel.storeSurveyFeedback("rating", questionPosition)
                }
                else -> viewModel.loadNextQuestion()
            }
        }

        mBinding?.seekBar?.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams) {}

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {}
            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
                when (mBinding?.seekBar?.progress) {
                    1 -> {
                        mBinding?.tvRating1?.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_rating_1_enable
                            ),
                            null,
                            null
                        )
                        mBinding?.tvRating10?.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_rating_10_disable
                            ),
                            null,
                            null
                        )
                    }
                    10 -> {
                        mBinding?.tvRating1?.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_rating_1_disable
                            ),
                            null,
                            null
                        )
                        mBinding?.tvRating10?.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_rating_10_enable
                            ),
                            null,
                            null
                        )
                    }
                    else -> {
                        mBinding?.tvRating1?.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_rating_1_disable
                            ),
                            null,
                            null
                        )
                        mBinding?.tvRating10?.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_rating_10_disable
                            ),
                            null,
                            null
                        )
                    }
                }

                thumbPosition = seekBar.progress
                viewModel.questionList.value!![questionPosition!!].feedback =
                    thumbPosition.toString()

                mBinding?.btNext?.isEnabled = true
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.questionList.observe(
            viewLifecycleOwner
        ) { questionList ->
            questionPosition?.let { position ->
                val ratingQuestion = questionList[position]
                mBinding?.tvTitle?.text = ratingQuestion.questionText
                mBinding?.btNext?.text = ratingQuestion.nextText
                ratingQuestion.feedback?.let { mBinding?.seekBar?.setProgress(it.toFloat()) }
                mBinding?.btNext?.isEnabled = ratingQuestion.feedback != null
            }
        }
    }


}