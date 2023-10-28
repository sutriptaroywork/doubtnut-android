package com.doubtnutapp.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.toast
import com.doubtnutapp.R

import com.doubtnutapp.base.RatingCheckboxClicked
import com.doubtnutapp.base.RatingCheckboxUnchecked
import com.doubtnutapp.common.model.PopUpSubDataModel
import com.doubtnutapp.databinding.StudentRatingDialogBinding
import com.doubtnutapp.hide
import com.doubtnutapp.home.adapter.StudentRatingOptionsAdapter
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingFragment

class StudentRatingDialogFragment :
    BaseBindingFragment<HomeFragmentViewModel, StudentRatingDialogBinding>(),
    ActionPerformer {

    private var studentRatingDataModel: PopUpSubDataModel? = null

    private val optionsList = mutableListOf<String>()

    private var rating: Int = 0

    private val notSelectedStars = listOf<Int>(R.drawable.ic_star_one_no, R.drawable.ic_star_two_no, R.drawable.ic_star_three_no, R.drawable.ic_star_four_no, R.drawable.ic_star_five_no)
    private val selectedStars = listOf<Int>(R.drawable.ic_star_one_yes, R.drawable.ic_star_two_yes, R.drawable.ic_star_three_yes, R.drawable.ic_star_four_yes, R.drawable.ic_star_five_yes)

    companion object {
        const val TAG = "StudentRatingDialogFragment"
        const val STUDENT_RATING = "student_rating"
        const val FRAGMENT_RATING_TAG = "rating_tag"

        fun newInstance(studentRatingDataModel: PopUpSubDataModel) = StudentRatingDialogFragment().apply {
            val bundle = Bundle()
            bundle.putParcelable(STUDENT_RATING, studentRatingDataModel)
            arguments = bundle
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            studentRatingDataModel = it.getParcelable(STUDENT_RATING)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpObservers()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding?.ratingForumGroup?.hide()
        mBinding?.ratingDialogHeader?.text = studentRatingDataModel?.header
        mBinding?.ratingSubheader?.text = studentRatingDataModel?.subHeader

        val starView = view.findViewById<LinearLayout>(R.id.starView)
        setStarView(starView)

        val adapter = StudentRatingOptionsAdapter(this)
        adapter.updateFeeds(studentRatingDataModel!!.options)
        mBinding?.ratingListView?.adapter = adapter
        mBinding?.ratingListView?.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        mBinding?.submitRating?.setOnClickListener {
            if(mBinding?.issueEditText?.text.toString().isNotEmpty())
                optionsList.add(mBinding?.issueEditText?.text.toString())

            viewModel.submitStudentFeedback(rating, optionsList)
            toast(getString(R.string.thanks_feedback))
        }

        mBinding?.studentRatingCross?.setOnClickListener {
            viewModel.studentRatingCross()
            viewModel.removeStudentRatingFragment()
        }

    }

    private fun setUpObservers() {
        viewModel.studentRatingSubmitLiveData.observe(requireActivity()) {
            if (it) {
                if (rating == notSelectedStars.size)
                    viewModel.removeStudentRatingFragment()
            }
        }

        viewModel.studentFeedbackSubmitLiveData.observe(requireActivity()) {
            if (it) {
                viewModel.removeStudentRatingFragment()
            }
        }
    }

    private fun setStarView(linearLayout: LinearLayout) {
        val viewList = mutableListOf<ImageView>()

        notSelectedStars.forEachIndexed { index, it ->
            val imageView = ImageView(context)
            imageView.setImageResource(it)

            val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(10, 0, 10, 0)

            imageView.setOnClickListener {
                changeStarBorder(index, viewList)
            }

            linearLayout.addView(imageView, layoutParams)
            viewList.add(imageView)
        }
    }

    private fun changeStarBorder(index: Int, viewList: List<ImageView>) {
        rating = index + 1
        viewList.forEachIndexed { ind, imageView ->
            if(ind <= index)
                imageView.setImageResource(selectedStars[ind])
            else
                imageView.setImageResource(notSelectedStars[ind])
        }

        if(rating == notSelectedStars.size) {
            viewModel.startRateScreen(rating)
            viewModel.startRatingScreen()
        }
        else
            mBinding?.ratingForumGroup?.show()
    }

    override fun performAction(action: Any) {
        when(action) {
            is RatingCheckboxClicked -> {
                if(!optionsList.contains(action.text))
                    optionsList.add(action.text)
            }

            is RatingCheckboxUnchecked -> {
                if (optionsList.contains(action.text))
                    optionsList.remove(action.text)
            }
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): StudentRatingDialogBinding {
        return StudentRatingDialogBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): HomeFragmentViewModel {
        return ViewModelProviders.of(requireParentFragment()).get(HomeFragmentViewModel::class.java)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }
}