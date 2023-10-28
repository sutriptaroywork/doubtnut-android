package com.doubtnutapp.gamification.userProfileData.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.Fragment
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ProfileFragmentBinding
import com.doubtnutapp.gamification.myachievment.ui.UserAchievementFragment
import com.doubtnutapp.gamification.mybio.ui.MyBioActivity
import com.doubtnutapp.gamification.settings.profilesetting.ui.ProfileSettingActivity
import com.doubtnutapp.gamification.userProfileData.model.UserProfileDTO
import com.doubtnutapp.gamification.userProfileData.model.UserProfileDataModel
import com.doubtnutapp.gamification.userProfileData.ui.adapter.ProfilePagerAdapter
import com.doubtnutapp.gamification.userProfileData.viewmodel.ProfileViewModel
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import javax.inject.Inject

/**
 * This fragment loads old profile of the user. Review UserProfileFragment for the new user profile
 * screen.
 *
 * This is called from "Points"
 */
class ProfileFragment : BaseBindingFragment<ProfileViewModel, ProfileFragmentBinding>() {

    companion object {
        const val LOGIN_REQUEST_CODE = 102
        const val EDIT_PROFILE_REQUEST_CODE = 103

        const val TAG = "ProfileFragment"

        private const val LOGIN_IMAGE_URL =
            "https://d10lpgp6xz60nq.cloudfront.net/images/profile_picture.png"

        fun newInstance() = ProfileFragment()
    }

    @Inject
    lateinit var screenNavigator: Navigator

    private lateinit var profilePagerAdapter: ProfilePagerAdapter

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ProfileFragmentBinding {
        statusbarColor(activity, R.color.grey_statusbar_color)
        val binding =
            inflate<ProfileFragmentBinding>(inflater, R.layout.profile_fragment, container, false)
        binding.viewmodel = viewModel
        binding.loginImage = LOGIN_IMAGE_URL
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): ProfileViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBinding ?: return
        profilePagerAdapter = ProfilePagerAdapter(childFragmentManager, emptyList(), emptyList())
        binding.profileViewPager.adapter = profilePagerAdapter

        binding.profileTab.setupWithViewPager(binding.profileViewPager)
        startShimmer()
        statusbarColor(activity, R.color.grey_profile_statusbar)
        setClickListener()
    }

    override fun onResume() {
        super.onResume()
        getProfileData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == LOGIN_REQUEST_CODE || requestCode == EDIT_PROFILE_REQUEST_CODE) {
            getProfileData()
        }
    }

    private fun startShimmer() {
        mBinding?.shimmerFrameLayout?.startShimmer()
        mBinding?.shimmerFrameLayout?.show()
    }

    private fun stopShimmer() {
        mBinding?.shimmerFrameLayout?.stopShimmer()
        mBinding?.shimmerFrameLayout?.hide()
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.userProfileLiveData.observeK(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.navigateLiveData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { navigationModel ->
                screenNavigator.startActivityForResultFromFragment(
                    this, navigationModel.screen,
                    navigationModel.hashMap?.toBundle(),
                    LOGIN_REQUEST_CODE
                )
                sendEvent(EventConstants.EVENT_NAME_LOGIN_BUTTON_CLICKED)
            }
        }
    }

    private fun onSuccess(userProfileDTO: UserProfileDTO) {
        setupProfilePager(userProfileDTO.userProfileDataModel)
//        defaultPrefs(activity!!).edit().putInt(StoreActivity.DN_CASH, userProfileDTO.userProfileDataModel.coins).apply()
    }

    private fun setupProfilePager(userProfileData: UserProfileDataModel) {
        val pageTitle = mutableListOf<String>()
        pageTitle.add(resources.getString(R.string.title_MyAchievement))
//        pageTitle.add(resources.getString(R.string.title_MyActivity))

        val fragmentList = mutableListOf<Fragment>()
        fragmentList.add(UserAchievementFragment.newInstance(userProfileData))
//        fragmentList.add(MyActivitiesFragment.newInstance())

        profilePagerAdapter.updateStoreResultList(pageTitle, fragmentList)
        profilePagerAdapter.notifyDataSetChanged()
    }

    private fun setClickListener() {
        mBinding?.ivProfileSetting?.setOnClickListener {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_SETTING_BUTTON_CLICK, ignoreSnowplow = true)
            startActivityForResult(
                Intent(activity, ProfileSettingActivity::class.java),
                LOGIN_REQUEST_CODE
            )
            sendEvent(EventConstants.EVENT_NAME_SETTING_BUTTON_CLICK)
        }

        mBinding?.ivProfileEdit?.setOnClickListener {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_EDIT_PROFILE_CLICK)
            sendEvent(EventConstants.EVENT_NAME_EDIT_PROFILE_CLICK)
            openEditProfilePage()
        }
        mBinding?.ivProfilePicture?.setOnClickListener {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_PROFILE_IMAGE_CLICK, ignoreSnowplow = true)
            sendEvent(EventConstants.EVENT_NAME_PROFILE_IMAGE_CLICK)
            openEditProfilePage()
        }
    }

    private fun openEditProfilePage() {
        activity?.let { currentActivity ->
            if (NetworkUtils.isConnected(currentActivity)) {
                startActivityForResult(
                    Intent(activity, MyBioActivity::class.java),
                    EDIT_PROFILE_REQUEST_CODE
                )
            } else {
                toast(getString(R.string.string_noInternetConnection))
            }
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun updateProgressBarState(state: Boolean) {
        mBinding?.progressProfile?.setVisibleState(state)
        if (state.not()) {
            stopShimmer()
        }
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(requireActivity()).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun getProfileData() {
        viewModel.getUserProfile()
    }

    private fun sendEvent(eventName: String) {
        activity?.apply {
            (activity?.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(getStudentId())
                .track()
        }
    }
}