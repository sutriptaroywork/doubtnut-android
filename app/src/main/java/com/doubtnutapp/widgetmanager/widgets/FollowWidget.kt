package com.doubtnutapp.widgetmanager.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.FollowWidgetItemsFetched
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.R
import com.doubtnutapp.base.GetFollowerWidgetItems
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.ItemWidgetFollowBinding
import com.doubtnutapp.databinding.WidgetFollowBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.feed.viewmodel.FeedViewModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.Utils
import com.google.android.material.button.MaterialButton
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 19/2/21.
 */

class FollowWidget(context: Context) :
    BaseBindingWidget<FollowWidget.WidgetHolder, FollowWidget.Model, WidgetFollowBinding>(context) {

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun getViewBinding(): WidgetFollowBinding {
        return WidgetFollowBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    @SuppressLint("CheckResult")
    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        val data = model.data
        val binding = holder.binding

        DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            if (it is FollowWidgetItemsFetched) {
                data.items = it.items
                binding.recyclerView.post {
                    updateUi(binding, model)
                }
            }
        }
        if (data.items == null) {
            performAction(GetFollowerWidgetItems)
        } else {
            binding.root.apply {
                updateUi(binding, model)
            }
        }

        return holder
    }

    private fun updateUi(binding: WidgetFollowBinding, model: Model) {
        val data = model.data
        if (data.items.isNullOrEmpty().not()) {
            binding.apply {
                tvtTitle.text = data.title
                recyclerView.adapter =
                    Adapter(data.items.orEmpty(), mAnalyticsPublisher, model.extraParams)
            }
        }
        trackingViewId = data.id
    }

    class Adapter(
        private var items: List<FollowerData>,
        private val analyticsPublisher: IAnalyticsPublisher,
        private val extraParams: HashMap<String, Any>?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemWidgetFollowBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            val context = holder.binding.root.context

            holder.binding.apply {
                root.updateLayoutParams {
                    width = Utils.getWidthFromScrollSize(context, "2.5")
                }
                ivProfileImage.loadImage(data.imageUrl)
                ivProfileImage.setOnClickListener {
                    DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(extraParams?.apply {
                        put(Constants.EVENT_NAME, EventConstants.FOLLOWER_WIDGET_PROFILE_CLICKED)
                    }))
                    FragmentWrapperActivity.userProfile(context, data.studentId, "follower_widget")
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.FOLLOWER_WIDGET_PROFILE_CLICKED,
                            hashMapOf(
                                Constants.SOURCE to (extraParams?.get(FeedViewModel.SOURCE) ?: ""),
                                Constants.CELEB to data.studentId
                            ), ignoreSnowplow = true
                        )
                    )
                }
                tvName.text = data.name
                tvFollowers.text = data.followerText

                buttonFollow.isFollowed = data.isFollowed
                buttonFollow.isClickable = true
                buttonFollow.setOnClickListener {
                    if (data.isFollowed.not()) {
                        buttonFollow.isFollowed = true
                        DataHandler.INSTANCE.teslaRepository.followUser(data.studentId)
                            .applyIoToMainSchedulerOnSingle()
                            .subscribeToSingle({
                                data.isFollowed = true
                                buttonFollow.isFollowed = true
                            }, {
                                data.isFollowed = false
                                buttonFollow.isFollowed = false
                            })
                    }
                    DoubtnutApp.INSTANCE.bus()?.send(WidgetClickedEvent(extraParams?.apply {
                        put(Constants.EVENT_NAME, EventConstants.FOLLOWER_WIDGET_FOLLOW_CLICKED)
                    }))
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.FOLLOWER_WIDGET_FOLLOW_CLICKED,
                            hashMapOf(
                                Constants.SOURCE to (extraParams?.get(FeedViewModel.SOURCE) ?: ""),
                                Constants.CELEB to data.studentId
                            ), ignoreSnowplow = true
                        )
                    )
                }
            }
        }

        private inline var MaterialButton.isFollowed: Boolean
            get() = isSelected
            set(value) {
                if (isFollowed == value) return
                isSelected = value
                isClickable = if (value) {
                    setTextColor(Color.BLACK)
                    setText(R.string.following)
                    setStrokeColorResource(R.color.black)
                    setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    false
                } else {
                    setTextColor(Color.WHITE)
                    setText(R.string.follow)
                    setStrokeColorResource(R.color.colorTransparent)
                    setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    true
                }

            }

        override fun getItemCount(): Int = items.size

        fun updateItems(items: List<FollowerData>) {
            this.items = items
            notifyDataSetChanged()
        }

        class ViewHolder(val binding: ItemWidgetFollowBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    interface RecyclerViewItem

    class WidgetHolder(binding: WidgetFollowBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetFollowBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String,
        @SerializedName("items") var items: List<FollowerData>?
    ) : WidgetData()

    data class FollowerData(
        @SerializedName("student_id") val studentId: String,
        @SerializedName("image_url") val imageUrl: String,
        @SerializedName("name") val name: String,
        @SerializedName("follower_text") val followerText: String,
        @SerializedName("is_followed") var isFollowed: Boolean = false
    )

}