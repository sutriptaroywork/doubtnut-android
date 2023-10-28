package com.doubtnutapp.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.bottomnavigation.repository.BottomNavRepository
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreferenceImpl
import com.doubtnutapp.data.remote.models.Language
import com.doubtnutapp.databinding.FragmentLanguageBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.onboarding.adapter.LanguageAdapter
import com.doubtnutapp.ui.onboarding.viewmodel.LanguageViewModel
import com.doubtnutapp.utils.ApxorUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.UXCamUtil
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.uxcam.UXCam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class LanguageFragment : BaseBindingFragment<DummyViewModel, FragmentLanguageBinding>() {

    private lateinit var adapter: LanguageAdapter
    private lateinit var eventTracker: Tracker
    private lateinit var languageViewModel: LanguageViewModel

    @Inject
    lateinit var bottomNavRepository: BottomNavRepository

    private var onBoardingCompleteListener: OnBoardingCompleteListener? = null

    companion object {
        const val TAG = "LanguageFragment"
        fun newInstance(navigate_from_edit_profile: Boolean): Fragment {
            val fragment = LanguageFragment()
            val args = Bundle()
            args.putBoolean(
                Constants.NAVIGATE_LANGUAGE_FRAGMENT_FROM_EDITPROFILE,
                navigate_from_edit_profile
            )
            fragment.arguments = args
            return fragment
        }

        fun newInstanceForProfile(navigate_from_profile: Boolean): Fragment {
            val fragment = LanguageFragment()
            val args = Bundle()
            args.putBoolean(
                Constants.NAVIGATE_LANGUAGE_FRAGMENT_FROM_PROFILE,
                navigate_from_profile
            )
            fragment.arguments = args
            return fragment
        }

        fun newInstanceForNav(navigate_from_edit_profile: Boolean): Fragment {
            val fragment = LanguageFragment()
            val args = Bundle()
            args.putBoolean(
                Constants.NAVIGATE_LANGUAGE_FRAGMENT_FROM_NAV,
                navigate_from_edit_profile
            )
            fragment.arguments = args
            return fragment
        }

    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLanguageBinding =
        FragmentLanguageBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel{
        languageViewModel = ViewModelProvider(this).get(LanguageViewModel::class.java)
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        statusbarColor(activity, R.color.grey_statusbar_color)
        eventTracker = getTracker()
        sendEvent(EventConstants.STATE_OPEN)
        statusbarColor(requireActivity(), android.R.color.transparent)
        sendEventForLanguagePageShowCleverTap(EventConstants.EVENT_NAME_LANGUAGE_PAGE)

        arguments?.let {
            if (it.getBoolean(Constants.NAVIGATE_LANGUAGE_FRAGMENT_FROM_NAV)) {
                binding.ivCancel.visibility = View.VISIBLE
            } else {
                binding.ivCancel.visibility = View.GONE
            }
        }

        binding.ivCancel.setOnClickListener {
            activity?.apply {
                requireActivity().onBackPressed()
                requireActivity().finish()
            }
        }

        adapter = LanguageAdapter(requireActivity(), eventTracker, fromOnboarding = false)
        binding.rvLanguages.adapter = adapter
        getLanguageData()
        if (!requireArguments().getBoolean(Constants.NAVIGATE_LANGUAGE_FRAGMENT_FROM_EDITPROFILE)
            && !requireArguments().getBoolean(Constants.NAVIGATE_LANGUAGE_FRAGMENT_FROM_PROFILE)
            && !requireArguments().getBoolean(Constants.NAVIGATE_LANGUAGE_FRAGMENT_FROM_NAV)
        ) {
            sendPageEvent(EventConstants.EVENT_NAME_SPLASH_LANGUAGE_STATE_SCREEN)
            ApxorUtils.logAppEvent(EventConstants.EVENT_NAME_SPLASH_LANGUAGE_OPEN, Attributes())
        }


        binding.rvLanguages.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                if (getStudentId().isNotBlank() && defaultPrefs(requireActivity()).getString(
                        Constants.STUDENT_LANGUAGE_CODE,
                        ""
                    ).isNullOrBlank()
                ) {
                    defaultPrefs(requireActivity()).edit(true) {
                        putString(
                            Constants.STUDENT_LANGUAGE_CODE,
                            adapter.languages!![position].code
                        )
                        putString(
                            Constants.STUDENT_LANGUAGE_NAME,
                            adapter.languages!![position].language
                        )
                        putString(
                            Constants.STUDENT_LANGUAGE_NAME_DISPLAY,
                            adapter.languages!![position].languageDisplay
                        )
                    }
                    sendPageEvent(EventConstants.EVENT_NAME_SPLASH_LANGUAGE_ITEM_CLICK)
                    ApxorUtils.logAppEvent(
                        EventConstants.EVENT_NAME_SPLASH_LANGUAGE_ITEM_CLICK,
                        Attributes().apply {
                            putAttribute(
                                EventConstants.SOURCE, adapter.languages?.getOrNull(position)?.code
                                    ?: ""
                            )
                        })
                    navigateToScreen()
                } else if (requireArguments().getBoolean(Constants.NAVIGATE_LANGUAGE_FRAGMENT_FROM_NAV)) {
                    updateLanguage(adapter.languages!![position])
                } else {
                    defaultPrefs(requireActivity()).edit(true) {
                        putString(
                            Constants.STUDENT_LANGUAGE_CODE,
                            adapter.languages!![position].code
                        )
                        putString(
                            Constants.STUDENT_LANGUAGE_NAME,
                            adapter.languages!![position].language
                        )
                        putString(
                            Constants.STUDENT_LANGUAGE_NAME_DISPLAY,
                            adapter.languages!![position].languageDisplay
                        )
                    }
                    navigateToScreen()
                    sendPageEvent(EventConstants.EVENT_NAME_SPLASH_LANGUAGE_ITEM_CLICK)
                }
                context?.let { currentContext ->
                    if (UXCamUtil.shouldStart(Constants.UXCAM_PERCENT, currentContext)) {
                        UXCam.setUserProperty(
                            Constants.LANG_CODE,
                            adapter.languages!![position].code
                        )
                    }
                }
                defaultPrefs().edit {
                    putBoolean(
                        UserPreferenceImpl.CAMERA_SCRREN_NAVIGATION_DATA_FETCHED_IN_CURRENT_SESSION,
                        false
                    )
                }
                LocaleManager.setLocale(requireActivity())

                sendEventForLanguageClickCleverTap(
                    EventConstants.EVENT_NAME_CLICK_LANGUAGE,
                    adapter.languages!![position].code
                )

                eventTracker.addEventNames(EventConstants.EVENT_NAME_SCREEN_STATE_LANGUAGE_CLICK)
                    .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                    .addStudentId(
                        getStudentId()
                    )
                    .addScreenName(EventConstants.PAGE_LANGUAGE)
                    .track()

                MoEngageUtils.setUserAttribute(
                    requireContext().applicationContext,
                    Constants.STUDENT_LANGUAGE_CODE,
                    adapter.languages!![position].code
                )
            }

        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onBoardingCompleteListener = context as? OnBoardingCompleteListener
    }

    fun getLanguageData() {
        languageViewModel.getLanguages().observe(viewLifecycleOwner) { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    val dialog = NetworkErrorDialogOnBoarding.newInstanceLanguage(
                        getString(R.string.string_noInternetConnection),
                        Constants.GET_LANGUAGE
                    )
                    dialog.show(childFragmentManager, "NetworkErrorDialog")
                    dialog.isCancelable = false
                    eventTracker.addEventNames(EventConstants.EVENT_NAME_GET_LANGUAGE_FAILURE)
                        .addScreenName(EventConstants.EVENT_PRAMA_API_RESPONSE_FAILLED)
                        .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                        .addStudentId(
                            getStudentId()
                        )
                        .addScreenName(EventConstants.PAGE_LANGUAGE)
                        .track()

                }
                is Outcome.ApiError -> {
                    binding.progressBar.visibility = View.GONE
                    toast(getString(R.string.api_error))
                    val dialog = NetworkErrorDialogOnBoarding.newInstanceLanguage(
                        getString(R.string.api_error),
                        Constants.GET_LANGUAGE
                    )
                    dialog.show(childFragmentManager, "NetworkErrorDialog")
                    dialog.isCancelable = false
                    eventTracker.addEventNames(EventConstants.EVENT_NAME_GET_LANGUAGE_API_ERROR)
                        .addScreenName(EventConstants.EVENT_PRAMA_API_RESPONSE_API_ERROR)
                        .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                        .addStudentId(
                            getStudentId()
                        )
                        .addScreenName(EventConstants.PAGE_LANGUAGE)
                        .track()
                }
                is Outcome.BadRequest -> {
                    binding.progressBar.visibility = View.GONE
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(childFragmentManager, "BadRequestDialog")
                    dialog.isCancelable = false
                    eventTracker.addEventNames(EventConstants.EVENT_NAME_GET_LANGUAGE_BAD_REQUEST)
                        .addScreenName(EventConstants.EVENT_PRAMA_API_RESPONSE_BAD_REQUEST)
                        .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                        .addStudentId(
                            getStudentId()
                        )
                        .addScreenName(EventConstants.PAGE_LANGUAGE)
                        .track()
                }
                is Outcome.Success -> {
                    binding.progressBar.visibility = View.GONE
                    adapter.updateData(response.data.data.languageList)

                }
            }
        }
    }

    fun updateLanguage(languagePosition: Language) {
        binding.progressBar.visibility = View.VISIBLE
        languageViewModel.setLanguage(languagePosition.code, requireActivity())
            .observe(this@LanguageFragment) { response ->
                when (response) {
                    is Outcome.Progress -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Outcome.Failure -> {
                        binding.progressBar.visibility = View.GONE
                        val dialog = NetworkErrorDialogOnBoarding.newInstanceLanguageUpdate(
                            getString(R.string.string_noInternetConnection),
                            Constants.UPDATE_LANGUAGE,
                            languagePosition
                            )
                        dialog.show(childFragmentManager, "NetworkErrorDialogOnBoarding")
                        dialog.isCancelable = false
                        eventTracker.addEventNames(EventConstants.EVENT_NAME_UPDATE_LANGUAGE_FAILURE)
                            .addScreenName(EventConstants.EVENT_PRAMA_API_RESPONSE_FAILLED)
                            .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                            .addStudentId(
                                getStudentId()
                            )
                            .addScreenName(EventConstants.PAGE_LANGUAGE)
                            .track()
                    }
                    is Outcome.ApiError -> {
                        binding.progressBar.visibility = View.GONE
                        toast(getString(R.string.api_error))
                        val dialog = NetworkErrorDialogOnBoarding.newInstanceLanguageUpdate(
                            getString(R.string.api_error),
                            Constants.UPDATE_LANGUAGE,
                            languagePosition
                        )
                        dialog.show(childFragmentManager, "NetworkErrorDialogOnBoarding")
                        dialog.isCancelable = false
                        eventTracker.addEventNames(EventConstants.EVENT_NAME_UPDATE_LANGUAGE_API_ERROR)
                            .addScreenName(EventConstants.EVENT_PRAMA_API_RESPONSE_API_ERROR)
                            .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                            .addStudentId(
                                getStudentId()
                            )
                            .addScreenName(EventConstants.PAGE_LANGUAGE)
                            .track()
                    }
                    is Outcome.BadRequest -> {
                        binding.progressBar.visibility = View.GONE
                        val dialog = NetworkErrorDialogOnBoarding.newInstanceLanguageUpdate(
                            getString(R.string.api_error),
                            Constants.UPDATE_LANGUAGE,
                            languagePosition
                        )
                        dialog.show(childFragmentManager, "NetworkErrorDialogOnBoarding")
                        dialog.isCancelable = false
                        eventTracker.addEventNames(EventConstants.EVENT_NAME_UPDATE_LANGUAGE_BAD_REQUEST)
                            .addScreenName(EventConstants.EVENT_PRAMA_API_RESPONSE_BAD_REQUEST)
                            .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                            .addStudentId(
                                getStudentId()
                            )
                            .addScreenName(EventConstants.PAGE_LANGUAGE)
                            .track()
                    }
                    is Outcome.Success -> {
                        fetchAppWideNavIcons()
                        ApxorUtils.logAppEvent(EventConstants.LANGUAGE_CHANGE_SUCCESS, Attributes())
                        MoEngageUtils.setUserAttribute(
                            requireContext(),
                            Constants.STUDENT_LANGUAGE_CODE,
                            languagePosition.code
                        )
                        binding.progressBar.visibility = View.GONE
                        defaultPrefs(requireActivity()).edit(true) {
                            putString(Constants.STUDENT_LANGUAGE_CODE, languagePosition.code)
                            putString(Constants.STUDENT_LANGUAGE_NAME, languagePosition.language)
                            putString(
                                Constants.STUDENT_LANGUAGE_NAME_DISPLAY,
                                languagePosition.languageDisplay
                            )
                            putBoolean(Constants.CHECK_LANGUAGE_SELECTED, true)
                        }
                        if (defaultPrefs(requireActivity()).getString(Constants.STUDENT_CLASS, "")
                                .isNullOrBlank()
                        ) {
                            updateLocalization()
                            navigateToScreen()
                        } else {
                            getClassesList()
                        }

                        val intent = Intent(
                            requireActivity(),
                            MainActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(0, 0)
                    }
                }
            }
    }

    fun fetchAppWideNavIcons(){
        viewLifecycleOwner.lifecycleScope.launch (Dispatchers.IO) {
            try {
                bottomNavRepository.fetchAndStoreNavIcons()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getClassesList() {
        binding.progressBar.visibility = View.VISIBLE
        languageViewModel.getClassesList(
            defaultPrefs(requireActivity()).getString(Constants.STUDENT_LANGUAGE_CODE, "en")
                ?: "en"
        ).observe(this) { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressBar.visibility = View.GONE
                }
                is Outcome.ApiError -> {
                    binding.progressBar.visibility = View.GONE
                }
                is Outcome.BadRequest -> {
                    binding.progressBar.visibility = View.GONE
                }
                is Outcome.Success -> {
                    val studentClass =
                        defaultPrefs(requireActivity()).getString(Constants.STUDENT_CLASS, "")
                    val requiredClassData =
                        response.data.data.firstOrNull { it.name.toString() == studentClass }
                    if (requiredClassData != null) {
                        defaultPrefs(requireActivity()).edit(true) {
                            putString(
                                Constants.STUDENT_CLASS_DISPLAY,
                                requiredClassData.classDisplay
                            )
                        }
                    }
                    binding.progressBar.visibility = View.GONE
                    updateLocalization()
                    navigateToScreen()
                }
            }
        }
    }

    fun navigateToScreen() {
        when {
            requireArguments().getBoolean(Constants.NAVIGATE_LANGUAGE_FRAGMENT_FROM_NAV) -> {
                with(requireActivity()) {
                    val intent = Intent(
                        this,
                        MainActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
            }
            defaultPrefs(requireActivity()).getString(Constants.STUDENT_CLASS, "")
                .isNullOrBlank() -> navigateTo(
                SelectClassFragment.newInstance(),
                Constants.SELECT_CLASS_FRAGMENT
            )
            else -> onBoardingCompleteListener?.onBoardingComplete()
        }
    }

    @Suppress("DEPRECATION")
    fun updateLocalization() {
        activity?.also {
            LocaleManager.setLocale(it)
        }
    }

    override fun onDetach() {
        super.onDetach()
        sendEvent(EventConstants.STATE_CLOSE)

    }

    private fun sendEvent(eventName: String) {
        activity?.apply {
            eventTracker.addEventNames(EventConstants.EVENT_NAME_SCREEN_STATE_LANGUAGE)
                .addScreenState(EventConstants.EVENT_PRAMA_SCREEN_STATE, eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(
                    getStudentId()
                )
                .addScreenName(EventConstants.PAGE_LANGUAGE)
                .track()
        }
    }

    private fun sendPageEvent(eventName: String) {
        activity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(
                    getStudentId()
                )
                .addScreenName(EventConstants.PAGE_LANGUAGE)
                .track()
        }
    }

    private fun sendEventForLanguagePageShowCleverTap(@Suppress("SameParameterValue") eventName: String) {
        activity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(
                    getStudentId()
                )
                .addScreenName(EventConstants.PAGE_LANGUAGE)
                .addEventParameter(EventConstants.PARAM_SHOW, true)
                .cleverTapTrack()

        }
    }

    private fun sendEventForLanguageClickCleverTap(
        @Suppress("SameParameterValue") eventName: String,
        clickedLanguage: String
    ) {
        activity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(
                    getStudentId()
                )
                .addScreenName(EventConstants.PAGE_LANGUAGE)
                .addEventParameter(EventConstants.PARAM_SUCCESS, true)
                .addEventParameter(EventConstants.PARAM_LANGUAGE, clickedLanguage)
                .cleverTapTrack()

        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = activity?.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

}
