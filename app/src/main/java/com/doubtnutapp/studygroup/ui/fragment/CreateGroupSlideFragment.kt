package com.doubtnutapp.studygroup.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnutapp.databinding.FragmentCreateGroupSlidePageBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnut.core.utils.viewModelProvider

class CreateGroupSlideFragment : BaseBindingFragment<DummyViewModel, FragmentCreateGroupSlidePageBinding>() {

    companion object {

        const val TAG = "CreateGroupSlideFragment"

        private const val ARG_DESCRIPTION = "description"
        private const val ARG_IMAGE_URL = "image_url"

        private var description: String? = null
        private var imageUrl: String? = null

        fun newInstance(title: String, imageUrl: String) = CreateGroupSlideFragment().apply {
            val bundle = Bundle()
            bundle.putString(ARG_DESCRIPTION, title)
            bundle.putString(ARG_IMAGE_URL, imageUrl)
            arguments = bundle
        }
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateGroupSlidePageBinding =
        FragmentCreateGroupSlidePageBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        description?.let { binding.tvSlide.text = it }
        imageUrl?.let { binding.ivSlide.loadImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        description = arguments?.getString(ARG_DESCRIPTION)
        imageUrl = arguments?.getString(ARG_IMAGE_URL)
    }
}