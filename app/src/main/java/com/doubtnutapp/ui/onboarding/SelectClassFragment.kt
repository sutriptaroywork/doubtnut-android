package com.doubtnutapp.ui.onboarding

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.StudentClass
import com.doubtnutapp.databinding.FragmentSelectClassBinding
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.ui.onboarding.adapter.StudentClassesAdapter
import com.doubtnutapp.ui.onboarding.viewmodel.SelectClassViewModel
import com.doubtnutapp.utils.ChatUtil
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.UXCamUtil
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.google.firebase.analytics.FirebaseAnalytics
import com.uxcam.UXCam
import java.util.*

class SelectClassFragment : BaseBindingFragment<SelectClassViewModel, FragmentSelectClassBinding>() {

    companion object {
        const val TAG = "SelectClassFragment"
        private const val NAVIGATE_FROM_LIB = "LIBRARAY"
        private const val NAVIGATE_FROM_NAV = "NAVIGATION_DRAWER"
        const val INTENT_SOURCE_LIBRARY = "intentSourceLibrary"
        const val LANGUAGE_EVENT_IDENTIFIER_KEY = "language_event_identifier"
        private var fromOnBoarding: Boolean = true


        fun newInstance() = SelectClassFragment()
        fun newInstanceFromLibrary(isFromLibrary: Boolean): SelectClassFragment {
            val fragment = SelectClassFragment()
            val args = Bundle()
            args.putBoolean(Constants.NAVIGATE_TO_SELECT_CLASS, isFromLibrary)
            args.putString(LANGUAGE_EVENT_IDENTIFIER_KEY, NAVIGATE_FROM_LIB)
            fromOnBoarding = false
            fragment.arguments = args
            return fragment
        }


        fun newInstanceForNav(
            navigate_from_edit_profile: Boolean,
            isSourceFromLibrary: Boolean
        ): Fragment {
            val fragment = SelectClassFragment()
            val args = Bundle()
            args.putBoolean(Constants.NAVIGATE_CLASS_FRAGMENT_FROM_NAV, navigate_from_edit_profile)
            args.putBoolean(INTENT_SOURCE_LIBRARY, isSourceFromLibrary)
            args.putString(LANGUAGE_EVENT_IDENTIFIER_KEY, NAVIGATE_FROM_NAV)
            fromOnBoarding = false
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var adapter: StudentClassesAdapter
    private lateinit var eventTracker: Tracker
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private var languageEventDistinguisher: String? = null

    private var onBoardingCompleteListener: OnBoardingCompleteListener? = null

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSelectClassBinding =
        FragmentSelectClassBinding.inflate(layoutInflater)

    override fun provideViewModel(): SelectClassViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        statusbarColor(activity, R.color.grey_statusbar_color)
        eventTracker = getTracker()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        sendEvent(EventConstants.STATE_OPEN)
        sendEventForClassPageShowCleverTap(EventConstants.EVENT_NAME_CLASS_PAGE)

        arguments?.let {
            if (it.getBoolean(Constants.NAVIGATE_CLASS_FRAGMENT_FROM_NAV) || it.getBoolean(Constants.NAVIGATE_TO_SELECT_CLASS) || it.getBoolean(
                    Constants.NAVIGATE_TO_SELECT_CLASS_FROM_PROFILE
                )
            ) {
                binding.ivCancel.visibility = View.VISIBLE
            } else {
                binding.ivCancel.visibility = View.GONE
            }

            languageEventDistinguisher = it.getString(LANGUAGE_EVENT_IDENTIFIER_KEY)
        }

        if (fromOnBoarding) {
            viewModel.eventWith(EventConstants.EVENT_NAME_SPLASH_CLASS_OPEN, ignoreSnowplow = true)
        }
        getClassesList()

        binding.ivCancel.setOnClickListener {
            activity?.apply {
                requireActivity().onBackPressed()
            }
        }

        adapter = StudentClassesAdapter(requireActivity(), eventTracker)
        binding.rvClasses.adapter = adapter

        binding.rvClasses.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                arguments?.let {
                    viewModel.publishSelectClassEvent(
                        it.getBoolean(INTENT_SOURCE_LIBRARY, false),
                        adapter.classes!![position].classDisplay.toString()
                    )
                    defaultPrefs().edit {
                        putString(
                            Constants.STUDENT_CLASS,
                            adapter.classes!![position].name.toString()
                        )
                        putString(
                            Constants.STUDENT_CLASS_DISPLAY,
                            adapter.classes!![position].classDisplay.toString()
                        )
                    }

                    if (adapter.classes!![position].name.toString() == "14") {
                        defaultPrefs().edit {
                            putString(Constants.STUDENT_COURSE, Constants.STRING_COURSE_GOVT_EXAM)
                        }
                    } else {
                        defaultPrefs().edit {
                            putString(Constants.STUDENT_COURSE, Constants.STRING_COURSE_NCERT)
                        }
                    }
                    context?.let { currentContext ->
                        if (UXCamUtil.shouldStart(Constants.UXCAM_PERCENT, currentContext)) {
                            UXCam.setUserProperty(
                                Constants.STUDENT_CLASS,
                                adapter.classes!![position].name.toString()
                            )
                        }
                    }
                } ?: run {
                    defaultPrefs().edit {
                        putString(
                            Constants.STUDENT_CLASS,
                            adapter.classes!![position].name.toString()
                        )
                        putString(
                            Constants.STUDENT_CLASS_DISPLAY,
                            adapter.classes!![position].classDisplay.toString()
                        )
                    }
                    sendEventForClassClickCleverTap(EventConstants.EVENT_NAME_CLICK_CLASS)
                    eventTracker.addEventNames(EventConstants.EVENT_NAME_CLASS_CLICK + languageEventDistinguisher)
                        .addNetworkState(NetworkUtils.isConnected(activity!!).toString())
                        .addStudentId(getStudentId())
                        .addScreenName(EventConstants.PAGE_CLASS)
                        .track()
                    viewModel.publishSplashSelectClassEvent(
                        adapter.classes?.getOrNull(position)?.name?.toString()
                            ?: ""
                    )
                }
                MoEngageUtils.setUserAttribute(
                    requireContext().applicationContext,
                    Constants.STUDENT_CLASS,
                    adapter.classes!![position].name.toString()
                )
                updateProfile(fromOnBoarding)

            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onBoardingCompleteListener = context as? OnBoardingCompleteListener
    }

    fun getClassesList() {
        viewModel.getClassesList(
            defaultPrefs(requireActivity()).getString(
                Constants.STUDENT_LANGUAGE_CODE,
                "en"
            ).orDefaultValue()
        ).observe(viewLifecycleOwner, { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressView.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressView.visibility = View.GONE
                    val dialog = NetworkErrorDialogClass.newInstanceClass(
                        getString(R.string.string_noInternetConnection),
                        Constants.CLASS_SSC
                    )
                    dialog.show(childFragmentManager, "NetworkErrorDialogClass")
                    eventTracker.addEventNames(EventConstants.EVENT_NAME_GET_CLASS_FAILURE)
                        .addScreenState(
                            EventConstants.EVENT_PRAMA_API_RESPONSE_STATE,
                            EventConstants.EVENT_PRAMA_API_RESPONSE_FAILLED
                        )
                        .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                        .addStudentId(getStudentId())
                        .addScreenName(EventConstants.PAGE_CLASS)
                        .track()
                }
                is Outcome.ApiError -> {
                    binding.progressView.visibility = View.GONE
                    val dialog = NetworkErrorDialogClass.newInstanceClass(
                        getString(R.string.api_error),
                        Constants.CLASS_SSC
                    )
                    dialog.show(childFragmentManager, "NetworkErrorDialogClass")
                    eventTracker.addEventNames(EventConstants.EVENT_NAME_GET_CLASS_API_ERROR)
                        .addScreenState(
                            EventConstants.EVENT_PRAMA_API_RESPONSE_STATE,
                            EventConstants.EVENT_PRAMA_API_RESPONSE_API_ERROR
                        )
                        .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                        .addStudentId(getStudentId())
                        .addScreenName(EventConstants.PAGE_CLASS)
                        .track()
                }
                is Outcome.BadRequest -> {
                    binding.progressView.visibility = View.GONE
                    val dialog = NetworkErrorDialogClass.newInstanceClass(
                        getString(R.string.api_error),
                        Constants.CLASS_SSC
                    )
                    dialog.show(childFragmentManager, "NetworkErrorDialogClass")
                    eventTracker.addEventNames(EventConstants.EVENT_NAME_GET_CLASS_Bad_Request)
                        .addScreenState(
                            EventConstants.EVENT_PRAMA_API_RESPONSE_STATE,
                            EventConstants.EVENT_PRAMA_API_RESPONSE_BAD_REQUEST
                        )
                        .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                        .addStudentId(getStudentId())
                        .addScreenName(EventConstants.PAGE_CLASS)
                        .track()
                }
                is Outcome.Success -> {
                    updateClassData(response.data.data)
                    binding.progressView.visibility = View.GONE
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateClassData(studentClass: ArrayList<StudentClass>) {
        adapter.updateData(studentClass)
    }

    override fun onDetach() {
        super.onDetach()
        sendEventClick(EventConstants.STATE_CLOSE)

    }

    private fun sendEvent(eventName: String) {
        activity?.apply {
            eventTracker.addEventNames(EventConstants.EVENT_NAME_SCREEN_STATE_CLASS)
                .addScreenState(EventConstants.EVENT_PRAMA_SCREEN_STATE, eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_CLASS)
                .track()
        }
    }

    private fun sendEventClick(eventName: String) {
        activity?.apply {
            eventTracker.addEventNames(eventName)
                .addScreenState(EventConstants.EVENT_PRAMA_SCREEN_STATE, eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_CLASS)
                .track()
        }
    }


    private fun sendEventForClassPageShowCleverTap(eventName: String) {
        activity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addEventParameter(EventConstants.PARAM_SHOW, true)
                .track()

        }
    }

    private fun sendEventForClassClickCleverTap(eventName: String) {
        activity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(getStudentId())
                .addEventParameter(Constants.STUDENT_CLASS, getStudentClass())
                .cleverTapTrack()
        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = activity?.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    private fun updateProfile(isFromOnBoarding: Boolean) {
        viewModel.updateProfileClass(isFromOnBoarding)
            .observe(this, { response ->
                when (response) {
                    is Outcome.Progress -> {
                        binding.progressView.visibility = View.VISIBLE
                    }
                    is Outcome.Failure -> {
                        binding.progressView.visibility = View.GONE

                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(childFragmentManager, "NetworkErrorDialog")
                    }
                    is Outcome.ApiError -> {
                        binding.progressView.visibility = View.GONE
                        toast(getString(R.string.api_error))

                    }
                    is Outcome.Success -> {
                        binding.progressView.visibility = View.GONE
                        if (context != null) {
                            defaultPrefs().edit {
                                putInt(Constants.CAMERA_INFO_COUNT, 0)
                            }
                        }
                        onBoardingCompleteListener?.onBoardingComplete()
                        updatePreferences()
                        val intent = Intent()
                        intent.action = Constants.NAVIGATE_FROM_CLASS_CHANGE
                        arguments?.let {
                            when {
                                it.getBoolean(Constants.NAVIGATE_CLASS_FRAGMENT_FROM_NAV) -> {

                                }
                                it.getBoolean(Constants.NAVIGATE_TO_SELECT_CLASS) -> intent.putExtra(
                                    Constants.NAVIGATE_LIBRARY,
                                    true
                                )
                                it.getBoolean(Constants.NAVIGATE_TO_SELECT_CLASS_FROM_PROFILE) -> intent.putExtra(
                                    Constants.NAVIGATE_TO_SELECT_CLASS_FROM_PROFILE,
                                    true
                                )
                                else -> {
                                }
                            }
                        }

                        if (requireActivity().parent == null) {
                            requireActivity().setResult(Activity.RESULT_OK, intent)
                        } else {
                            requireActivity().parent.setResult(Activity.RESULT_OK, intent)
                        }
                        requireActivity().finish()
                    }
                }
            })
    }

    private fun updatePreferences() {
        activity?.run {
            defaultPrefs(this).edit {
                putBoolean(Constants.KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER, true)
                viewModel.updatePreferences()
                ChatUtil.sendToken(
                    context = applicationContext,
                    token = defaultPrefs(applicationContext).getString(Constants.GCM_REG_ID, "")
                        ?: ""
                )
            }
        }
    }

    fun reloadAfterNetworkError() {
        updateProfile(fromOnBoarding)
    }


}