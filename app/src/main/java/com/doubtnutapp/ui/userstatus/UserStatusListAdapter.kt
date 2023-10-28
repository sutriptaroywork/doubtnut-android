package com.doubtnutapp.ui.userstatus

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.remote.models.userstatus.UserStatus
import com.doubtnutapp.databinding.ItemNoticeBoardBinding
import com.doubtnutapp.databinding.ItemUserStatusBinding
import com.doubtnut.noticeboard.NoticeBoardConstants
import com.doubtnut.noticeboard.ui.NoticeBoardDetailActivity
import com.doubtnutapp.utils.UserUtil
import com.google.gson.Gson

class UserStatusListAdapter(
    val context: Context,
    var items: ArrayList<Pair<Int, UserStatus?>>,
    val actionPerformer: ActionPerformer? = null,
    var isUserBanned: Boolean = false,
    var showNoticeBoard: Boolean =
        FeaturesManager.isFeatureEnabled(context, Features.NOTICE_BOARD)
                || defaultPrefs(context).getBoolean(NoticeBoardConstants.NB_FEED_ENABLED, false),
    var statusIndex: Int = 0,
    val analyticsPublisher: AnalyticsPublisher
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var adStatus: ArrayList<UserStatus> = arrayListOf()

    init {
        initItemList()
        if (showNoticeBoard) {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = EventConstants.DN_NB_ICON_VISIBLE,
                    params = hashMapOf(
                        EventConstants.SOURCE to EventConstants.SOURCE_FEED,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.STUDENT_CLASS to UserUtil.getStudentClass(),
                        EventConstants.BOARD to UserUtil.getUserBoard(),
                    ),
                    ignoreFacebook = true
                )
            )
        }
    }

    fun initItemList() {
        if (!isUserBanned) {
            items.add(statusIndex, Pair(HEADER, null))
            statusIndex++
        }
        if (showNoticeBoard) {
            items.add(statusIndex, Pair(NOTICE_BOARD, null))
            statusIndex++
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == NOTICE_BOARD) {
            return NoticeBoardVH(
                ItemNoticeBoardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        if (viewType == HEADER) {
            return HeaderViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_user_status,
                    parent,
                    false
                )
            )
        }

        return StatusViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_user_status,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NoticeBoardVH -> holder.bind()
            is HeaderViewHolder -> holder.bind()
            is StatusViewHolder -> holder.bind()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].first
    }

    fun updateList(statusList: ArrayList<UserStatus>) {
        statusList.forEach { items.add(Pair(STATUS, it)) }
        notifyDataSetChanged()
    }

    fun updateAdStatusList(statusList: ArrayList<UserStatus>) {
        if (!statusList.isNullOrEmpty()) {
            adStatus.clear()
            statusList.forEach { adStatus.add(it) }
        }
    }

    fun refresh(isUserBanned: Boolean) {
        if (this.isUserBanned == isUserBanned) return
        statusIndex = 0
        items.removeAll { it.second == null }
        initItemList()
        notifyDataSetChanged()
    }

    fun resetDataset(userStatusList: java.util.ArrayList<UserStatus>) {
        items.clear()
        statusIndex = 0
        initItemList()
        userStatusList.forEach { items.add(Pair(STATUS, it)) }
        notifyDataSetChanged()
    }

    inner class StatusViewHolder(val binding: ItemUserStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val userStatus = items[bindingAdapterPosition].second ?: return
            binding.status = userStatus
            binding.statusProgress.apply {
                setAnimation("lottie_status_circle.zip")
                repeatCount = LottieDrawable.INFINITE
            }
            binding.statusProgress.playAnimation()

            binding.root.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                openStatus(it.context, bindingAdapterPosition - statusIndex)
            }
            binding.userImage.loadImage(
                userStatus.getPreviewImage(),
                R.color.grey_feed,
                R.color.grey_feed
            )

            if (userStatus.studentId == UserUtil.getStudentId()) {
                binding.tvUserName.text = context.getString(R.string.my_status)
            } else {
                binding.tvUserName.text = userStatus.userName
            }
            if (showNoticeBoard) {
                binding.tvUserName.maxLines = 2
                binding.tvUserName.minLines = 2
            }
        }

        private fun openStatus(context: Context, startPosition: Int) {
            context.startActivity(
                StatusDetailActivity.getStartIntent(
                    context,
                    EventConstants.STATUS_SOURCE_HEADER,
                    Gson().toJson(items.filter { it.second != null }.map { it.second ?: return }),
                    adStatus,
                    startPosition
                )
            )
        }
    }

    inner class HeaderViewHolder(val binding: ItemUserStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.userImage.loadImage(
                userProfileImage(binding.root.context),
                R.color.grey_feed,
                R.color.grey_feed
            )

            binding.statusProgress.hide()
            binding.userImageBg.show()
            binding.tvUserName.text = "Your Story"
            binding.root.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                createPost(it.context)
            }
        }

        private fun createPost(context: Context) {
            CreateStatusActivity.getStartIntent(context, "status").apply {
                context.startActivity(this)
            }
        }
    }

    inner class NoticeBoardVH(
        val binding: ItemNoticeBoardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        name = EventConstants.DN_NB_ICON_CLICKED,
                        params = hashMapOf(
                            EventConstants.SOURCE to EventConstants.SOURCE_FEED,
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.STUDENT_CLASS to UserUtil.getStudentClass(),
                            EventConstants.BOARD to UserUtil.getUserBoard(),
                        ),
                        ignoreFacebook = true
                    )
                )
                context.startActivity(NoticeBoardDetailActivity.getStartIntent(context))
            }
        }

        fun bind() {
        }
    }

    companion object {
        private const val HEADER = 0
        private const val NOTICE_BOARD = 1
        private const val STATUS = 2
    }
}