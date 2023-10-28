package com.doubtnutapp.ui.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.LayoutNpsFeedbackBinding
import com.doubtnutapp.model.AppActiveFeedback
import com.doubtnutapp.ui.answer.DislikeFeedbackAdapter
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.google.android.material.bottomsheet.BottomSheetBehavior


class NPSFeedbackFragment :
    BaseBindingBottomSheetDialogFragment<FeedbackViewModel, LayoutNpsFeedbackBinding>() {

    companion object {
        const val TAG = "NPSFeedbackFragment"
        fun newInstance(feedback: AppActiveFeedback): NPSFeedbackFragment {
            val fragment = NPSFeedbackFragment()
            val args = Bundle()
            args.putParcelable(Constants.FEEDBACK_OBJECT, feedback)
            fragment.arguments = args
            return fragment
        }
    }

    private var mBehavior: BottomSheetBehavior<*>? = null
    val adapter = DislikeFeedbackAdapter()
    private lateinit var adapterNPS: NPSAdapter
    var myList: MutableList<Int>? = mutableListOf()
    private lateinit var feedback: AppActiveFeedback

    var selectedRating: String = "11"

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutNpsFeedbackBinding {
        return LayoutNpsFeedbackBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String = TAG

    override fun provideViewModel(): FeedbackViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        feedback = arguments?.getParcelable(Constants.FEEDBACK_OBJECT)!!
        submitFeedback()
        for (i in 1..10) {
            myList?.add(i)
        }

        adapterNPS = NPSAdapter()
        binding.rvRate.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRate.adapter = adapterNPS
        adapterNPS.updateData(myList)


        binding.tvtitleFeedback.text = feedback.title
        binding.btnSubmit.text = feedback.submit
        binding.tvQuestion.text = feedback.question

        binding.rvRate.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                adapterNPS.updateView(adapterNPS.dataList[position])
                selectedRating = (adapterNPS.dataList[position]).toString()
            }
        })

        binding.ivClose.setOnClickListener {
            dialog?.dismiss()
        }

        binding.btnSubmit.setOnClickListener {
            viewModel.updateNPSFeedbackResponse(feedback.type, feedback.id, selectedRating)
                .observe(this, Observer { response ->
                    when (response) {
                        is Outcome.Progress -> {
                            binding.progressbar.visibility = View.VISIBLE
                        }
                        is Outcome.Failure -> {
                            binding.progressbar.visibility = View.GONE
                            dialog?.dismiss()
                        }
                        is Outcome.ApiError -> {
                            binding.progressbar.visibility = View.GONE
                            dialog?.dismiss()
                            apiErrorToast(response.e)

                        }
                        is Outcome.BadRequest -> {
                            binding.progressbar.visibility = View.GONE
                            val dialog = BadRequestDialog.newInstance("unauthorized")
                            dialog.show(childFragmentManager, "BadRequestDialog")
                        }
                        is Outcome.Success -> {
                            binding.progressbar.visibility = View.GONE
                            dialog?.dismiss()
                            toast(getString(R.string.string_thanks_for_feedback))
                        }
                    }
                })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.setCancelable(false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun submitFeedback() {
        viewModel.updateNPSFeedbackResponse(feedback.type, feedback.id, "0")
            .observe(this, Observer { response ->
                when (response) {
                    is Outcome.Progress -> {
                        mBinding?.progressbar?.visibility = View.VISIBLE
                    }
                    is Outcome.Failure -> {
                        mBinding?.progressbar?.visibility = View.GONE
                        dialog?.dismiss()
                    }
                    is Outcome.ApiError -> {
                        mBinding?.progressbar?.visibility = View.GONE
                        dialog?.dismiss()
                        apiErrorToast(response.e)

                    }
                    is Outcome.BadRequest -> {
                        mBinding?.progressbar?.visibility = View.GONE
                        val dialog = BadRequestDialog.newInstance("unauthorized")
                        dialog.show(childFragmentManager, "BadRequestDialog")
                    }
                    is Outcome.Success -> {
                        mBinding?.progressbar?.visibility = View.GONE
                    }
                }
            })
    }
}