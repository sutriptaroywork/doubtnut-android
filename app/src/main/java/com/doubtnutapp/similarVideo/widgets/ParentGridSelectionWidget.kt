package com.doubtnutapp.similarVideo.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.InsertItemsOfATabKey
import com.doubtnutapp.EventBus.ShuffleNcertSimilarVideoList
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.InsertChildrenAtNode
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.WidgetNcertTopicBinding
import com.doubtnutapp.databinding.WidgetNcertTopicListBinding
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


class ParentGridSelectionWidget(context: Context) :
    BaseBindingWidget<ParentGridSelectionWidget.WidgetHolder,
            ParentGridSelectionWidget.Model, WidgetNcertTopicListBinding>(context) {

    var source: String? = null

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var exerciseListAdapter: TopicAdapter? = null

    private var similarVideoAdapter: WidgetLayoutAdapter? = null

    private lateinit var binding: WidgetNcertTopicListBinding

    interface TopicClickListener {
        fun onTopicClick(playlistId: String, type: String, key: String, position: Int)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model)
        val data = model.data
        binding = holder.binding

        similarVideoAdapter = WidgetLayoutAdapter(context, actionPerformer, source.orEmpty())

        DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { action ->
            when (action) {
                is InsertItemsOfATabKey -> {
                    updateItems(action.playlistId, action.value, data)
                }

                is ShuffleNcertSimilarVideoList -> {
                    updateItems(action.playlistId, action.value, data)
                }
            }
        }?.apply {
            compositeDisposable.add(this)
        }

        exerciseListAdapter = TopicAdapter(data.tabs, object : TopicClickListener {
            override fun onTopicClick(
                playlistId: String,
                type: String,
                key: String,
                position: Int
            ) {
                val tab = data.tabs[position]
                val tabKey = tab.key

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EXERCISE_TAPPED,
                        hashMapOf(
                            EventConstants.PLAYLIST_ID to playlistId,
                            EventConstants.PLAYLIST_TYPE to type,
                            Constants.TAB_KEY to key,
                            Constants.TAB_TITLE to tab.title,
                            EventConstants.SOURCE to source.orEmpty(),
                            EventConstants.PARENT_TITLE to data.title.orEmpty()
                        ), ignoreSnowplow = true
                    )
                )

                data.items?.let {
                    val ncertVideoList = it[tabKey]
                    if (ncertVideoList != null) {
                        similarVideoAdapter?.setWidgets(ncertVideoList)
                        notifyTabs(key, data)
                    } else {
                        loadNcertVideos(
                            playlistId = playlistId,
                            key = key,
                            type = type,
                            data = data
                        )
                    }
                } ?: loadNcertVideos(playlistId = playlistId, key = key, type = type, data = data)
            }
        })
        binding.rvTopicList.adapter = exerciseListAdapter
        exerciseListAdapter?.notifyDataSetChanged()
        binding.rvTopicList.smoothScrollToPosition(data.tabs.indexOfFirst { it.isSelected })

        binding.rvNcertSimilar.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvNcertSimilar.adapter = similarVideoAdapter
        val selectedTab = data.tabs.find { it.isSelected }
        if (selectedTab != null) {
            data.items?.let {
                val similarList = it[selectedTab.key]
                similarVideoAdapter?.setWidgets(similarList.orEmpty())
            }
        }
        return holder
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateItems(playlistId: String, value: List<WidgetEntityModel<*, *>>?, data: Data) {
        val itemList = value ?: return
        val items = data.items ?: return
        val key = data.tabs.find { tab -> tab.playListId == playlistId }?.key ?: return
        val newMap = items.toMutableMap()
        newMap[key] = itemList
        data.items = newMap

        // Notify if currently showing this list
        if (data.tabs.find { it.isSelected }?.playListId == playlistId) {
            similarVideoAdapter?.setWidgets(itemList)
            similarVideoAdapter?.notifyDataSetChanged()
        } else {
            notifyTabs(key, data)
        }
    }

    class TopicAdapter(
        val tabList: List<TabData>,
        private var topicClickListener: TopicClickListener,
    ) : RecyclerView.Adapter<TopicAdapter.ViewHolder>() {

        class ViewHolder(val binding: WidgetNcertTopicBinding) :
            RecyclerView.ViewHolder(binding.root) {
            val textView: TextView

            init {
                textView = binding.root.findViewById(R.id.tvTitle)
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                WidgetNcertTopicBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
            )
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

            val tabData = tabList[position]
            viewHolder.textView.apply {
                text = tabData.title
                if (tabData.isSelected) {
                    background =
                        ContextCompat.getDrawable(context, R.drawable.bg_ncert_topic_selected)
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                } else {
                    background =
                        ContextCompat.getDrawable(context, R.drawable.bg_ncert_topic_unselected)
                    setTextColor(ContextCompat.getColor(context, R.color.purple))
                }
                setOnClickListener {
                    topicClickListener.onTopicClick(
                        tabData.playListId,
                        tabData.type,
                        tabData.key,
                        position
                    )
                }
            }
        }

        override fun getItemCount() = tabList.size

    }


    @SuppressLint("NotifyDataSetChanged")
    private fun notifyTabs(key: String, data: Data) {
        data.tabs.forEach { it.isSelected = false }
        data.tabs.find { it.key == key }?.isSelected = true
        exerciseListAdapter?.notifyDataSetChanged()
    }

    private fun loadNcertVideos(playlistId: String, key: String, type: String, data: Data) {
        binding.progressBar.show()
        compositeDisposable.add(
            DataHandler.INSTANCE.ncertSimilarVideoRepository
                .getNcertVideoAdditionalData(playlistId = playlistId, type = type, questionId = "")
                .applyIoToMainSchedulerOnSingle()
                .subscribe(
                    {
                        binding.progressBar.hide()
                        it.data.ncertSimilar?.let { ncertSimilar ->
                            similarVideoAdapter?.setWidgets(ncertSimilar)
                            data.items?.let { map ->
                                val newMap = map.toMutableMap()
                                newMap[key] = ncertSimilar
                                data.items = newMap
                            }

                            notifyTabs(key, data)

                            actionPerformer?.performAction(
                                InsertChildrenAtNode(
                                    playlistId,
                                    ncertSimilar
                                )
                            )
                        }
                    },
                    {
                        binding.progressBar.hide()
                        it.printStackTrace()
                    }
                ))
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        compositeDisposable.clear()
    }

    class WidgetHolder(
        binding: WidgetNcertTopicListBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetNcertTopicListBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String,
        @SerializedName("title") val title: String?,
        @SerializedName("title_text_size") val titleTextSize: Float?,
        @SerializedName("is_title_bold") val isTitleBold: Boolean?,
        @SerializedName("link_text") val linkText: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("scroll_direction") val scrollDirection: String?,
        @SerializedName("tabs") val tabs: List<TabData>,
        @SerializedName("items") var items: Map<String, List<WidgetEntityModel<*, *>>>?,
    ) : WidgetData()

    @Keep
    data class TabData(
        @SerializedName("id") val playListId: String,
        @SerializedName("type") val type: String,
        @SerializedName("title") val title: String,
        @SerializedName("key") val key: String,
        @SerializedName("is_selected") var isSelected: Boolean,
    )

    override fun getViewBinding(): WidgetNcertTopicListBinding {
        return WidgetNcertTopicListBinding.inflate(LayoutInflater.from(context), this, true)
    }
}