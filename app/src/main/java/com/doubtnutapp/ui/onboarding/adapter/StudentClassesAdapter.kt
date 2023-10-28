package com.doubtnutapp.ui.onboarding.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.R
import com.doubtnutapp.addEventNames
import com.doubtnutapp.data.remote.models.StudentClass
import com.doubtnutapp.databinding.ItemClassBinding
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId

class StudentClassesAdapter(val activity: FragmentActivity, val eventTracker: Tracker) :
    RecyclerView.Adapter<StudentClassesAdapter.ViewHolder>() {

    var classes: ArrayList<StudentClass>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            DataBindingUtil.inflate<ItemClassBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_class, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(classes!![position])
        sendEventItem(classes!![position].classDisplay ?: "")

    }

    override fun getItemCount(): Int {
        return classes?.size ?: 0
    }

    class ViewHolder constructor(var binding: ItemClassBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clazz: StudentClass) {
            binding.studentclass = clazz
            binding.executePendingBindings()

        }
    }

    fun updateData(classes: ArrayList<StudentClass>) {
        this.classes = classes
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


    private fun sendEvent(eventName: String) {
        activity?.apply {
            eventTracker.addEventNames(eventName)
                .addScreenState(
                    EventConstants.EVENT_PRAMA_SCREEN_STATE,
                    EventConstants.PAGE_CLASS_ADAPTER
                )
                .addNetworkState(NetworkUtils.isConnected(activity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_CLASS)
                .track()
        }
    }

    private fun sendEventItem(eventName: String) {
        activity?.apply {
            eventTracker.addEventNames(EventConstants.EVENT_NAME_ITEM_ENTITY)
                .addScreenState(EventConstants.EVENT_PRAMA_ADAPTER_SCREEN_ITEM, eventName)
                .addNetworkState(NetworkUtils.isConnected(activity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_CLASS)
                .track()
        }
    }


}