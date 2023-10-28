package com.doubtnutapp.widgets.countrycodepicker

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.base.CountryCodeItemClicked
import com.doubtnutapp.base.extension.setNavigationResult
import com.doubtnutapp.databinding.FragmentCountryCodePickerDialogBinding
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.utils.RxEditTextObservable
import com.doubtnutapp.widgets.countrycodepicker.model.CountryCode
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class CountryCodePickerDialogFragment :
    BaseBindingDialogFragment<CountryCodePickerViewModel, FragmentCountryCodePickerDialogBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "CountryCodePickerDialogFragment"
        const val COUNTRY_CODE = "country_code"
    }

    private val mAdapter: CountryCodePickerAdapter by lazy { CountryCodePickerAdapter(this) }
    private lateinit var mDisposable: Disposable

    private var mOnCountryCodePickedListener: OnCountryCodePickedListener? = null

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCountryCodePickerDialogBinding =
        FragmentCountryCodePickerDialogBinding.inflate(layoutInflater)

    override fun provideViewModel(): CountryCodePickerViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setupUi()
        viewModel.getCountryCodesList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar)
    }

    private fun setupUi() {
        binding.recyclerView.adapter = mAdapter

        mDisposable = RxEditTextObservable.fromView(binding.etSearch)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                viewModel.searchCountryCodesList(it)
            }

        binding.rootLayout.setOnClickListener {
            dismiss()
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.countryCodesListLiveData.observe(viewLifecycleOwner, Observer {
            mAdapter.updateList(it)
            binding.tvNoResult.isVisible = it.isEmpty()
        })
    }

    override fun performAction(action: Any) {
        when (action) {
            is CountryCodeItemClicked -> {
                dismiss()
                mOnCountryCodePickedListener?.onCountryCodePicked(action.countryCodeData)
                runCatching {
                    setNavigationResult(action.countryCodeData, COUNTRY_CODE)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDisposable.dispose()
    }

    fun setOnCountryCodePickedListener(listener: OnCountryCodePickedListener) {
        mOnCountryCodePickedListener = listener
    }

    interface OnCountryCodePickedListener {
        fun onCountryCodePicked(countryCodeData: CountryCode)
    }
}