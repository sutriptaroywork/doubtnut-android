package com.doubtnutapp.gamification.otheruserprofile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentOtherProfileActivitiesBinding
import com.doubtnutapp.gamification.otheruserprofile.model.OtherUserStatsDataModel
import com.doubtnutapp.gamification.otheruserprofile.ui.adapter.OtherProfileStatsAdapter
import com.doubtnutapp.gamification.otheruserprofile.viewmodel.OtherUserProfileViewModel
import com.doubtnutapp.statusbarColor
import com.doubtnutapp.ui.base.BaseBindingFragment

class OtherUserActivityFragment :
    BaseBindingFragment<OtherUserProfileViewModel, FragmentOtherProfileActivitiesBinding>() {

    companion object {

        const val TAG = "OtherUserActivityFragment"

        private const val OTHER_USER_STATS: String = "other_user_stats"
        fun newInstance(otherUserStatList: List<OtherUserStatsDataModel>): OtherUserActivityFragment {
            val fragment = OtherUserActivityFragment()
            val args = Bundle()
            args.putParcelableArrayList(
                OTHER_USER_STATS,
                otherUserStatList as ArrayList<OtherUserStatsDataModel>
            )
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var otherUserActivityAdapter: OtherProfileStatsAdapter

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOtherProfileActivitiesBinding {
        return FragmentOtherProfileActivitiesBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): OtherUserProfileViewModel {
        return activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        statusbarColor(activity, R.color.grey_statusbar_color)
        otherUserActivityAdapter = OtherProfileStatsAdapter()
        mBinding?.activityList?.adapter = otherUserActivityAdapter
        setIntentData()
    }

    private fun setIntentData() {
        val data = arguments?.getParcelableArrayList<OtherUserStatsDataModel>(OTHER_USER_STATS)
        data?.let {
            otherUserActivityAdapter.updateData(data)
            otherUserActivityAdapter.notifyDataSetChanged()
        }
        viewModel.sendEvent(EventConstants.EVENT_NAME_NOT_OTHERS_MY_ACTIVITY_CLICK)

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            viewModel.sendEvent(EventConstants.EVENT_NAME_NOT_OTHERS_MY_ACTIVITY_CLICK)
        }
    }
}