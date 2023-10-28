package com.doubtnutapp.revisioncorner.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.revisioncorner.RevisionCornerHomeData
import com.doubtnutapp.databinding.FragmentRevisionCornerHomeBinding
import com.doubtnutapp.revisioncorner.viewmodel.RcHomeViewModel
import com.doubtnutapp.ui.FragmentHolderActivity
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import javax.inject.Inject

/**
 * Created by devansh on 07/08/21.
 */

class RcHomeFragment : Fragment(R.layout.fragment_revision_corner_home) {

    companion object{
        const val TAG = "rc_home_fragment"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val binding by viewBinding(FragmentRevisionCornerHomeBinding::bind)
    private val viewModel by viewModels<RcHomeViewModel> { viewModelFactory }
    private val navController by findNavControllerLazy()

    private val widgetAdapter by lazy { WidgetLayoutAdapter(requireContext(), source = TAG) }

    private val selectClassLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                updateClassAtTop()
                viewModel.getRevisionCornerHomeData()
            }
        }

    private var rxBusDisposable: Disposable? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUi()
        setupObservers()
        viewModel.getRevisionCornerHomeData()
        viewModel.sendEvent(EventConstants.RC_PAGE_VISIT, ignoreSnowplow = true)
    }

    private fun setupUi() {
        with(binding) {
            ivBack.setOnClickListener {
                navController.navigateUpOrFinish(activity)
            }

            updateClassAtTop()

            tvClassChange.setOnClickListener {
                val intent = Intent(context, FragmentHolderActivity::class.java).apply {
                    action = Constants.NAVIGATE_CLASS_FRAGMENT_FROM_NAV
                }
                selectClassLauncher.launch(intent)
            }
        }
    }

    private fun updateClassAtTop() {
        binding.tvClassChange.text = defaultPrefs().getString(Constants.STUDENT_CLASS_DISPLAY, "")
    }

    private fun setupObservers() {
        viewModel.homeLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = it.loading
                }
                is Outcome.Success -> {
                    updateUi(it.data)
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
            }
        }

        viewModel.rulesLiveData.observeEvent(viewLifecycleOwner) {
            if (it != null) {
                mayNavigate {
                    navigate(RcHomeFragmentDirections.actionShowRulesDialog(it))
                }
            }
        }
    }

    private fun updateUi(data: RevisionCornerHomeData) {
        with(binding) {
            ivProgressReport.loadImage(data.progressReportIcon)
            ivProgressReport.setOnClickListener {
                navController.navigate(R.id.actionOpenStatsScreen)
                viewModel.sendEvent(
                    EventConstants.RC_PERFORMANCE_REPORT_ICON_CLICK,
                    hashMapOf(
                        Constants.SOURCE to "home",
                    )
                )
            }

            rvWidgets.adapter = widgetAdapter
            widgetAdapter.setWidgets(data.carousels.orEmpty())
        }
    }

    override fun onStart() {
        super.onStart()
        rxBusDisposable = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            viewModel.handleRxBusEvent(it)
        }
    }

    override fun onStop() {
        super.onStop()
        rxBusDisposable?.dispose()
    }
}