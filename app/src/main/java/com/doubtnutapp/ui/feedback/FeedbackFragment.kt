package com.doubtnutapp.ui.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.core.view.get
import androidx.recyclerview.widget.GridLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.LayoutFeedbackBinding
import com.doubtnutapp.model.AppActiveFeedback
import com.doubtnutapp.ui.answer.DislikeFeedbackAdapter
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip

class FeedbackFragment :
    BaseBindingBottomSheetDialogFragment<FeedbackViewModel, LayoutFeedbackBinding>() {

    companion object {
        const val TAG = "FeedbackFragment"
        fun newInstance(feedback: AppActiveFeedback): FeedbackFragment {
            val fragment = FeedbackFragment()
            val args = Bundle()
            args.putParcelable(Constants.FEEDBACK_OBJECT, feedback)
            fragment.arguments = args
            return fragment
        }

    }

    private var mBehavior: BottomSheetBehavior<*>? = null
    val adapter = DislikeFeedbackAdapter()
    var selectedOptionsString: String = ""

    private lateinit var cbOptionsList: ArrayList<String>
    private lateinit var eventTracker: Tracker

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutFeedbackBinding {
        return LayoutFeedbackBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String = TAG

    override fun provideViewModel(): FeedbackViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        eventTracker = Tracker()
        mBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        val feedback: AppActiveFeedback = arguments?.getParcelable(Constants.FEEDBACK_OBJECT)!!
        cbOptionsList = arrayListOf()
        binding.tvtitleFeedback.text = feedback.title
        binding.btnSubmit.text = feedback.submit
        binding.tvQuestion.text = feedback.question
        if (feedback.options.contains("::")) {
            for (s in feedback.options.split("::")) {
                cbOptionsList.add(s)
            }
        } else {
            cbOptionsList.add(feedback.options)
        }
        binding.rvOptions.layoutManager = GridLayoutManager(requireActivity(), 1)
        binding.rvOptions.adapter = adapter

        adapter.updateData(cbOptionsList)

        binding.rvOptions.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                if (view.isSelected) {
                    binding.rvOptions[position]
                    view.isSelected = false
                } else {
                    view.isSelected = true
                }

            }
        })

        binding.btnSubmit.setOnClickListener {
            for (i in 0 until adapter.itemCount) {
                if (binding.rvOptions.get(i).isSelected) {
                    selectedOptionsString =
                        selectedOptionsString + (binding.rvOptions.get(i) as Chip).chipText + "::"
                }
            }
            if (selectedOptionsString == "") {
                toast(getString(R.string.choose_options))
            } else {
                selectedOptionsString =
                    selectedOptionsString.substring(0, selectedOptionsString.length - 2)
                sendFeedback(feedback.type, feedback.id, selectedOptionsString)
            }
            sendEvent(EventConstants.EVENT_NAME_SUBMIT_FEEDBACK)
        }

        binding.btnClose.setOnClickListener {
            mBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
            sendFeedback(feedback.type, feedback.id, "dismiss")
            sendEvent(EventConstants.EVENT_NAME_CLOSE_FROM_FEEDBACK)
            dialog?.cancel()
        }

        if (mBehavior != null) {
            (mBehavior as BottomSheetBehavior<View>).state = BottomSheetBehavior.STATE_EXPANDED
            (mBehavior as BottomSheetBehavior<View>).setBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                    (mBehavior as BottomSheetBehavior<View>).state =
                        BottomSheetBehavior.STATE_EXPANDED
                }

                override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {}
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun sendFeedback(type: String, feedback_id: String, options: String) {
        viewModel.updateFeedbackResponse(type, feedback_id, options)
            .observe(this) { response ->
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
                        dialog.show(requireFragmentManager(), "BadRequestDialog")
                    }
                    is Outcome.Success -> {
                        mBinding?.progressbar?.visibility = View.GONE
                        dialog?.dismiss()

                    }
                }

            }
    }

    private fun sendEvent(eventName: String) {
        context?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireContext()).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_FEEDBACK_FRAGMENT)
                .track()
        }
    }

}