package com.doubtnutapp.newglobalsearch.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnutapp.databinding.FragmentNoDataFoundBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnut.core.utils.viewModelProvider

/**
 * A simple [Fragment] subclass.
 */
class NoDataFoundFragment : BaseBindingFragment<DummyViewModel, FragmentNoDataFoundBinding>() {

    companion object {
        const val TAG = "NoDataFoundFragment"
        fun newInstance() = NoDataFoundFragment()
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNoDataFoundBinding =
        FragmentNoDataFoundBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        binding.noDataView.setOnTouchListener { v, event ->
            (activity as InAppSearchActivity).closeKeyboard()
            return@setOnTouchListener false
        }
    }
}
