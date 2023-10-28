package com.doubtnutapp.gallery.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.base.ImageDirectoryClicked
import com.doubtnutapp.camera.viewmodel.CameraActivityViewModel
import com.doubtnutapp.databinding.FragmentGalleryBinding
import com.doubtnutapp.gallery.adapter.GalleryImagesAdapter
import com.doubtnutapp.gallery.viewmodel.GalleryFragmentViewModel
import com.doubtnutapp.hide
import com.doubtnutapp.imagedirectory.ui.ImageDirectoryFragment
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.main.demoanimation.DemoAnimationActivity
import com.doubtnutapp.utils.Utils

class GalleryFragment : BaseBindingFragment<GalleryFragmentViewModel, FragmentGalleryBinding>(),
    ImageDirectoryFragment.FragmentInteractionListener {

    companion object {
        const val TAG = "GalleryFragment"

        fun newInstance(): Fragment = GalleryFragment()
    }

    private lateinit var mCameraActivityViewModel: CameraActivityViewModel

    private var mActionPerformer: ActionPerformer2? = null

    private val mGalleryImagesAdapter: GalleryImagesAdapter by lazy {
        GalleryImagesAdapter(mActionPerformer!!, R.layout.item_gallery_grid_image)
    }

    private var mCurrentBucketId: String? = null

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGalleryBinding =
        FragmentGalleryBinding.inflate(layoutInflater)

    override fun provideViewModel(): GalleryFragmentViewModel {
        val galleryViewModel: GalleryFragmentViewModel = viewModelProvider(viewModelFactory)
        val cameraActivityViewModel: CameraActivityViewModel by viewModels(
            ownerProducer = { requireActivity() }
        ) { viewModelFactory }
        mCameraActivityViewModel = cameraActivityViewModel
        return galleryViewModel
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setupUi()
        getImagesFromGallery()
        mCameraActivityViewModel.getAllDirectoriesWithImages()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ActionPerformer2) {
            mActionPerformer = context
        } else {
            // Implement ActionPerformer in this Fragment
            throw IllegalArgumentException("$context doesn't implement ActionPerformer")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mActionPerformer = null
    }

    private fun setupUi() {
        binding.recyclerViewImages.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = mGalleryImagesAdapter
        }

        binding.buttonHowTo.setOnClickListener {
            viewModel.sendEvent(EventConstants.GALLERY_HOW_TO_CLICKED, ignoreSnowplow = true)
            startActivity(DemoAnimationActivity.getStartIntent(requireContext(), 0, TAG))
        }

        binding.ivClose.setOnClickListener {
            val directoryFragment =
                childFragmentManager.findFragmentByTag(ImageDirectoryFragment.TAG)
            if (directoryFragment != null) {
                removeDirectoryFragment(directoryFragment)
            } else {
                requireActivity().onBackPressed()
            }
        }

        binding.tvTitle.setOnClickListener {
            addOrRemoveDirectoryFragment()
        }

        binding.ivDropdown.setOnClickListener {
            addOrRemoveDirectoryFragment()
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        mCameraActivityViewModel.storageReadPermissionReceivedLiveData.observe(viewLifecycleOwner) {
            if (it) {
                getImagesFromGallery()
                mCameraActivityViewModel.getAllDirectoriesWithImages()
            }
        }
        mCameraActivityViewModel.loadAllImagesInGalleryLiveData.observe(viewLifecycleOwner) {
            if (it) {
                getImagesFromGallery()
                binding.tvTitle.text = getString(R.string.all)
            }
        }
        mCameraActivityViewModel.refreshGalleryFragmentImagesLiveData.observe(viewLifecycleOwner) {
            if (it) {
                getImagesFromGallery(mCurrentBucketId, true)
            }
        }
    }

    private fun getImagesFromGallery(
        bucketId: String? = null,
        alwaysLoadFromBeginning: Boolean = false
    ) {
        // Update recyclerViewImages' height to take up full space even when there are a few images to show
        mBinding?.recyclerViewImages?.post {
            mBinding?.recyclerViewImages?.layoutParams?.height = Utils.screenHeight
        }
        viewModel.getGalleryImageItemsList(bucketId, alwaysLoadFromBeginning)

        viewModel.galleryImageListLiveData?.observe(viewLifecycleOwner) {
            mGalleryImagesAdapter.submitList(it)
        }
    }

    private fun addOrRemoveDirectoryFragment() {
        val directoryFragment = childFragmentManager.findFragmentByTag(ImageDirectoryFragment.TAG)
        if (directoryFragment == null) {
            binding.bottomHowToView.hide()
            binding.ivDropdown.animate().rotation(180f).start()

            childFragmentManager.commit {
                add(
                    R.id.directoryFragmentContainer,
                    ImageDirectoryFragment.newInstance("", "All"), ImageDirectoryFragment.TAG
                )
            }
        } else {
            removeDirectoryFragment(directoryFragment)
        }
    }

    private fun removeDirectoryFragment(directoryFragment: Fragment) {
        childFragmentManager.commit {
            remove(directoryFragment)
        }

        binding.ivDropdown.animate().rotation(0f).start()
        binding.bottomHowToView.show()
    }

    override fun onImageDirectoryClicked(action: ImageDirectoryClicked) {
        mCurrentBucketId = action.bucketId
        getImagesFromGallery(action.bucketId)
        childFragmentManager.findFragmentByTag(ImageDirectoryFragment.TAG)?.let {
            removeDirectoryFragment(it)
        }
        binding.tvTitle.text = action.directoryName
    }
}