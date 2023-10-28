package com.doubtnutapp.course.widgets

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemStoryBinding
import com.doubtnutapp.databinding.WidgetStoryBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.liveclass.ui.StoryDetailActivity
import com.doubtnutapp.loadImage
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

class StoryWidget(context: Context) : BaseBindingWidget<StoryWidget.WidgetHolder,
    StoryWidgetModel, WidgetStoryBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetStoryBinding {
        return WidgetStoryBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: StoryWidgetModel,
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        binding.rvStories.layoutManager =
            LinearLayoutManager(holder.itemView.context, RecyclerView.HORIZONTAL, false)
        binding.rvStories.adapter =
            StoryListAdapter(model.data.items.orEmpty(), actionPerformer)
        return holder
    }

    class StoryListAdapter(
        var items: List<StoryWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
    ) : RecyclerView.Adapter<StoryListAdapter.StatusViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
            return StatusViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_story, parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
            val data = items[position]
            holder.binding.statusProgress.setPortionsCount(data.getNotViewedItemCount())
            holder.binding.userImage.loadImage(
                data.avatarImageUrl,
                R.color.grey_feed,
                R.color.grey_feed
            )
            holder.binding.tvUserName.text = data.title
            holder.itemView.setOnClickListener {
                holder.itemView.context.startActivity(
                    StoryDetailActivity.getStartIntent(
                        holder.itemView.context,
                        EventConstants.STATUS_SOURCE_HEADER,
                        items as ArrayList<StoryWidgetItem>,
                        position
                    )
                )
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemStoryBinding.bind(itemView)
        }
    }

    class WidgetHolder(binding: WidgetStoryBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetStoryBinding>(binding, widget)
}

class StoryWidgetModel : WidgetEntityModel<StoryWidgetData, WidgetAction>()

@Keep
data class StoryWidgetData(
    @SerializedName("items") val items: List<StoryWidgetItem>?,
) : WidgetData()

@Keep
@Parcelize
data class StoryWidgetItem(
    @SerializedName("avatar_url") val avatarImageUrl: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("stories") val storyList: List<Story>?,
    @SerializedName("cta_text") val ctaText: String?,
    @SerializedName("cta_action") val deeplink: String?,
    @SerializedName("cta_text_size") val ctaTextSize: Int?,
    @SerializedName("cta_background_color") val ctaBackground: String?,
    @SerializedName("cta_text_color") val ctaTextColor: String?,
) : Parcelable {

    fun getNotViewedItemCount(): Int {
        return getAttachmentCount() - getViewedItemCount()
    }

    private fun getAttachmentCount(): Int {
        if (storyList.isNullOrEmpty()) {
            return 0
        }
        return storyList.size
    }

    private fun getViewedItemCount(): Int {
        var viewedCount = 0
        if (!storyList.isNullOrEmpty()) {
            for (item in storyList) {
                if (item.isViewed == 1) {
                    viewedCount++
                }
            }
        }
        return viewedCount
    }
}

@Keep
@Parcelize
data class Story(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("is_viewed") val isViewed: Int?,
    @SerializedName("video_resource") val videoResource: VideoResource?,
) : Parcelable

@Keep
@Parcelize
data class VideoResource(
    @SerializedName("resource") val resource: String?,
    @SerializedName("question_id") val questionId: String?,
    @SerializedName("page") val page: String?,
    @SerializedName("drm_scheme") val drmScheme: String?,
    @SerializedName("drm_license_url") val drmLicenseUrl: String?,
    @SerializedName("media_type") val mediaType: String?,
) : Parcelable
