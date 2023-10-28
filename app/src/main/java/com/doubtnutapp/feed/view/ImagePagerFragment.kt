package com.doubtnutapp.feed.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.databinding.FragmentImagePagerBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.BaseBindingFragment

class ImagePagerFragment : BaseBindingFragment<DummyViewModel, FragmentImagePagerBinding>() {

    companion object {
        const val TAG = "ImagePagerFragment"

        fun newInstance(image: String): ImagePagerFragment {
            return ImagePagerFragment().apply {
                arguments = Bundle().apply {
                    putString("image", image)
                }
            }
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentImagePagerBinding {
        return FragmentImagePagerBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBinding?.imageView?.loadImage(requireArguments().getString("image"), null)
    }
}