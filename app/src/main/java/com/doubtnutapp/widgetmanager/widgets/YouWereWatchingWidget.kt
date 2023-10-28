package com.doubtnutapp.widgetmanager.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemYouWereWatchingBinding
import com.doubtnutapp.databinding.ItemYouWereWatchingVideoCardBinding
import com.doubtnutapp.databinding.WidgetYouWereWatchingBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.google.gson.annotations.SerializedName
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by devansh on 9/2/21.
 */

class YouWereWatchingWidget(context: Context) :
    BaseBindingWidget<YouWereWatchingWidget.WidgetHolder, YouWereWatchingWidget.Model, WidgetYouWereWatchingBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetYouWereWatchingBinding {
        return WidgetYouWereWatchingBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        val data = model.data
        val binding = holder.binding
        val recyclerViewItems = listOf(YouWereWatchingItem(), *data.videos.toTypedArray())
        binding.recyclerView.adapter =
            Adapter(recyclerViewItems, deeplinkAction, model.extraParams)
        trackingViewId = data.id
        return holder
    }

    class Adapter(
        private val items: List<RecyclerViewItem>,
        private val deeplinkAction: DeeplinkAction,
        private val extraParams: HashMap<String, Any>?
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                R.layout.item_you_were_watching_video_card -> YouAreWatchingVideoCardViewHolder(
                    LayoutInflater.from(parent.context).inflate(viewType, parent, false)
                )
                R.layout.item_you_were_watching -> ViewHolder(
                    LayoutInflater.from(parent.context).inflate(viewType, parent, false)
                )
                else -> throw IllegalStateException("Cannot create view for the view type.")
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val data = items[position]) {
                is VideoData -> {

                    val binding = (holder as YouAreWatchingVideoCardViewHolder).binding
                    holder.itemView.apply {
                        val width = Utils.getWidthFromScrollSize(context, "1.4") -
                                (binding.cardView.marginStart + binding.cardView.marginEnd)
                        binding.cardView.updateLayoutParams {
                            this.width = width
                        }
                        requestLayout()

                        if (data.ocrText.isNullOrBlank().not()) {
                            binding.mathView.apply {
                                show()
                                setFontSize(12)
                                setTextColor("black")
                                text = data.ocrText
                            }
                            binding.ivThumbnail.hide()
                        } else {
                            binding.mathView.hide()
                            binding.ivThumbnail.show()
                            binding.ivThumbnail.loadImage(data.thumbnailImage)
                        }

                        if (data.totalTimeSeconds <= 0) {
                            binding.progressViews.invisible()
                        } else {
                            binding.progressViews.show()

                            val getDurationText: (seconds: Int) -> CharSequence = { seconds ->
                                "%02d:%02d".format(seconds / 60, seconds % 60)
                            }

                            val (watchedTimeHour, totalTimeHour) = Pair(
                                TimeUnit.SECONDS.toHours(data.watchedTimeSeconds.toLong()),
                                TimeUnit.SECONDS.toHours(data.totalTimeSeconds.toLong())
                            )
                            val getHourText: (timeHour: Long) -> CharSequence = { timeHour ->
                                if (totalTimeHour > 0) "%02d:".format(timeHour) else ""
                            }

                            binding.tvWatchedTime.text =
                                "${getHourText(watchedTimeHour)}${getDurationText(data.watchedTimeSeconds)}"
                            binding.tvTotalTime.text =
                                "${getHourText(totalTimeHour)}${getDurationText(data.totalTimeSeconds)}"

                            binding.seekbar.apply {
                                max = data.totalTimeSeconds
                                progress = data.watchedTimeSeconds
                                disableSeek()
                            }
                        }

                        setOnClickListener {
                            DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(extraParams?.apply {
                                put(Constants.QUESTION_ID, data.questionId)
                            }))
                            deeplinkAction.performAction(context, data.deeplink)
                        }
                    }
                }
                is YouWereWatchingItem -> {
                    /* no-op All data is set in XML */
                }
            }
        }

        override fun getItemCount(): Int = items.size

        override fun getItemViewType(position: Int): Int {
            return when (items[position]) {
                is VideoData -> R.layout.item_you_were_watching_video_card
                is YouWereWatchingItem -> R.layout.item_you_were_watching
                else -> 0
            }
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemYouWereWatchingBinding.bind(itemView)
        }

        class YouAreWatchingVideoCardViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            val binding = ItemYouWereWatchingVideoCardBinding.bind(itemView)
        }
    }

    interface RecyclerViewItem

    class WidgetHolder(binding: WidgetYouWereWatchingBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetYouWereWatchingBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String?,
        @SerializedName("videos") val videos: List<VideoData>,
    ) : WidgetData()

    @Keep
    data class VideoData(
        @SerializedName("question_id") val questionId: String,
        @SerializedName("watched_time") val watchedTimeSeconds: Int,
        @SerializedName("total_time") val totalTimeSeconds: Int,
        @SerializedName("ocr_text") val ocrText: String?,
        @SerializedName("thumbnail_image") val thumbnailImage: String?,
        @SerializedName("deeplink") val deeplink: String
    ) : RecyclerViewItem

    @Keep
    class YouWereWatchingItem : RecyclerViewItem
}