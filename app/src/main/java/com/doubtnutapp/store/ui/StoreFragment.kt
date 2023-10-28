package com.doubtnutapp.store.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*

import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.store.model.StoreResult
import com.doubtnutapp.store.ui.adapter.StoreResultListAdapter
import com.doubtnutapp.store.viewmodel.StoreResultViewModel
import com.doubtnutapp.store.viewmodel.StoreViewModel
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.showToast
import dagger.android.support.AndroidSupportInjection
import java.util.*
import javax.inject.Inject

class StoreFragment : Fragment(), ActionPerformer {

    override fun performAction(action: Any) {
        viewModel.handleStoreItemClick(action)
    }

    companion object {
        const val TAG = "StoreFragment"
        private const val PARAM_KEY_STORE_RESULT = "store_result_list"
        private const val AVAILABLE_DN_CASH = "available_dn_cash"

        fun newInstance(storeResultList: List<StoreResult>, availableDnCash: Int) = StoreFragment().also {
            it.arguments = bundleOf(
                    PARAM_KEY_STORE_RESULT to storeResultList,
                            AVAILABLE_DN_CASH to availableDnCash
            )
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var screenNavigator: Navigator

    private lateinit var viewModel: StoreResultViewModel

    private lateinit var storeViewModel: StoreViewModel

    private var storeResultList: RecyclerView? = null

    private lateinit var storeResultAdapter: StoreResultListAdapter

    private var storeResults: ArrayList<StoreResult>? = null
    private var availableDnCash: Int? = -999

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.store_result_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(StoreResultViewModel::class.java)
        storeViewModel = activityViewModelProvider(viewModelFactory)
        setUpRecyclerView()
        updateUi()
        setupObserver()
    }

    private fun setUpRecyclerView() {
        storeResultList = view?.findViewById(R.id.storeResultList)
        storeResultAdapter = StoreResultListAdapter(this)
        storeResultList?.adapter = storeResultAdapter
    }

    private fun updateUi() {
        storeResults = arguments?.getParcelableArrayList<StoreResult>(PARAM_KEY_STORE_RESULT)
        availableDnCash = arguments?.getInt(AVAILABLE_DN_CASH, -999)

        viewModel.setAvailableDnCash(availableDnCash)

        storeResults?.apply {
            if (size > 0) {
                storeResultList?.show()
                updateStoreResult(this)
            } else {
                storeResultList?.hide()
            }
        }

        if (storeResults == null) {
            storeResultList?.hide()
        }
    }

    private fun setupObserver() {
        viewModel.dialogScreenLiveData.observe(this, Observer {

            it.getContentIfNotHandled()?.let { navigation ->
                val args: Bundle? = navigation.hashMap?.toBundle()
                screenNavigator.openDialogFromFragment(activity!!, navigation.screen, args, activity!!.supportFragmentManager)
            }
        })

        viewModel.redeemStoreItemLiveData.observeK(this,
            ::onRedeemSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState)

        viewModel.myOrderCountLiveData.observe(this, Observer {
            storeViewModel.setMyOrderCount(it)
        })

        viewModel.navigateLiveData.observe(this, Observer {
            it.getContentIfNotHandled()?.let { navigationData ->
                navigate(navigationData)
            }
        })
    }

    private fun navigate(navigationData: NavigationModel) {
        val screen = navigationData.screen
        val arg = navigationData.hashMap?.toBundle()
        screenNavigator.startActivityFromActivity(activity!!, screen, arg)
    }


    private fun onRedeemSuccess(message: String) {
        activity?.let {
            if (message.isNotEmpty()) showToast(it, message)
        }

        storeViewModel.getStoreResults()
    }

    private fun updateStoreResult(storeResults: ArrayList<StoreResult>) {
        storeResultAdapter.updateFeeds(storeResults)
    }

    private fun unAuthorizeUserError() {
        activity?.let {
            val dialog = BadRequestDialog.newInstance("unauthorized")
            dialog.show(it.supportFragmentManager, "BadRequestDialog")
        }
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun updateProgressBarState(state: Boolean) {

    }

    private fun ioExceptionHandler() {
        activity?.let {
            if (NetworkUtils.isConnected(it).not()) {
                toast(getString(R.string.string_noInternetConnection))
            } else {
                toast(getString(R.string.somethingWentWrong))
            }
        }

    }
}
