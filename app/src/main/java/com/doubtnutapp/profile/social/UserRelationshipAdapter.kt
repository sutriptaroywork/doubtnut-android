package com.doubtnutapp.profile.social

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.ProfileData
import com.doubtnutapp.databinding.ItemUserRelationshipBinding
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class UserRelationshipAdapter(
    private val type: String,
    private val isSelf: Boolean
) : RecyclerView.Adapter<UserRelationshipAdapter.ViewHolder>() {

    private val userList: ArrayList<ProfileData> = arrayListOf()

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_user_relationship,
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val userProfile = userList[position]
        holder.binding.tvUsername.text = userProfile.student_username
        holder.binding.ivProfileImage.loadImage(
            userProfile.img_url,
            R.color.grey_feed,
            R.color.grey_feed
        )

        if (userProfile.isVerified != null && userProfile.isVerified) {
            Utils.setVerifiedTickTextView(holder.binding.tvUsername)
        }

        holder.binding.tvUserClass.text = userProfile.student_class

        val isFollowing = userProfile.is_following?.toBoolean() ?: false

        val isSelfStudent =
            userProfile.student_id == defaultPrefs().getString(Constants.STUDENT_ID, "")
        if (isSelf) {
            if (type == UserRelationshipsActivity.TYPE_FOLLOWERS) {
                if (!isFollowing && !isSelfStudent) {
                    holder.binding.tvFollow.show()
                } else {
                    holder.binding.tvFollow.hide()
                }
                holder.binding.tvFollow.setOnClickListener {
                    DataHandler.INSTANCE.teslaRepository.followUser(userProfile.student_id)
                        .subscribeOn(Schedulers.io()).subscribe()
                    holder.binding.tvFollow.hide()
                    showToast(it.context, "Followed ${userProfile.student_username}")
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.EVENT_USER_FOLLOW,
                            EventConstants.SOURCE, "follow_label_follower_list",
                            ignoreSnowplow = true
                        )
                    )
                }

                holder.binding.tvUserRelationship.text = "Remove"
                holder.binding.tvUserRelationship.setOnClickListener {
                    DataHandler.INSTANCE.socialRepository.removeFollower(userProfile.student_id)
                        .subscribeOn(Schedulers.io()).subscribe()
                    userList.removeAt(position)
                    notifyDataSetChanged()
                    analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_REMOVE_FOLLOWER_CLICK, ignoreSnowplow = true))
                    showToast(it.context, "Removed your follower- ${userProfile.student_username}")
                }
            } else if (type == UserRelationshipsActivity.TYPE_FOLLOWING) {
                holder.binding.tvFollow.hide()
                holder.binding.tvUserRelationship.text = "Remove"
                holder.binding.tvUserRelationship.setOnClickListener {
                    DataHandler.INSTANCE.teslaRepository.unfollowUser(userProfile.student_id)
                        .subscribeOn(Schedulers.io()).subscribe()
                    userList.removeAt(position)
                    notifyDataSetChanged()
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.EVENT_USER_UNFOLLOW,
                            EventConstants.SOURCE, type,
                            ignoreSnowplow = true
                        )
                    )
                    showToast(it.context, "Unfollowed ${userProfile.student_username}")
                }
            }

        } else {
            holder.binding.tvFollow.hide()
            if (isFollowing) {
                holder.binding.tvUserRelationship.text = "Following"
                holder.binding.tvUserRelationship.setOnClickListener {
                    DataHandler.INSTANCE.teslaRepository.unfollowUser(userProfile.student_id)
                        .subscribeOn(Schedulers.io()).subscribe()
                    holder.binding.tvUserRelationship.text = "Follow"
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.EVENT_USER_UNFOLLOW,
                            EventConstants.SOURCE, type,
                            ignoreSnowplow = true
                        )
                    )
                }
            } else {
                holder.binding.tvUserRelationship.text = "Follow"
                holder.binding.tvUserRelationship.setOnClickListener {
                    DataHandler.INSTANCE.teslaRepository.followUser(userProfile.student_id)
                        .subscribeOn(Schedulers.io()).subscribe()
                    holder.binding.tvUserRelationship.text = "Following"
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.EVENT_USER_FOLLOW,
                            EventConstants.SOURCE, type,
                            ignoreSnowplow = true
                        )
                    )
                }
            }
        }

        if (isSelfStudent) {
            holder.binding.tvUserRelationship.hide()
        } else {
            holder.binding.tvUserRelationship.show()
        }

        holder.binding.root.setOnClickListener {
            FragmentWrapperActivity.userProfile(it.context, userProfile.student_id, source = type)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateData(users: List<ProfileData>) {
        val startPosition = this.userList.size
        this.userList.addAll(users)
        notifyItemRangeInserted(startPosition, users.size)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding = ItemUserRelationshipBinding.bind(itemView)
    }
}