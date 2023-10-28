package com.doubtnutapp.ui.course.microconcept

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.addEventNames
import com.doubtnutapp.data.remote.models.MicroConcept
import com.doubtnutapp.databinding.ItemMicroconceptListBinding
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.videoPage.ui.VideoPageActivity

class MicroconceptListAdapter(
    val microconceptsActivity: MicroconceptsActivity,
    val eventTracker: Tracker
) : RecyclerView.Adapter<MicroconceptListAdapter.ViewHolder>() {

    var topics: List<MicroConcept>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_microconcept_list, parent, false
            ), eventTracker
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(topics!![position])
        holder.color(position)
        sendEventItem(topics!![position].mc_text)

    }

    override fun getItemCount(): Int {
        return topics?.size ?: 0
    }

    class ViewHolder(var binding: ItemMicroconceptListBinding, val eventTracker: Tracker) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(microConcept: MicroConcept) {
            binding.microconcept = microConcept
            binding.executePendingBindings()
            binding.mathView.setFontSize(17)
            binding.mathView.setTextColor("black")
            binding.mathView.text = microConcept.mc_text

            binding.chipMicroconcept.setOnClickListener {

                val intent = VideoPageActivity.startActivity(
                    binding.root.context,
                    microConcept.mc_id,
                    "",
                    "",
                    Constants.PAGE_SC,
                    microConcept.clazz.toString(),
                    true,
                    "",
                    "",
                    false
                )
                sendEventItem(EventConstants.EVENT_NAME_MICROCONCEPT_CHIP_CLICK, microConcept.mc_id)
                sendCleverTapEventItem(
                    EventConstants.EVENT_NAME_MICROCONCEPT_CHIP_CLICK,
                    microConcept.mc_id,
                    Constants.PAGE_SSC
                )
                binding.root.context.startActivity(intent)
            }
        }

        private fun sendEventItem(eventName: String, mc_id: String) {
            val context = binding.root.context
            context?.apply {
                eventTracker.addEventNames(eventName)
                    .addScreenState(EventConstants.EVENT_PRAMA_ADAPTER_SCREEN_ITEM, mc_id)
                    .addNetworkState(NetworkUtils.isConnected(context).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_MICRO_CONCEPTS_ACTIVITY)
                    .track()
            }
        }

        private fun sendCleverTapEventItem(eventName: String, mc_id: String, page: String) {
            val context = binding.root.context
            context?.apply {
                eventTracker.addEventNames(eventName)
                    .addScreenState(EventConstants.EVENT_PRAMA_ADAPTER_SCREEN_ITEM, mc_id)
                    .addNetworkState(NetworkUtils.isConnected(context).toString())
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_MICRO_CONCEPTS_ACTIVITY)
                    .addEventParameter(Constants.PAGE, page)
                    .addEventParameter(Constants.STUDENT_CLASS, getStudentClass())
                    .track()
            }
        }

        fun color(colorIndex: Int) {
            val colorPair = Utils.getColorPair(colorIndex)
        }
    }

    fun updateData(topics: List<MicroConcept>) {
        this.topics = topics
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
        microconceptsActivity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(microconceptsActivity).toString())
                .addScreenState(
                    EventConstants.EVENT_PRAMA_SCREEN_STATE,
                    EventConstants.MICRO_CONCEPTS_LIST_ADAPTER
                )
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_MICRO_CONCEPTS_ACTIVITY)
                .track()
        }
    }

    private fun sendEventItem(eventName: String) {
        microconceptsActivity?.apply {
            eventTracker.addEventNames(EventConstants.EVENT_NAME_ITEM_ENTITY)
                .addScreenState(EventConstants.EVENT_PRAMA_ADAPTER_SCREEN_ITEM, eventName)
                .addNetworkState(NetworkUtils.isConnected(microconceptsActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_MICRO_CONCEPTS_ACTIVITY)
                .track()
        }
    }

}