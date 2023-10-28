package com.doubtnutapp.ui.games

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnut.core.view.GridSpaceItemDecorator
import com.doubtnutapp.R
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityDnGamesBinding
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.showApiErrorToast
import javax.inject.Inject

class DnGamesActivity : BaseBindingActivity<GamesViewModel, ActivityDnGamesBinding>() {

    private lateinit var gamesAdapter: GamesAdapter

    @Inject
    lateinit var gamesEventManager: GamesEventManager

    override fun getStatusBarColor(): Int {
        return R.color.grey_statusbar_color
    }

    override fun provideViewBinding(): ActivityDnGamesBinding {
        return ActivityDnGamesBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): GamesViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        init()
        setAppbar()
        setRecyclerView()
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.fetchGames().observe(this, {
            binding.progressView.isVisible = it is Outcome.Progress
            binding.mainView.isVisible = it is Outcome.Success
            when (it) {
                is Outcome.Failure -> {
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                }
                is Outcome.Success -> {
                    gamesAdapter.submitList(it.data.data.list)
                    binding.banner.loadImage(it.data.data.bannerUrl)
                }
                is Outcome.ApiError -> {
                    it.e.printStackTrace()
                    showApiErrorToast(this)
                    finish()
                }
                is Outcome.BadRequest -> {
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(supportFragmentManager, "BadRequestDialog")
                }
                else -> {
                }
            }
        })
    }

    private fun init() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(GamesViewModel::class.java)
        gamesAdapter = GamesAdapter(gamesEventManager)
    }

    private fun setAppbar() {
        setSupportActionBar(binding.toolbar)
        binding.textViewTitle.text = getString(R.string.dn_games)
        binding.buttonBack.setOnClickListener {
            finish()
        }
        binding.toolbar.setContentInsetsAbsolute(0, 0)
    }

    private fun setRecyclerView() {
        binding.recyclerView.apply {
            itemAnimator = DefaultItemAnimator()
            adapter = gamesAdapter
            addItemDecoration(GridSpaceItemDecorator(2, 32, true))
        }
    }

    companion object {
        const val TAG = "DnGamesActivity"
    }
}
