package com.doubtnutapp.ui.contest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.*
import com.doubtnutapp.data.remote.models.ContestList
import com.doubtnutapp.databinding.ItemContestBinding
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId

class ContestAdapter(val contestListActivity: ContestListActivity, val eventTracker: Tracker) : RecyclerView.Adapter<ContestAdapter.ViewHolder>() {

    var contests: ArrayList<ContestList>? = null
    var cdnPath: String = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate<ItemContestBinding>(LayoutInflater.from(parent.context),
                R.layout.item_contest, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(contests!![position])
        sendEventItem(contests!![position].toString())

    }

    override fun getItemCount(): Int {
        return contests?.size ?: 0
    }

    class ViewHolder constructor(var binding: ItemContestBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contestList: ContestList) {
            binding.contestListItem=contestList
            binding.executePendingBindings()
        }


    }

    fun updateData(contests: ArrayList<ContestList>) {
        this.contests = contests
        notifyDataSetChanged()
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        sendEvent(EventConstants.EVENT_NAME_SCREEN_ADAPTER_ATTACH)

    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        sendEvent(EventConstants.EVENT_NAME_SCREEN_ADAPTER_DETACH)
    }

    private fun sendEvent(eventName : String){
        contestListActivity?.apply {
            eventTracker.addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(contestListActivity).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_CONTEST_LIST)
                    .track()
        }
    }

    private fun sendEventItem(eventName : String){
        contestListActivity?.apply {
            eventTracker.addEventNames(EventConstants.EVENT_NAME_ITEM_ENTITY)
                    .addScreenState(EventConstants.EVENT_PRAMA_ADAPTER_SCREEN_ITEM, eventName)
                    .addNetworkState(NetworkUtils.isConnected(contestListActivity).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_CONTEST_LIST)
                    .track()
        }
    }


}
