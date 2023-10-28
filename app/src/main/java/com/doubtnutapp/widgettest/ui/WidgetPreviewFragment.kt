package com.doubtnutapp.widgettest.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.databinding.FragmentWidgetPreviewBinding
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgettest.viewmodel.ApiTestViewModel

/**
 * Created by Mehul Bisht on 18/11/21
 */

class WidgetPreviewFragment :
    BaseBindingFragment<ApiTestViewModel, FragmentWidgetPreviewBinding>() {

    companion object {
        const val TAG = "WidgetPreviewFragment"
    }

    private lateinit var adapter: WidgetLayoutAdapter

    override fun providePageName(): String = TAG

    override fun provideViewModel(): ApiTestViewModel =
        activityViewModelProvider(viewModelFactory)

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWidgetPreviewBinding =
        FragmentWidgetPreviewBinding.inflate(layoutInflater)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        adapter = WidgetLayoutAdapter(requireContext())
        initRecyclerView()
        observe()
    }

    private fun initRecyclerView() {
        binding.apiTestWidgets.layoutManager = LinearLayoutManager(requireContext())
        binding.apiTestWidgets.adapter = adapter
    }

    private fun observe() {

        viewModel.widgetsLiveData.observe(this) { widgetStatus ->
            when (widgetStatus) {
                is ApiTestViewModel.Widgets.Initial -> Unit
                is ApiTestViewModel.Widgets.Success -> {
                    adapter.setWidgets(widgetStatus.data)
                }
                is ApiTestViewModel.Widgets.Error -> {
                    adapter.setWidgets(listOf())
                    ToastUtils.makeText(requireContext(), "Invalid Json", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}