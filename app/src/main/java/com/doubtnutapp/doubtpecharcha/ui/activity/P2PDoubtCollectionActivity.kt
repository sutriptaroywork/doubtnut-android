package com.doubtnutapp.doubtpecharcha.ui.activity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.MenuInflater
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.OnFilterSelected
import com.doubtnutapp.common.ui.dialog.FilterBottomSheetFragment
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityP2pDoubtCollectionBinding
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.doubtpecharcha.model.*
import com.doubtnutapp.doubtpecharcha.ui.adapter.DoubtPeCharchaViewPagerAdapter
import com.doubtnutapp.doubtpecharcha.ui.fragment.DoubtPeCharchaRewardInfoBottomSheetFragment
import com.doubtnutapp.doubtpecharcha.viewmodel.DoubtCollectionViewModel
import com.doubtnutapp.freeclasses.bottomsheets.FilterListBottomSheetDialogFragment
import com.doubtnutapp.freeclasses.widgets.FilterSortWidget
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.showToast
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class P2PDoubtCollectionActivity :
    BaseBindingActivity<DoubtCollectionViewModel, ActivityP2pDoubtCollectionBinding>(),
    ActionPerformer {

    private lateinit var filters: java.util.ArrayList<FilterData>

    companion object {
        private const val TAG = "P2PDoubtCollectionActivity"
        const val PRIMARY_TAB_ID = "primary_tab_id"
        const val SECONDARY_TAB_ID = "secondary_tab_id"
        const val SUBJECT_FILTERS = "subject_filters"
        const val CLASS_FILTERS = "class_filters"
        const val LANGUAGE_FILTERS = "language_filters"

        const val DEFAULT_PRIMARY_TAB_ID=1
        const val DEFAULT_SECONDARY_TAB_ID=0

        fun getStartIntent(
            context: Context,
            primaryTabId: String?,
            secondaryTabId: String? = null,
            subjectFilters: ArrayList<String>? = null,
            classFilters: ArrayList<String>? = null,
            languageFilters: ArrayList<String>? = null
        ): Intent {
            val intent = Intent(context, P2PDoubtCollectionActivity::class.java)
            intent.putExtra(SECONDARY_TAB_ID, secondaryTabId)
            intent.putExtra(SUBJECT_FILTERS, subjectFilters)
            intent.putExtra(CLASS_FILTERS, classFilters)
            intent.putExtra(LANGUAGE_FILTERS, languageFilters)
            return intent
        }
    }

    private val subjectFilters: ArrayList<String>? by lazy {
        intent?.getStringArrayListExtra(SUBJECT_FILTERS)
    }

    private val classFilters: ArrayList<String>? by lazy {
        intent?.getStringArrayListExtra(CLASS_FILTERS)
    }

    private val languageFilters: ArrayList<String>? by lazy {
        intent?.getStringArrayListExtra(LANGUAGE_FILTERS)
    }

    private val secondaryTabId: String? by lazy {
        intent?.getStringExtra(SECONDARY_TAB_ID)
    }

    override fun provideViewBinding(): ActivityP2pDoubtCollectionBinding {
        return ActivityP2pDoubtCollectionBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DoubtCollectionViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.redTomato
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.userDoubtsLiveData.observe(this) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBarLoader.isVisible = it.loading
                }
                is Outcome.Success -> {
                    onUserDoubtSuccess(it.data.second)
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
                else -> {
                }
            }
        }
    }

    override fun setupView(savedInstanceState: Bundle?) {
        viewModel.getUserDoubts(
            DEFAULT_PRIMARY_TAB_ID, DEFAULT_SECONDARY_TAB_ID,
            ArrayList<Filter>(), ArrayList<Filter>(), ArrayList<Filter>()
        )
        setupUi()
        setStatusBarColor()
    }

    private fun setupUi() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.ivOverflow.setOnClickListener {
            showPopUp()
        }

        binding.rewardsClickLayout.setOnClickListener {
            val intent = Intent(this, DoubtPeCharchaRewardsActivity::class.java)
            startActivity(intent)
        }


    }


    private fun showPopUp() {
        val popup = PopupMenu(this, binding.ivOverflow)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_doubt_p2p_collection, popup.menu)
        val item = popup.menu.findItem(R.id.autoDownload)

        val isAutoDownloadEnabled =
            defaultPrefs().getBoolean(Constants.P2P_IMAGE_AUTO_DOWNLOAD, false)
        if (isAutoDownloadEnabled) {
            item.title = getString(R.string.photos_auto_download_off)
        } else {
            item.title = getString(R.string.photos_auto_download_on)
        }

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.autoDownload -> {
                    viewModel.sendEvent(
                        EventConstants.P2P_IMAGE_AUTO_DOWNLOAD,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.IS_ENABLED, isAutoDownloadEnabled.not())
                        }
                    )
                    defaultPrefs().edit {
                        putBoolean(Constants.P2P_IMAGE_AUTO_DOWNLOAD, isAutoDownloadEnabled.not())
                        item.title = if (isAutoDownloadEnabled) {
                            getString(R.string.photos_auto_download_off)
                        } else {
                            getString(R.string.photos_auto_download_on)
                        }
                    }
                    true
                }
                else -> {
                    true
                }
            }
        }
        popup.show()
    }



    private fun onUserDoubtSuccess(data: P2PDoubtTypes) {
        val doubtTypes = data.primaryTabs
        setViewPager(doubtTypes, data.activeTab)
    }

    private fun setViewPager(doubtTypes: List<PrimaryTabData>?, activeTab: Int) {

        doubtTypes?.let {
            val adapter =
                DoubtPeCharchaViewPagerAdapter(
                    supportFragmentManager, lifecycle, it,
                    secondaryTabId,
                    subjectFilters,
                    classFilters, languageFilters
                )
            binding.viewPager.adapter = adapter

            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = doubtTypes[position].tabName
            }.attach()
            binding.viewPager.currentItem = activeTab

            binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabReselected(p0: TabLayout.Tab?) {
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                }

                override fun onTabSelected(p0: TabLayout.Tab?) {
                    val selectedTab = p0?.text?.toString()
                    viewModel.sendEvent(
                        EventConstants.P2P_COLLECTION_TAB,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.SOURCE, selectedTab.orEmpty())
                        },
                        ignoreSnowplow = true
                    )
                }
            })
            binding.viewPager.currentItem = 0
        }
    }

    override fun performAction(action: Any) {
    }

    private fun setStatusBarColor() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
    }
}
