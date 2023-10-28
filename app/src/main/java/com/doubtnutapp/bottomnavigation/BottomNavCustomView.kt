package com.doubtnutapp.bottomnavigation

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.MenuItem
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.bottomnavigation.model.BottomNavigationTabsData
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.libraryhome.library.ui.adapter.LibraryTabAdapter
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class BottomNavCustomView(context: Context, attrs: AttributeSet) :
    BottomNavigationView(context, attrs) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var defaultDataSource: DefaultDataStore

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    lateinit var job: Job

    private var useCustomItemSelectedListener = false
    private var useCustomColorForIcons:Int=0

    companion object {
        const val EVENT_TAG_BOTTOM_NAVIGATION = "bottom_navigation"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        getAttributes(attrs)
        initialize()
    }

    private fun initialize() {

        inflateMenu(R.menu.menu_bottom_nav_home)
        elevation = 4f.dpToPx()
        labelVisibilityMode = LABEL_VISIBILITY_LABELED
        itemIconSize = 25.dpToPx()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            itemIconTintList = null
        }

        val coroutineScope = CoroutineScope(Dispatchers.Main)

        job = coroutineScope.launch {
            val response = defaultDataSource.bottomNavigationIconsData.firstOrNull().orEmpty()
            if (Utils.isBottomNavigationIconsApiDataAvailable(response)) {
                setBackEndDrivenTabsData(response)
            } else {
                setDefaultData()
            }
        }

        if (!useCustomItemSelectedListener) {
            setCommonItemSelectedListenerForBottomNav()
        }


    }

    private fun getAttributes(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BottomNavCustomView)
        try {
            useCustomItemSelectedListener = typedArray.getBoolean(
                R.styleable.BottomNavCustomView_useCustomItemSelectedListener,
                false
            )

            useCustomColorForIcons =
                typedArray.getColor(R.styleable.BottomNavCustomView_customColorForIcons,Color.BLACK)

        } finally {
            typedArray.recycle()
        }
    }

    private fun setBackEndDrivenTabsData(response: String) {

        val responseBottomNavData = Gson().fromJson(response, BottomNavigationTabsData::class.java)

        val defaultColorValue = if (useCustomColorForIcons!=0) useCustomColorForIcons else Color.BLACK

        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(-android.R.attr.state_pressed)
        )

        val colors = intArrayOf(
            ContextCompat.getColor(context, R.color.colorPrimary),
            defaultColorValue,
            ContextCompat.getColor(context, R.color.colorPrimary),
            defaultColorValue,
        )
        itemTextColor = ColorStateList(states, colors)
        itemIconTintList = ColorStateList(states, colors)

        val tab1Data = responseBottomNavData.tab1
        val menuItemHome = menu.findItem(R.id.homeFragment);
        renderTabData(tab1Data, menuItemHome)

        val tab2Data = responseBottomNavData.tab2
        val menuItemOnlineClass = menu.findItem(R.id.libraryFragment);
        renderTabData(tab2Data, menuItemOnlineClass)

        val tab3Data = responseBottomNavData.tab3
        val menuItemFriends = menu.findItem(R.id.forumFragment);
        renderTabData(tab3Data, menuItemFriends)

        val tab4Data = responseBottomNavData.tab4
        val menuItemProfile = menu.findItem(R.id.userProfileFragment);
        renderTabData(tab4Data, menuItemProfile)


    }

    private fun renderTabData(tabData: BottomNavigationTabsData.TabData?, menuItem: MenuItem) {
        menuItem.title = tabData?.name
        loadIcon(tabData?.iconUrlActive, menuItem)
    }

    private fun loadIcon(iconUrl: String?, menuItem: MenuItem) {
        Glide.with(DoubtnutApp.INSTANCE)
            .asBitmap()
            .load(iconUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    val drawable = BitmapDrawable(resources, resource)
                    menuItem.icon = drawable
                }
            })
    }

    private fun setDefaultData() {

        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(-android.R.attr.state_pressed)
        )

        val colors = intArrayOf(
            ContextCompat.getColor(context, R.color.colorPrimary),
            Color.BLACK,
            ContextCompat.getColor(context, R.color.colorPrimary),
            Color.BLACK
        )
        val colorStateList = ColorStateList(states, colors)
        itemTextColor = colorStateList
        itemIconTintList = colorStateList

        val menuItemHome = menu.findItem(R.id.homeFragment);
        menuItemHome.setIcon(R.drawable.ic_bottomnavigation_home)

        val menuItemOnlineClass = menu.findItem(R.id.libraryFragment);
        menuItemOnlineClass.setIcon(R.drawable.ic_bottomnavigation_library)

        val menuItemFriends = menu.findItem(R.id.forumFragment);
        menuItemFriends.setIcon(R.drawable.ic_bottomnavigation_forum)

        val menuItemProfile = menu.findItem(R.id.userProfileFragment);
        menuItemProfile.setIcon(R.drawable.ic_bottomnavigation_profile)


    }

    private fun setCommonItemSelectedListenerForBottomNav() {
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        this.setOnItemSelectedListener { item ->
            var isBottomNavIconsDataAvailable = false
            job = coroutineScope.launch {
                val responseJson =
                    defaultDataSource.bottomNavigationIconsData.firstOrNull().orEmpty()
                isBottomNavIconsDataAvailable =
                    Utils.isBottomNavigationIconsApiDataAvailable(responseJson)

                if (isBottomNavIconsDataAvailable) {

                    val responseBottomNavData =
                        Gson().fromJson(responseJson, BottomNavigationTabsData::class.java)

                    when (item.itemId) {
                        R.id.homeFragment -> {

                            launchDeeplinkAndSendEvent(responseBottomNavData.tab1, "1")
                            setSelectable(responseBottomNavData.tab1, R.id.homeFragment)
                        }
                        R.id.libraryFragment -> {

                            launchDeeplinkAndSendEvent(responseBottomNavData.tab2, "2")
                            setSelectable(responseBottomNavData.tab2, R.id.libraryFragment)
                        }
                        R.id.forumFragment -> {

                            launchDeeplinkAndSendEvent(responseBottomNavData.tab3, "3")
                            setSelectable(responseBottomNavData.tab3, R.id.forumFragment)
                        }
                        R.id.userProfileFragment -> {

                            launchDeeplinkAndSendEvent(responseBottomNavData.tab4, "4")
                            setSelectable(responseBottomNavData.tab4, R.id.userProfileFragment)

                        }
                        else -> {
                            false
                        }
                    }

                } else {
                    when (item.itemId) {
                        R.id.homeFragment -> {
                            val intent = Intent(context, MainActivity::class.java)
                            intent.action = Constants.NAVIGATE_HOME
                            context.startActivity(intent)
                        }
                        R.id.libraryFragment -> {
                            val intent = Intent(context, MainActivity::class.java).also {
                                it.action = Constants.NAVIGATE_LIBRARY
                                it.putExtra(Constants.TAG, LibraryTabAdapter.TAG_MY_COURSES)
                            }
                            context.startActivity(intent)
                        }
                        R.id.forumFragment -> {
                            val intent = Intent(context, MainActivity::class.java).apply {
                                action = Constants.NAVIGATE_FEED
                            }
                            context.startActivity(intent)

                        }
                        R.id.userProfileFragment -> {
                            val intent = Intent(context, MainActivity::class.java)
                            intent.action = Constants.NAVIGATE_PROFILE
                            context.startActivity(intent)
                        }
                    }
                }
            }
            true
        }

    }

    private fun launchDeeplinkAndSendEvent(
        responseTabData: BottomNavigationTabsData.TabData?,
        position: String
    ) {
        deeplinkAction.performAction(
            context,
            responseTabData?.deeplink
        )
        Utils.publishBottomNavTabClickEvent(
            analyticsPublisher,
            responseTabData?.name.orEmpty(),
            position,
        )
    }

    private fun setSelectable(
        responseTabData: BottomNavigationTabsData.TabData?,
        navDestinationId: Int
    ) {
        responseTabData?.let {
            if (it.isSelectable != null && !it.isSelectable) {
                menu.findItem(navDestinationId).isCheckable = false
                return@let false
            }
            return@let true
        } ?: kotlin.run {
            true
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job.cancel()
    }

}