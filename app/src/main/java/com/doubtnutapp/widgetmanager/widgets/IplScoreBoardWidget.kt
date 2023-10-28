package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetShownEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnLiveScoreConnectionSuccess
import com.doubtnutapp.base.OnLiveScoreReceived
import com.doubtnutapp.databinding.ItemIplScoreboardBinding
import com.doubtnutapp.databinding.WidgetIplScoreBoardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.matchquestion.viewmodel.MatchQuestionViewModel
import com.doubtnutapp.sales.event.PrePurchaseCallingCardDismiss
import com.doubtnutapp.socket.SocketManager
import com.doubtnutapp.socket.entity.LiveScoreData
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.google.android.material.tabs.TabLayout
import com.google.gson.annotations.SerializedName
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

class IplScoreBoardWidget(context: Context) :
    BaseBindingWidget<IplScoreBoardWidget.WidgetHolder, IplScoreBoardWidget.Model, WidgetIplScoreBoardBinding>(
        context
    ) {

    companion object {
        const val TAG = "IplScoreBoardWidget"
        const val PATH = "cricket-score"
    }

    @Inject
    lateinit var socketManager: SocketManager

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    var source: String? = null

    var roomId : String? = ""
    var selectedTab : String? = ""

    private var disposable: Disposable? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    var adapter: IplScoreBoardAdapter? = null

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model)
        socketManager.connectForLiveScore(PATH)
        val data: Data = model.data
        roomId = data.widgetRoomId

        disposable = DoubtnutApp.INSTANCE
            .bus()?.toObservable()?.subscribe { event ->
                when (event) {
                    is OnLiveScoreConnectionSuccess -> {

                        try {
                            compositeDisposable.add(
                                socketManager.join(roomId)
                                    .subscribeOn(Schedulers.io())
                                    .subscribe()
                            )
                        }catch (e : Exception){
                            e.printStackTrace()
                        }

                    }
                    is OnLiveScoreReceived ->{
                        if(event.liveScore is LiveScoreData){
                            data.items?.get(selectedTab)?.find { it.key==roomId }?.let {
                                it.teamOneScore = event.liveScore.teamOneScore
                                it.teamTwoScore = event.liveScore.teamTwoScore
                                it.matchResult = event.liveScore.matchResult
                                GlobalScope.launch(Dispatchers.Main) {  adapter?.notifyDataSetChanged() }

                            }

                        }
                    }
                }
            }
        disposable?.let { compositeDisposable.add(it) }

        widgetViewHolder.binding.apply {


            textViewTitleMain.apply {
                text = data.title.orEmpty()
                isVisible = !data.title.isNullOrEmpty()
            }

            tvSubtitle.apply {
                text = data.titleTwo.orEmpty()
            }

            recyclerView.layoutManager =
                LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = IplScoreBoardAdapter(
                model._data?.items?.get("Recent"),
                actionPerformer,
                analyticsPublisher,
                socketManager
            )


            recyclerView.adapter = adapter

            tabLayout.apply {
                // Clearing is necessary to prevent selected tab getting reset on recycler view scroll
                clearOnTabSelectedListeners()
                removeAllTabs()

                var selectedTabPosition = 0
                data.tabs?.forEachIndexed { index, tabData ->
                    tabLayout.addTab(
                        tabLayout.newTab()
                            .setText(tabData.title)
                            .setTag(tabData.key)
                    )
                    if (tabData.isSelected) {
                        selectedTabPosition = index
                        adapter?.setItems(data.items?.get(tabData.key).orEmpty())
                        selectedTab = tabData.key
                    }
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.HOME_PAGE_CAROUSEL_TAB_VISIBLE,
                            hashMapOf(
                                Constants.TAB_TITLE to tabData.title,
                                Constants.TAB_KEY to tabData.key,
                                EventConstants.ITEM_POSITION to index,
                                EventConstants.SOURCE to source.orEmpty(),
                                EventConstants.PARENT_TITLE to data.title.orEmpty()
                            )
                        )
                    )
                }

                addOnTabSelectedListener { tab ->
                    val selectedTabItems = data.items?.get(tab.tag).orEmpty()
                    if (selectedTabItems.isNotEmpty()) {
                        adapter?.setItems(selectedTabItems)
                        widgetViewHolder.binding.recyclerView?.smoothScrollToPosition(0)
                        recyclerView.show()
                        textViewNoData.hide()
                    } else {
                        recyclerView.hide()
                        textViewNoData.show()
                    }

                    data.tabs?.forEach {
                        it.isSelected = false
                    }
                    if (tab.position != TabLayout.Tab.INVALID_POSITION) {
                        selectedTabPosition = tab.position
                    }
                    selectedTabPosition = tab.position
                    data.tabs?.getOrNull(tab.position)?.let {
                        it.isSelected = true
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.HOME_PAGE_CAROUSEL_TAB_CLICKED,
                                hashMapOf(
                                    Constants.TAB_TITLE to it.title,
                                    Constants.TAB_KEY to it.key,
                                    EventConstants.ITEM_POSITION to tab.position,
                                    EventConstants.SOURCE to source.orEmpty(),
                                    EventConstants.PARENT_TITLE to data.title.orEmpty()
                                ), ignoreSnowplow = true
                            )
                        )
                    }
                }

                afterMeasured {
                    tabLayout.getTabAt(selectedTabPosition)?.select()
                }
            }

        }

        return holder
    }


    override fun onDetachedFromWindow() {
        compositeDisposable.clear()
        socketManager.disconnectLiveScore()
        super.onDetachedFromWindow()
    }

    class WidgetHolder(
        binding: WidgetIplScoreBoardBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetIplScoreBoardBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("title_two") val titleTwo: String?,
        @SerializedName("tabs") val tabs: List<TabData>?,
        @SerializedName("items") var items: Map<String, List<MatchData>>?,
        @SerializedName("widget_room_id") val widgetRoomId: String?
    ) : WidgetData()

    @Keep
    data class TabData(
        @SerializedName("id") val playListId: String,
        @SerializedName("type") val type: String,
        @SerializedName("title") val title: String,
        @SerializedName("key") val key: String,
        @SerializedName("is_selected") var isSelected: Boolean,
    )

    @Keep
    data class MatchData(
        @SerializedName("match_details") val matchDetails: String?,
        @SerializedName("match_status") val matchStatus: String?,
        @SerializedName("match_venue") val matchVenue: String?,
        @SerializedName("team_one") val teamOne: String?,
        @SerializedName("team_one_flag") val teamOneFlag: String?,
        @SerializedName("team_one_score") var teamOneScore: String?,
        @SerializedName("team_two") val teamTwo: String?,
        @SerializedName("team_two_flag") val teamTwoFlag: String?,
        @SerializedName("team_two_score") var teamTwoScore: String?,
        @SerializedName("match_result") var matchResult: String?,
        @SerializedName("reminder_text") val reminderText: String?,
        @SerializedName("key") val key: String?,
    )

    override fun getViewBinding(): WidgetIplScoreBoardBinding {
        return WidgetIplScoreBoardBinding.inflate(LayoutInflater.from(context), this, true)
    }


    class IplScoreBoardAdapter(
        var items: List<MatchData>?,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val socketManager: SocketManager
    ) : RecyclerView.Adapter<IplScoreBoardAdapter.IplScoreBoardHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IplScoreBoardHolder {
            return IplScoreBoardHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_ipl_scoreboard, parent, false)
            )
        }

        override fun onBindViewHolder(holder: IplScoreBoardHolder, position: Int) {
            holder.binding.apply {
                val scoreItem = items?.get(position)
                tvMatchDetails.text = scoreItem?.matchDetails.orEmpty()
                if (scoreItem?.matchStatus?.contains("Live") == true) {
                    tvMatchStatus.text = "LIVE"
                    tvMatchStatus.show()
                } else {
                    tvMatchStatus.hide()
                }
                tvMatchVenue.text = scoreItem?.matchVenue.orEmpty()
                ivFlagTeamOne.loadImage(scoreItem?.teamOneFlag.orEmpty())
                tvTeamOne.text = scoreItem?.teamOne.orEmpty()
                tvTeamOneScore.text = scoreItem?.teamOneScore.orEmpty()
                ivFlagTeamTwo.loadImage(scoreItem?.teamTwoFlag.orEmpty())
                tvTeamTwo.text = scoreItem?.teamTwo.orEmpty()
                tvTeamTwoScore.text = scoreItem?.teamTwoScore.orEmpty()
                tvMatchResult.text = scoreItem?.matchResult.orEmpty()
            }
        }

        override fun getItemCount(): Int = items?.size!!

        @JvmName("setItems1")
        fun setItems(items: List<MatchData>?) {
            this.items = items
            notifyDataSetChanged()
        }

        class IplScoreBoardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemIplScoreboardBinding.bind(itemView)
        }
    }
}
