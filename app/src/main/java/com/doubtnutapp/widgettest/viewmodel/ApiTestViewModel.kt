package com.doubtnutapp.widgettest.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.widgettest.model.Resource
import com.doubtnutapp.widgettest.repository.ApiTestRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class ApiTestViewModel @Inject constructor(
    private val apiTestRepository: ApiTestRepository,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _widgetsLiveData: MutableLiveData<Widgets> =
        MutableLiveData(Widgets.Initial)

    val widgetsLiveData: LiveData<Widgets> get() = _widgetsLiveData

    sealed class Widgets {
        object Initial : Widgets()
        data class Success(val data: List<WidgetEntityModel<*, *>>) : Widgets()
        object Error : Widgets()
    }

    fun fetch(json: String) {
        viewModelScope.launch {
            apiTestRepository.fetchWidgetData(json)
                .collect {
                    when (it) {
                        is Resource.Initial -> Unit
                        is Resource.Success -> {
                            _widgetsLiveData.value =
                                Widgets.Success(it.data as List<WidgetEntityModel<*, *>>)
                        }
                        is Resource.Error -> {
                            _widgetsLiveData.value =
                                Widgets.Error
                        }
                    }
                }
        }
    }

    fun reset() {
        _widgetsLiveData.value = Widgets.Success(listOf())
    }
}