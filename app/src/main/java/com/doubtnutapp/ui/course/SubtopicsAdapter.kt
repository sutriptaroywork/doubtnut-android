package com.doubtnutapp.ui.course

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.*
import com.doubtnutapp.data.remote.models.MicroConcept
import com.doubtnutapp.data.remote.models.Subtopic
import com.doubtnutapp.databinding.ItemSubtopicBinding
import com.doubtnutapp.ui.course.microconcept.MicroconceptsActivity
import com.doubtnutapp.ui.course.microconcept.MicroconceptsAdapter
import com.doubtnutapp.utils.BranchIOUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import org.json.JSONObject

class SubtopicsAdapter(val activity: FragmentActivity, val eventTracker: Tracker) :
    RecyclerView.Adapter<SubtopicsAdapter.ViewHolder>() {

    var topics: ArrayList<Subtopic>? = null
    var colorIndex: Int = 0
    var chapter: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_subtopic, parent, false
            ), eventTracker
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(topics!![position], colorIndex, chapter)
    }

    override fun getItemCount(): Int {
        return topics?.size ?: 0
    }

    class ViewHolder(val binding: ItemSubtopicBinding, val eventTracker: Tracker) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(subtopic: Subtopic, colorIndex: Int, chapter: String) {
            binding.subtopic = subtopic
            binding.eventTracker = eventTracker
            binding.colorindex = colorIndex
            binding.txtIndex.text = (position + 1).toString()
            binding.txtIndex.setTextColor(
                binding.root.context.resources.getColor(
                    Utils.getColorPair(
                        colorIndex
                    )[1]
                )
            )
            binding.executePendingBindings()
            binding.tvSubtopic.setTextColor(
                binding.root.context.resources.getColor(
                    Utils.getColorPair(
                        colorIndex
                    )[1]
                )
            )
            binding.btnViewSubtopic.setTextColor(
                binding.root.context.resources.getColor(
                    Utils.getColorPair(
                        colorIndex
                    )[1]
                )
            )

            binding.rlSubtopic.setOnClickListener {
                binding.root.context.startActivity(
                    Intent(binding.root.context, MicroconceptsActivity::class.java)
                        .putExtra("concepts", subtopic.microconcepts)
                        .putExtra(MicroconceptsActivity.INTENT_EXTRA_SUBTOPIC, subtopic.subtopic)
                        .putExtra(MicroconceptsActivity.INTENT_EXTRA_CHAPTER, chapter)
                )

            }

            sendEventItem(subtopic.subtopic, binding.root.context)
        }

        private fun sendEventItem(eventName: String, context: Context) {

            eventTracker.addEventNames(EventConstants.EVENT_NAME_ITEM_ENTITY)
                .addScreenState(EventConstants.EVENT_PRAMA_ADAPTER_SCREEN_ITEM, eventName)
                .addNetworkState(NetworkUtils.isConnected(context).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_CHAPTER_DETAILS_FRAGMENT)
                .track()
        }
    }

    fun updateData(topics: ArrayList<Subtopic>) {
        this.topics = topics
        notifyDataSetChanged()
    }

    fun updateChapter(chapter: String) {
        this.chapter = chapter
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
                .addNetworkState(NetworkUtils.isConnected(activity).toString())
                .addScreenState(
                    EventConstants.EVENT_PRAMA_SCREEN_STATE,
                    EventConstants.PAGE_SUBTOPIC_ADAPTER
                )
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_CHAPTER_DETAILS_FRAGMENT)
                .track()
        }
    }
}

@BindingAdapter("microConcepts", "colorindex", "eventTracker")
fun addMicroConcepts(
    rvMicroConcepts: RecyclerView,
    concepts: ArrayList<MicroConcept>,
    colorIndex: Int,
    tracker: Tracker
) {
    val context = rvMicroConcepts.context

    val adapter = MicroconceptsAdapter(colorIndex, tracker)
    rvMicroConcepts.layoutManager =
        LinearLayoutManager(rvMicroConcepts.context, LinearLayoutManager.HORIZONTAL, false)
    rvMicroConcepts.adapter = adapter
    rvMicroConcepts.clearAllItemClickListener()




    rvMicroConcepts.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
        override fun onItemClick(position: Int, view: View) {

            adapter.topics?.get(position)?.let {
                val intent = VideoPageActivity.startActivity(
                    context,
                    adapter.topics?.get(position)?.mc_id!!,
                    "",
                    "",
                    Constants.PAGE_SC,
                    adapter.topics!![position].clazz.toString(),
                    true,
                    "",
                    "",
                    false
                )
                context.startActivity(intent)
            }
            sendEventByRv(
                view.context,
                EventConstants.EVENT_NAME_MICRO_CONCEPT_ITEM_CLICK,
                adapter.topics!![position].mc_id
            )
            sendCleverTapEventByRv(
                view.context,
                EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
                adapter.topics!![position].mc_id,
                Constants.PAGE_SC
            )

            // Send this event to Branch
//            BranchIOUtils.userCompletedAction(
//                EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
//                JSONObject().apply {
//                    put(
//                        EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
//                        adapter.topics!![position].mc_id
//                    )
//                })
        }

        fun sendEventByRv(context: Context, eventName: String, microConcept: String) {
            context?.apply {
                tracker.addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(context!!).toString())
                    .addScreenState(
                        EventConstants.EVENT_PRAMA_SCREEN_STATE,
                        EventConstants.PAGE_MICRO_CONCEPTS_ADAPTER
                    )
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_CHAPTER_DETAILS_FRAGMENT)
                    .addEventParameter(Constants.MICROCONCEPT, microConcept)
                    .track()

            }
        }

        fun sendCleverTapEventByRv(
            context: Context,
            eventName: String,
            microConcept: String,
            pagename: String
        ) {
            context?.apply {
                tracker.addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(context!!).toString())
                    .addScreenState(
                        EventConstants.EVENT_PRAMA_SCREEN_STATE,
                        EventConstants.PAGE_MICRO_CONCEPTS_ADAPTER
                    )
                    .addStudentId(getStudentId())
                    .addScreenName(EventConstants.PAGE_CHAPTER_DETAILS_FRAGMENT)
                    .addEventParameter(Constants.MICROCONCEPT, microConcept)
                    .addEventParameter(
                        Constants.STUDENT_CLASS,
                        getStudentClass()
                    )
                    .addEventParameter(Constants.PAGE, pagename)
                    .cleverTapTrack()

            }
        }

    })

    val items: ArrayList<MicroConcept> = arrayListOf()
    if (concepts.size > 3) {
        for (i in 0..2) {
            items.add(concepts[i])
            adapter.updateData(items)
        }
    } else adapter.updateData(concepts)

    adapter.updateTopicsToViewAll(concepts)

}

