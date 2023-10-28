package com.doubtnutapp.shorts.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.utils.Resource
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.shorts.model.ShortsCategoryData
import com.doubtnutapp.shorts.repository.ShortsRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShortsCategoryBottomSheetVM @Inject constructor(
    private val shortsRepository: ShortsRepository,
    compositeDisposable: CompositeDisposable
) :
    BaseViewModel(compositeDisposable) {

    private val _categoryData: MutableLiveData<Resource<ShortsCategoryData>> = MutableLiveData()
    val categoryData: LiveData<Resource<ShortsCategoryData>>
        get() = _categoryData

    private val _sendCategoryData: MutableLiveData<Resource<ApiResponse<Any>>> = MutableLiveData()
    val sendCategoryData: LiveData<Resource<ApiResponse<Any>>>
        get() = _sendCategoryData

    fun getCategoryBottomSheet() {
        _categoryData.value = Resource.Loading()
        viewModelScope.launch {
            shortsRepository.getCategoryBottomSheet()
                .catch {
                    _categoryData.value = Resource.Error(it.message.toString())
                }.collect {
                    _categoryData.value = Resource.Success(it.data)
                }
        }
    }

    fun sendCategoriesData(categories: List<Int>) {
        _sendCategoryData.value = Resource.Loading()
        viewModelScope.launch {
            shortsRepository.sendCategoriesData(categories)
                .catch {
                    _sendCategoryData.value = Resource.Error(it.message.toString())
                }.collect {
                    _sendCategoryData.value = Resource.Success(it)
                }
        }
    }
}