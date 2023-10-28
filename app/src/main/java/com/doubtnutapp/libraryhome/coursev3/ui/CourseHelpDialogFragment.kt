package com.doubtnutapp.libraryhome.coursev3.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.remote.models.CourseHelpData
import com.doubtnutapp.databinding.CourseHelpDialogBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.getScreenWidth
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnut.core.utils.viewModelProvider
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class CourseHelpDialogFragment :
    BaseBindingDialogFragment<DummyViewModel, CourseHelpDialogBinding>() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "CourseHelpDialogFragment"
        const val COURSE_HELP_DATA = "course_help_data"

        fun newInstance(courseHelpData: CourseHelpData): CourseHelpDialogFragment {
            return CourseHelpDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(COURSE_HELP_DATA, courseHelpData)
                }
            }
        }
    }

    private var data: CourseHelpData? = null
    private lateinit var v: View

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fullScreenMode()
        initUI()
    }

    private fun initUI() {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.COURSE_HELP_VIEW,
                hashMapOf()
            )
        )
        data = arguments?.getParcelable(COURSE_HELP_DATA)
        binding.titleTv.text = data?.title.orEmpty()
        binding.subtitleTv.text = data?.subtitle.orEmpty()
        binding.tvCancel.text = data?.cancelButtonText.orEmpty()
        binding.buttonYes.text = data?.requestButtonText.orEmpty()
        binding.tvCancel.setOnClickListener {
            dismiss()
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.COURSE_HELP_NO_CLCKED,
                    hashMapOf()
                )
            )
        }
        binding.buttonYes.setOnClickListener {
            dismiss()
            deeplinkAction.performAction(requireContext(), data?.deeplink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.COURSE_HELP_YES_CLICKED,
                    hashMapOf()
                )
            )
        }
    }

    private fun fullScreenMode() {
        val width = requireActivity().getScreenWidth() - 100
        val params = dialog?.window?.attributes
        dialog?.window?.attributes = params
        dialog?.window?.setGravity(Gravity.BOTTOM)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): CourseHelpDialogBinding {
        return CourseHelpDialogBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }
}

