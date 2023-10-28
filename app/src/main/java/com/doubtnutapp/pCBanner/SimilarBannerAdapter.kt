package com.doubtnutapp.pCBanner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.*

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.data.remote.models.BannerActionData
import com.doubtnutapp.eventmanager.CommonEventManager
import com.doubtnutapp.utils.BannerActionUtils
import com.doubtnutapp.utils.UserUtil.getStudentId

/**
 * Created by pradip on
 * 28, May, 2019
 **/
class SimilarBannerAdapter(private val actionsPerformer: ActionPerformer?, private val commonEventManager: CommonEventManager, private val sourceTag: String) : RecyclerView.Adapter<SimilarBannerAdapter.ViewHolder>() {

    var pcBannerDataList: MutableList<SimilarPCBannerVideoItem.PCListViewItem> = mutableListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate<com.doubtnutapp.databinding.ItemBannerSimilarVideoBinding>(LayoutInflater.from(parent.context),
                R.layout.item_banner_similar_video, parent, false), commonEventManager, sourceTag).apply {
            actionPerformer = this@SimilarBannerAdapter.actionsPerformer
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pcBannerDataList[position])

    }


    override fun getItemCount(): Int {
        return pcBannerDataList.size
    }

    class ViewHolder(var binding: com.doubtnutapp.databinding.ItemBannerSimilarVideoBinding, private val commonEventManager: CommonEventManager, private val sourceTag: String) : BaseViewHolder<SimilarPCBannerVideoItem.PCListViewItem>(binding.root) {


        override fun bind(data: SimilarPCBannerVideoItem.PCListViewItem) {
            binding.pcListViewItem = data
            binding.executePendingBindings()
            binding.cardContainer.setOnClickListener {
                checkInternetConnection(binding.root.context) {
                    (binding.root.context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(EventConstants.PC_BANNER_CLICK)
                            .addStudentId(getStudentId())
                            .addScreenName(sourceTag)
                            .track()
                    commonEventManager.onPcBannerClick(sourceTag, data.actionData.eventKey)
                    val context = binding.root.context
                    BannerActionUtils.navigateToPage(context, data.actionActivity, mapToBannerActionData(data.actionData), eventTracker = (context.applicationContext as DoubtnutApp).getEventTracker())


                }
            }

        }

        private fun mapToBannerActionData(actionData: SimilarPCBannerVideoItem.PCListViewItem.BannerPCActionDataViewItem): BannerActionData = actionData.run {
            BannerActionData("", "", actionData.playlistId, actionData.playlistTitle, actionData.isLast, "", "", "", "", "", "", "", "", "", "", "", "", "", "", facultyId, ecmId, subject)
        }


    }

    fun updateDataList(items: List<RecyclerViewItem>) {
        pcBannerDataList.addAll(items as List<SimilarPCBannerVideoItem.PCListViewItem>)
        notifyDataSetChanged()
    }
}