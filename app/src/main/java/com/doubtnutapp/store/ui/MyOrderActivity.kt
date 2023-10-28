package com.doubtnutapp.store.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*

import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityMyOrderBinding
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.store.dto.MyOrderResultDTO
import com.doubtnutapp.store.model.MyOrderResult
import com.doubtnutapp.store.ui.adapter.MyOrderListAdapter
import com.doubtnutapp.store.viewmodel.MyOrderViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import javax.inject.Inject

class MyOrderActivity : BaseBindingActivity<MyOrderViewModel, ActivityMyOrderBinding>(),
    View.OnClickListener, ActionPerformer {

    companion object {
        private const val TAG = "MyOrderActivity"

        fun startActivity(context: Context) {
            Intent(context, MyOrderActivity::class.java).also {
                context.startActivity(it)
            }
        }
    }

    @Inject
    lateinit var screenNavigator: Navigator

    private lateinit var myOrderList: RecyclerView
    private lateinit var msgNoResultFound: TextView

    private lateinit var myOrderListAdapter: MyOrderListAdapter

    override fun provideViewBinding(): ActivityMyOrderBinding {
        return ActivityMyOrderBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): MyOrderViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.grey_statusbar_color
    }

    override fun setupView(savedInstanceState: Bundle?) {
        init()
    }

    private fun init() {
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(MyOrderViewModel::class.java)
        setUpRecyclerView()
        setUpObservers()
        setListeners()
    }

    private fun setUpRecyclerView() {
        myOrderList = findViewById(R.id.myOrderList)
        msgNoResultFound = findViewById(R.id.msgNoResultFound)

        myOrderListAdapter = MyOrderListAdapter(this)
        myOrderList.adapter = myOrderListAdapter
    }

    private fun setListeners() {
        binding.closeStoreScreen.setOnClickListener(this)
    }

    private fun setUpObservers() {
        viewModel.myOrderResultResultLiveData.observeK(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )


        viewModel.navigateLiveData.observe(this, {
            it.getContentIfNotHandled()?.let { navigationData ->
                navigate(navigationData)
            }
        })
    }

    private fun navigate(navigationData: NavigationModel) {
        val screen = navigationData.screen
        val arg = navigationData.hashMap?.toBundle()
        screenNavigator.startActivityFromActivity(this, screen, arg)
    }

    private fun onSuccess(myOrderResultDTO: MyOrderResultDTO) {
        updateStoreResult(myOrderResultDTO)

        // No accessor for StoreActivity.MY_ORDER_COUNT
        // Set My Order count 0
        defaultPrefs(this).edit().putInt(StoreActivity.MY_ORDER_COUNT, 0).apply()
    }

    private fun updateStoreResult(myOrderResultDTO: MyOrderResultDTO) {
        if (myOrderResultDTO.myOrderList.isNullOrEmpty()) {
            myOrderList.hide()
            msgNoResultFound.show()
        } else {
            myOrderList.show()
            msgNoResultFound.hide()
            updateMyOrderResults(myOrderResultDTO.myOrderList)
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun updateProgressBarState(state: Boolean) {
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.closeStoreScreen -> finish()
        }
    }

    private fun updateMyOrderResults(myOrderResults: List<MyOrderResult>) {
        myOrderListAdapter.updateFeeds(myOrderResults)
    }

    override fun performAction(action: Any) {
        viewModel.handleItemClick(action)
    }
}
