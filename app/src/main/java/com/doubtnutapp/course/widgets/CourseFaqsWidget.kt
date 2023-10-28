package com.doubtnutapp.course.widgets

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.applyBackgroundColor
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.PlayAudioEvent
import com.doubtnutapp.EventBus.VideoSeekEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemFaqsBinding
import com.doubtnutapp.databinding.WidgetCourseFaqsBinding
import com.doubtnutapp.domain.videoPage.entities.ApiVideoResource
import com.doubtnutapp.libraryhome.coursev3.ui.VideoHolderActivity
import com.doubtnutapp.model.Video
import com.doubtnutapp.utils.DebouncedOnClickListener
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.videoPage.model.VideoResource
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.widget_course_faqs.view.*
import javax.inject.Inject

class CourseFaqsWidget(context: Context) : BaseBindingWidget<CourseFaqsWidget.WidgetHolder,
        CourseFaqsWidgetModel, WidgetCourseFaqsBinding>(context) {

    companion object {
        const val TAG = "CourseFaqsWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp
            .INSTANCE
            .daggerAppComponent
            ?.inject(this)
    }

    override fun getViewBinding(): WidgetCourseFaqsBinding {
        return WidgetCourseFaqsBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CourseFaqsWidgetModel): WidgetHolder {
        if (model.layoutConfig == null) {
            model.layoutConfig = WidgetLayoutConfig(
                marginTop = 16,
                marginBottom = 12,
                marginLeft = 16,
                marginRight = 16
            )
        }
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: CourseFaqsWidgetData = model.data
        updateFaqsToggle(
            context,
            binding.ivToggle,
            data.toggle,
            binding.recyclerView
        )

        widgetViewHolder.itemView.parentLayout.applyBackgroundColor(data.backgroundColor)
        binding.textViewTitleMain.text = data.title.orEmpty()
        binding.textViewDescription.text = data.description.orEmpty()
        binding.textViewDescription.isVisible = data.description.isNotNullAndNotEmpty()

        binding.tvBottom.text = data.bottomText.orEmpty()
        binding.tvBottom.isVisible = data.bottomText.isNotNullAndNotEmpty()
        binding.tvSeeMore.text = data.seeMoreText.orEmpty()
        binding.tvSeeMore.isVisible = data.seeMoreText.isNotNullAndNotEmpty()

        binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL, false
        )

        binding.recyclerView.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            model.extraParams ?: HashMap()
        )

        binding.viewToggle.setOnClickListener {
            data.toggle = data.toggle != true
            updateFaqsToggle(
                context,
                binding.ivToggle,
                data.toggle,
                binding.recyclerView
            )
        }

        return holder
    }

    private fun updateFaqsToggle(
        context: Context,
        imageView: ImageView,
        toggle: Boolean?,
        view: View
    ) {
        val toggleResID: Int
        if (toggle == true) {
            toggleResID = R.drawable.ic_course_minus
            view.show()
        } else {
            toggleResID = R.drawable.ic_course_plus
            view.hide()
        }
        imageView.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                toggleResID
            )
        )
    }

    class Adapter(
        val items: List<CourseFaqsWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemFaqsBinding
                    .inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            val data = items[position]
            updateToggle(
                context = binding.root.context,
                imageView = binding.imageViewToggle,
                toggle = data.toggle,
                enableToggle = data.enablToggle,
                view = binding.textViewDesc,
                playView = binding.tvPlay,
                hasPlayText = !data.videoInfo?.text.isNullOrBlank()
            )
            binding.viewToggleHelper.setOnClickListener {
                if (data.enablToggle == false) return@setOnClickListener

                data.toggle = data.toggle != true
                updateToggle(
                    context = binding.root.context,
                    imageView = binding.imageViewToggle,
                    toggle = data.toggle,
                    enableToggle = data.enablToggle,
                    view = binding.textViewDesc,
                    playView = binding.tvPlay,
                    hasPlayText = !data.videoInfo?.text.isNullOrBlank()
                )

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.COURSE_DETAILS_EXPAND,
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to TAG,
                            EventConstants.TOGGLE to data.toggle.toString()
                        ).apply {
                            putAll(extraParams)
                        }
                    )
                )
            }
            binding.textViewTitle.text = data.title.orEmpty()
            binding.textViewTitle.applyTextColor(data.titleColor)
            binding.textViewTitle.applyTextSize(data.titleSize)

            binding.textViewDesc.text = data.description.orEmpty()
            binding.textViewDesc.applyTextColor(data.descriptionColor)
            binding.textViewDesc.applyTextSize(data.descriptionSize)

            binding.tvPlay.setOnClickListener(
                object : DebouncedOnClickListener(800) {
                    override fun onDebouncedClick(v: View?) {
                        if (data.videoInfo != null) {
                            if (!data.videoInfo?.videoResources.isNullOrEmpty()) {
                                val video = Video(
                                    data.videoInfo?.questionId,
                                    true,
                                    data.videoInfo?.viewId,
                                    data.videoInfo?.videoResources?.map { apiResource ->
                                        VideoResource(
                                            resource = apiResource.resource,
                                            drmScheme = apiResource.drmScheme,
                                            drmLicenseUrl = apiResource.drmLicenseUrl,
                                            mediaType = apiResource.mediaType,
                                            isPlayed = false,
                                            dropDownList = null,
                                            timeShiftResource = null,
                                            offset = apiResource.offset
                                        )
                                    },
                                    0,
                                    data.videoInfo?.page,
                                    false,
                                    VideoFragment.DEFAULT_ASPECT_RATIO
                                )
                                DoubtnutApp.INSTANCE.bus()?.send(PlayAudioEvent(true))
                                val intent = VideoHolderActivity.getStartIntent(
                                    binding.root.context,
                                    video
                                )
                                binding.root.context.startActivity(intent)
                            } else {
                                DoubtnutApp.INSTANCE.bus()?.send(
                                    VideoSeekEvent(
                                        data.videoInfo?.position?.toLongOrNull()
                                            ?: 0
                                    )
                                )
                            }
                            analyticsPublisher.publishEvent(
                                AnalyticsEvent(
                                    EventConstants.FAQ_PLAY_VIDEO,
                                    hashMapOf<String, Any>(
                                        EventConstants.WIDGET to TAG,
                                        EventConstants.SOURCE_ID to data.videoInfo?.id.orEmpty(),
                                        EventConstants.SOURCE to data.id.orEmpty()
                                    ).apply {
                                        putAll(extraParams)
                                    },
                                    ignoreSnowplow = true
                                )
                            )
                        }
                    }
                }
            )
            binding.tvPlay.text = data.videoInfo?.text.orEmpty()
        }

        private fun updateToggle(
            context: Context,
            imageView: ImageView,
            toggle: Boolean?,
            enableToggle: Boolean?,
            view: View,
            playView: View,
            hasPlayText: Boolean
        ) {
            val toggleResID: Int
            if (toggle == true) {
                toggleResID = R.drawable.ic_up_arrow
                view.show()
                playView.setVisibleState(hasPlayText)
            } else {
                toggleResID = R.drawable.ic_drop_down
                view.hide()
                playView.hide()
            }

            if (enableToggle == false) {
                imageView.setImageDrawable(null)
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        toggleResID
                    )
                )
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemFaqsBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetCourseFaqsBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseFaqsBinding>(binding, widget)
}

