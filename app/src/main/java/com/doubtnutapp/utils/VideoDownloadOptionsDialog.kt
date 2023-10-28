package com.doubtnutapp.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Toast
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.databinding.DialogVideoDownloadOptionsBinding
import com.doubtnutapp.downloadedVideos.ExoDownloadTracker
import com.doubtnutapp.downloadedVideos.OfflineMediaItem
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnut.core.utils.viewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.*

class VideoDownloadOptionsDialog : BaseBindingBottomSheetDialogFragment<DummyViewModel, DialogVideoDownloadOptionsBinding>(), ExoDownloadTracker.Listener {

    private var mBehavior: BottomSheetBehavior<*>? = null

    companion object {
        const val TAG = "VideoDownloadOptionsDialog"
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogVideoDownloadOptionsBinding =
        DialogVideoDownloadOptionsBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        binding.btnStartDownload.setOnClickListener {
            if (validate()) {
                startDownloading(binding.root)
            }
        }
        binding.btnClearDownloads.setOnClickListener {
            ExoDownloadTracker.getInstance(requireContext())
                .clearAllDownloads()
            ToastUtils.makeText(requireContext(), "Clearing All Downloads", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun startDownloading(view: View) {
        val videoUrl = binding.editVideoUrl.text.toString()
        val thumbUrl = binding.editThumbUrl.text.toString()
        val licenseUrl = binding.editLicenseUrl.text.toString()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, 7)
        val offlineMediaItem = OfflineMediaItem(0, videoUrl, 121, videoUrl, licenseUrl, thumbUrl, cal.timeInMillis, "dash", "16:9", cal.timeInMillis)
        ExoDownloadTracker.getInstance(requireContext()).apply {
            addListener(this@VideoDownloadOptionsDialog)
            downloadMedia(offlineMediaItem)
        }
        context?.let { ToastUtils.makeText(it, "Preparing video download", Toast.LENGTH_SHORT).show() }
        dismiss()
    }

    private fun validate(): Boolean {
        return when {
            binding.editVideoUrl.text.isNullOrEmpty() -> {
                binding.layoutVideoUrl.isErrorEnabled = true
                binding.layoutVideoUrl.error = "Required"
                false
            }
            !URLUtil.isValidUrl(binding.editVideoUrl.text.toString()) -> {
                binding.layoutVideoUrl.isErrorEnabled = true
                binding.layoutVideoUrl.error = "Invalid Url"
                false
            }
            binding.editLicenseUrl.text.isNullOrEmpty() -> {
                binding.layoutLicenseUrl.isErrorEnabled = true
                binding.layoutLicenseUrl.error = "Required"
                false
            }
            !URLUtil.isValidUrl(binding.editLicenseUrl.text.toString()) -> {
                binding.layoutLicenseUrl.isErrorEnabled = true
                binding.layoutLicenseUrl.error = "Invalid Url"
                false
            }
            else -> {
                binding.layoutVideoUrl.isErrorEnabled = false
                binding.layoutLicenseUrl.isErrorEnabled = false
                true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

}