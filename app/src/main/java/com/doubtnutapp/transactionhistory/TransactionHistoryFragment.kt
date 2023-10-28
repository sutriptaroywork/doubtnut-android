package com.doubtnutapp.transactionhistory

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.FragmentTransactionHistorySuccessBinding
import com.doubtnutapp.databinding.TransactionHistoryItemBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.payment.entities.TransactionHistoryItem
import com.doubtnutapp.payment.ui.PaymentStatusActivity
import com.doubtnutapp.transactionhistory.viewmodel.TransactionHistoryViewModel
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.utils.EventObserver
import javax.inject.Inject

class TransactionHistoryFragment :
    BaseBindingFragment<TransactionHistoryViewModel, FragmentTransactionHistorySuccessBinding>() {

    companion object {
        const val TAG = "TransactionHistoryFragment"

        fun newInstance(success: Boolean) =
            TransactionHistoryFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("success", success)
                }
            }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val adapter: TransactionHistoryAdapter by lazy {
        TransactionHistoryAdapter(ArrayList(), deeplinkAction, analyticsPublisher)
    }

    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    private var status: String = "successful"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var success = true
        arguments?.let {
            success = it.getBoolean("success")
        }
        if (!success) {
            status = "failure"
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTransactionHistorySuccessBinding {
        return FragmentTransactionHistorySuccessBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): TransactionHistoryViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val startPage = 1
        mBinding?.rvTransactionHistory?.clearOnScrollListeners()
        val linearLayoutManager = LinearLayoutManager(requireContext())
        mBinding?.rvTransactionHistory?.layoutManager = linearLayoutManager

        infiniteScrollListener = object : TagsEndlessRecyclerOnScrollListener(linearLayoutManager) {
            override fun onLoadMore(currentPage: Int) {
                viewModel.getTransactionHistoryV2(status, currentPage)
            }
        }.also {
            it.setStartPage(startPage)
        }

        mBinding?.rvTransactionHistory?.addOnScrollListener(infiniteScrollListener)
        mBinding?.rvTransactionHistory?.adapter = adapter
        viewModel.getTransactionHistoryV2(status, startPage)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.transactionHistoryV2List.observe(viewLifecycleOwner, EventObserver {
            if (it.isNullOrEmpty() and adapter.items.isNullOrEmpty()) {
                mBinding?.noDataGroup?.show()
                infiniteScrollListener.isLastPageReached = true
            } else {
                adapter.updateList(it)
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            infiniteScrollListener.setDataLoading(it)
        })
    }
}

class TransactionHistoryAdapter(
    val items: ArrayList<TransactionHistoryItem>,
    val deeplinkAction: DeeplinkAction,
    val analyticsPublisher: AnalyticsPublisher
) : RecyclerView.Adapter<TransactionHistoryAdapter.TransactionHistoryViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionHistoryViewHolder {
        return TransactionHistoryViewHolder(
            TransactionHistoryItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: TransactionHistoryViewHolder, position: Int) {
        val data = items[position]
        val view = holder.itemView
        val binding = holder.binding

        if (data.status == "FAILURE") {
            var mAmount = ""
            data.amount.orEmpty().forEach {
                if (it.isDigit())
                    mAmount += it
            }
            view.setOnClickListener {
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.PAYMENT_HISTORY_FAILED_CLICK, ignoreSnowplow = true))
                val intent = PaymentStatusActivity.getStartIntent(
                    view.context,
                    false,
                    data.partnerTxnId.orEmpty(),
                    mAmount,
                    true,
                    data.paymentFor.orEmpty(),
                    data.type.orEmpty(),
                    currencySymbol = data.currencySymbol.orEmpty()
                )
                view.context.startActivity(intent)
            }
        }

        binding.ivLogo.loadImageEtx(data.imageUrl.orEmpty())
        binding.tvTitle.text = data.name.orEmpty()
        binding.tvPrice.text = data.amount.orEmpty()
        binding.tvPrice.applyTextColor(data.amountColor)
        binding.tvOrderId.text = data.orderId.orEmpty()
        binding.tvOrderId.isVisible = data.orderId.isNotNullAndNotEmpty()
        binding.tvTimestamp.text = data.date.orEmpty()
        binding.tvExpireText.toggleVisibilityAndSetText(data.expireText)
        if (!data.btn1Text.isNullOrEmpty()) {
            binding.btn1.show()
            binding.btn1.text = data.btn1Text.orEmpty()
            binding.btn1.setOnClickListener {
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.PAYMENT_HISTORY_DEEPLINK_CLICK, ignoreSnowplow = true))
                if (!data.pdfUrl.isNullOrEmpty()) {
                    PdfViewerActivity.previewPdfFromTheUrl(view.context, data.pdfUrl.orEmpty())
                } else {
                    deeplinkAction.performAction(view.context, data.btn1Deeplink)
                }
            }
        } else {
            binding.btn1.hide()
        }
        if (!data.btn2Text.isNullOrEmpty()) {
            binding.btn2.show()
            binding.btn2.text = data.btn2Text.orEmpty()
            binding.btn2.setOnClickListener {
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.PAYMENT_HISTORY_DEEPLINK_CLICK, ignoreSnowplow = true))
                if (!data.invoiceUrl.isNullOrEmpty()) {
                    PdfViewerActivity.previewPdfFromTheUrl(view.context, data.invoiceUrl.orEmpty())
                } else {
                    deeplinkAction.performAction(view.context, data.btn2Deeplink)
                }
            }
        } else {
            binding.btn2.hide()
        }
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<TransactionHistoryItem>) {
        items.addAll(list)
        notifyDataSetChanged()
    }

    class TransactionHistoryViewHolder(val binding: TransactionHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}