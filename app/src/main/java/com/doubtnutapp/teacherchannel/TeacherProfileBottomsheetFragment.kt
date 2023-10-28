package com.doubtnutapp.teacherchannel


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.teacherchannel.TeacherProfile
import com.doubtnutapp.databinding.LayoutTeacherBioBottomsheetBinding
import com.doubtnutapp.teacherchannel.viewmodel.TeacherChannelViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import javax.inject.Inject


class TeacherProfileBottomsheetFragment :
    BaseBindingFragment<TeacherChannelViewModel, LayoutTeacherBioBottomsheetBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "TeacherProfileFragment"
        fun newInstance(channelTitle: String?, teacherId: Int): TeacherProfileBottomsheetFragment {
            val fragment = TeacherProfileBottomsheetFragment()
            val args = Bundle()
            args.putInt(Constants.TEACHER_ID, teacherId)
            args.putString(Constants.CHANNEL_NAME, channelTitle)
            fragment.arguments = args
            return fragment
        }
    }
    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var behavior: BottomSheetBehavior<*>
    private lateinit var channelTitle: String
    private var teacherId: Int = -1

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutTeacherBioBottomsheetBinding {
        return LayoutTeacherBioBottomsheetBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return "TeacherProfileBottomsheetFragment"
    }

    override fun provideViewModel(): TeacherChannelViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        channelTitle = arguments?.getString(Constants.CHANNEL_NAME) ?: ""
        teacherId = arguments?.getInt(Constants.TEACHER_ID) ?: -1

        val eventParams = hashMapOf<String, Any>()
        eventParams[Constants.TEACHER_ID] = teacherId
        analyticsPublisher?.publishEvent(
            AnalyticsEvent(
                EventConstants.TEACHER_PROFILE_PAGE_OPEN,
                eventParams,
                ignoreSnowplow = true
            )
        )

        setBottomSheetBehaviour()
        setUpObserver()

        binding.tvTitle.text = channelTitle
        binding.tvTitle.setOnClickListener {
            expandBottomSheet(true)
        }
        binding.ivClose.setOnClickListener {
            collapseFragmentView()
        }

        viewModel.fetchTeacherProfileData(teacherId)
    }

    private fun setUpObserver() {
        viewModel.teacherProfileData.observe(viewLifecycleOwner, {
            when (it) {
                is Outcome.Progress -> {
                    binding.progress.show()
                }
                is Outcome.Success -> {
                    binding.progress.hide()
                    setupProfileData(it.data)
                }
                is Outcome.Failure -> {
                    binding.progress.hide()
                    apiErrorToast(it.e)
                }
                is Outcome.ApiError -> {
                    binding.progress.hide()
                    apiErrorToast(it.e)
                }
            }
        })
    }

    private fun setupProfileData(profileData: TeacherProfile) {
        if (profileData.profileData == null) {
            return
        }
        val data = profileData.profileData!!

        binding.apply {
            layoutProfileData.show()
            ivTeacher.loadImage(data.profileImage, R.drawable.bg_circle_white)

            tvTeacherName.isVisible = !(data.fname + data.lname).isNullOrEmpty()
            tvTeacherName.text = data.fname + data.lname

            tvSubscribers.isVisible = data.subscribers != null
            tvSubscribers.text = data.subscribers

            labelEmail.isVisible = !data.email.isNullOrEmpty()
            tvEmail.isVisible = !data.email.isNullOrEmpty()
            tvEmail.text = data.email

            labelPhoneNumber.isVisible = !data.mobile.isNullOrEmpty()
            tvPhoneNumber.isVisible = !data.mobile.isNullOrEmpty()
            tvPhoneNumber.text = data.mobile

            labelLocation.isVisible = !data.location.isNullOrEmpty()
            tvLocation.isVisible = !data.location.isNullOrEmpty()
            tvLocation.text = data.location

            labelCollege.isVisible = !data.college.isNullOrEmpty()
            tvCollege.isVisible = !data.college.isNullOrEmpty()
            tvCollege.text = data.college

            labelDegree.isVisible = !data.degree.isNullOrEmpty()
            tvDegree.isVisible = !data.degree.isNullOrEmpty()
            tvDegree.text = data.degree

            labelAbout.isVisible = !data.about.isNullOrEmpty()
            tvAbout.isVisible = !data.about.isNullOrEmpty()
            tvAbout.text = data.about



            if (profileData.teachingDetails == null) {
                layoutExperienceData.hide()
            }else{
                layoutExperienceData.show()

                labelLanguage.isVisible = !profileData.teachingDetails?.locale.isNullOrEmpty()
                tvLanguage.isVisible = !profileData.teachingDetails?.locale.isNullOrEmpty()
                tvLanguage.text = profileData.teachingDetails?.locale.orEmpty()

                labelTaughtClasses.isVisible = !profileData.teachingDetails?.classTaught.isNullOrEmpty()
                tvTaughtClasses.isVisible = !profileData.teachingDetails?.classTaught.isNullOrEmpty()
                tvTaughtClasses.text = profileData.teachingDetails?.classTaught.orEmpty()


                labelBoard.isVisible = !profileData.teachingDetails?.board.isNullOrEmpty()
                tvBoard.isVisible = !profileData.teachingDetails?.board.isNullOrEmpty()
                tvBoard.text = profileData.teachingDetails?.board.orEmpty()

                labelExams.isVisible = !profileData.teachingDetails?.exam.isNullOrEmpty()
                tvExams.isVisible = !profileData.teachingDetails?.exam.isNullOrEmpty()
                tvExams.text = profileData.teachingDetails?.exam.orEmpty()

                labelSubject.isVisible = !profileData.teachingDetails?.subject.isNullOrEmpty()
                tvSubject.isVisible = !profileData.teachingDetails?.subject.isNullOrEmpty()
                tvSubject.text = profileData.teachingDetails?.subject.orEmpty()
            }
        }

    }

    override fun performAction(action: Any) {
        viewModel.handleAction(action)
    }

    private fun setBottomSheetBehaviour() {
        behavior = BottomSheetBehavior.from(binding.rootLayout)
        behavior.isDraggable = true
        behavior.isHideable = false
        behavior.peekHeight = requireActivity().getScreenHeight() / 2
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.isDraggable = false
                    behavior.removeBottomSheetCallback(this)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
    }

    private fun expandBottomSheet(toExpand: Boolean) {
        if (toExpand && behavior.state == STATE_COLLAPSED) {
            behavior.state = STATE_EXPANDED
        } else if (behavior.state == STATE_EXPANDED) {
            behavior.state = STATE_COLLAPSED
        }
    }

    fun expandFragmentView() {
        behavior.state = STATE_EXPANDED
    }

    fun collapseFragmentView() {
        (requireActivity() as TeacherChannelActivity).closeProfileBottomSheet()
    }

    fun isFragmentExpanded() = behavior.state == STATE_EXPANDED

}






