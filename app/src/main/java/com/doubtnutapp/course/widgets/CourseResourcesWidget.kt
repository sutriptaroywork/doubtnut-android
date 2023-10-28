package com.doubtnutapp.course.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Keep
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.data.*
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnutapp.*
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.ItemCourseCardLeaderboardBinding
import com.doubtnutapp.databinding.ItemCourseResourceBinding
import com.doubtnutapp.databinding.WidgetCourseResourceBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.leaderboard.widget.LeaderboardPersonalDataItem
import com.doubtnutapp.leaderboard.widget.LeaderboardPersonalWidget
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import com.skydoves.balloon.*
import kotlinx.coroutines.flow.firstOrNull
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class CourseResourcesWidget(context: Context) :
    BaseBindingWidget<CourseResourcesWidget.WidgetHolder,
        CourseResourcesWidget.Model, WidgetCourseResourceBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var defaultDataStore: DefaultDataStore

    var source: String? = null
    var lifecycleOwner: LifecycleOwner? = null

    companion object {
        const val TAG = "CourseResourcesWidget"

        const val VIEW_DEFAULT = 1
        const val VIEW_LEADERBOARD = 2

        /**
         * Handling Safely/manually based on view lifecycle.
         */
        @SuppressLint("StaticFieldLeak")
        var balloon: Balloon? = null
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetCourseResourceBinding {
        return WidgetCourseResourceBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val data: Data = model.data
        val binding = holder.binding
        binding.textViewTitleMain.text = data.title.orEmpty()

        lifecycleOwner?.lifecycleScope?.launchWhenResumed {
            val ids = defaultDataStore.courseCardBalloonIdsShown.firstOrNull()
                ?.split("--")
                ?.filter { it.isNotNullAndNotEmpty() }
                ?.distinct()
                .orEmpty()

            data.items.forEach {
                if (it.ttsDisplayText.isNotNullAndNotEmpty() && !ids.contains(it.cardId)) {
                    it.showBalloon = true
                }
            }
            setAdapter(binding, model)
        } ?: setAdapter(binding, model)

        return holder
    }

    private fun setAdapter(binding: WidgetCourseResourceBinding, model: Model) {
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        val adapter = Adapter(
            items = model.data.items,
            extraParams = model.extraParams
                ?: HashMap(),
            parentTitle = model.data.title.orEmpty(),
            ttsLocale = model.data.ttsLocale.ifEmptyThenNull() ?: "en_IN"
        )
        binding.recyclerView.adapter = adapter
    }

    class WidgetHolder(binding: WidgetCourseResourceBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseResourceBinding>(binding, widget) {

        override fun onViewDetachedFromWindow() {
            super.onViewDetachedFromWindow()
            balloon?.dismiss()
        }
    }

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("tts_locale") val ttsLocale: String?,
        @SerializedName("items") val items: List<Item>
    ) : WidgetData()

    @Keep
    data class Item(
        @SerializedName("card_id") val cardId: String?,
        @SerializedName("bg_image") val bgImage: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("display_name") val displayName: String?,
        @SerializedName("bottom_title") val bottomTitle: String?,
        @SerializedName("is_content_available") val isContentAvailable: Boolean?,
        @SerializedName("display_name_color") val displayNameColor: String?,
        @SerializedName("text_font_size") val textFontSize: String?,
        @SerializedName("data") val data: ItemData?,
        @SerializedName("tts_display_text") var ttsDisplayText: String?,
        @SerializedName("tts_text") val ttsText: String?,
        @SerializedName("priority") val priority: Int?,
        @SerializedName("show_balloon") var showBalloon: Boolean?,
    )

    @Keep
    data class ItemData(
        @SerializedName("1") val items: List<LeaderboardPersonalDataItem>?
    )

    inner class Adapter(
        val items: List<Item>,
        val extraParams: HashMap<String, Any>,
        val parentTitle: String,
        val ttsLocale: String
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), TextToSpeech.OnInitListener {

        /**
         * In Vivo device this comes out to be null
         * https://console.firebase.google.com/u/0/project/doubtnut-e000a/crashlytics/app/android:com.doubtnutapp/issues/a0d5a7e03a607e3f0c27a14f57c317ac?time=last-twenty-four-hours&sessionEventKey=61DE893000050001521FE594C293BF58_1630671644693311953&sessionTab=data&userInfoQuery=163030672
         */
        private var tts: TextToSpeech? = TextToSpeech(context, this).apply {
            language = Locale(ttsLocale)
        }

        private var isTtsConnected = false
        private var balloonData: Item? = null
        private var attachedView: View? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                VIEW_LEADERBOARD -> LeaderBoardVH(
                    ItemCourseCardLeaderboardBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
                else -> ViewHolder(
                    ItemCourseResourceBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is ViewHolder -> holder.bind()
                is LeaderBoardVH -> holder.bind()
            }
        }

        override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
            super.onViewAttachedToWindow(holder)
            if (balloon?.isShowing == true) return
            if (attachedView != null) return
            if (balloonData != null) return

            if (items.getOrNull(holder.bindingAdapterPosition)?.showBalloon == true) {
                attachedView = holder.itemView
                balloonData = items.getOrNull(holder.bindingAdapterPosition)
            }
        }

        private fun showBalloon() {
            if (balloon?.isShowing == true) return
            if (attachedView == null) return
            if (balloonData == null) return
            if (balloonData?.showBalloon != true) return

            balloon?.dismiss()

            balloon = createBalloon(context) {
//                setMaxWidth(max(attachedView?.width?.pxToDp() ?: 0, 200))

                setText(balloonData?.ttsDisplayText.orEmpty())
                setTextColorResource(R.color.white)
                setTextSize(14f)

                if (isTtsConnected && balloonData?.ttsText.isNotNullAndNotEmpty()) {
                    setIconSize(18)
                    setIconDrawableResource(R.drawable.ic_volume)
                    setIconGravity(IconGravity.END)
                    setIconColorResource(R.color.white)
                }

//                setArrowOrientation(ArrowOrientation.TOP)
//                setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)

                setArrowSize(10)
                if (balloonData?.ttsDisplayText.orEmpty().length <= 18) {
                    setArrowPosition(0.8f)
                }

                setPadding(12)
                setCornerRadius(2f)
                setBackgroundColor(Color.parseColor("#d9000000"))
                setLifecycleOwner(lifecycleOwner)

                setDismissWhenTouchOutside(false)

                setOnBalloonDismissListener {
                    tts?.stop()
                    balloon = null
                }
            }

            balloon?.setOnBalloonClickListener {
                balloon?.dismiss()
            }

            balloon?.showAlignBottom(
                anchor = attachedView ?: return,
                xOff = 0,
                yOff = 0
            )

            balloonData?.showBalloon = false

            lifecycleOwner?.lifecycleScope?.launchWhenResumed {
                val ids = defaultDataStore.courseCardBalloonIdsShown.firstOrNull()
                if (balloonData?.cardId.isNullOrEmpty()) return@launchWhenResumed

                if (ids.isNullOrEmpty()) {
                    defaultDataStore.set(
                        DefaultDataStoreImpl.COURSE_CARD_BALLOON_IDS_SHOWN,
                        balloonData?.cardId.orEmpty()
                    )
                } else {
                    defaultDataStore.set(
                        DefaultDataStoreImpl.COURSE_CARD_BALLOON_IDS_SHOWN,
                        ids + "--" + balloonData?.cardId.orEmpty()
                    )
                }
            }

            if (isTtsConnected && balloonData?.ttsText.isNotNullAndNotEmpty()) {
                tts?.speak(
                    balloonData?.ttsText.orEmpty(),
                    TextToSpeech.QUEUE_ADD,
                    null,
                    balloonData?.ttsText.orEmpty()
                )
            }
        }

        private val utteranceProgressListener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
            }

            override fun onDone(utteranceId: String?) {
                balloon?.dismissWithDelay(2000)
            }

            override fun onError(utteranceId: String?) {
            }
        }

        override fun getItemCount(): Int = items.size

        override fun getItemViewType(position: Int): Int {
            return when (items[position].cardId) {
                "leaderboard" -> VIEW_LEADERBOARD
                else -> VIEW_DEFAULT
            }
        }

        inner class ViewHolder(val binding: ItemCourseResourceBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind() {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
                val item = items[bindingAdapterPosition]
                binding.cardContainer.loadBackgroundImage(item.bgImage, R.color.grey)
                binding.textViewHeader.text = item.displayName.orEmpty()
                binding.textViewHeader.setTextColor(Utils.parseColor(item.displayNameColor))
                binding.textViewHeader.textSize = item.textFontSize?.toFloat() ?: 16f
                binding.textViewSubHeader.text = item.bottomTitle.orEmpty()
                binding.textViewSubHeader.setTextColor(Utils.parseColor(item.displayNameColor))

                binding.cardView.isEnabled =
                    item.deeplink.isNotNullAndNotEmpty() || item.bottomTitle.isNotNullAndNotEmpty()

                binding.root.setOnClickListener {
                    if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                    if (item.isContentAvailable!!) {
                        deeplinkAction.performAction(
                            binding.root.context,
                            item.deeplink,
                            source.orEmpty()
                        )
                    } else if (item.bottomTitle?.length!! > 0) {
                        ToastUtils.makeText(
                            binding.root.context,
                            item.bottomTitle,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    publishEvent(item)
                }
            }
        }

        inner class LeaderBoardVH(val binding: ItemCourseCardLeaderboardBinding) :
            RecyclerView.ViewHolder(binding.root) {

            init {
                binding.cvClick.setOnClickListener {
                    if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                    val item = items[bindingAdapterPosition]
                    if (item.isContentAvailable == true) {
                        deeplinkAction.performAction(
                            binding.root.context,
                            item.deeplink,
                            source.orEmpty()
                        )
                    } else if (item.bottomTitle?.length!! > 0) {
                        ToastUtils.makeText(
                            binding.root.context,
                            item.bottomTitle,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    publishEvent(item)
                }
            }

            fun bind() {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
                val item = items[bindingAdapterPosition]
                binding.clContainer.loadBackgroundImage(item.bgImage, R.color.grey)
                binding.tvTitle.text = item.displayName

                binding.tvTitle.text = item.displayName.orEmpty()
                binding.tvTitle.applyTextColor(item.displayNameColor)
                binding.tvTitle.applyTextSize(item.textFontSize)

                item.data?.items?.getOrNull(0)?.let {
                    LeaderboardPersonalWidget.bind(binding.itemOne, it, deeplinkAction)
                }

                item.data?.items?.getOrNull(1)?.let {
                    LeaderboardPersonalWidget.bind(binding.itemTwo, it, deeplinkAction)
                }

                binding.tvBottomTitle.text = item.bottomTitle
                binding.tvBottomTitle.applyTextColor(item.displayNameColor)

                binding.cvClick.isEnabled =
                    item.deeplink.isNotNullAndNotEmpty() || item.bottomTitle.isNotNullAndNotEmpty()
            }
        }

        fun publishEvent(item: Item) {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.LC_COURSE_PAGE_CARD_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.CARD_ID to item.cardId.orEmpty(),
                        EventConstants.CARD_STATE to if (item.isContentAvailable == false) {
                            "locked"
                        } else {
                            "unlocked"
                        }
                    ).apply {
                        putAll(extraParams)
                    },
                    ignoreBranch = false
                )
            )
        }

        override fun onInit(status: Int) {
            if (status != TextToSpeech.SUCCESS) return
            isTtsConnected = true
            tts?.setOnUtteranceProgressListener(utteranceProgressListener)
            lifecycleOwner?.lifecycleScope?.launchWhenResumed {
                showBalloon()
            }
        }
    }
}
