package com.doubtnutapp.ui.onboarding.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R
import com.doubtnutapp.addEventNames

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.remote.models.Language
import com.doubtnutapp.databinding.ItemLanguageBinding
import com.doubtnutapp.login.ui.viewholder.LanguageViewHolder
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId

class LanguageAdapter(
    val activity: FragmentActivity?,
    val eventTracker: Tracker,
    private val fromOnboarding: Boolean,
    private val actionPerformer: ActionPerformer? = null
) : RecyclerView.Adapter<BaseViewHolder<Language>>() {

    var languages: ArrayList<Language>? = null

    override fun onBindViewHolder(holder: BaseViewHolder<Language>, position: Int) {
        holder.bind(languages!![position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Language> {
        return if (fromOnboarding)
            LanguageViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item__select_language, parent, false
                )
            ).apply {
                this.actionPerformer = this@LanguageAdapter.actionPerformer
            }
        else
            ViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_language, parent, false
                )
            )
    }

    override fun getItemCount(): Int {
        return languages?.size ?: 0
    }

    class ViewHolder constructor(var binding: ItemLanguageBinding) :
        BaseViewHolder<Language>(binding.root) {

        override fun bind(data: Language) {
            binding.language = data
            binding.executePendingBindings()
        }
    }

    fun updateData(languages: ArrayList<Language>) {
        this.languages = languages
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

        if (activity == null) return

        activity.apply {
            eventTracker.addEventNames(eventName)
                .addScreenState(
                    EventConstants.EVENT_PRAMA_SCREEN_STATE,
                    EventConstants.PAGE_LANGUAGE_ADAPTER
                )
                .addNetworkState(NetworkUtils.isConnected(activity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_LANGUAGE)
                .track()
        }
    }

}