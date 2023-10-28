package com.doubtnut.referral.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.utils.applyBackgroundColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.utils.loadImage2
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.referral.databinding.ItemReferredFriendBinding
import com.doubtnut.referral.databinding.WidgetReferredFriendsBinding
import com.google.gson.annotations.SerializedName

class ReferredFriendsWidget(context: Context) :
    CoreBindingWidget<ReferredFriendsWidget.WidgetHolder, ReferredFriendsWidget.WidgetModel,
            WidgetReferredFriendsBinding>(context) {

    override fun getViewBinding(): WidgetReferredFriendsBinding {
        return WidgetReferredFriendsBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
        CoreApplication.INSTANCE.androidInjector().inject(this)
    }

    override fun bindWidget(holder: WidgetHolder, model: WidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data = model.data
        binding.rvReferredFriends.layoutManager = LinearLayoutManager(context)
        binding.tvLabelFriends.text = data.title.orEmpty()
        binding.rootContainer.applyBackgroundColor(data.bgColor)
        if (data.items != null) {
            val adapter = Adapter(data.items)
            binding.rvReferredFriends.adapter = adapter
        }
        binding.textViewEmptyState.text = data.textEmptyState.orEmpty()
        binding.imageViewEmptyState.loadImage2(data.emptyStateImageUrl)

        return holder
    }

    class Adapter(private val listFriends: List<ReferredFriendsData.ReferredFriendItemData>) :
        RecyclerView.Adapter<Adapter.ReferredFriendViewholder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ReferredFriendViewholder {
            val binding =
                ItemReferredFriendBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            return ReferredFriendViewholder(binding)
        }

        override fun onBindViewHolder(holder: ReferredFriendViewholder, position: Int) {
            holder.bind(listFriends[position])
        }

        override fun getItemCount(): Int {
            return listFriends.size
        }

        class ReferredFriendViewholder(val binding: ItemReferredFriendBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(referredFriendData: ReferredFriendsData.ReferredFriendItemData) {
                binding.apply {
                    tvName.text = referredFriendData.title.orEmpty()
                    tvSubHeading.text = referredFriendData.subTitle.orEmpty()
                    tvName.applyTextSize(referredFriendData.titleTextSize)
                    tvSubHeading.applyTextSize(referredFriendData.subTitleTextSize)
                    tvPointsEarned.text = referredFriendData.pointsEarned.orEmpty()
                    binding.imageViewFriend.loadImage2(referredFriendData.imageUrl)
                }
            }
        }
    }

    class WidgetHolder(binding: WidgetReferredFriendsBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetReferredFriendsBinding>(binding, widget)

    @Keep
    class WidgetModel : WidgetEntityModel<ReferredFriendsData, WidgetAction>()

    @Keep
    data class ReferredFriendsData(
        @SerializedName("title") val title: String?,
        @SerializedName("bg_color") val bgColor: String?,
        @SerializedName("items") val items: List<ReferredFriendItemData>?,
        @SerializedName("empty_state_text") val textEmptyState: String?,
        @SerializedName("empty_state_img_url") val emptyStateImageUrl: String?
    ) : WidgetData() {

        @Keep
        data class ReferredFriendItemData(
            @SerializedName("title") val title: String?,
            @SerializedName("title_text_size") val titleTextSize: String?,
            @SerializedName("subtitle") val subTitle: String?,
            @SerializedName("subtitle_text_size") val subTitleTextSize: String?,
            @SerializedName("image_url") val imageUrl: String?,
            @SerializedName("text_points_earned") val pointsEarned: String?

        )
    }


}