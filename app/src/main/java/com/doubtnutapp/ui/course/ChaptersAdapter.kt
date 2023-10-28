package com.doubtnutapp.ui.course

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.R
import com.doubtnutapp.addEventNames
import com.doubtnutapp.data.remote.models.Chapter
import com.doubtnutapp.databinding.ItemChapterBinding
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils

class ChaptersAdapter(val activity: FragmentActivity, val eventTracker: Tracker) :
    RecyclerView.Adapter<ChaptersAdapter.ViewHolder>() {

    var cours: ArrayList<Chapter>? = null
    var isCourseDetail: Boolean = false
    var detailIndex: Int = 0
    var cdnPath: String = ""
    var fromNotification: Boolean = false
    var notificationWidth: Int = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_chapter, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cours!![position], cdnPath)
        if (isCourseDetail) {
            holder.width(WIDTH, fromNotification, notificationWidth)
            holder.height(HEIGHT, fromNotification)
            holder.color()

            if (position == detailIndex) {
                holder.width(WIDTH, fromNotification, notificationWidth)
                holder.height(HEIGHT, fromNotification)
                holder.color()
            }
            sendEventItem(cours!![position].chapter)
        } else {
            holder.color()

            if (!DIMEN_SET) {
                holder.setDimen()
            }
        }

        if (position == detailIndex) {
            holder.applyTransition()
        }
    }

    override fun getItemCount(): Int {
        return cours?.size ?: 0
    }

    class ViewHolder constructor(var binding: ItemChapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chapter: Chapter, cdnPath: String) {
            binding.chapter = chapter
            binding.cdnPath = cdnPath
            binding.executePendingBindings()
        }

        fun color() {
            val colorPair = Utils.getColorPair(adapterPosition)
            binding.tvChapter.setBackgroundResource(colorPair[1])
            binding.ivChapter.setBackgroundResource(colorPair[0])
        }

        fun setDimen() {
            binding.root.post {
                WIDTH = Utils.convertPixelsToDp(binding.root.width.toFloat(), binding.root.context)
                    .toInt()
                HEIGHT = Utils.convertPixelsToDp(
                    binding.ivChapter.layoutParams.height.toFloat(),
                    binding.root.context
                ).toInt()
                DIMEN_SET = true
            }
        }

        fun width(width: Int, fromNotification: Boolean, notificationWidth: Int) {
            if (fromNotification) {
                binding.root.layoutParams.width = notificationWidth / 3
            } else {
                binding.root.layoutParams.width = Utils.convertDpToPixel(width.toFloat()).toInt()
            }
        }

        fun height(height: Int, fromNotification: Boolean) {
            if (fromNotification) {
                val heightnav = 100
                binding.ivChapter.layoutParams.height =
                    Utils.convertDpToPixel(heightnav.toFloat()).toInt()
            } else {
                binding.ivChapter.layoutParams.height =
                    Utils.convertDpToPixel(height.toFloat()).toInt()
            }
        }

        fun applyTransition() {
            if (Utils.is21()) {
                binding.root.transitionName = "rootTransition"
            }
        }
    }

    fun updateData(cours: ArrayList<Chapter>) {
        this.cours = cours
        notifyDataSetChanged()
    }

    fun cdnPath(path: String) {
        this.cdnPath = path
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
                    EventConstants.PAGE_CHAPTER_ADAPTER
                )
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_CHAPTER_FRAGMENT)
                .track()
        }
    }

    private fun sendEventItem(eventName: String) {
        activity?.apply {
            eventTracker.addEventNames(EventConstants.EVENT_NAME_ITEM_ENTITY)
                .addScreenState(EventConstants.EVENT_PRAMA_ADAPTER_SCREEN_ITEM, eventName)
                .addNetworkState(NetworkUtils.isConnected(activity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_CHAPTER_FRAGMENT)
                .track()
        }
    }

}

var DIMEN_SET: Boolean = false
var HEIGHT: Int = 0
var WIDTH: Int = 0