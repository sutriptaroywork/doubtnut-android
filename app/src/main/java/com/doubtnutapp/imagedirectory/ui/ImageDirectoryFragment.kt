package com.doubtnutapp.imagedirectory.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.base.ImageDirectoryClicked
import com.doubtnutapp.databinding.FragmentImageDirectoryBinding
import com.doubtnutapp.imagedirectory.ui.adapter.ImageDirectoryAdapter
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.camera.viewmodel.CameraActivityViewModel

class ImageDirectoryFragment : BaseBindingFragment<CameraActivityViewModel, FragmentImageDirectoryBinding>(), ActionPerformer2 {

    private lateinit var mCameraActivityViewModel: CameraActivityViewModel

    private val mAdapter: ImageDirectoryAdapter by lazy { ImageDirectoryAdapter(this) }

    private var mFragmentInteractionListener: FragmentInteractionListener? = null

    companion object {
        const val TAG = "ImageDirectoryFragment"

        private const val DIRECTORY_PATH = "directory_path"
        private const val DIRECTORY_NAME = "directory_name"

        fun newInstance(path: String, name: String): Fragment =
                ImageDirectoryFragment().apply {
                    arguments = Bundle().apply {
                        putString(DIRECTORY_PATH, path)
                        putString(DIRECTORY_NAME, name)
                    }
                }
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentImageDirectoryBinding =
        FragmentImageDirectoryBinding.inflate(layoutInflater)

    override fun provideViewModel(): CameraActivityViewModel {
        val cameraActivityViewModel: CameraActivityViewModel by viewModels(
            ownerProducer = { requireActivity() }
        ) { viewModelFactory }
        return cameraActivityViewModel
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setupUi()
    }

    override fun performAction(action: Any) {
        when(action)  {
            is ImageDirectoryClicked -> {
                mFragmentInteractionListener?.onImageDirectoryClicked(action)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is FragmentInteractionListener) {
            mFragmentInteractionListener = parentFragment as? FragmentInteractionListener
        } else {
            throw IllegalArgumentException("$parentFragment must implement ImageDirectoryFragment.FragmentInteractionListener")
        }
        mCameraActivityViewModel = ViewModelProvider(requireActivity()).get(CameraActivityViewModel::class.java)
    }

    override fun onDetach() {
        super.onDetach()
        mFragmentInteractionListener = null
    }

    private fun setupUi() {
        mBinding?.recyclerView?.adapter = mAdapter
    }

    override fun setupObservers() {
        mCameraActivityViewModel.imageBucketListLiveData.observe(viewLifecycleOwner, Observer {
            mAdapter.updateList(it)
        })
    }

    interface FragmentInteractionListener {
        fun onImageDirectoryClicked(action: ImageDirectoryClicked)
    }
}