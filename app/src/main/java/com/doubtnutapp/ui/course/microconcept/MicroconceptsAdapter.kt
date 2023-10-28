package com.doubtnutapp.ui.course.microconcept

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.R
import com.doubtnutapp.addEventNames
import com.doubtnutapp.data.remote.models.MicroConcept
import com.doubtnutapp.databinding.ItemSubtopicMicroconceptBinding
import com.doubtnutapp.databinding.LayoutFooterMicroconceptBinding
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils

class MicroconceptsAdapter(colorIndex: Int, val tracker: Tracker) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var topics: ArrayList<MicroConcept>? = null
    var topicsViewAll: ArrayList<MicroConcept>? = null
    var colorIndex: Int = 0
    private val TYPE_FOOTER = 1
    private val TYPE_ITEM = 2

    init {
        this.colorIndex = colorIndex
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_FOOTER) {
            FooterHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.layout_footer_microconcept, parent, false
                )
            )
        } else {
            ViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_subtopic_microconcept, parent, false
                ), tracker
            )
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FooterHolder) {
            getItemViewType(position)
            val footerHolder = holder
            if (topics!!.size >= 3) footerHolder.binding.root11.show() else footerHolder.binding.root11.hide()

            footerHolder.color(colorIndex)
            footerHolder.binding.textViewMore.setOnClickListener {
                footerHolder.binding.root.context.startActivity(
                    Intent(footerHolder.binding.root11.context, MicroconceptsActivity::class.java)
                        .putExtra("concepts", topicsViewAll)
                        .putExtra("title", topics!!.get(0).subtopic)
                        .putExtra("chapter", topics!!.get(0).chapter)
                )
                sendEvent(
                    EventConstants.EVENT_NAME_ITEM_VIEW_ALL_CLICK,
                    footerHolder.binding.root11.context
                )
            }

        } else if (holder is ViewHolder) {
            holder.bind(topics!![position])
            holder.color(colorIndex)
        }

    }

    override fun getItemCount() = topics?.size?.plus(1) ?: 0

    class ViewHolder(val binding: ItemSubtopicMicroconceptBinding, val tracker: Tracker) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(microConcept: MicroConcept) {
            binding.microconcept = microConcept
            binding.executePendingBindings()
            binding.mathView.setFontSize(12)
            binding.mathView.setTextColor("white")
            binding.mathView?.apply {
                binding.mathView.text = microConcept.mc_text
                binding.mathView.isClickable = false
            }
        }

        fun color(colorIndex: Int) {
            val colorPair = Utils.getColorPair(colorIndex)
            binding.tvMicroconcept.setBackgroundResource(colorPair[1])
            binding.root.setBackgroundResource(colorPair[0])

        }
    }

    fun updateData(topics: ArrayList<MicroConcept>) {
        this.topics = topics
        notifyDataSetChanged()
    }

    class FooterHolder constructor(var binding: LayoutFooterMicroconceptBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun color(colorIndex: Int) {
            val colorPair = Utils.getColorPair(colorIndex)
            binding.root11.setBackgroundResource(colorPair[0])
        }
    }

    override fun getItemViewType(position: Int) =
        if (position == topics!!.size) TYPE_FOOTER else TYPE_ITEM

    fun updateTopicsToViewAll(concepts: ArrayList<MicroConcept>) {
        this.topicsViewAll = concepts
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        sendEvent(EventConstants.EVENT_NAME_SCREEN_ADAPTER_ATTACH, recyclerView.context)

    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        sendEvent(EventConstants.EVENT_NAME_SCREEN_ADAPTER_DETACH, recyclerView.context)
    }

    private fun sendEvent(eventName: String, context: Context) {

        tracker.addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(context).toString())
            .addScreenState(
                EventConstants.EVENT_PRAMA_SCREEN_STATE,
                EventConstants.PAGE_MICRO_CONCEPTS_ADAPTER
            )
            .addStudentId(getStudentId())
            .addScreenName(EventConstants.PAGE_CHAPTER_DETAILS_FRAGMENT)
            .track()
    }
}
