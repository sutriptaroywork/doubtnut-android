package com.doubtnutapp.gamification.otheruserprofile.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.doubtnutapp.R
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityOthersProfileBinding
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.gamification.otheruserprofile.model.OthersUserProfileDataModel
import com.doubtnutapp.gamification.otheruserprofile.ui.adapter.OtherProfileTabsAdapter
import com.doubtnutapp.gamification.otheruserprofile.viewmodel.OtherUserProfileViewModel
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnut.core.utils.viewModelProvider
import javax.inject.Inject

class OthersProfileActivity : BaseBindingActivity<OtherUserProfileViewModel, ActivityOthersProfileBinding>() {

    @Inject
    lateinit var networkErrorHandler: NetworkErrorHandler

    @Inject
    lateinit var screenNavigator: Navigator

    private lateinit var adapter: OtherProfileTabsAdapter

    private val titleList = mutableListOf<String>()

    companion object {

        private const val TAG = "OthersProfileActivity"

        private const val INTENT_EXTRA_USER_ID = "user_id"

        fun startActivity(context: Context, userId: String): Intent {
            return Intent(context, OthersProfileActivity::class.java).apply {
                putExtra(INTENT_EXTRA_USER_ID, userId)
            }
        }
    }

    private var userId: String? = ""


    override fun provideViewBinding(): ActivityOthersProfileBinding {
       return ActivityOthersProfileBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): OtherUserProfileViewModel {
       return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.grey_statusbar_color
    }
    override fun setupView(savedInstanceState: Bundle?) {
        titleList.add(resources.getString(R.string.activity_other_profile_tab_achivement))
        titleList.add(resources.getString(R.string.activity_other_profile_tab_activity))
        init()
        setupTabs()
        setupObserver()
        setUpListener()
    }


    private fun setUpListener() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupObserver() {
        viewModel.userProfileLiveData.observeK(
            this,
            ::onSuccess,
            networkErrorHandler::onApiError,
            networkErrorHandler::unAuthorizeUserError,
            networkErrorHandler::ioExceptionHandler,
            ::updateProgressState
        )
    }

    private fun updateProgressState(state: Boolean) {
        binding.progressbar.setVisibleState(state)
    }

    private fun onSuccess(othersUserProfileDataModel: OthersUserProfileDataModel) {
        binding.userName.text = othersUserProfileDataModel.userName
        othersUserProfileDataModel.studentClass?.run {
            binding.studentClass.show()
            binding.studentClass.text = this
        }
        othersUserProfileDataModel.bannerImg?.let {
            binding.banner.show()
            Glide.with(this)
                .load(it)
                .apply(
                    RequestOptions.placeholderOf(R.drawable.ic_profilefragment_profileplaceholder)
                        .error(R.drawable.ic_profilefragment_profileplaceholder)
                )
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false

                    }
                })
                .into(binding.banner)
        }

        othersUserProfileDataModel.profileImage?.let {
            Glide.with(this)
                .load(it)
                .apply(
                    RequestOptions.placeholderOf(R.drawable.ic_profilefragment_profileplaceholder)
                        .error(R.drawable.ic_profilefragment_profileplaceholder)
                )
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(binding.userImage)
        }



        updateAdapter(othersUserProfileDataModel)
    }

    fun init() {
        this.userId = intent.getStringExtra(INTENT_EXTRA_USER_ID)
        viewModel.getOthersUserProfile(userId!!)
    }

    private fun setupTabs() {
        adapter = OtherProfileTabsAdapter(supportFragmentManager)
        binding.otherProfileViewPager.adapter = adapter
        binding.otherProfileTabs.setupWithViewPager(binding.otherProfileViewPager)
        binding.otherProfileViewPager.offscreenPageLimit = 2
    }

    private fun updateAdapter(othersUserProfileDataModel: OthersUserProfileDataModel) {
        val fragmentList = mutableListOf<Fragment>(
            OtherUserAchievementFragment.newInstance(
                othersUserProfileDataModel,
                userId
            ), OtherUserActivityFragment.newInstance(othersUserProfileDataModel.otherUserStats)
        )
        adapter.updateTabs(titleList, fragmentList)
    }


}
