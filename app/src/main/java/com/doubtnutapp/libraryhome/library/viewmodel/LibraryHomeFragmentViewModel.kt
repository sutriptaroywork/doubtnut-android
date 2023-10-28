package com.doubtnutapp.libraryhome.library.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.domain.library.entities.ClassListEntity
import com.doubtnutapp.domain.library.entities.ClassListViewEntity
import com.doubtnutapp.domain.library.interactor.GetClassesList
import com.doubtnutapp.libraryhome.library.mapper.ClassesViewItemMapper
import com.doubtnutapp.plus
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject


class LibraryHomeFragmentViewModel @Inject constructor(
        private val getClassesListUseCase: GetClassesList,
        private val classesViewMapper: ClassesViewItemMapper,
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _classesListLiveData = MutableLiveData<Outcome<ClassListViewEntity>>()

    val classesListLiveData: LiveData<Outcome<ClassListViewEntity>>
        get() = _classesListLiveData

    private val _searchHintMapLiveData = MutableLiveData<Map<String, List<String>?>>()
    val searchHintMapLiveData: LiveData<Map<String, List<String>?>>
        get() = _searchHintMapLiveData

    fun getClassesList() {
        compositeDisposable + getClassesListUseCase
                .execute(Unit)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onClassListSuccess, this::onError)
    }

    private fun onClassListSuccess(classesData: ClassListEntity) {
        _classesListLiveData.value = Outcome.success(classesViewMapper.map(classesData))
    }

    private fun onError(error: Throwable) {
        _classesListLiveData.value = Outcome.loading(false)
        _classesListLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
        logException(error)
    }

    private fun logException(error: Throwable) {
        try {
            if (error is JsonSyntaxException
                    || error is NullPointerException
                    || error is ClassCastException
                    || error is FormatException
                    || error is IllegalArgumentException) {
                Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

    fun getSearchHintData(){
        compositeDisposable + DataHandler.INSTANCE.searchRepository
                .getHintAnimationStrings()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    _searchHintMapLiveData.value = it.data
                })
    }


}