package com.doubtnutapp.gamification.otheruserprofile.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.BindingAdapter
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.gamification.myachievment.ui.viewholder.DailyAttendanceViewHolder
import com.doubtnutapp.gamification.myachievment.ui.viewholder.DailyStreakViewHolder
import com.doubtnutapp.gamification.userProfileData.model.DailyAttendanceDataModel

class DailyStreakListAdapter(private val requiredWidth: Int)
    : RecyclerView.Adapter<BaseViewHolder<RecyclerViewItem>>(), BindingAdapter<List<DailyAttendanceDataModel>> {
    override fun onBindViewHolder(holder: BaseViewHolder<RecyclerViewItem>, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val dailyStreakProgressList: MutableList<DailyAttendanceDataModel> = mutableListOf<DailyAttendanceDataModel>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<RecyclerViewItem> {
        return when (viewType) {
            R.layout.item_profile_dailystreak_badge -> DailyStreakViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                    R.layout.item_profile_dailystreak_badge, parent, false), requiredWidth) as BaseViewHolder<RecyclerViewItem>

            R.layout.item_profile_dailystreak_badgenormal -> DailyAttendanceViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                    R.layout.item_profile_dailystreak_badgenormal, parent, false), requiredWidth) as BaseViewHolder<RecyclerViewItem>

            else -> throw IllegalArgumentException("Unsupported View type")
        }
    }

//    override fun getItemViewType(position: Int) = dailyStreakProgressList[position].viewType

    override fun getItemCount() = dailyStreakProgressList.size

//    override fun onBindViewHolder(holder: BaseViewHolder<RecyclerViewItem>, position: Int) {
//        holder.bind(dailyStreakProgressList[position])
//    }


    override fun setData(data: List<DailyAttendanceDataModel>) {
        dailyStreakProgressList.addAll(data)
        notifyDataSetChanged()
    }
}