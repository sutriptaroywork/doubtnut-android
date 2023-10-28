package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
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
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.course.widgets.CourseCarouselWidget.*
import com.doubtnutapp.databinding.ItemCourseCarouselBinding
import com.doubtnutapp.databinding.WidgetCourseCarouselBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.liveclass.ui.LiveClassActivity
import com.doubtnutapp.utils.DateUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseCarouselWidget(context: Context) :
    BaseBindingWidget<WidgetHolder,
            Model, WidgetCourseCarouselBinding>(context) {

    companion object {
        const val TAG = "CourseCarouselWidget"
        const val EVENT_TAG = "widget_course_carousel"

        const val FUTURE = 0
        const val LIVE = 1
        const val PAST = 2
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetCourseCarouselBinding {
        return WidgetCourseCarouselBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: Model
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
        })
        val binding = holder.binding
        val data: Data = model.data

        binding.textViewTitleMain.text = data.title.orEmpty()

        binding.textViewViewAll.isVisible = !data.viewAllText.isNullOrBlank()
        binding.textViewViewAll.text = data.viewAllText.orEmpty()
        binding.textViewViewAll.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    model.type + "_"+ EventConstants.CTA_CLICKED,
                    hashMapOf<String, Any>(
                        Constants.TEXT to data.viewAllText.toString()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
            deeplinkAction.performAction(context, data.viewAllDeeplink)
        }

        binding.rvItems.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL, false
        )
        binding.rvItems.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            deeplinkAction,
            context,
            model.extraParams,
            model.type
        )

        return holder
    }

    class Adapter(
        val items: List<Item>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val context: Context,
        val extraParams: HashMap<String, Any>?,
        val type: String?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemCourseCarouselBinding
                    .inflate(LayoutInflater.from(context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Utils.setWidthBasedOnPercentage(
                holder.itemView.context,
                holder.itemView,
                "1.1",
                R.dimen.spacing
            )
            val item = items[position]
            val binding = holder.binding
            binding.cardContainer.setBackgroundColor(
                Utils.parseColor(
                    item.color.orDefaultValue("#c9f3ff"),
                    Color.BLUE
                )
            )

            binding.textViewSubject.text = item.subject.orEmpty()
            binding.textViewTitleInfo.text = item.title.orEmpty()

            binding.textViewFacultyInfo.text = item.faculty.orEmpty()
            binding.tvLectureInfo.text = item.lectureNumber.orEmpty()
            binding.tvBottom.text = item.bottomText.orEmpty()

            binding.imageViewFaculty.loadImageEtx(item.facultyImage.orEmpty())
            binding.root.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        type + "_" + EventConstants.CLICKED,
                        hashMapOf<String, Any>().apply {
                            putAll(extraParams.orEmpty())
                        }
                    )
                )
                if (!item.deeplink.isNullOrBlank()) {
                    deeplinkAction.performAction(holder.itemView.context, item.deeplink)
                } else {
                    playVideo(
                        holder.itemView.context,
                        item.questionId,
                        item.page,
                        item.state,
                        item.liveAt
                    )
                }
            }
        }

        private fun playVideo(
            context: Context,
            id: String?,
            page: String?,
            state: Int,
            liveAt: String?
        ) {
            if (state == LIVE || state == PAST || DateUtils.isBeforeCurrentTime(liveAt?.toLongOrNull())) {
                // allow to watch video
                if (context is LiveClassActivity) {
                    context.finish()
                }
                context.startActivity(
                    VideoPageActivity.startActivity(
                        context = context,
                        questionId = id.orEmpty(),
                        page = page.orEmpty()
                    )
                )
            } else {
                // for future state
                showToast(context, context.getString(R.string.coming_soon))
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemCourseCarouselBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetCourseCarouselBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseCarouselBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("items") val items: List<Item>?,
        @SerializedName("title") val title: String?,
        @SerializedName("view_all_text") val viewAllText: String?,
        @SerializedName("view_all_deeplink") val viewAllDeeplink: String?
    ) : WidgetData()


    @Keep
    data class Item(
        @SerializedName("title") val title: String?,
        @SerializedName("subject") val subject: String?,
        @SerializedName("faculty") val faculty: String?,
        @SerializedName("faculty_image") val facultyImage: String?,
        @SerializedName("color") val color: String?,
        @SerializedName("lecture_number") val lectureNumber: String?,
        @SerializedName("bottom_text") val bottomText: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("question_id") val questionId: String?,
        @SerializedName("page") val page: String?,
        @SerializedName("live_at") val liveAt: String?,
        @SerializedName("state") val state: Int
    )

}


