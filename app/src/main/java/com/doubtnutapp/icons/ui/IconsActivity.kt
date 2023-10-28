package com.doubtnutapp.icons.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ActivityIconsBinding
import com.doubtnutapp.icons.ui.viewmodel.IconsActivityVM
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.extension.observeNonNull
import com.doubtnut.core.utils.viewModelProvider

class IconsActivity : BaseBindingActivity<IconsActivityVM, ActivityIconsBinding>() {

    private lateinit var navController: NavController

    override fun provideViewBinding(): ActivityIconsBinding {
        return ActivityIconsBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): IconsActivityVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.setGraph(R.navigation.nav_graph_icons, intent.extras)
        handleIntent(intent)

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.title.observeNonNull(this) {
            binding.tvTitle.text = it
        }
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.getParcelableExtra<Uri>(INTENT_EXTRA_URI)
        if (uri != null) {
            val deeplinkUri = Uri.parse(uri.toString())
            navController.navigate(
                deeplinkUri,
                navOptions {
                    popUpTo(R.id.iconsHomeFragment) {
                        inclusive = true
                    }
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent ?: return
        handleIntent(intent)
    }

    companion object {
        const val TAG = "IconsActivity"

        private const val INTENT_EXTRA_URI = "uri"

        fun getStartIntent(
            context: Context
        ): Intent =
            Intent(context, IconsActivity::class.java)

        fun getDeeplinkStartIntent(context: Context, uri: Uri): Intent =
            Intent(context, IconsActivity::class.java).apply {
                putExtra(INTENT_EXTRA_URI, uri)
            }
    }
}