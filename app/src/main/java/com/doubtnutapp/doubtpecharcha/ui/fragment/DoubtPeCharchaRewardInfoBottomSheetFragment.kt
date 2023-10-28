package com.doubtnutapp.doubtpecharcha.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentDoubtPeCharchaRewardsInfoBottomSheetBinding
import com.doubtnutapp.doubtpecharcha.model.P2PDoubtTypes
import com.doubtnutapp.doubtpecharcha.ui.adapter.DoubtPeCharchaRewardInfoAdapter
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.widgetmanager.widgets.GradientBannerWithActionButtonWidget
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DoubtPeCharchaRewardInfoBottomSheetFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentDoubtPeCharchaRewardsInfoBottomSheetBinding

    companion object {
        const val TAG = "doubt_pe_charcha_bottom_sheet"
        const val REWARDS_DATA = "rewards_data"

        fun newInstance(data: GradientBannerWithActionButtonWidget.RewardsData) =
            DoubtPeCharchaRewardInfoBottomSheetFragment().apply {
                val bundle = Bundle()
                bundle.putParcelable(REWARDS_DATA, data)
                arguments = bundle
            }
    }

    val rewardsData: GradientBannerWithActionButtonWidget.RewardsData? by lazy {
        arguments?.getParcelable(REWARDS_DATA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDoubtPeCharchaRewardsInfoBottomSheetBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = ArrayList<String>()

        binding.rvInfoItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInfoItems.setHasFixedSize(true)
        binding.tvTitle.text = rewardsData?.title.orEmpty()
        binding.imageViewReward.loadImage(rewardsData?.imageUrl)
        val adapter =
            DoubtPeCharchaRewardInfoAdapter(rewardsData?.listData!!, rewardsData!!.bulletImgUrl!!)
        binding.rvInfoItems.adapter = adapter

        binding.buttonOK.setOnClickListener {
            dismiss()
        }

        Glide.with(this)
            .load("https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/61D890F0-34D4-60F0-A3A8-59FF9EED9C1E.webp")
            .into(binding.imageViewReward)
    }


}