class CourseFaqsWidgetModel : WidgetEntityModel<CourseFaqsWidgetData, WidgetAction>()

@Keep
data class CourseFaqsWidgetData(
    @SerializedName("bg_color") val backgroundColor: String?,
    @SerializedName("items") val items: List<CourseFaqsWidgetItem>?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("bottom_text") val bottomText: String?,
    @SerializedName("see_more_text") val seeMoreText: String?,
    @SerializedName("toggle") var toggle: Boolean?
) : WidgetData()

@Keep
data class CourseFaqsWidgetItem(
    @SerializedName("title", alternate = ["name"]) val title: String?,
    @SerializedName("title_color", alternate = ["name_color"]) val titleColor: String?,
    @SerializedName("title_size", alternate = ["name_size"]) val titleSize: String?,
    @SerializedName("description", alternate = ["value"]) val description: String?,
    @SerializedName("description_color", alternate = ["value_color"]) val descriptionColor: String?,
    @SerializedName("description_size", alternate = ["value_size"]) val descriptionSize: String?,
    @SerializedName("toggle") var toggle: Boolean?,
    @SerializedName("enable_toggle") var enablToggle: Boolean?,
    @SerializedName("video_info") var videoInfo: VideoInfo?,
    @SerializedName("id") var id: String?
)

@Parcelize
@Keep
data class VideoInfo(
    @SerializedName("text") val text: String?,
    @SerializedName("position") val position: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("view_id") val viewId: String?,
    @SerializedName("page") val page: String?,
    @SerializedName("question_id", alternate = ["qid"]) val questionId: String?,
    @SerializedName("video_resources") val videoResources: List<ApiVideoResource>?
) : Parcelable
