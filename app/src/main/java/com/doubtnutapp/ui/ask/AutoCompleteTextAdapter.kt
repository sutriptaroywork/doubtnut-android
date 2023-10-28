package com.doubtnutapp.ui.ask

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.addEventNames
import com.doubtnutapp.data.remote.models.HitsQuestionList
import com.doubtnutapp.databinding.ItemAutoCompleteBinding
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId

class AutoCompleteTextAdapter(val eventTracker: Tracker) :
    RecyclerView.Adapter<AutoCompleteTextAdapter.ViewHolder>() {

    var hitsQuestionList: List<HitsQuestionList>? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAutoCompleteBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), eventTracker
        )
    }

    override fun getItemCount(): Int {
        return hitsQuestionList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(hitsQuestionList!![position])
    }

    class ViewHolder constructor(var binding: ItemAutoCompleteBinding, val eventTracker: Tracker) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(hitsQuestionList: HitsQuestionList) {
            binding.mathViewAutocomplete.text = HtmlCompat.fromHtml(
                hitsQuestionList._source.ocrText,
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            if (hitsQuestionList.displayMultipleLine == true) {
                binding.mathViewAutocomplete.maxLines = 3
            } else {
                binding.mathViewAutocomplete.maxLines = 1
            }
        }
    }

    fun updateData(hitsQuestionList: List<HitsQuestionList>) {
        this.hitsQuestionList = hitsQuestionList
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

    private fun sendEvent(eventName: String, context: Context?) {
        context?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(context).toString())
                .addScreenState(
                    EventConstants.EVENT_PRAMA_SCREEN_STATE,
                    EventConstants.EVENT_NAME_TEXT_SEARCH_AUTOCOMPLETE_ADAPTER
                )
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_TEXT_SEARCH)
                .track()
        }
    }

}