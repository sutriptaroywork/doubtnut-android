package com.doubtnutapp.ui.course.microconcept

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.MicroConcept
import com.doubtnutapp.domain.microconcept.GetMicroConcepts
import com.doubtnutapp.domain.microconcept.MicroConceptEntity
import com.doubtnutapp.plus
import com.doubtnutapp.ui.course.microconcept.mapper.MicroConceptMapper
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class MicroConceptViewModel @Inject constructor(
        private val getMicroConcepts: GetMicroConcepts,
        private val microConceptMapper: MicroConceptMapper,
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {


    private val _microConceptsLiveData: MutableLiveData<Outcome<List<MicroConcept>>> = MutableLiveData()


    val microConceptsLiveData: LiveData<Outcome<List<MicroConcept>>>
        get() = _microConceptsLiveData


    fun getMicroConcepts(clazz: String, course: String, chapter: String, subtopic: String) {
        _microConceptsLiveData.value = Outcome.loading(true)
        compositeDisposable + getMicroConcepts.execute(GetMicroConcepts.Param(clazz, course, chapter, subtopic))
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onSuccess, this::onError)

    }

    private fun onSuccess(microConceptEntityList: List<MicroConceptEntity>) {
        _microConceptsLiveData.value = Outcome.loading(false)
        val microConceptList = microConceptEntityList.map {
            microConceptMapper.map(it)
        }
        _microConceptsLiveData.value = Outcome.success(microConceptList)
    }

    private fun onError(error: Throwable) {
        _microConceptsLiveData.value = Outcome.loading(false)
        _microConceptsLiveData.value = if (error is HttpException) {
            val errorCode = error.response()?.code()
            when (errorCode) {
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
}