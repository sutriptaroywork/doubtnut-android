package com.doubtnutapp.ui.mockTest

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityMockTestBinding
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.RecyclerItemClickListener
import dagger.android.HasAndroidInjector

class MockTestActivity : BaseBindingActivity<MockTestListViewModel, ActivityMockTestBinding>(),
    HasAndroidInjector {

    private lateinit var adapter: MockTestAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    companion object {
        const val TAG = "MockTestActivity"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onStart() {
        super.onStart()
        setObservers()
    }

    private fun init() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[MockTestListViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = MockTestAdapter(this)
        layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMockTest.layoutManager = layoutManager
        binding.recyclerViewMockTest.adapter = adapter
    }

    private fun setUpListener() {
        binding.recyclerViewMockTest.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                val intent = Intent(this@MockTestActivity!!, MockTestListActivity::class.java)
                intent.putParcelableArrayListExtra(Constants.MOCK_TEST_LIST, adapter.items[position].mockTestList)
                intent.putExtra(Constants.MOCK_TEST_TOOLBAR_TEXT, adapter.items[position].course)
                startActivity(intent)
            }
        })

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setObservers() {
        viewModel.getMockTestDetails().observe(this, Observer { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBarMockTest.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressBarMockTest.visibility = View.GONE
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                }
                is Outcome.ApiError -> {
                    binding.progressBarMockTest.visibility = View.GONE
                    toast(getString(R.string.api_error))
                }
                is Outcome.BadRequest -> {
                    binding.progressBarMockTest.visibility = View.GONE
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(supportFragmentManager, "BadRequestDialog")
                }
                is Outcome.Success -> {
                    binding.progressBarMockTest.visibility = View.GONE
                    if (response.data.data.isNotEmpty()) {
                        adapter.updateData(response.data.data)
                    } else {
                        binding.recyclerViewMockTest.visibility = View.GONE
                        binding.tvNoTest.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    override fun provideViewBinding(): ActivityMockTestBinding {
        return ActivityMockTestBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): MockTestListViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        init()
        setupRecyclerView()
        setUpListener()
    }
}