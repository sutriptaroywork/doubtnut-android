package com.doubtnutapp.freeclasses.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.common.di.module.CommonRepository
import com.doubtnutapp.freeclasses.bottomsheets.FilterListData
import com.doubtnutapp.freeclasses.widgets.FilterSortWidget
import com.doubtnutapp.toMapOfStringVString
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 31/01/22.
 */
class FilterListBottomSheetDialogVM
@Inject constructor(
    compositeDisposable: CompositeDisposable,
    val commonRepository: CommonRepository
) : BaseViewModel(compositeDisposable) {

    val filterLiveData: LiveData<FilterListData>
        get() = _filterLiveData
    private val _filterLiveData: MutableLiveData<FilterListData> = MutableLiveData()

    val progressState: LiveData<Boolean>
        get() = _progressState
    private val _progressState: MutableLiveData<Boolean> = MutableLiveData(false)

    fun getData(
        type: FilterSortWidget.FilterType,
        assortmentId: String,
        filters: HashMap<String, List<String>>
    ) {
        _progressState.postValue(true)
        viewModelScope.launch {
            commonRepository.getFilterBottomSheetData(
                type.name,
                assortmentId,
                filters
            )
                .catch {
                    _progressState.postValue(false)
                }
                .collect {
                    _progressState.postValue(false)
                    _filterLiveData.postValue(it)
                }
        }
    }
}