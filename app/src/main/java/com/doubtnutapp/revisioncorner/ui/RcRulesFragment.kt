package com.doubtnutapp.revisioncorner.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.revisioncorner.RulesInfo
import com.doubtnutapp.databinding.FragmentRulesDetailBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.revisioncorner.ui.adapter.RuleAdapter
import com.doubtnutapp.revisioncorner.viewmodel.RcRulesViewModel
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class RcRulesFragment : Fragment(R.layout.fragment_rules_detail) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val viewModel by viewModels<RcRulesViewModel> { viewModelFactory }
    private val binding by viewBinding(FragmentRulesDetailBinding::bind)
    private val navController by findNavControllerLazy()
    private val args by navArgs<RcRulesFragmentArgs>()

    private val ruleAdapter by lazy { RuleAdapter() }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupObservers()
        viewModel.getRulesData(args.widgetId, args.chapterAlias, args.subject)
    }

    private fun setupObservers() {
        viewModel.ruleLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = it.loading
                }
                is Outcome.Success -> {
                    setupUi(it.data)
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
            }
        }
    }

    private fun setupUi(data: RulesInfo) {

        viewModel.sendEvent(
            EventConstants.RC_TEST_INSTRUCTION_PAGE_SHOWN,
            hashMapOf(
                EventConstants.TYPE to data.title
            ), ignoreSnowplow = true
        )
        with(binding) {

            tvTitle.text = data.title
            tvRulesTitle.text = data.ruleTitle
            rvRules.adapter = ruleAdapter
            ruleAdapter.updateList(data.rules.orEmpty())

            btStartTest.apply {
                text = data.ctaText
                setOnClickListener {
                    viewModel.sendEvent(
                        EventConstants.RC_START_TEST_CLICK, hashMapOf(
                            EventConstants.TYPE to data.title
                        )
                    )
                    // Do not keep rules screen in back stack
                    navController.popBackStack()
                    deeplinkAction.performAction(it.context, data.ctaDeeplink)
                }
            }

            ivBack.setOnClickListener {
                navController.navigateUp()
            }
        }
    }

}