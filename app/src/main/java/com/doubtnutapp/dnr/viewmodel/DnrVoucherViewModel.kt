package com.doubtnutapp.dnr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.dnr.model.DnrPendingVoucherData
import com.doubtnutapp.dnr.model.VoucherData
import com.doubtnutapp.plus
import com.doubtnutapp.studygroup.service.DnrRepository
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class DnrVoucherViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val dnrRepository: DnrRepository,
) : BaseViewModel(compositeDisposable) {

    private val _pendingVoucherLiveData: MutableLiveData<DnrPendingVoucherData?> = MutableLiveData()
    val pendingVoucherLiveData: LiveData<DnrPendingVoucherData?>
        get() = _pendingVoucherLiveData

    private val _tabListLiveData: MutableLiveData<VoucherData> = MutableLiveData()
    val tabListLiveData: LiveData<VoucherData>
        get() = _tabListLiveData

    fun getTabList() {
        compositeDisposable +
            dnrRepository.getVoucherTabList()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        _tabListLiveData.postValue(it.data)
                    },
                    {
                        it.printStackTrace()
                    }
                )
    }

    fun getPendingVoucherBottomSheetData() {
        compositeDisposable +
            dnrRepository.getPendingVoucherBottomSheetData()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        _pendingVoucherLiveData.value = it.data
                    },
                    {
                        it.printStackTrace()
                    }
                )
    }
}
