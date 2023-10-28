package com.doubtnutapp.ui.dailyPrize

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.R
import com.doubtnutapp.addEventNames
import com.doubtnutapp.data.remote.models.LastDayUser
import com.doubtnutapp.databinding.ItemTodaysUsersBinding
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId

class LastDayUsersAdapter(private val context: Context, val eventTracker: Tracker) :
    RecyclerView.Adapter<LastDayUsersAdapter.ViewHolder>() {

    var items: List<LastDayUser> = ArrayList()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding = ItemTodaysUsersBinding.bind(itemView)

        fun bind(lastUsersList: LastDayUser, position: Int) {
            lastUsersList.studentFname.let { binding.tvFname.text = it }
            lastUsersList.studentUsername.let { binding.tvEmail.text = it }
            var watchedvideos = lastUsersList.videoCount
            watchedvideos.let { binding.tvWatchedVideos.text = it.toString() }
            val requestOptions = RequestOptions().placeholder(R.drawable.ic_person_black)
            Glide.with(binding.ivCertificate)
                .load(lastUsersList.profileImage)
                .apply(requestOptions)
                .into(binding.ivCertificate)
            binding.badgeRank.text = position.toString()
            sendEventItem(lastUsersList?.studentUsername)

        }
    }

    fun updateData(items: List<LastDayUser>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LastDayUsersAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_todays_users, parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: LastDayUsersAdapter.ViewHolder, position: Int) {
        holder.bind(items[position], position + 1)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        sendEvent(EventConstants.EVENT_NAME_SCREEN_ADAPTER_ATTACH)

    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        sendEvent(EventConstants.EVENT_NAME_SCREEN_ADAPTER_DETACH)
    }

    private fun sendEvent(eventName: String) {
        context?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(context).toString())
                .addScreenState(
                    EventConstants.EVENT_PRAMA_SCREEN_STATE,
                    EventConstants.PAGE_TODAY_WINNER_ADAPTER
                )
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_DAILY_PRIZE_ACTIVITY)
                .track()
        }
    }

    private fun sendEventItem(eventName: String?) {
        context?.apply {
            eventTracker.addEventNames(EventConstants.EVENT_NAME_ITEM_ENTITY)
                .addNetworkState(NetworkUtils.isConnected(context).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_DAILY_PRIZE_ACTIVITY)
                .track()
        }
    }

}