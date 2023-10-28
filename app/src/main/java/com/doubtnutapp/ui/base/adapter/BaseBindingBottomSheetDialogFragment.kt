package com.doubtnutapp.ui.base.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.doubtnutapp.base.BaseViewModel
import com.doubtnut.core.utils.toast
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

abstract class BaseBindingBottomSheetDialogFragment<VM : BaseViewModel, VB : ViewBinding> :
    BottomSheetDialogFragment() {

    lateinit var viewModel: VM

    private var _binding: VB? = null

    val mBinding get() = _binding

    val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = provideViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = provideViewBinding(inflater, container)
        return binding.root
    }

    protected open fun setupObservers() {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        provideActivityViewModel()
        setupObservers()
        setupView(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun showMessage(message: String) = toast(message)

    fun showMessage(@StringRes resId: Int) = showMessage(getString(resId))

    fun goBack() {
        if (activity is BaseBindingActivity<*, *>) (activity as BaseBindingActivity<*, *>).goBack()
    }

    fun openKeyboard() {
        if (activity is BaseBindingActivity<*, *>) (activity as BaseBindingActivity<*, *>).openKeyboard()
    }

    fun closeKeyboard() {
        if (activity is BaseBindingActivity<*, *>) (activity as BaseBindingActivity<*, *>).closeKeyboard()
    }

    protected abstract fun provideViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    protected abstract fun providePageName(): String

    protected abstract fun provideViewModel(): VM

    protected open fun provideActivityViewModel() {}

    protected abstract fun setupView(view: View, savedInstanceState: Bundle?)

    val isViewModelInitialized get() = ::viewModel.isInitialized
}
