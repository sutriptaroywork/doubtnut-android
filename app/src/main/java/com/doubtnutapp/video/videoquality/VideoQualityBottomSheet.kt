package com.doubtnutapp.video.videoquality

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.constant.CoreConstants
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.addOnItemClick
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.SheetVideoQualityBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.utils.RecyclerItemClickListener
import com.doubtnutapp.videoPage.model.VideoResource
import javax.inject.Inject

class VideoQualityBottomSheet :
    BaseBindingBottomSheetDialogFragment<DummyViewModel, SheetVideoQualityBinding>() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var onQualitySelectionListener: OnQualitySelectionListener? = null

    private val items: List<VideoResource.PlayBackData>? by lazy {
        arguments?.getSerializable(CoreConstants.VIDEO_QUALITY_DATA) as ArrayList<VideoResource.PlayBackData>
    }

    companion object {
        const val TAG = "VideoQualityBottomSheet"
        fun newInstance(
            items: ArrayList<VideoResource.PlayBackData>
        ): VideoQualityBottomSheet {
            val fragment = VideoQualityBottomSheet()
            val args = Bundle()
            args.putSerializable(
                CoreConstants.VIDEO_QUALITY_DATA,
                items
            )
            fragment.arguments = args
            return fragment
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): SheetVideoQualityBinding {
        return SheetVideoQualityBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpRecyclerView()
        @Suppress("UNCHECKED_CAST")
        binding.rvVideoQualityOptions.adapter = VideoQualityAdapter(items.orEmpty())
        dialog?.setCanceledOnTouchOutside(true)
    }

    private fun setUpRecyclerView() {
        binding.rvVideoQualityOptions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
                override fun onItemClick(position: Int, view: View) {
                    val allItems = items ?: return
                    val item = allItems[position]
                    onQualitySelectionListener?.onQualityChanged(item)
                }
            })
        }
    }

    fun setItemClickListener(onQualitySelectionListener: OnQualitySelectionListener) {
        this.onQualitySelectionListener = onQualitySelectionListener
    }
}

interface OnQualitySelectionListener {
    fun onQualityChanged(playbackData: VideoResource.PlayBackData)
}