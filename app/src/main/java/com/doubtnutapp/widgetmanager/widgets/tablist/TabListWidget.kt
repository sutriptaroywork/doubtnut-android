package com.doubtnutapp.widgetmanager.widgets.tablist

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.newlibrary.model.ApiListingData
import com.doubtnutapp.data.newlibrary.service.LibraryHomeService
import com.doubtnutapp.databinding.WidgetTabListBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.newlibrary.entities.NextVideoEntity
import com.doubtnutapp.domain.newlibrary.entities.WhatsappFeedEntity
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.tabs.TabLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class TabListWidget(context: Context) :
    BaseBindingWidget<TabListWidget.WidgetHolder, TabListWidgetModel, WidgetTabListBinding>(context) {
    @Inject
    lateinit var libraryHomeService: LibraryHomeService

    @Inject
    lateinit var whatsAppSharing: WhatsAppSharing

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var userPreference: UserPreference

    private lateinit var widgetModel: TabListWidgetModel
    private lateinit var selectedTab: TabListItemsData

    private var adapter: TabListItemAdapter? = null

    class WidgetHolder(binding: WidgetTabListBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTabListBinding>(binding, widget)

    override fun getViewBinding(): WidgetTabListBinding {
        return WidgetTabListBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: TabListWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        widgetModel = model
        val data: TabListWidgetData = model.data
        binding.tvTitle.text = data.title
        addTabs(model)

        trackingViewId = data.id
        binding.tabLayout.isNestedScrollingEnabled = false
        return holder
    }

    private fun addTabs(model: TabListWidgetModel) {
        if (model.data.items.isNullOrEmpty()) {
            return
        }
        var pos = 0
        widgetViewHolder.binding.tabLayout.removeAllTabs()

        if (widgetViewHolder.binding.tabLayout.tabCount == 0) {
            for (tabData: TabListItemsData in model.data.items) {
                val tabView =
                    LayoutInflater.from(context).inflate(R.layout.tab_custom_view, null).apply {
                        layoutParams = LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setPadding(0, 0, 0, 0)
                        widgetViewHolder.binding.tvTitle.text = tabData?.title
                    }

                val tab = widgetViewHolder.binding.tabLayout.newTab().apply {
                    customView = tabView
                    tag = pos++
                }

                widgetViewHolder.binding.tabLayout.addTab(tab)
            }
        }
        selectedTab = model.data.items[0]
        populateTabData(0)

        widgetViewHolder.binding.tabLayout.getTabAt(0)?.select()

        widgetViewHolder.binding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val tabPos: Int = it.tag as Int

                    selectedTab = widgetModel.data.items[tabPos]
                    populateTabData(tabPos)
                }
            }
        })
    }

    private fun populateTabData(tabPos: Int) {
        val tabData = widgetModel.data.items[tabPos]
        tabData?.let {
            if (tabData.items.isNullOrEmpty()) {
                showProgress()
                fetchTabData(tabData)
            } else {
                updateAdpaterData(tabData.items, tabData)
            }
        }
    }

    private fun updateAdpaterData(items: List<TabItemData>, tabData: TabListItemsData) {
        if (adapter == null) {
            adapter = TabListItemAdapter(
                widgetModel,
                tabData,
                analyticsPublisher,
                whatsAppSharing,
                deeplinkAction
            )
            resetAdapter(items, tabData)
        } else {
            if (selectedTab.playlist == tabData.playlist) {
                resetAdapter(items, tabData)
            }
        }
    }

    private fun resetAdapter(items: List<TabItemData>, tabData: TabListItemsData) {
        if (items.isNullOrEmpty()) {
            showError()
        } else {
            hideError()
        }
        if (getRecyclerViewItemViewType(tabData.viewType) == TabListItemAdapter.TYPE_GRID) {
            widgetViewHolder.binding.rvItems.layoutManager =
                GridLayoutManager(context, 4, RecyclerView.HORIZONTAL, false)
        } else {
            widgetViewHolder.binding.rvItems.layoutManager =
                LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        }
        widgetViewHolder.binding.rvItems.adapter = adapter
        adapter?.tabData = tabData
        adapter?.updateItems(items)
        hideProgress()

    }

    private fun getRecyclerViewItemViewType(viewType: String?): Int {
        if (viewType == "grid_view") {
            return TabListItemAdapter.TYPE_GRID
        }
        return TabListItemAdapter.TYPE_LIST
    }

    fun fetchTabData(tabData: TabListItemsData) {
        val subscribe = libraryHomeService.getLibraryListingData(
            1,
            tabData.playlist.toString(),
            userPreference.getUserClass(),
            "",
            ""
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                mapListingDataToTabItemData(it.data.list, tabData)
            }
            .subscribe(
                { result ->
                    tabData.items = result
                    updateAdpaterData(result, tabData)

                },
                { error ->
                    hideProgress()
                }
            )
    }

    private fun showProgress() {
        widgetViewHolder.binding.progress.setVisibleState(true)
    }

    private fun hideProgress() {
        widgetViewHolder.binding.progress.setVisibleState(false)
    }

    private fun showError() {
        widgetViewHolder.binding.tvError.setVisibleState(true)
    }

    private fun hideError() {
        widgetViewHolder.binding.tvError.setVisibleState(false)
    }

    private fun mapListingDataToTabItemData(
        apiListingDataList: List<ApiListingData>,
        tabData: TabListItemsData
    ): List<TabItemData> =
        apiListingDataList.filter { it.viewType != WhatsappFeedEntity.type && it.viewType != NextVideoEntity.type }
            .map {
                mapToTabItemData(it, tabData)
            }

    private fun mapToTabItemData(apiListingData: ApiListingData, tabData: TabListItemsData) =
        with(apiListingData) {
            val deeplink = createDeeplink(apiListingData, tabData)

            var imageUrl = apiListingData.imageUrl
            if (imageUrl.isNullOrEmpty()) {
                imageUrl = tabData.imageUrl
            }
            TabItemData(
                apiListingData.id ?: "",
                apiListingData.name,
                imageUrl,
                apiListingData.resourceType,
                deeplink
            )
        }

    private fun createDeeplink(apiListingData: ApiListingData, tabData: TabListItemsData): String? {
        when (getRecyclerViewItemViewType(tabData.viewType)) {
            TabListItemAdapter.TYPE_GRID -> {
                return tabData.deeplink
            }
            TabListItemAdapter.TYPE_LIST -> {
                tabData.deeplink?.let {
                    val parse = Uri.parse(tabData.deeplink)
                    val paramPlaylist = parse.getQueryParameters("playlist_id")
                    return tabData.deeplink!!.replace(
                        paramPlaylist[0],
                        apiListingData.id.toString(),
                        false
                    )
                }

            }
        }
        return ""
    }
}